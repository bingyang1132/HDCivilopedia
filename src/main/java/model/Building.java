package model;

import java.awt.image.BufferedImage;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import model.abstracts.UnlockableWithIcon;
import tools.ImageEditor;
import tools.Tools;

public class Building extends UnlockableWithIcon {

    public static final Map<String, Building> buildings = new HashMap<>();

    public String description;
    public String trait;

    public int cost;
    public String district;
    public int housing;
    public int entertainment;
    public int entertainmentWithPower;
    public String purchaseYield;
    public int maintenance;
    public int citizenSlots;
    public int regionalRange;
    public String governmentTierRequirement;
    public boolean enabledByReligion;

    public int requiredPower;
    public int tourism;

    public List<String> prereqBuildings = new ArrayList<>();
    public Map<String, Integer> yieldChanges = new HashMap<>();
    public Map<String, Integer> yieldChangesWhenPowered = new HashMap<>();
    public Map<String, String> yieldDistrictCopies = new HashMap<>();
    public Map<String, Integer> yieldPerEra = new HashMap<>();
    public Map<String, Integer> greatWorks = new HashMap<>();
    public Map<String, Integer> greatPersonPoints = new HashMap<>();
    public Map<String, Integer> citizenYieldChanges = new HashMap<>();
    public List<String> exclusive = new ArrayList<>();
	
	public int regionalRangeM;
	public Map<String, Integer> regionalYield = new HashMap<>();
	public Map<String, Integer> regionalYieldWithPower = new HashMap<>();

    public String replace;
    public String belongs;

    public Building(String tag) {
        super(tag);
        buildings.put(tag, this);
    }

    // load buildings from database
    public static void load() {
        Statement gameplay = null;
        try {
            gameplay = DriverManager.getConnection(Tools.GAMEPLAY_DATABASE).createStatement();

            // load buildings list
            ResultSet r1 = gameplay.executeQuery("select * from Buildings where InternalOnly = \"0\" and IsWonder = \"0\" and PrereqDistrict is not null and BuildingType not like \"BUILDING_%_DUMMY_%\";");
            while (r1.next()) {
                String tag = r1.getString("BuildingType");
                Building building = new Building(tag);
                building.name = r1.getString("Name");
                building.description = r1.getString("Description");
                building.trait = r1.getString("TraitType");

                building.prereqTech = r1.getString("PrereqTech");
                building.prereqCivic = r1.getString("prereqCivic");
                building.cost = r1.getInt("Cost");
                building.district = r1.getString("PrereqDistrict");
                building.housing = r1.getInt("Housing");
                building.entertainment = r1.getInt("Entertainment");
                building.purchaseYield = r1.getString("PurchaseYield");
                building.maintenance = r1.getInt("Maintenance");
                building.citizenSlots = r1.getInt("CitizenSlots");
                building.regionalRange = r1.getInt("RegionalRange");
                building.governmentTierRequirement = r1.getString("GovernmentTierRequirement");
                building.enabledByReligion = r1.getBoolean("EnabledByReligion");
            }

            // load other information
            for (Entry<String, Building> entry : buildings.entrySet()) {
                String tag = entry.getKey();
                Building building = entry.getValue();

                // load prerequired buildings
                ResultSet r2 = gameplay.executeQuery("select * from BuildingPrereqs where Building = \"" + tag + "\";");
                while (r2.next()) {
                    building.prereqBuildings.add(r2.getString("PrereqBuilding"));
                }

                // load replace
                ResultSet r3 = gameplay.executeQuery("select * from BuildingReplaces where CivUniqueBuildingType = \"" + tag + "\";");
                if (r3.next()) {
                    building.replace = r3.getString("ReplacesBuildingType");
                }

                // load yields
                ResultSet r4 = gameplay.executeQuery("select * from Building_YieldChanges where BuildingType = \"" + tag + "\";");
                while (r4.next()) {
                    building.yieldChanges.put(r4.getString("YieldType"), r4.getInt("YieldChange"));
                }

                // load yields when powered
                ResultSet r5 = gameplay.executeQuery("select * from Building_YieldChangesBonusWithPower where BuildingType = \"" + tag + "\";");
                while (r5.next()) {
                    building.yieldChangesWhenPowered.put(r5.getString("YieldType"), r5.getInt("YieldChange"));
                }

                // load xp2 information
                ResultSet r6 = gameplay.executeQuery("select * from Buildings_XP2 where BuildingType = \"" + tag + "\";");
                if (r6.next()) {
                    building.requiredPower = r6.getInt("RequiredPower");
                    building.entertainmentWithPower = r6.getInt("EntertainmentBonusWithPower");
                }

                // load yield district copies
                ResultSet r7 = gameplay.executeQuery("select * from Building_YieldDistrictCopies where BuildingType = \"" + tag + "\";");
                while (r7.next()) {
                    building.yieldDistrictCopies.put(r7.getString("OldYieldType"), r7.getString("NewYieldType"));
                }

                // load yield per era
                ResultSet r8 = gameplay.executeQuery("select * from Building_YieldsPerEra where BuildingType = \"" + tag + "\";");
                while (r8.next()) {
                    building.yieldPerEra.put(r8.getString("YieldType"), r8.getInt("YieldChange"));
                }

                // load great work slots
                ResultSet r9 = gameplay.executeQuery("select * from Building_GreatWorks where BuildingType = \"" + tag + "\";");
                while (r9.next()) {
                    building.greatWorks.put(r9.getString("GreatWorkSlotType"), r9.getInt("NumSlots"));
                }

                // load great person points
                ResultSet r10 = gameplay.executeQuery("select * from Building_GreatPersonPoints where BuildingType = \"" + tag + "\";");
                while (r10.next()) {
                    building.greatPersonPoints.put(r10.getString("GreatPersonClassType"), r10.getInt("PointsPerTurn"));
                }

                // load citizen yield changes
                ResultSet r11 = gameplay.executeQuery("select * from Building_CitizenYieldChanges where BuildingType = \"" + tag + "\";");
                while (r11.next()) {
                    building.citizenYieldChanges.put(r11.getString("YieldType"), r11.getInt("YieldChange"));
                }

                // load tourism
                ResultSet r12 = gameplay.executeQuery("select * from Building_TourismBombs_XP2 where BuildingType = \"" + tag + "\";");
                if (r12.next()) {
                    building.tourism = r12.getInt("TourismBombValue");
                }

                // load exclusive
                ResultSet r13 = gameplay.executeQuery("select * from MutuallyExclusiveBuildings where Building = \"" + tag + "\";");
                while (r13.next()) {
                    building.exclusive.add(r13.getString("MutuallyExclusiveBuilding"));
                }

				try {
					// load regional range (modifier version)
					ResultSet r14 = gameplay.executeQuery("select * from HD_BuildingRegionalRange where BuildingType = \"" + tag + "\";");
					if (r14.next()) {
						building.regionalRangeM = r13.getInt("RegionalRange");
					}

					// load regional yield (modifier version)
					ResultSet r15 = gameplay.executeQuery("select * from HD_BuildingRegionalYields where BuildingType = \"" + tag + "\";");
					while (r15.next()) {
						if (r15.getInt("RequiresPower") == 1) {
							building.regionalYieldWithPower.put(r15.getString("YieldType"), r15.getInt("YieldChange"));
						} else {
							building.regionalYield.put(r15.getString("YieldType"), r15.getInt("YieldChange"));
						}
					}
				} catch (Exception e) {
				}

                // load building icon
                String iconString = "ICON_" + tag;
                BufferedImage icon = Tools.getImage(iconString);
                if (icon != null) {
                    String path = iconString + ".png";
                    building.icon = Tools.IMAGE_URL + "/" + path;
                    ImageEditor.saveImage(icon, path);
                }
            }

            // delete fake buildings
            for (Entry<String, Building> entry : buildings.entrySet()) {
                Building building = entry.getValue();
                List<String> remove = new ArrayList<>();
                for (String build : building.prereqBuildings) {
                    Building b = Building.buildings.get(build);
                    if (b == null) {
                        remove.add(build);
                    }
                }
                building.prereqBuildings.removeAll(remove);
                remove = new ArrayList<>();
                for (String build : building.exclusive) {
                    Building b = Building.buildings.get(build);
                    if (b == null) {
                        remove.add(build);
                    }
                }
                building.exclusive.removeAll(remove);
            }
        } catch (Exception e) {
            System.err.println("Error loading buildings.");
            System.err.println(e.getClass().getName() + e.getMessage());
        } finally {
            try {
                if (gameplay != null) {
                    gameplay.close();
                }
            } catch (Exception e) {
            }
        }
    }

    // Convert information into json page
    @Override
    public JSONObject toJson(String language) {
        JSONObject object = super.toJson(language);

        JSONArray leftColumnItems = new JSONArray();
        if (description != null) {
            leftColumnItems.add(Tools.getHeader(Tools.getControlText("Description", language)));
            leftColumnItems.add(Tools.getBody(null, Tools.getTextWithAlt(description, language)));
        }
        object.put("leftColumnItems", leftColumnItems);

        JSONArray rightColumnItems = new JSONArray();
        object.put("rightColumnItems", rightColumnItems);

        JSONArray traitContents = new JSONArray();

        if (belongs != null) {
            traitContents.add(Tools.getSeparator());
            traitContents.add(Tools.getHeader(Tools.getControlText("Unique To", language)));
            Civilization civilization = Civilization.civilizations.get(belongs);
            if (civilization != null) {
                traitContents.add(civilization.getIconLabel(language));
            }
            Leader leader = Leader.leaders.get(belongs);
            if (leader != null) {
                traitContents.add(leader.getIconLabel(language));
            }
        }

        if (replace != null) {
            // the replaced building may be internal-only (e.g. the XHH_*_STALL
            // buildings replace *_INTERNAL bases), in which case it is not in the map
            Building bui = buildings.get(replace);
            if (bui != null) {
                traitContents.add(Tools.getSeparator());
                traitContents.add(Tools.getHeader(Tools.getControlText("Replaces", language)));
                traitContents.add(bui.getIconLabel(language));
            }
        } else {
            boolean hasReplace = false;
            for (Entry<String, Building> entry : buildings.entrySet()) {
                if (tag.equals(entry.getValue().replace)) {
                    hasReplace = true;
                    break;
                }
            }
            if (hasReplace) {
                traitContents.add(Tools.getSeparator());
                traitContents.add(Tools.getHeader(Tools.getControlText("Replaced by", language)));
                for (Entry<String, Building> entry : buildings.entrySet()) {
                    Building bui = entry.getValue();
                    if (tag.equals(bui.replace)) {
                        traitContents.add(bui.getIconLabel(language));
                    }
                }
            }
        }

        if (regionalRange > 0) {
            traitContents.add(Tools.getSeparator());
            traitContents.add(Tools.getLabel(Tools.getControlText("regional 1", language) + regionalRange + Tools.getControlText("regional 2", language)));
        }

        if (regionalRangeM > 0) {
            traitContents.add(Tools.getSeparator());
            traitContents.add(Tools.getLabel(Tools.getControlText("regional 1M", language) + regionalRangeM + Tools.getControlText("regional 2M", language)));

			for (Entry<String, Integer> entry : regionalYield.entrySet()) {
				traitContents.add(Tools.getLabel("+" + entry.getValue() + Tools.getYield(entry.getKey(), language)));
			}
			for (Entry<String, Integer> entry : regionalYieldWithPower.entrySet()) {
                traitContents.add(Tools.getLabel(Tools.getControlText("powered 1", language) + "+" + entry.getValue() + Tools.getYield(entry.getKey(), language) + Tools.getControlText("powered 2", language)));
			}
        }

        if (housing > 0 || entertainment > 0 || entertainmentWithPower > 0 || citizenSlots > 0 || yieldChanges.size() > 0 || yieldChangesWhenPowered.size() > 0 || yieldDistrictCopies.size() > 0 || yieldPerEra.size() > 0 || greatWorks.size() > 0 || greatPersonPoints.size() > 0) {
            traitContents.add(Tools.getSeparator());

            for (Entry<String, Integer> entry : yieldChanges.entrySet()) {
                traitContents.add(Tools.getLabel("+" + entry.getValue() + Tools.getYield(entry.getKey(), language)));
            }

            for (Entry<String, Integer> entry : yieldChangesWhenPowered.entrySet()) {
                traitContents.add(Tools.getLabel(Tools.getControlText("powered 1", language) + "+" + entry.getValue() + Tools.getYield(entry.getKey(), language) + Tools.getControlText("powered 2", language)));
            }

            for (Entry<String, String> entry : yieldDistrictCopies.entrySet()) {
                traitContents.add(Tools.getLabel(Tools.getYield(entry.getValue(), language) + Tools.getControlText("district copy 1", language) + Tools.getYield(entry.getKey(), language) + Tools.getControlText("district copy 2", language)));
            }

            for (Entry<String, Integer> entry : yieldPerEra.entrySet()) {
                traitContents.add(Tools.getLabel(Tools.getControlText("per era 1", language) + "+" + entry.getValue() + Tools.getYield(entry.getKey(), language) + Tools.getControlText("per era 2", language)));
            }

            for (Entry<String, Integer> entry : greatPersonPoints.entrySet()) {
                String tag = entry.getKey();
                String type = tag.substring(19, tag.length());
                int value = entry.getValue();
                traitContents.add(Tools.getLabel("+" + value + " [ICON_GREAT" + type + "] " + Tools.getTextWithAlt("LOC_" + tag + "_NAME", language) + Tools.getControlText("points", language)));
            }

            if (housing != 0) {
                traitContents.add(Tools.getLabel("+" + housing + " [ICON_HOUSING] " + Tools.getControlText("housing", language)));
            }

            if (entertainment != 0) {
                traitContents.add(Tools.getLabel("+" + entertainment + " [ICON_AMENITIES] " + Tools.getControlText("amenity", language)));
            }

            if (entertainmentWithPower != 0) {
                traitContents.add(Tools.getLabel(Tools.getControlText("powered 1", language) + "+" + entertainment + " [ICON_AMENITIES] " + Tools.getControlText("amenity", language) + Tools.getControlText("powered 2", language)));
            }

            if (citizenSlots != 0) {
                traitContents.add(Tools.getLabel("+" + citizenSlots + " [ICON_CITIZEN] " + Tools.getControlText("citizen slot", language)));
            }

            for (Entry<String, Integer> entry : greatWorks.entrySet()) {
                traitContents.add(Tools.getLabel("+" + entry.getValue() + Tools.getControlText(entry.getKey(), language)));
            }
        }

        if (tourism > 0) {
            traitContents.add(Tools.getSeparator());
            traitContents.add(Tools.getLabel(Tools.getControlText("Tourism from Rock Concerts 1", language) + "+" + tourism + Tools.getControlText("Tourism from Rock Concerts 2", language)));
        }

        if (citizenYieldChanges.size() > 0) {
            traitContents.add(Tools.getSeparator());
            traitContents.add(Tools.getHeader(Tools.getControlText("Citizen Yields", language)));
            for (Entry<String, Integer> entry : citizenYieldChanges.entrySet()) {
                if (entry.getValue() > 0) {
                    traitContents.add(Tools.getLabel("+" + entry.getValue() + Tools.getYield(entry.getKey(), language)));
                } else {
                    traitContents.add(Tools.getLabel(entry.getValue() + Tools.getYield(entry.getKey(), language)));
                }
            }
        }

        if (traitContents.size() > 0) {
            rightColumnItems.add(Tools.getStatbox(Tools.getControlText("Traits", language), traitContents));
        }

        JSONArray requirementContents = new JSONArray();
        rightColumnItems.add(Tools.getStatbox(Tools.getControlText("Requirements", language), requirementContents));
        District dist = null;
        if (district != null) {
            dist = District.distincts.get(district);
        }
        if (dist != null || prereqBuildings.size() > 0) {
            requirementContents.add(Tools.getSeparator());
            if (dist != null) {
                requirementContents.add(Tools.getHeader(Tools.getControlText("District", language)));
                requirementContents.add(dist.getIconLabel(language));
            }
            if (prereqBuildings.size() > 0) {
                if (prereqBuildings.size() > 1) {
                    requirementContents.add(Tools.getHeader(Tools.getControlText("Building any", language)));
                } else {
                    requirementContents.add(Tools.getHeader(Tools.getControlText("Building", language)));
                }
                for (String b : prereqBuildings) {
                    Building prereq = buildings.get(b);
                    if (prereq != null) {
                        requirementContents.add(prereq.getIconLabel(language));
                    }
                }
            }
        }
        if (governmentTierRequirement != null) {
            requirementContents.add(Tools.getSeparator());
            requirementContents.add(Tools.getHeader(Tools.getControlText("Government", language)));
            requirementContents.add(Tools.getLabel(Tools.getControlText(governmentTierRequirement, language)));
        }
        if (exclusive.size() > 0) {
            requirementContents.add(Tools.getSeparator());
            requirementContents.add(Tools.getHeader(Tools.getControlText("Exclusive", language)));
            for (String b : exclusive) {
                Building exc = buildings.get(b);
                requirementContents.add(exc.getIconLabel(language));
            }
        }
        if (prereqTech != null) {
            Technology tech = Technology.technologies.get(prereqTech);
            if (tech != null) {
                requirementContents.add(Tools.getSeparator());
                requirementContents.add(Tools.getHeader(Tools.getControlText("Technology", language)));
                requirementContents.add(tech.getIconLabel(language));
            }
        }
        if (prereqCivic != null) {
            Civic civic = Civic.civics.get(prereqCivic);
            if (civic != null) {
                requirementContents.add(Tools.getSeparator());
                requirementContents.add(Tools.getHeader(Tools.getControlText("Civic", language)));
                requirementContents.add(civic.getIconLabel(language));
            }
        }
        requirementContents.add(Tools.getSeparator());
        requirementContents.add(Tools.getHeader(Tools.getControlText("Production Cost", language)));
        requirementContents.add(Tools.getLabel(cost + Tools.getYield("YIELD_PRODUCTION", language)));
        if (purchaseYield != null) {
            requirementContents.add(Tools.getHeader(Tools.getControlText("Purchase Cost", language)));
            int c = cost;
            if (purchaseYield.equals("YIELD_FAITH")) {
                c = c * 2;
            } else if (purchaseYield.equals("YIELD_GOLD")) {
                c = c * 4;
            }
            requirementContents.add(Tools.getLabel(c + Tools.getYield(purchaseYield, language)));
        }
        if (maintenance > 0) {
            requirementContents.add(Tools.getHeader(Tools.getControlText("Maintenance Cost", language)));
            requirementContents.add(Tools.getLabel(maintenance + " " + Tools.getYield("YIELD_GOLD", language)));
        }
        if (requiredPower > 0) {
            requirementContents.add(Tools.getHeader(Tools.getControlText("Power Cost", language)));
            requirementContents.add(Tools.getLabel(requiredPower + " [ICON_POWER] " + Tools.getControlText("Power", language)));
        }

        return object;
    }

    @Override
    public int getOrder() {
        return (belongs != null ? 131072 : 0) + super.getOrder();
    }

    @Override
    public String getChapter() {
        return "buildings";
    }

    @Override
    public String getTagPrefix() {
        return "BUILDING_";
    }

    @Override
    public String getFolder() {
        District dist = null;
        if (district != null) {
            dist = District.distincts.get(district);
        }
        if (dist != null) {
            return dist.getEnglishTitle();
        } else {
            return "index";
        }
    }

    @Override
    public String getFolderName(String language) {
        District dist = null;
        if (district != null) {
            dist = District.distincts.get(district);
        }
        if (dist != null) {
            return dist.getTitle(language);
        } else {
            return "";
        }
    }

    @Override
    public String getCat() {
        return "区域&建筑改动";
    }
    
    @Override
    public int getCatOrder() {
        return -1800;
    }
}
