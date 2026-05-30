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

public class Technology extends WritableWithIcon {

    public static final Map<String, Technology> technologies = new HashMap<>();

    public String description;
    public List<String> quotes = new ArrayList<>();
    public List<Integer> randomCost = new ArrayList<>();

    public int cost;
    public String era;
    public int treeRow;

    public String boost;

    public List<String> prereqTech = new ArrayList<>();
    public List<String> prereqCivic = new ArrayList<>();
    public List<String> leadToTech = new ArrayList<>();

    public List<String> buildings = new ArrayList<>();
    public List<String> districts = new ArrayList<>();
    public List<String> improvements = new ArrayList<>();
    public List<String> projects = new ArrayList<>();
    public List<String> resources = new ArrayList<>();
    public List<String> units = new ArrayList<>();
    public List<String> routes = new ArrayList<>();

    public Technology(String tag) {
        super(tag);
        technologies.put(tag, this);
    }

    public static void load() {
        Statement gameplay = null;
        Statement text = null;
        try {
            gameplay = DriverManager.getConnection(Tools.GAMEPLAY_DATABASE).createStatement();
            text = DriverManager.getConnection(Tools.TEXT_DATABASE).createStatement();
    
            ResultSet r1 = gameplay.executeQuery("select * from Technologies;");
            while (r1.next()) {
                String tag = r1.getString("TechnologyType");
                Technology technology = new Technology(tag);
                technology.name = r1.getString("Name");
                technology.description = r1.getString("Description");
                technology.cost = r1.getInt("Cost");
                technology.era = r1.getString("EraType");
                technology.treeRow = r1.getInt("UITreeRow");

                // 加载引言
                for (int i = 1; i <= 2; i++) {
                    String quoteTag = "LOC_" + tag + "_QUOTE_" + i;
                    String hdQuoteTag = "LOC_" + tag + "_HD_QUOTE_" + i;
                    String quote = Tools.getText(hdQuoteTag, "zh_Hans_CN");
                    if (quote == null) {
                        quote = Tools.getText(quoteTag, "zh_Hans_CN");
                    }
                    if (quote != null) {
                        technology.quotes.add(quote);
                    }
                }

                // 加载图标
                String iconString = "ICON_" + tag;
                BufferedImage icon = Tools.getImage(iconString);
                if (icon != null) {
                    String path = iconString + ".png";
                    technology.icon = Tools.IMAGE_URL + "/" + path;
                    ImageEditor.saveImage(icon, path);
                }
            }
    
            // 加载其他数据
            for(Entry<String, Technology> entry : technologies.entrySet()) {
                String tag = entry.getKey();
                Technology technology = entry.getValue();

                // 加载前置科技
                ResultSet r2 = gameplay.executeQuery("select * from TechnologyPrereqs where Technology = \"" + tag + "\";");
                while (r2.next()) {
                    technology.prereqTech.add(r2.getString("PrereqTech"));
                }

                // 加载后续科技
                ResultSet r3 = gameplay.executeQuery("select * from TechnologyPrereqs where PrereqTech = \"" + tag + "\";");
                while (r3.next()) {
                    technology.leadToTech.add(r3.getString("Technology"));
                }

                // 加载激励
                ResultSet r4 = gameplay.executeQuery("select * from Boosts where TechnologyType = \"" + tag + "\";");
                if (r4.next()) {
                    technology.boost = r4.getString("TriggerDescription");
                }

                // 加载随机花费
                ResultSet r5 = gameplay.executeQuery("select * from TechnologyRandomCosts where TechnologyType = \"" + tag + "\";");
                while (r5.next()) {
                    technology.randomCost.add(r5.getInt("Cost"));
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading technologies.");
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
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
        
        JSONArray leftColumnItems = new JSONArray();
        if (description != null) {
            leftColumnItems.add(Tools.getHeader(Tools.getControlText("Description", language)));
            leftColumnItems.add(Tools.getBody("", Tools.getText(description, language)));
        }
        
        // 添加引言显示
        if (!quotes.isEmpty()) {
            leftColumnItems.add(Tools.getHeader(Tools.getControlText("Quotes", language)));
            for (String quote : quotes) {
                leftColumnItems.add(Tools.getBody("", quote));
            }
        }
        
        object.put("leftColumnItems", leftColumnItems);

        JSONArray rightColumnItems = new JSONArray();
        object.put("rightColumnItems", rightColumnItems);

        // 添加特性框
        JSONArray traitContents = new JSONArray();
        rightColumnItems.add(Tools.getStatbox(Tools.getControlText("Traits", language), traitContents));

        traitContents.add(Tools.getSeparator());
        traitContents.add(Tools.getHeader(Tools.getControlText("Era", language)));
        traitContents.add(Tools.getLabel(Tools.getText("LOC_" + era + "_NAME", language)));
        if (randomCost.size() == 0) {
            traitContents.add(Tools.getHeader(Tools.getControlText("Science Cost", language)));
            traitContents.add(Tools.getLabel(cost + Tools.getYield("SCIENCE", language)));
        } else {
            traitContents.add(Tools.getHeader(Tools.getControlText("Science Cost", language) + Tools.getControlText(" (Random)", language)));
            for (Integer c : randomCost) {
                traitContents.add(Tools.getLabel(c + Tools.getYield("SCIENCE", language)));
            }
        }

        if (boost != null) {
            traitContents.add(Tools.getHeader(Tools.getControlText("Eureka", language)));
            traitContents.add(Tools.getLabel(Tools.getText(boost, language)));
        }

        // 添加解锁内容框
        JSONArray unlockContents = new JSONArray();
        unlockContents.add(Tools.getSeparator());
        List<UnlockableWithIcon> uniques = new ArrayList<>();

        for (Writable o : Main.WRITABLES) {
            if (o instanceof UnlockableWithIcon) {
                UnlockableWithIcon obj = (UnlockableWithIcon) o;
                if (tag.equals(obj.prereqTech)) {
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

        if (unlockContents.size() != 1 && uniques.size() > 0) {
            unlockContents.add(Tools.getSeparator());
        }

        for (UnlockableWithIcon obj : uniques) {
            unlockContents.add(obj.getIconLabel(language));
        }

        if (unlockContents.size() != 1) {
            rightColumnItems.add(Tools.getStatbox(Tools.getControlText("Unlocks", language), unlockContents));
        }

        // 添加科技树框
        JSONArray treeContents = new JSONArray();
        rightColumnItems.add(Tools.getStatbox(Tools.getControlText("Tech Tree", language), treeContents));

        treeContents.add(Tools.getSeparator());

        if (!prereqTech.isEmpty()) {
            treeContents.add(Tools.getHeader(Tools.getControlText("Prerequires", language)));
            for (String t : prereqTech) {
                Technology tech = technologies.get(t);
                if (tech != null) {
                    treeContents.add(tech.getIconLabel(language));
                }
            }
        }

        if (!prereqTech.isEmpty() && !leadToTech.isEmpty()) {
            treeContents.add(Tools.getSeparator());
        }

        if (!leadToTech.isEmpty()) {
            treeContents.add(Tools.getHeader(Tools.getControlText("Leads To Tech", language)));
            for (String t : leadToTech) {
                Technology tech = technologies.get(t);
                if (tech != null) {
                    treeContents.add(tech.getIconLabel(language));
                }
            }
        }

        return object;
    }

    @Override
    public String getChapter() {
        return "technologies";
    }

    @Override
    public String getFolder() {
        return era;
    }

    @Override
    public int getFolderOrder() {
        return Era.eras.get(era).chronologyIndex;
    }

    @Override
    public String getFolderName(String language) {
        return Tools.getText("LOC_" + era + "_NAME", language);
    }

    @Override
    public String getTagPrefix() {
        return "TECH_";
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
