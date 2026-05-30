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

public class Feature extends WritableWithIcon {

    public static final Map<String, Feature> features = new HashMap<>();

    public int movement;
    public int sight;
    public int defense;
    public int appeal;

    public Map<String, Integer> yields = new HashMap<>();
    public Map<String, Integer> removeYields = new HashMap<>();

    public Feature (String tag) {
        super(tag);
        features.put(tag, this);
    }

    // load features from database
    public static void load () {
        Statement gameplay = null;
        try {
            gameplay = DriverManager.getConnection(Tools.GAMEPLAY_DATABASE).createStatement();

            // load feature list
            ResultSet r1 = gameplay.executeQuery("select * from Features where NaturalWonder = 0;");
            while (r1.next()) {
                String tag = r1.getString("FeatureType");
                Feature feature = new Feature(tag);
                feature.name = r1.getString("Name");
                if (!r1.getBoolean("Impassable")) {
                    feature.movement = r1.getInt("MovementChange");
                }
                feature.sight = r1.getInt("SightThroughModifier");
                feature.defense = r1.getInt("DefenseModifier");
                feature.appeal = r1.getInt("Appeal");
            }
    
            // load other information
            for(Entry<String, Feature> entry : features.entrySet()) {
                String tag = entry.getKey();
                Feature feature = entry.getValue();

                // load yields
                ResultSet r2 = gameplay.executeQuery("select * from Feature_YieldChanges where FeatureType = \"" + tag + "\";");
                while (r2.next()) {
                    feature.yields.put(r2.getString("YieldType"), r2.getInt("YieldChange"));
                }

                // load remove yields
                ResultSet r3 = gameplay.executeQuery("select * from Feature_Removes where FeatureType = \"" + tag + "\";");
                while (r3.next()) {
                    feature.removeYields.put(r3.getString("YieldType"), r3.getInt("Yield"));
                }

                // load icon
                String iconString = "ICON_" + tag;
                BufferedImage icon = Tools.getImage(iconString);
                if (icon != null) {
                    String path = iconString + ".png";
                    feature.icon = Tools.IMAGE_URL + "/" + path;
                    ImageEditor.saveImage(icon, path);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading features.");
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
        object.put("leftColumnItems", leftColumnItems);

        JSONArray rightColumnItems = new JSONArray();
        object.put("rightColumnItems", rightColumnItems);

        if (!(tag.equals("FEATURE_ICE") || tag.equals("FEATURE_VOLCANO") || tag.equals("FEATURE_VOLCANIC_SOIL"))) {
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
    
            if (removeYields.size() > 0) {
                traitContents.add(Tools.getSeparator());
                traitContents.add(Tools.getHeader(Tools.getControlText("Harvest", language)));
                for (Entry<String, Integer> entry : removeYields.entrySet()) {
                    traitContents.add(Tools.getLabel(Tools.signed(entry.getValue()) + Tools.getYield(entry.getKey(), language)));
                }
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
        return "features";
    }

    @Override
    public String getFolderName(String language) {
        return Tools.getControlText("Features", language);
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
