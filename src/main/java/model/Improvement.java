package model;

import java.awt.image.BufferedImage;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
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

public class Improvement extends UnlockableWithIcon {

    public static final Map<String, Improvement> improvements = new HashMap<>();

    public String description;
    public String trait;

    public String plunderType;
    public int plunderAmount;
    public double housing;
    public int appeal;

    static class BonusYieldChange {
        String yieldType;
        int yieldChange;
        String preTech;
        String preCivic;
    }

    public Map<String, Integer> yields = new HashMap<>();
    public List<BonusYieldChange> bonusYields = new ArrayList<>();
    public List<String> validUnit = new ArrayList<>();
    public List<String> yieldChanges = new ArrayList<>();
    public List<String> validResouces = new ArrayList<>();
    
    public String belongs;
    
    public Improvement (String tag) {
        super(tag);
        improvements.put(tag, this);
    }
    
    // load improvements from database
    public static void load () {
        Statement gameplay = null;
        try {
            gameplay = DriverManager.getConnection(Tools.GAMEPLAY_DATABASE).createStatement();
    
            // load improvements list
            ResultSet r1 = gameplay.executeQuery("select * from Improvements where Goody = \"0\" and BarbarianCamp = \"0\";");
            while (r1.next()) {
                String tag = r1.getString("ImprovementType");
                Improvement improvement = new Improvement(tag);
                improvement.name = r1.getString("Name");
                improvement.description = r1.getString("Description");
                improvement.trait = r1.getString("TraitType");
                improvement.prereqCivic = r1.getString("PrereqCivic");
                improvement.prereqTech = r1.getString("PrereqTech");
                improvement.plunderType = r1.getString("PlunderType");
                improvement.plunderAmount = r1.getInt("plunderAmount");
                improvement.housing = ((double) r1.getInt("Housing")) / r1.getInt("TilesRequired");
                improvement.appeal = r1.getInt("Appeal");
            }
    
            // load other information
            for(Entry<String, Improvement> entry : improvements.entrySet()) {
                String tag = entry.getKey();
                Improvement improvement = entry.getValue();

                // load basic yields
                ResultSet r2 = gameplay.executeQuery("select * from Improvement_YieldChanges where ImprovementType = \"" + tag + "\" and YieldChange != \"0\";");
                while (r2.next()) {
                    improvement.yields.put(r2.getString("YieldType"), r2.getInt("YieldChange"));
                }

                // load bonus yields (those who require tech or civic)
                ResultSet r3 = gameplay.executeQuery("select * from Improvement_BonusYieldChanges where ImprovementType = \"" + tag + "\";");
                while (r3.next()) {
                    BonusYieldChange change = new BonusYieldChange();
                    change.yieldType = r3.getString("YieldType");
                    change.yieldChange = r3.getInt("BonusYieldChange");
                    change.preTech = r3.getString("PrereqTech");
                    change.preCivic = r3.getString("PrereqCivic");
                    improvement.bonusYields.add(change);
                }

                // load valid units
                ResultSet r4 = gameplay.executeQuery("select * from Improvement_ValidBuildUnits where ImprovementType = \"" + tag + "\";");
                while (r4.next()) {
                    improvement.validUnit.add(r4.getString("UnitType"));
                }

                // load adjacency yield changes
                ResultSet r5 = gameplay.executeQuery("select * from Improvement_Adjacencies where ImprovementType = \"" + tag + "\";");
                while (r5.next()) {
                    improvement.yieldChanges.add(r5.getString("YieldChangeId"));
                }

                // load valid resources
                ResultSet r6 = gameplay.executeQuery("select * from Improvement_ValidResources where ImprovementType = \"" + tag + "\" and (exists (select * from Resource_ValidFeatures where ResourceType = Improvement_ValidResources.ResourceType) or exists (select * from Resource_ValidTerrains where ResourceType = Improvement_ValidResources.ResourceType));");
                while (r6.next()) {
                    improvement.validResouces.add(r6.getString("ResourceType"));
                }

                // load improvement icon
                String iconString = "ICON_" + tag;
                BufferedImage icon = Tools.getImage(iconString);
                if (icon != null) {
                    String path = iconString + ".png";
                    improvement.icon = Tools.IMAGE_URL + "/" + path;
                    ImageEditor.saveImage(icon, path);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading improvements.");
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

    @Override
    public JSONObject toJson(String language) {
        JSONObject object = super.toJson(language);

        JSONArray leftColumnItems = new JSONArray();
        if (description != null) {
            leftColumnItems.add(Tools.getHeader(Tools.getControlText("Description", language)));
            leftColumnItems.add(Tools.getBody(null, Tools.getTextWithAlt(description, language)));
        }
        object.put("leftColumnItems", leftColumnItems);

        if (yieldChanges.size() > 0) {
            leftColumnItems.add(Tools.getHeader(Tools.getControlText("Adacency Bonus", language)));
            String s = "";
            for (String chg : yieldChanges) {
                YieldChange change = YieldChange.yieldChanges.get(chg);
                if (change != null) {
                    s += change.toString(language) + "[NEWLINE]";
                } else {
                    s += "[" + chg + "]" + "[NEWLINE]";
                }
            }
            leftColumnItems.add(Tools.getBody(null, s));
        }

        JSONArray rightColumnItems = new JSONArray();
        object.put("rightColumnItems", rightColumnItems);

        if (belongs != null || yields.size() > 0 || bonusYields.size() > 0 || housing > 0 || appeal != 0 || (plunderType != null && !plunderType.equals("NO_PLUNDER") && !plunderType.equals("PLUNDER_NONE") && plunderAmount != 0)) {
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
    
            if (yields.size() > 0 || bonusYields.size() > 0 || housing > 0 || appeal != 0 || (plunderType != null && !plunderType.equals("NO_PLUNDER") && !plunderType.equals("PLUNDER_NONE") && plunderAmount != 0)) {
                traitContents.add(Tools.getSeparator());
                if (yields.size() > 0) {
                    for (Entry<String, Integer> entry : yields.entrySet()) {
                        traitContents.add(Tools.getLabel("+" + entry.getValue() + Tools.getYield(entry.getKey(), language)));
                    }
                }
    
                if (bonusYields.size() > 0) {
                    for (BonusYieldChange yield : bonusYields) {
                        String s = "";
                        s += "+";
                        s += yield.yieldChange;
                        s += Tools.getYield(yield.yieldType, language);
                        if (yield.preTech != null) {
                            s += Tools.getControlText(", requires ", language);
                            Technology tech = Technology.technologies.get(yield.preTech);
                            if (tech != null) {
                            s += tech.getLinkedTitle(language);
                            } else {
                                s += "[" + yield.preTech + "]";
                            }
                        } else if (yield.preCivic != null) {
                            s += Tools.getControlText(", requires ", language);
                            Civic civic = Civic.civics.get(yield.preCivic);
                            if (civic != null) {
                            s += civic.getLinkedTitle(language);
                            } else {
                                s += "[" + yield.preCivic + "]";
                            }
                        }
                        traitContents.add(Tools.getLabel(s));
                    }
                }
    
                if (housing != 0) {
                    String h = "";
                    if (housing == (int) housing) {
                        h = (int) housing + "";
                    } else {
                        h = new DecimalFormat("0.0").format(housing);
                    }
                    traitContents.add(Tools.getLabel("+" + h + " [ICON_HOUSING] " + Tools.getControlText("housing", language)));
                }
    
                if (appeal != 0) {
                    traitContents.add(Tools.getLabel(Tools.getControlText("appeal 1", language) + (appeal > 0 ? "+" : "") + appeal + Tools.getControlText("appeal 2", language)));
                }
    
                if (plunderType != null && !plunderType.equals("NO_PLUNDER") && !plunderType.equals("PLUNDER_NONE") && plunderAmount != 0) {
                    traitContents.add(Tools.getLabel(Tools.getControlText("plunder yields", language) + plunderAmount + Tools.getYield(plunderType, language)));
                }
            }
        }

        if (prereqTech != null || prereqCivic != null || validUnit.size() > 0 || validResouces.size() > 0) {
            JSONArray requirementContents = new JSONArray();
            rightColumnItems.add(Tools.getStatbox(Tools.getControlText("Requirements", language), requirementContents));
            if (prereqTech != null) {
                Technology tech = Technology.technologies.get(prereqTech);
                if (tech != null) {
                    requirementContents.add(Tools.getSeparator());
                    requirementContents.add(Tools.getHeader(Tools.getControlText("Technology", language)));
                    requirementContents.add(tech.getIconLabel(language));
                } else {
                    requirementContents.add(Tools.getSeparator());
                    requirementContents.add(Tools.getHeader(Tools.getControlText("Technology", language)));
                    requirementContents.add(Tools.getLabel("[" + prereqTech + "]"));
                }
            }
            if (prereqCivic != null) {
                Civic civic = Civic.civics.get(prereqCivic);
                if (civic != null) {
                    requirementContents.add(Tools.getSeparator());
                    requirementContents.add(Tools.getHeader(Tools.getControlText("Civic", language)));
                    requirementContents.add(civic.getIconLabel(language));
                } else {
                    requirementContents.add(Tools.getSeparator());
                    requirementContents.add(Tools.getHeader(Tools.getControlText("Civic", language)));
                    requirementContents.add(Tools.getLabel("[" + prereqCivic + "]"));
                }
            }
            if (validUnit.size() > 0) {
                requirementContents.add(Tools.getSeparator());
                requirementContents.add(Tools.getHeader(Tools.getControlText("Built By", language)));
                for (String u : validUnit) {
                    Unit unit = Unit.units.get(u);
                    if (unit != null) {
                    requirementContents.add(unit.getIconLabel(language));
                    } else {
                        requirementContents.add(Tools.getLabel("[" + u + "]"));
                    }
                }
            }
            if (validResouces.size() > 0) {
                requirementContents.add(Tools.getSeparator());
                requirementContents.add(Tools.getHeader(Tools.getControlText("Resouces", language)));
                for (String r : validResouces) {
                    Resource resource = Resource.resources.get(r);
                    if (resource != null) {
                    requirementContents.add(resource.getIconLabel(language));
                    } else {
                        requirementContents.add(Tools.getLabel("[" + r + "]"));
                    }
                }
            }
        }

        return object;
    }

    @Override
    public String getChapter () {
        return "improvements";
    }

    @Override
    public String getFolder() {
        if (belongs == null) {
            return "common";
        } else {
            return "unique";
        }
    }

    @Override
    public String getFolderName(String language) {
        if (belongs == null) {
            return Tools.getControlText("Common Improvements", language);
        } else {
            return Tools.getControlText("Unique Improvements", language);
        }
    }

    @Override
    public String getTagPrefix() {
        return "IMPROVEMENT_";
    }

    @Override
    public String getCat() {
        return "改良改动";
    }
    
    @Override
    public int getCatOrder() {
        return -1000;
    }
}
