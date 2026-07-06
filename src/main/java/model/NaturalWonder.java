package model;

import java.awt.image.BufferedImage;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import model.abstracts.WritableWithIcon;
import tools.ImageEditor;
import tools.Tools;

public class NaturalWonder extends WritableWithIcon {
    
    public static final Map<String, NaturalWonder> wonders = new HashMap<>();

    public String description;
    public String quoteTag;
    public int movement;
    public int sight;
    public int defense;
    public int appeal;

    public Map<String, Integer> yields = new HashMap<>();

    public NaturalWonder (String tag) {
        super(tag);
        wonders.put(tag, this);
    }

    // load natural wonders from database
    public static void load () {
        Statement gameplay = null;
        try {
            gameplay = DriverManager.getConnection(Tools.GAMEPLAY_DATABASE).createStatement();

            // load natural wonder list
            ResultSet r1 = gameplay.executeQuery("select * from Features where NaturalWonder = 1;");
            while (r1.next()) {
                String tag = r1.getString("FeatureType");
                NaturalWonder wonder = new NaturalWonder(tag);
                wonder.name = r1.getString("Name");
                wonder.description = r1.getString("Description");
                wonder.quoteTag = r1.getString("Quote");
                if (!r1.getBoolean("Impassable")) {
                    wonder.movement = r1.getInt("MovementChange");
                }
                wonder.sight = r1.getInt("SightThroughModifier");
                wonder.defense = r1.getInt("DefenseModifier");
                wonder.appeal = r1.getInt("Appeal");
            }
    
            // load other information
            for(Entry<String, NaturalWonder> entry : wonders.entrySet()) {
                String tag = entry.getKey();
                NaturalWonder wonder = entry.getValue();

                // load yields
                ResultSet r2 = gameplay.executeQuery("select * from Feature_YieldChanges where FeatureType = \"" + tag + "\";");
                while (r2.next()) {
                    wonder.yields.put(r2.getString("YieldType"), r2.getInt("YieldChange"));
                }

                // load icon
                String iconString = "ICON_" + tag;
                BufferedImage icon = Tools.getImage(iconString);
                if (icon != null) {
                    String path = iconString + ".png";
                    wonder.icon = Tools.IMAGE_URL + "/" + path;
                    ImageEditor.saveImage(icon, path);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading natural wonders.");
            System.err.println(e.getClass().getName() + " " + e.getMessage());
        } finally {
            try {
                if(gameplay != null) {
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
        // 历史背景 from the game's civilopedia prose, then the flavour 引言
        String history = Tools.getFeatureHistory(tag, language);
        if (history != null) {
            leftColumnItems.add(Tools.getHeader(Tools.getControlText("History", language)));
            leftColumnItems.add(Tools.getBody(null, history));
        }
        if (quoteTag != null) {
            String quote = Tools.getText(quoteTag, language);
            if (quote != null) {
                leftColumnItems.add(Tools.getHeader(Tools.getControlText("Quotes", language)));
                leftColumnItems.add(Tools.getQuote(quote));
            }
        }
        object.put("leftColumnItems", leftColumnItems);

        JSONArray rightColumnItems = new JSONArray();
        object.put("rightColumnItems", rightColumnItems);
        
        JSONArray traitContents = new JSONArray();
        rightColumnItems.add(Tools.getStatbox(Tools.getControlText("Traits", language), traitContents));

        if (movement > 0 || sight > 0 || defense != 0 || appeal != 0) {
            traitContents.add(Tools.getSeparator());
        }

        if (movement > 0) {
            traitContents.add(Tools.getLabel(Tools.getControlText("Movement Cost: ", language) + "+" + movement));
        }
        
        if (sight > 0) {
            traitContents.add(Tools.getLabel(Tools.getControlText("Sight Height: ", language) + "+" + sight));
        }
        
        if (defense != 0) {
            traitContents.add(Tools.getLabel(Tools.getControlText("Defense Modifier: ", language) + Tools.signed(defense)));
        }

        if (appeal != 0) {  
            traitContents.add(Tools.getLabel(Tools.getControlText("appeal 1", language) + Tools.signed(appeal) + Tools.getControlText("appeal 2", language)));
        }

        if (yields.size() > 0) {
            traitContents.add(Tools.getSeparator());
            for (Entry<String, Integer> entry : yields.entrySet()) {
                traitContents.add(Tools.getLabel(Tools.signed(entry.getValue()) + Tools.getYield(entry.getKey(), language)));
            }
        }

        return object;
    }

    @Override
    public String getChapter() {
        return "features";
    }

    @Override
    public String getTagPrefix() {
        return "FEATURE_";
    }

    @Override
    public String getFolder() {
        return "naturalwonders";
    }

    @Override
    public String getFolderName(String language) {
        return Tools.getControlText("Natural Wonders", language);
    }
    
    @Override
    public String getCat() {
        return "地形, 地貌, 资源&自然奇观改动";
    }
    
    @Override
    public int getCatOrder() {
        return -1100;
    }
}
