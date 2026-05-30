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

import model.abstracts.Main;
import model.abstracts.UnlockableWithIcon;
import model.abstracts.Writable;
import model.abstracts.WritableWithIcon;
import tools.ImageEditor;
import tools.Tools;

public class Civic extends WritableWithIcon {

    public static final Map<String, Civic> civics = new HashMap<>();

    public String description;
    public List<String> quotes = new ArrayList<>();
    public List<Integer> randomCost = new ArrayList<>();

    public int cost;
    public String era;
    public int treeRow;

    public String boost;

    public List<String> prereqCivic = new ArrayList<>();
    public List<String> prereqTech = new ArrayList<>();
    public List<String> leadsToCivic = new ArrayList<>();
    public List<String> leadsToBonus = new ArrayList<>();

    public List<String> buildings = new ArrayList<>();
    public List<String> districts = new ArrayList<>();
    public List<String> improvements = new ArrayList<>();
    public List<String> units = new ArrayList<>();
    public List<String> resources = new ArrayList<>();
    public List<String> projects = new ArrayList<>();
    public List<String> policies = new ArrayList<>();
    public List<String> governments = new ArrayList<>();
    public List<String> envoys = new ArrayList<>();

    public Civic(String tag) {
        super(tag);
        civics.put(tag, this);
    }

    public static void load() {
        Statement gameplay = null;
        Statement text = null;
        try {
            gameplay = DriverManager.getConnection(Tools.GAMEPLAY_DATABASE).createStatement();
            text = DriverManager.getConnection(Tools.TEXT_DATABASE).createStatement();
    
            ResultSet r1 = gameplay.executeQuery("select * from Civics;");
            while (r1.next()) {
                String tag = r1.getString("CivicType");
                Civic civic = new Civic(tag);
                civic.name = r1.getString("Name");
                civic.description = r1.getString("Description");
                civic.cost = r1.getInt("Cost");
                civic.era = r1.getString("EraType");
                civic.treeRow = r1.getInt("UITreeRow");
                
                // 加载引言
                for (int i = 1; i <= 2; i++) {
                    String quoteTag = "LOC_" + tag + "_QUOTE_" + i;
                    String hdQuoteTag = "LOC_" + tag + "_HD_QUOTE_" + i;
                    String quote = Tools.getText(hdQuoteTag, "zh_Hans_CN");
                    if (quote == null) {
                        quote = Tools.getText(quoteTag, "zh_Hans_CN");
                    }
                    if (quote != null) {
                        civic.quotes.add(quote);
                    }
                }
            }
    
            // load other information
            for(Entry<String, Civic> entry : civics.entrySet()) {
                String tag = entry.getKey();
                Civic civic = entry.getValue();

                // load prereq civics
                ResultSet r2 = gameplay.executeQuery("select * from CivicPrereqs where Civic = \"" + tag + "\";");
                while (r2.next()) {
                    civic.prereqCivic.add(r2.getString("PrereqCivic"));
                }

                // load lead to civics
                ResultSet r3 = gameplay.executeQuery("select * from CivicPrereqs where PrereqCivic = \"" + tag + "\";");
                while (r3.next()) {
                    civic.leadsToCivic.add(r3.getString("Civic"));
                }

                // load prereq techs
                r2 = gameplay.executeQuery("select * from CivicPrereqs where Civic = \"" + tag + "\" and PrereqCivic in (select TechnologyType from Technologies);");
                while (r2.next()) {
                    civic.prereqTech.add(r2.getString("PrereqCivic"));
                }

                // load boost
                ResultSet r4 = gameplay.executeQuery("select * from Boosts where CivicType = \"" + tag + "\";");
                if (r4.next()) {
                    civic.boost = r4.getString("TriggerDescription");
                }

                // load random cost
                ResultSet r5 = gameplay.executeQuery("select * from CivicRandomCosts where CivicType = \"" + tag + "\";");
                while (r5.next()) {
                    civic.randomCost.add(r5.getInt("Cost"));
                }

                // load buildings
                r2 = gameplay.executeQuery("select BuildingType from Buildings where PrereqCivic = \"" + tag + "\";");
                while (r2.next()) {
                    civic.buildings.add(r2.getString("BuildingType"));
                }

                // load districts
                r2 = gameplay.executeQuery("select DistrictType from Districts where PrereqCivic = \"" + tag + "\";");
                while (r2.next()) {
                    civic.districts.add(r2.getString("DistrictType"));
                }

                // load improvements
                r2 = gameplay.executeQuery("select ImprovementType from Improvements where PrereqCivic = \"" + tag + "\";");
                while (r2.next()) {
                    civic.improvements.add(r2.getString("ImprovementType"));
                }

                // load projects
                r2 = gameplay.executeQuery("select ProjectType from Projects where PrereqCivic = \"" + tag + "\";");
                while (r2.next()) {
                    civic.projects.add(r2.getString("ProjectType"));
                }

                // load units
                r2 = gameplay.executeQuery("select UnitType from Units where PrereqCivic = \"" + tag + "\";");
                while (r2.next()) {
                    civic.units.add(r2.getString("UnitType"));
                }

                // load policies
                r2 = gameplay.executeQuery("select PolicyType from Policies where PrereqCivic = \"" + tag + "\";");
                while (r2.next()) {
                    civic.policies.add(r2.getString("PolicyType"));
                }

                // load governments
                r2 = gameplay.executeQuery("select GovernmentType from Governments where PrereqCivic = \"" + tag + "\";");
                while (r2.next()) {
                    civic.governments.add(r2.getString("GovernmentType"));
                }

                // load civic icon
                String iconString = "ICON_" + tag;
                BufferedImage icon = Tools.getImage(iconString);
                if (icon != null) {
                    String path = iconString + ".png";
                    civic.icon = Tools.IMAGE_URL + "/" + path;
                    ImageEditor.saveImage(icon, path);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading civics.");
            System.err.println(e.getClass().getName() + e.getMessage());
        } finally {
            try {
                if (gameplay != null) gameplay.close();
                if (text != null) text.close();
            } catch (Exception e) {
            }
        }
    }

    
    @Override
    public JSONObject toJson(String language) {
        JSONObject object = super.toJson(language);
        object.put("description", description);
        object.put("quotes", quotes);
        object.put("cost", cost);
        object.put("era", Tools.getText("LOC_" + era + "_NAME", language));
        object.put("treeRow", treeRow);
        object.put("boost", boost);
        object.put("prereqCivic", prereqCivic);
        object.put("prereqTech", prereqTech);
        object.put("leadsToCivic", leadsToCivic);
        object.put("leadsToBonus", leadsToBonus);
        object.put("buildings", buildings);
        object.put("districts", districts);
        object.put("improvements", improvements);
        object.put("units", units);
        object.put("resources", resources);
        object.put("projects", projects);
        object.put("policies", policies);
        object.put("governments", governments);
        object.put("envoys", envoys);

        // 添加leftColumnItems
        JSONArray leftColumnItems = new JSONArray();
        
        // 添加描述
        if (description != null) {
            JSONObject descHeader = new JSONObject();
            descHeader.put("type", "header");
            descHeader.put("text", Tools.getControlText("Description", language));
            leftColumnItems.add(descHeader);
            
            JSONObject descBody = new JSONObject();
            descBody.put("type", "body");
            descBody.put("text", Tools.getText(description, language));
            leftColumnItems.add(descBody);
        }
        
        // 添加引言
        if (!quotes.isEmpty()) {
            JSONObject quoteHeader = new JSONObject();
            quoteHeader.put("type", "header");
            quoteHeader.put("text", Tools.getControlText("Quotes", language));
            leftColumnItems.add(quoteHeader);
            
            for (String quote : quotes) {
                JSONObject quoteBody = new JSONObject();
                quoteBody.put("type", "body");
                quoteBody.put("text", quote);
                leftColumnItems.add(quoteBody);
            }
        }
        
        object.put("leftColumnItems", leftColumnItems);

        // 添加rightColumnItems
        JSONArray rightColumnItems = new JSONArray();
        object.put("rightColumnItems", rightColumnItems);

        // 添加特性框
        JSONArray traitContents = new JSONArray();
        rightColumnItems.add(Tools.getStatbox(Tools.getControlText("Traits", language), traitContents));

        traitContents.add(Tools.getSeparator());
        traitContents.add(Tools.getHeader(Tools.getControlText("Era", language)));
        traitContents.add(Tools.getLabel(Tools.getText("LOC_" + era + "_NAME", language)));
        if (randomCost.size() == 0) {
            traitContents.add(Tools.getHeader(Tools.getControlText("Culture Cost", language)));
            traitContents.add(Tools.getLabel(cost + Tools.getYield("CULTURE", language)));
        } else {
            traitContents.add(Tools.getHeader(Tools.getControlText("Culture Cost", language) + Tools.getControlText(" (Random)", language)));
            for (Integer c : randomCost) {
                traitContents.add(Tools.getLabel(c + Tools.getYield("CULTURE", language)));
            }
        }

        if (boost != null) {
            traitContents.add(Tools.getHeader(Tools.getControlText("Inspiration", language)));
            traitContents.add(Tools.getLabel(Tools.getText(boost, language)));
        }

        // 添加解锁内容框
        if (!buildings.isEmpty() || !districts.isEmpty() || !improvements.isEmpty() || 
            !projects.isEmpty() || !resources.isEmpty() || !units.isEmpty() || 
            !policies.isEmpty() || !governments.isEmpty() || !envoys.isEmpty()) {
            JSONArray unlockContents = new JSONArray();
            rightColumnItems.add(Tools.getStatbox(Tools.getControlText("Unlocks", language), unlockContents));
            
            unlockContents.add(Tools.getSeparator());
            List<UnlockableWithIcon> uniques = new ArrayList<>();

            for (Writable o : Main.WRITABLES) {
                if (o instanceof UnlockableWithIcon) {
                    UnlockableWithIcon obj = (UnlockableWithIcon) o;
                    if (tag.equals(obj.prereqCivic)) {
                        boolean isUnique = false;
                        for (Trait trait : Trait.traits.values()) {
                            if(trait.pointsTo != null && trait.pointsTo.equals(obj.tag)) {
                                isUnique = true;
                                break;
                            }
                        }
                        if (!isUnique) {
                            unlockContents.add(obj.getIconLabel(language));
                        } else {
                            uniques.add(obj);
                        }
                    }
                }
            }

            if (!uniques.isEmpty()) {
                unlockContents.add(Tools.getSeparator());
                for (UnlockableWithIcon obj : uniques) {
                    unlockContents.add(obj.getIconLabel(language));
                }
            }
        }

        // 添加市政树框
        JSONArray treeContents = new JSONArray();
        rightColumnItems.add(Tools.getStatbox(Tools.getControlText("Civic Tree", language), treeContents));

        treeContents.add(Tools.getSeparator());

        if (!prereqCivic.isEmpty()) {
            treeContents.add(Tools.getHeader(Tools.getControlText("Prerequires", language)));
            for (String c : prereqCivic) {
                Civic civic = civics.get(c);
                if (civic != null) {
                    treeContents.add(civic.getIconLabel(language));
                }
            }
        }

        if (!prereqCivic.isEmpty() && !leadsToCivic.isEmpty()) {
            treeContents.add(Tools.getSeparator());
        }

        if (!leadsToCivic.isEmpty()) {
            treeContents.add(Tools.getHeader(Tools.getControlText("Leads To Civic", language)));
            for (String c : leadsToCivic) {
                Civic civic = civics.get(c);
                if (civic != null) {
                    treeContents.add(civic.getIconLabel(language));
                }
            }
        }
        
        return object;
    }

    @Override
    public String getChapter() {
        return "civics";
    }

    @Override
    public String getFolder() {
        return era;
    }

    @Override
    public String getFolderName(String language) {
        return Tools.getText("LOC_" + era + "_NAME", language);
    }

    @Override
    public String getTagPrefix() {
        return "CIVIC_";
    }

    @Override
    public int getOrder() {
        return cost * 8 + treeRow;
    }

    @Override
    public String getCat() {
        return "科技&市政改动";
    }
    
    @Override
    public int getCatOrder() {
        return -1400;
    }
}
