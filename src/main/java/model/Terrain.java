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

public class Terrain extends WritableWithIcon {
    
    public static final Map<String, Terrain> terrains = new HashMap<>();

    public int movement;
    public int sight;
    public int defense;
    public int appeal;

    public Map<String, Integer> yields = new HashMap<>();

    public Terrain (String tag) {
        super(tag);
        terrains.put(tag, this);
    }

    // load terrains from database 
    public static void load () {
        Statement gameplay = null;
        try {
            gameplay = DriverManager.getConnection(Tools.GAMEPLAY_DATABASE).createStatement();

            // load terrain list
            ResultSet r1 = gameplay.executeQuery("select * from Terrains;");
            while (r1.next()) {
                String tag = r1.getString("TerrainType");
                Terrain terrain = new Terrain(tag);
                terrain.name = r1.getString("Name");
                if (!r1.getBoolean("Impassable")) {
                    terrain.movement = r1.getInt("MovementCost");
                }
                terrain.sight = r1.getInt("SightModifier");
                terrain.defense = r1.getInt("DefenseModifier");
                terrain.appeal = r1.getInt("Appeal");
            }
    
            // load other information
            for(Entry<String, Terrain> entry : terrains.entrySet()) {
                String tag = entry.getKey();
                Terrain terrain = entry.getValue();

                // load yields
                ResultSet r2 = gameplay.executeQuery("select * from Terrain_YieldChanges where TerrainType = \"" + tag + "\";");
                while (r2.next()) {
                    terrain.yields.put(r2.getString("YieldType"), r2.getInt("YieldChange"));
                }

                // load icon
                String iconString = "ICON_" + tag;
                BufferedImage icon = Tools.getImage(iconString);
                if (icon != null) {
                    String path = iconString + ".png";
                    terrain.icon = Tools.IMAGE_URL + "/" + path;
                    ImageEditor.saveImage(icon, path);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading terrains.");
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

        JSONArray traitContents = new JSONArray();
        rightColumnItems.add(Tools.getStatbox(Tools.getControlText("Traits", language), traitContents));

        traitContents.add(Tools.getSeparator());
        if (movement > 0) {
            traitContents.add(Tools.getLabel(Tools.getControlText("Movement Cost: ", language) + movement));
        }
        
        traitContents.add(Tools.getLabel(Tools.getControlText("Sight Height: ", language) + sight));
        
        if (defense != 0) {
            traitContents.add(Tools.getLabel(Tools.getControlText("Defense Modifier: ", language) + Tools.signed(defense)));
        }

        if (appeal != 0) {
            traitContents.add(Tools.getLabel(Tools.getControlText("appeal 1", language) + Tools.signed(appeal) + Tools.getControlText("appeal 2", language)));
        }

        if (yields.size() > 0) {
            traitContents.add(Tools.getSeparator());
            for (Entry<String, Integer> entry : yields.entrySet()) {
                traitContents.add(Tools.getLabel("+" + entry.getValue() + Tools.getYield(entry.getKey(), language)));
            }
        }

        return object;
    }

    @Override
    public String getChapter() {
        return "features";
    }

    @Override
    public String getFolder() {
        return "terrains";
    }

    @Override
    public String getFolderName(String language) {
        return Tools.getControlText("Terrains", language);
    }

    @Override
    public String getTagPrefix() {
        return "TERRAIN_";
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
