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

public class Unit extends UnlockableWithIcon {

    public static final Map<String, Unit> units = new HashMap<>();

    public String description;
    public String trait;

    public int baseSightRange;
    public int baseMoves;
    public int combat;
    public int rangedCombat;
    public int range;
    public int bombard;
    public int antiAirCombat;
    public String formationClass;
    public int cost;
    public int buildCharges;
    public int religiousStrength;
    public int religiousEvictPercent;
    public int spreadCharges;
    public int religiousHealCharges;
    public String costProgressionModel;
    public int costProgressionParam1;
    public String promotionClass;
    public boolean canTrain;
    public String strategicResource;
    public String purchaseYield;
    public boolean mustPurchase;
    public int maintenance;
    
    public String belongs;
    public String replace;
    public List<String> upgrades = new ArrayList<>();
    public List<String> prereqBuildings = new ArrayList<>();
    public int resourceMaintenanceAmount;
    public int resourceCost;
    public String resourceMaintenanceType;

    public Unit (String tag) {
        super(tag);
        units.put(tag, this);
    }

    // load units from database
    public static void load () {
        Statement gameplay = null;
        try {
            gameplay = DriverManager.getConnection(Tools.GAMEPLAY_DATABASE).createStatement();
    
            // load units list
            ResultSet r1 = gameplay.executeQuery("select * from Units where UnitType != \"UNIT_PRODUCT\";");
            while (r1.next()) {
                String tag = r1.getString("UnitType");
                Unit unit = new Unit(tag);
                unit.name = r1.getString("Name");
                unit.description = r1.getString("Description");
                unit.trait = r1.getString("TraitType");

                unit.baseSightRange = r1.getInt("BaseSightRange");
                unit.baseMoves = r1.getInt("BaseMoves");
                unit.combat = r1.getInt("Combat");
                unit.rangedCombat = r1.getInt("RangedCombat");
                unit.range = r1.getInt("Range");
                unit.bombard = r1.getInt("Bombard");
                unit.antiAirCombat = r1.getInt("AntiAirCombat");
                unit.formationClass = r1.getString("FormationClass");
                unit.cost = r1.getInt("Cost");
                unit.buildCharges = r1.getInt("BuildCharges");
                unit.religiousStrength = r1.getInt("ReligiousStrength");
                unit.religiousEvictPercent = r1.getInt("ReligionEvictPercent");
                unit.spreadCharges = r1.getInt("SpreadCharges");
                unit.religiousHealCharges = r1.getInt("ReligiousHealCharges");
                unit.costProgressionModel = r1.getString("CostProgressionModel");
                unit.costProgressionParam1 = r1.getInt("CostProgressionParam1");
                unit.promotionClass = r1.getString("PromotionClass");
                unit.prereqTech = r1.getString("PrereqTech");
                unit.prereqCivic = r1.getString("PrereqCivic");
                unit.canTrain = r1.getBoolean("CanTrain");
                unit.strategicResource = r1.getString("StrategicResource");
                unit.purchaseYield = r1.getString("PurchaseYield");
                unit.mustPurchase = r1.getBoolean("MustPurchase");
                unit.maintenance = r1.getInt("Maintenance");
            }
    
            // load other information
            for(Entry<String, Unit> entry : units.entrySet()) {
                String tag = entry.getKey();
                Unit unit = entry.getValue();

                // load replace
                ResultSet r2 = gameplay.executeQuery("select * from UnitReplaces where CivUniqueUnitType = \"" + tag + "\";");
                if (r2.next()) {
                    unit.replace = r2.getString("ReplacesUnitType");
                }

                // load upgrades
                ResultSet r3 = gameplay.executeQuery("select * from UnitUpgrades where Unit = \"" + tag + "\";");
                while (r3.next()) {
                    unit.upgrades.add(r3.getString("UpgradeUnit"));
                }

                // load prerequired building
                ResultSet r4 = gameplay.executeQuery("select * from Unit_BuildingPrereqs where Unit = \"" + tag + "\";");
                while (r4.next()) {
                    unit.prereqBuildings.add(r4.getString("PrereqBuilding"));
                }

                // load XP2 information
                ResultSet r5 = gameplay.executeQuery("select * from Units_XP2 where UnitType = \"" + tag + "\";");
                if (r5.next()) {
                    unit.resourceMaintenanceAmount = r5.getInt("ResourceMaintenanceAmount");
                    unit.resourceCost = r5.getInt("ResourceCost");
                    unit.resourceMaintenanceType = r5.getString("ResourceMaintenanceType");
                }

                // load unit icon
                String iconString = "ICON_" + tag;
                BufferedImage icon = Tools.getImage(iconString);
                if (icon != null) {
                    String path = iconString + ".png";
                    unit.icon = Tools.IMAGE_URL + "/" + path;
                    ImageEditor.saveImage(icon, path);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading units.");
            System.err.println(e.getClass().getName() + e.getMessage());
        } finally {
            try {
                if (gameplay != null) gameplay.close();
            } catch (Exception e) {
            }
        }
    }

    // this method shold be called after all other models are loaded, which links information
    public static void linkData () {
        for (Entry<String, Unit> entry : units.entrySet()) {
            Unit unit = entry.getValue();
            List<String> remove = new ArrayList<>();
            for (String build : unit.prereqBuildings) {
                Building building = Building.buildings.get(build);
                if (building == null) {
                    remove.add(build);
                }
            }
            unit.prereqBuildings.removeAll(remove);
        }
    }
    
    // Convert information into json page
    @Override
    public JSONObject toJson (String language) {
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
            CityState cityState = CityState.cityStates.get(belongs);
            if (cityState != null) {
                traitContents.add(cityState.getIconLabel(language));
            }
        }

        if (replace != null) {
            traitContents.add(Tools.getSeparator());
            traitContents.add(Tools.getHeader(Tools.getControlText("Replaces", language)));
            Unit uni = units.get(replace);
            traitContents.add(uni.getIconLabel(language));
        } else {
            boolean hasReplace = false;
            for (Entry<String, Unit> entry : units.entrySet()) {
                if (tag.equals(entry.getValue().replace)) {
                    hasReplace = true;
                    break;
                }
            }
            if (hasReplace) {
                traitContents.add(Tools.getSeparator());
                traitContents.add(Tools.getHeader(Tools.getControlText("Replaced by", language)));
                for (Entry<String, Unit> entry : units.entrySet()) {
                    Unit uni = entry.getValue();
                    if (tag.equals(uni.replace)) {
                        traitContents.add(uni.getIconLabel(language));
                    }
                }
            }
        }

        if (upgrades.size() > 0) {
            traitContents.add(Tools.getSeparator());
            traitContents.add(Tools.getHeader(Tools.getControlText("Upgrades To", language)));
            for (String u : upgrades) {
                Unit uni = units.get(u);
                if (uni != null) {
                    traitContents.add(uni.getIconLabel(language));
                }
            }
        }
        boolean hasUpgrade = false;
        for (Entry<String, Unit> entry : units.entrySet()) {
            Unit uni = entry.getValue();
            if ((uni.upgrades.contains(tag) || uni.upgrades.contains(replace)) && (uni.belongs == null || uni.belongs.equals(belongs))) {
                hasUpgrade = true;
                break;
            }
        }
        if (hasUpgrade) {
            traitContents.add(Tools.getSeparator());
            traitContents.add(Tools.getHeader(Tools.getControlText("Upgraded From", language)));
            for (Entry<String, Unit> entry : units.entrySet()) {
                Unit uni = entry.getValue();
                if ((uni.upgrades.contains(tag) || uni.upgrades.contains(replace)) && (uni.belongs == null || uni.belongs.equals(belongs))) {
                    traitContents.add(uni.getIconLabel(language));
                }
            }
        }

        traitContents.add(Tools.getSeparator());
        if (promotionClass != null) {
            UnitPromotionClass cla = UnitPromotionClass.classes.get(promotionClass);
            if (cla != null) {
                traitContents.add(Tools.getLabel(Tools.getControlText("Promotion Class", language) + cla.getLinkedTitle(language)));
            }
        }
        
        if (baseSightRange > 0) {
            traitContents.add(Tools.getIconlabel("", "", "", baseSightRange + "", Tools.getControlText("SightRange", language)));
        }

        if (baseMoves > 0) {
            traitContents.add(Tools.getIconlabel("", Tools.IMAGE_URL + "/ICON_MOVES.png", "ICON_MOVES", baseMoves + "", Tools.getControlText("MovementPoints", language)));
        }

        if (combat > 0) {
            traitContents.add(Tools.getIconlabel("", Tools.IMAGE_URL + "/ICON_STRENGTH.png", "ICON_STRENGTH", combat + "", Tools.getControlText("MeleeStrength", language)));
        }

        if (rangedCombat > 0) {
            traitContents.add(Tools.getIconlabel("", Tools.IMAGE_URL + "/ICON_RANGED_STRENGTH.png", "ICON_RANGED_STRENGTH", rangedCombat + "", Tools.getControlText("RangedStrength", language)));
        }

        if (range > 0) {
            traitContents.add(Tools.getIconlabel("", Tools.IMAGE_URL + "/ICON_RANGE.png", "ICON_RANGE", range + "", Tools.getControlText("Range", language)));
        }

        if (bombard > 0) {
            traitContents.add(Tools.getIconlabel("", Tools.IMAGE_URL + "/ICON_BOMBARD.png", "ICON_BOMBARD", bombard + "", Tools.getControlText("BombardStrength", language)));
        }

        if (antiAirCombat > 0) {
            traitContents.add(Tools.getIconlabel("", Tools.IMAGE_URL + "/ICON_STATS_ANTIAIR.png", "ICON_STATS_ANTIAIR", antiAirCombat + "", Tools.getControlText("Anti-airStrength", language)));
        }

        if (buildCharges > 0) {
            traitContents.add(Tools.getIconlabel("", Tools.IMAGE_URL + "/ICON_BUILD_CHARGES.png", "ICON_BUILD_CHARGES", buildCharges + "", Tools.getControlText("BuildCharges", language)));
        }

        if (religiousStrength > 0) {
            traitContents.add(Tools.getIconlabel("", Tools.IMAGE_URL + "/ICON_RELIGION.png", "ICON_RELIGION", religiousStrength + "", Tools.getControlText("ReligiousStrength", language)));
        }

        if (spreadCharges > 0) {
            traitContents.add(Tools.getIconlabel("", Tools.IMAGE_URL + "/ICON_RELIGION.png", "ICON_RELIGION", spreadCharges + "", Tools.getControlText("SpreadCharges", language)));
        }
        
        if (religiousHealCharges > 0) {
            traitContents.add(Tools.getIconlabel("", Tools.IMAGE_URL + "/ICON_RELIGION.png", "ICON_RELIGION", religiousHealCharges + "", Tools.getControlText("HealCharges", language)));
        }

        if (religiousEvictPercent > 0) {
            traitContents.add(Tools.getIconlabel("", Tools.IMAGE_URL + "/ICON_RELIGION.png", "ICON_RELIGION", religiousEvictPercent + "%", Tools.getControlText("PressureEliminated", language)));
        }

        if (prereqBuildings.size() > 0 || prereqTech != null || prereqCivic != null || (canTrain && (!mustPurchase || purchaseYield != null)) || maintenance > 0) {
            JSONArray requirementContents = new JSONArray();
            rightColumnItems.add(Tools.getStatbox(Tools.getControlText("Requirements", language), requirementContents));
            if (prereqBuildings.size() > 0) {
                requirementContents.add(Tools.getSeparator());
                if (prereqBuildings.size() > 1) {
                    requirementContents.add(Tools.getHeader(Tools.getControlText("Building any", language)));
                } else {
                    requirementContents.add(Tools.getHeader(Tools.getControlText("Building", language)));
                }
                for (String b : prereqBuildings) {
                    Building prereq = Building.buildings.get(b);
                    if (prereq != null) {
                        requirementContents.add(prereq.getIconLabel(language));
                    } else {
                        System.out.println(b);
                    }
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
            if ((canTrain && (!mustPurchase || purchaseYield != null)) || maintenance > 0) {
                requirementContents.add(Tools.getSeparator());
                if (canTrain) {
                    if (!mustPurchase) {
                        requirementContents.add(Tools.getHeader(Tools.getControlText("Production Cost", language)));
                        if(costProgressionModel.equals("COST_PROGRESSION_PREVIOUS_COPIES") && costProgressionParam1 != 0) {
                            requirementContents.add(Tools.getLabel(Tools.getControlText("Basic Cost", language) + cost + Tools.getYield("YIELD_PRODUCTION", language)));
                            requirementContents.add(Tools.getLabel(Tools.getControlText("Extra Cost 1", language) + getTitle(language) + Tools.getControlText("Extra Cost 2", language) + costProgressionParam1 + Tools.getYield("YIELD_PRODUCTION", language)));
                        } else {
                            requirementContents.add(Tools.getLabel(cost + Tools.getYield("YIELD_PRODUCTION", language)));
                        }
                    }
                    if (purchaseYield != null) {
                        requirementContents.add(Tools.getHeader(Tools.getControlText("Purchase Cost", language)));
                        int c = cost;
                        int p = costProgressionParam1;
                        if (purchaseYield.equals("YIELD_FAITH")) {
                            c = c * 2;
                            p = p * 2;
                        } else if (purchaseYield.equals("YIELD_GOLD")) {
                            c = c * 4;
                            p = p * 4;
                        }
                        if(costProgressionModel.equals("COST_PROGRESSION_PREVIOUS_COPIES") && costProgressionParam1 != 0) {
                            requirementContents.add(Tools.getLabel(Tools.getControlText("Basic Cost", language) + c + Tools.getYield(purchaseYield, language)));
                            requirementContents.add(Tools.getLabel(Tools.getControlText("Extra Cost 1", language) + getTitle(language) + Tools.getControlText("Extra Cost 2", language) + p + Tools.getYield(purchaseYield, language)));
                        } else {
                            requirementContents.add(Tools.getLabel(c + Tools.getYield(purchaseYield, language)));
                        }
                    }
                }
                if (strategicResource != null) {
                    requirementContents.add(Tools.getHeader(Tools.getControlText("Strategic Resource", language)));
                    requirementContents.add(Tools.getLabel(resourceCost + Tools.getResource(strategicResource, language)));
                }
                if (maintenance > 0 || resourceMaintenanceType != null) {
                    requirementContents.add(Tools.getHeader(Tools.getControlText("Maintenance Cost", language)));
                    if (maintenance > 0) {
                        requirementContents.add(Tools.getLabel(maintenance + " " + Tools.getYield("YIELD_GOLD", language)));
                    }
                    if (resourceMaintenanceType != null) {
                        requirementContents.add(Tools.getLabel(resourceMaintenanceAmount + Tools.getResource(resourceMaintenanceType, language)));
                    }
                }
            }
        }
        return object;
    }

    @Override
    public int getOrder() {
        return (belongs != null ? 131072 : 0) + super.getOrder();
    }

    @Override
    public String getChapter() {
        return "units";
    }

    @Override
    public String getTagPrefix() {
        return "UNIT_";
    }

    @Override
    public String getFolder () {
        if (formationClass.equals("FORMATION_CLASS_NAVAL") || formationClass.equals("FORMATION_CLASS_LAND_COMBAT")) {
            return promotionClass.substring("PROMOTION_CLASS_".length(), promotionClass.length());
        } else {
            return formationClass.substring("FORMATION_CLASS_".length(), formationClass.length());
        }
    }

    @Override
    public String getFolderName (String language) {
        if (formationClass.equals("FORMATION_CLASS_NAVAL") || formationClass.equals("FORMATION_CLASS_LAND_COMBAT")) {
            return Tools.getText("LOC_" + promotionClass + "_NAME", language);
        } else {
            return Tools.getText("LOC_" + formationClass + "_NAME", language);
        }
    }

    @Override
    public String getCat() {
        return "单位改动";
    }

    @Override
    public int getCatOrder() {
        return -1600;
    }
}
