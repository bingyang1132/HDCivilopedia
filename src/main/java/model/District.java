package model;

import java.awt.image.BufferedImage;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import model.abstracts.UnlockableWithIcon;
import tools.ImageEditor;
import tools.Tools;

public class District extends UnlockableWithIcon {

    public static final Map<String, District> distincts = new HashMap<>();

    public String description;
    public String trait;

    public int cost;
    public boolean requiresPopulation;
    public String plunderType;
    public int plunderAmount;
    public String costProgression;
    public int costProgressionParam;
    public int appeal;
    public int housing;
    public int entertainment;
    public int maintenance;
    public int citizenSlots;
    public boolean coast;
    public boolean noAdjacentCity;
    public boolean aqueduct;

    public List<String> yieldChanges = new ArrayList<>();
    public Map<String, Integer> greatPersons = new HashMap<>();
    public Map<String, Integer> citizenGreatPersons = new HashMap<>();
    public Map<String, Integer> citizenYields = new HashMap<>();
    public Map<String, Double> domestic = new HashMap<>();
    public Map<String, Double> international = new HashMap<>();
    public List<String> exclusive = new ArrayList<>();
    
    public String replace;
    public List<String> buildings = new ArrayList<>();
    public String belongs;
    
    public District(String tag) {
        super(tag);
        distincts.put(tag, this);
    }

    // load districts from database
    public static void load () {
        Statement gameplay = null;
        try {
            gameplay = DriverManager.getConnection(Tools.GAMEPLAY_DATABASE).createStatement();
    
            // load districts list
            ResultSet r1 = gameplay.executeQuery("select * from Districts;");
            while (r1.next()) {
                String tag = r1.getString("DistrictType");
                if (tag.equals("DISTRICT_WONDER")) continue;
                District district = new District(tag);
                district.name = r1.getString("Name");
                district.description = r1.getString("Description");
                district.trait = r1.getString("TraitType");
                district.prereqTech = r1.getString("PrereqTech");
                district.prereqCivic = r1.getString("PrereqCivic");
                district.cost = r1.getInt("Cost");
                district.requiresPopulation = r1.getBoolean("RequiresPopulation");
                district.plunderType = r1.getString("PlunderType");
                district.plunderAmount = r1.getInt("PlunderAmount");
                district.costProgression = r1.getString("CostProgressionModel");
                district.costProgressionParam = r1.getInt("CostProgressionParam1");
                district.appeal = r1.getInt("Appeal");
                district.housing = r1.getInt("Housing");
                district.entertainment = r1.getInt("Entertainment");
                district.maintenance = r1.getInt("Maintenance");
                district.citizenSlots = r1.getInt("CitizenSlots");

                district.aqueduct = r1.getBoolean("Aqueduct");
                district.coast = r1.getBoolean("Coast");
                district.noAdjacentCity = r1.getBoolean("NoAdjacentCity");
            }
    
            // load other information
            for(Entry<String, District> entry : distincts.entrySet()) {
                String tag = entry.getKey();
                District district = entry.getValue();

                // load adjacency yield changes
                ResultSet r2 = gameplay.executeQuery("select * from District_Adjacencies where DistrictType = \"" + tag + "\";");
                while (r2.next()) {
                    district.yieldChanges.add(r2.getString("YieldChangeId"));
                }

                // load great person points
                ResultSet r3 = gameplay.executeQuery("select * from District_GreatPersonPoints where DistrictType = \"" + tag + "\";");
                while (r3.next()) {
                    String type = r3.getString("GreatPersonClassType");
                    int point = r3.getInt("PointsPerTurn");
                    district.greatPersons.put(type, point);
                }

                // load citizen yield changes
                ResultSet r4 = gameplay.executeQuery("select * from District_CitizenYieldChanges where DistrictType = \"" + tag + "\";");
                while (r4.next()) {
                    String type = r4.getString("YieldType");
                    int point = r4.getInt("YieldChange");
                    district.citizenYields.put(type, point);
                }

                // load citizen great person yield changes
                ResultSet r5 = gameplay.executeQuery("select * from District_CitizenGreatPersonPoints where DistrictType = \"" + tag + "\";");
                while (r5.next()) {
                    String type = r5.getString("GreatPersonClassType");
                    int point = r5.getInt("PointsPerTurn");
                    district.citizenGreatPersons.put(type, point);
                }

                // load trade route yield
                ResultSet r6 = gameplay.executeQuery("select * from District_TradeRouteYields where DistrictType = \"" + tag + "\";");
                while (r6.next()) {
                    String type = r6.getString("YieldType");
                    double domestic = r6.getDouble("YieldChangeAsDomesticDestination");
                    double international = r6.getDouble("YieldChangeAsInternationalDestination");
                    if (domestic != 0) {
                        district.domestic.put(type, domestic);
                    }
                    if (international != 0) {
                        district.international.put(type, international);
                    }
                }

                // load replaces
                ResultSet r7 = gameplay.executeQuery("select * from DistrictReplaces where CivUniqueDistrictType = \"" + tag + "\";");
                if (r7.next()) {
                    district.replace = r7.getString("ReplacesDistrictType");
                }

                // load buildings
                ResultSet r8 = gameplay.executeQuery("select * from Buildings where PrereqDistrict = \"" + (district.replace == null ? tag : district.replace) + "\"");
                while (r8.next()) {
                    district.buildings.add(r8.getString("BuildingType"));
                }

                // load exclusive
                ResultSet r9 = gameplay.executeQuery("select * from MutuallyExclusiveDistricts where District = \"" + tag + "\";");
                while (r9.next()) {
                    district.exclusive.add(r9.getString("MutuallyExclusiveDistrict"));
                }

                // load district icon
                String iconString = "ICON_" + tag;
                BufferedImage icon = Tools.getImage(iconString);
                if (icon != null) {
                    String path = iconString + ".png";
                    district.icon = Tools.IMAGE_URL + "/" + path;
                    ImageEditor.saveImage(icon, path);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading districts.");
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

    // this method shold be called after all other models are loaded, which links information
    public static void linkData () {
        for (Entry<String, District> entry : distincts.entrySet()) {
            District district = entry.getValue();
            List<String> remove = new ArrayList<>();
            for (String build : district.buildings) {
                Building building = Building.buildings.get(build);
                if (building == null) {
                    remove.add(build);
                }
            }
            district.buildings.removeAll(remove);
        }
        for (Entry<String, District> entry : distincts.entrySet()) {
            District district = entry.getValue();
            district.buildings.sort(new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    Building b1 = Building.buildings.get(o1);
                    Building b2 = Building.buildings.get(o2);
                    if (b1 != null && b2 != null) {
                        return Integer.compare(b1.getOrder(), b2.getOrder());
                    }
                    return 0;
                }
            });
        }
    }

    // Convert information into json page
    @Override
    public JSONObject toJson (String language) {
        JSONObject object = super.toJson(language);
        
        JSONArray leftColumnItems = new JSONArray();
        leftColumnItems.add(Tools.getHeader(Tools.getControlText("Description", language)));
        leftColumnItems.add(Tools.getBody(null, Tools.getTextWithAlt(description, language)));
        object.put("leftColumnItems", leftColumnItems);

        if (yieldChanges.size() > 0) {
            leftColumnItems.add(Tools.getHeader(Tools.getControlText("Adacency Bonus", language)));
            String s = "";
            for (String chg : yieldChanges) {
                YieldChange change = YieldChange.yieldChanges.get(chg);
                if (change != null) {
                    s += change.toString(language) + "[NEWLINE]";
                }
            }
            leftColumnItems.add(Tools.getBody(null, s));
        }

        JSONArray rightColumnItems = new JSONArray();
        object.put("rightColumnItems", rightColumnItems);

        JSONArray traitContents = new JSONArray();
        rightColumnItems.add(Tools.getStatbox(Tools.getControlText("Traits", language), traitContents));

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
            traitContents.add(Tools.getSeparator());
            traitContents.add(Tools.getHeader(Tools.getControlText("Replaces", language)));
            District dis = distincts.get(replace);
            traitContents.add(dis.getIconLabel(language));
        } else {
            boolean hasReplace = false;
            for (Entry<String, District> entry : distincts.entrySet()) {
                if (tag.equals(entry.getValue().replace)) {
                    hasReplace = true;
                    break;
                }
            }
            if (hasReplace) {
                traitContents.add(Tools.getSeparator());
                traitContents.add(Tools.getHeader(Tools.getControlText("Replaced by", language)));
                for (Entry<String, District> entry : distincts.entrySet()) {
                    District dis = entry.getValue();
                    if (tag.equals(dis.replace)) {
                        traitContents.add(dis.getIconLabel(language));
                    }
                }
            }
        }
        if (!tag.equals("DISTRICT_CITY_CENTER")) {    
            traitContents.add(Tools.getSeparator());
            for (Entry<String, Integer> entry : greatPersons.entrySet()) {
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
            if (citizenSlots != 0) {
                traitContents.add(Tools.getLabel("+" + citizenSlots + " [ICON_CITIZEN] " + Tools.getControlText("citizen slot", language)));
            }
            if (appeal != 0) {
                traitContents.add(Tools.getLabel(Tools.getControlText("appeal 1", language) + (appeal > 0 ? "+" : "") + appeal + Tools.getControlText("appeal 2", language)));
            }
            if (plunderType != null && !plunderType.equals("NO_PLUNDER") && !plunderType.equals("PLUNDER_NONE") && plunderAmount != 0) {
                traitContents.add(Tools.getLabel(Tools.getControlText("plunder yields", language) + plunderAmount + Tools.getYield(plunderType, language)));
            }
        }
        if (citizenYields.size() + citizenGreatPersons.size() > 0) {
            traitContents.add(Tools.getSeparator());
            traitContents.add(Tools.getHeader(Tools.getControlText("Citizen Yields", language)));
            for (Entry<String, Integer> entry : citizenYields.entrySet()) {
                traitContents.add(Tools.getLabel("+" + entry.getValue() + Tools.getYield(entry.getKey(), language)));
            }
            for (Entry<String, Integer> entry : citizenGreatPersons.entrySet()) {
                String tag = entry.getKey();
                String type = tag.substring(19, tag.length());
                int value = entry.getValue() * 2;
                traitContents.add(Tools.getLabel("+" + value + " [ICON_GREAT" + type + "] " + Tools.getTextWithAlt("LOC_" + tag + "_NAME", language) + Tools.getControlText("points", language)));
            }
        }
        if (domestic.size() + international.size() > 0) {
            traitContents.add(Tools.getSeparator());
            traitContents.add(Tools.getHeader(Tools.getControlText("Trade Yields", language)));
            if (domestic.size() > 0) {
                traitContents.add(Tools.getLabel(Tools.getControlText("Domestic Destination", language)));
                for (Entry<String, Double> entry : domestic.entrySet()) {
                    String value = null;
                    if (((int) entry.getValue().intValue()) - entry.getValue() == 0) {
                        value = entry.getValue().intValue() + "";
                    } else {
                        value = entry.getValue() + "";
                    }
                    traitContents.add(Tools.getLabel("+" + value + Tools.getYield(entry.getKey(), language)));
                }
            }
            if (international.size() > 0) {
                traitContents.add(Tools.getLabel(Tools.getControlText("International Destination", language)));
                for (Entry<String, Double> entry : international.entrySet()) {
                    String value = null;
                    if (((int) entry.getValue().intValue()) - entry.getValue() == 0) {
                        value = entry.getValue().intValue() + "";
                    } else {
                        value = entry.getValue() + "";
                    }
                    traitContents.add(Tools.getLabel("+" + value + Tools.getYield(entry.getKey(), language)));
                }
            }
        }

        if (!tag.equals("DISTRICT_CITY_CENTER")) {
            JSONArray requirementContents = new JSONArray();
            rightColumnItems.add(Tools.getStatbox(Tools.getControlText("Requirements", language), requirementContents));
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
            if (exclusive.size() > 0) {
                requirementContents.add(Tools.getSeparator());
                requirementContents.add(Tools.getHeader(Tools.getControlText("Exclusive", language)));
                for (String d : exclusive) {
                    District exc = distincts.get(d);
                    requirementContents.add(exc.getIconLabel(language));
                }
            }

            requirementContents.add(Tools.getSeparator());
            requirementContents.add(Tools.getHeader(Tools.getControlText("Production Cost", language)));
            if (costProgression.equals("COST_PROGRESSION_PREVIOUS_COPIES") && costProgressionParam != 0) {
                requirementContents.add(Tools.getLabel(Tools.getControlText("Basic Cost", language) + cost + Tools.getYield("YIELD_PRODUCTION", language)));
                requirementContents.add(Tools.getLabel(Tools.getControlText("Extra Cost 1", language) + getTitle(language) + Tools.getControlText("Extra Cost 2", language) + costProgressionParam + Tools.getYield("YIELD_PRODUCTION", language)));
            } else {
                requirementContents.add(Tools.getLabel(cost + Tools.getYield("YIELD_PRODUCTION", language)));
            }
            if (maintenance > 0) {
                requirementContents.add(Tools.getHeader(Tools.getControlText("Maintenance Cost", language)));
                requirementContents.add(Tools.getLabel(maintenance + Tools.getYield("YIELD_GOLD", language)));
            }
        }

        if (buildings.size() > 0) {
            JSONArray buildingContents = new JSONArray();
            rightColumnItems.add(Tools.getStatbox(Tools.getControlText("Buildings", language), buildingContents));

            boolean hasUnique = false;
            boolean religion = false;
            buildingContents.add(Tools.getSeparator());
            buildingContents.add(Tools.getHeader(Tools.getControlText("Common Buildings", language)));
            for (String b : buildings) {
                Building building = Building.buildings.get(b);
                if (building.belongs != null) {
                    hasUnique = true;
                } else if (building.enabledByReligion) {
                    religion = true;
                } else {
                    buildingContents.add(building.getIconLabel(language));
                }
            }

            if (religion) {
                buildingContents.add(Tools.getSeparator());
                buildingContents.add(Tools.getHeader(Tools.getControlText("Worship Buildings", language)));
                for (String b : buildings) {
                    Building building = Building.buildings.get(b);
                    if (building.enabledByReligion) {
                        buildingContents.add(building.getIconLabel(language));
                    }
                }
            }

            if (belongs == null && hasUnique) {
                buildingContents.add(Tools.getSeparator());
                buildingContents.add(Tools.getHeader(Tools.getControlText("Unique Buildings", language)));
                for (String b : buildings) {
                    Building building = Building.buildings.get(b);
                    if (building.belongs != null) {
                        buildingContents.add(building.getIconLabel(language));
                    }
                }
            }
        }
        return object;
    }

    @Override
    public String getChapter() {
        return "districts";
    }

    @Override
    public String getTagPrefix() {
        return "DISTRICT_";
    }

    @Override
    public String getFolder () {
        if (replace != null) {
            return "unique";
        } else {
            return "common";
        }
    }

    @Override
    public String getFolderName(String language) {
        if (replace != null) {
            return Tools.getControlText("Unique Districts", language);
        } else {
            return Tools.getControlText("Common Districts", language);
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
