package model;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import tools.Tools;

public class YieldChange {
    
    public static final Map<String, YieldChange> yieldChanges = new HashMap<>();

    public String tag;
    public String description;
    public String yieldType;
    public int yieldChange;
    public int tilesRequired;
    public boolean otherDistrict;
    public boolean seaResource;
    public String terrain;
    public String feature;
    public boolean river;
    public boolean wonder;
    public boolean natureWonder;
    public String improvement;
    public String distinct;
    public String prereqCivic;
    public String prereqTech;
    public String obsoleteCivic;
    public String obsoleteTech;
    public boolean resource;
    public String resourceClass;
    public boolean self;

    public YieldChange (String tag) {
        this.tag = tag;
        yieldChanges.put(tag, this);
    }

    public static void load() {
        Statement gameplay = null;
        try {
            gameplay = DriverManager.getConnection(Tools.GAMEPLAY_DATABASE).createStatement();
    
            // load adjacency yield change list
            ResultSet r1 = gameplay.executeQuery("select * from Adjacency_YieldChanges;");
            while (r1.next()) {
                String tag = r1.getString("ID");
                YieldChange change = new YieldChange(tag);
                change.description = r1.getString("Description");
                change.yieldType = r1.getString("YieldType");
                change.yieldChange = r1.getInt("YieldChange");
                change.tilesRequired = r1.getInt("TilesRequired");
                change.otherDistrict = r1.getBoolean("OtherDistrictAdjacent");
                change.seaResource = r1.getBoolean("AdjacentSeaResource");
                change.terrain = r1.getString("AdjacentTerrain");
                change.feature = r1.getString("AdjacentFeature");
                change.river = r1.getBoolean("AdjacentRiver");
                change.wonder = r1.getBoolean("AdjacentWonder");
                change.natureWonder = r1.getBoolean("AdjacentNaturalWonder");
                change.improvement = r1.getString("AdjacentImprovement");
                change.distinct = r1.getString("AdjacentDistrict");
                change.prereqCivic = r1.getString("PrereqCivic");
                change.prereqTech = r1.getString("PrereqTech");
                change.obsoleteCivic = r1.getString("ObsoleteCivic");
                change.obsoleteTech = r1.getString("ObsoleteTech");
                change.resource = r1.getBoolean("AdjacentResource");
                change.resourceClass = r1.getString("AdjacentResourceClass");
                change.self = r1.getBoolean("Self");
            }
        } catch (Exception e) {
            System.err.println("Error loading adjacency yield changes.");
            System.err.println(e.getClass().getName() + e.getMessage());
        } finally {
            try {
                if (gameplay != null) gameplay.close();
            } catch (Exception e) {
            }
        }
    }

    public String toString (String language) {
        String s = "";
        switch (language) {
            case "zh_Hans" :
            case "zh_Hans_CN" : {
                s += "+" + yieldChange + Tools.getYield(yieldType, language);
                if (self) {
                } else if (river) {
                    s += "，如果相邻河流";
                } else {
                    s += "，来自每";
                    if (tilesRequired > 1) {
                        s += tilesRequired;
                    }
                    s += "个相邻";
                    if (otherDistrict) {
                        s += "区域";
                    } else if (seaResource) {
                        s += "海洋资源";
                    } else if (terrain != null) {
                        s += Tools.getTextWithAlt("LOC_" + terrain + "_NAME", language);
                    } else if (feature != null) {
                        s += Tools.getTextWithAlt("LOC_" + feature + "_NAME", language);
                    } else if (wonder) {
                        s += "奇观";
                    } else if (natureWonder) {
                        s += "自然奇观";
                    } else if (improvement != null) {
                        Improvement impr = Improvement.improvements.get(improvement);
                        s += impr.getLinkedTitle(language);
                    } else if (distinct != null) {
                        District dist = District.distincts.get(distinct);
                        s += dist.getLinkedTitle(language);
                    } else if (resource) {
                        s += "资源";
                    } else if (resourceClass != null && resourceClass.equals("RESOURCECLASS_BONUS")) {
                        s += "加成资源";
                    } else if (resourceClass != null && resourceClass.equals("RESOURCECLASS_LUXURY")) {
                        s += "奢侈资源";
                    } else if (resourceClass != null && resourceClass.equals("RESOURCECLASS_STRATEGIC")) {
                        s += "战略资源";
                    } else {
                        System.out.println(tag);
                    }
                    s += "单元格";
                }
                if (prereqCivic != null) {
                    s += "，需求" + Tools.getTextWithAlt("LOC_" + prereqCivic + "_NAME", language) + "";
                }
                if (prereqTech != null) {
                    s += "，需求" + Tools.getTextWithAlt("LOC_" + prereqTech + "_NAME", language) + "";
                }
                if (obsoleteCivic != null) {
                    s += "，" + Tools.getTextWithAlt("LOC_" + obsoleteCivic + "_NAME", language) + "后失效";
                }
                if (obsoleteTech != null) {
                    s += "，" + Tools.getTextWithAlt("LOC_" + obsoleteTech + "_NAME", language) + "后失效";
                }
                s += "。";
                break;
            }
            case "en_US" : {
                s += "+" + yieldChange + Tools.getYield(yieldType, language);
                if (self) {
                } else if (river) {
                    s += " for having an adjacent river tile";
                } else {
                    s += " from";
                    if (tilesRequired == 1) {
                        s += " each";
                    } else {
                        s += " every " + tilesRequired;
                    }
                    s += " adjacent ";
                    if (seaResource) {
                        s += "sea resource";
                    } else if (terrain != null) {
                        s += Tools.getTextWithAlt("LOC_" + terrain + "_NAME", language);
                    } else if (feature != null) {
                        s += Tools.getTextWithAlt("LOC_" + feature + "_NAME", language);
                    } else if (wonder) {
                        s += "wonder";
                    } else if (natureWonder) {
                        s += "natural wonder";
                    } else if (improvement != null) {
                        Improvement impr = Improvement.improvements.get(improvement);
                        s += impr.getLinkedTitle(language);
                    } else if (distinct != null) {
                        District dist = District.distincts.get(distinct);
                        s += dist.getLinkedTitle(language);
                    } else if (resource) {
                        s += "resource";
                    } else if (resourceClass != null && resourceClass.equals("RESOURCECLASS_BONUS")) {
                        s += "bonus resource";
                    } else if (resourceClass != null && resourceClass.equals("RESOURCECLASS_LUXURY")) {
                        s += "luxury resource";
                    } else if (resourceClass != null && resourceClass.equals("RESOURCECLASS_STRATEGIC")) {
                        s += "strategic resource";
                    }
                    if (tilesRequired == 1) {
                        s += " tile";
                    } else {
                        s += " tiles";
                    }
                }
                if (prereqCivic != null) {
                    s += ", requires " + Tools.getTextWithAlt("LOC_" + prereqCivic + "_NAME", language);
                }
                if (prereqTech != null) {
                    s += ", requires " + Tools.getTextWithAlt("LOC_" + prereqTech + "_NAME", language);
                }
                if (obsoleteCivic != null) {
                    s += ", becomse obsolete with " + Tools.getTextWithAlt("LOC_" + obsoleteCivic+ "_NAME", language);
                }
                if (obsoleteTech != null) {
                    s += ", becomse obsolete with " + Tools.getTextWithAlt("LOC_" + obsoleteTech + "_NAME", language);
                }
                s += ". ";
                break;
            }
            default : {
                s = Tools.getTextWithAlt(description, language);
            }
        }
        return s;
    }
}
