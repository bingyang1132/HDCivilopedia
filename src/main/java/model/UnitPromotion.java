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

import tools.ImageEditor;
import tools.Tools;

public class UnitPromotion {
    
    public static final Map<String, UnitPromotion> promotions = new HashMap<>();

    public String tag;
    public String name;
    public String description;
    public int level;
    public String promotionClass;
    public int column;
    public String icon;

    public List<String> prereqs = new ArrayList<>();

    public UnitPromotion (String tag) {
        this.tag = tag;
        promotions.put(tag, this);
    }

    // load unit promotions
    public static void load () {
        Statement gameplay = null;
        try {
            gameplay = DriverManager.getConnection(Tools.GAMEPLAY_DATABASE).createStatement();
    
            // load promotions list
            ResultSet r1 = gameplay.executeQuery("select * from UnitPromotions;");
            while (r1.next()) {
                String tag = r1.getString("UnitPromotionType");
                UnitPromotion promotion = new UnitPromotion(tag);
                promotion.name = r1.getString("Name");
                promotion.description = r1.getString("Description");
                promotion.level = r1.getInt("Level");
                promotion.promotionClass = r1.getString("PromotionClass");
                promotion.column = r1.getInt("Column");
            }
    
            // load other information
            for(Entry<String, UnitPromotion> entry : promotions.entrySet()) {
                String tag = entry.getKey();
                UnitPromotion promotion = entry.getValue();

                // load prereq promotions
                ResultSet r2 = gameplay.executeQuery("select * from UnitPromotionPrereqs where UnitPromotion = \"" + tag + "\";");
                while (r2.next()) {
                    promotion.prereqs.add(r2.getString("PrereqUnitPromotion"));
                }

                // load promotion icon
                String iconString = "ICON_" + tag;
                BufferedImage icon = Tools.getImage(iconString);
                if (icon != null) {
                    String path = iconString + ".png";
                    promotion.icon = Tools.IMAGE_URL + "/" + path;
                    ImageEditor.saveImage(icon, path);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading unit promotions.");
            System.err.println(e.getClass().getName() + e.getMessage());
        } finally {
            try {
                if (gameplay != null) gameplay.close();
            } catch (Exception e) {
            }
        }
    }

}
