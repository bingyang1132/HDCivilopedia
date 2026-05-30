package model;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import tools.Tools;

public class GovernorPromotion {
    
    public static final Map<String, GovernorPromotion> promotions = new HashMap<>();

    public String tag;
    public String name;
    public String description;
    public int level;
    public int column;
    public boolean base;

    public String governor;

    public List<String> prereqs = new ArrayList<>();

    public GovernorPromotion (String tag) {
        this.tag = tag;
        promotions.put(tag, this);
    }

    // load governor promotions
    public static void load () {
        Statement gameplay = null;
        try {
            gameplay = DriverManager.getConnection(Tools.GAMEPLAY_DATABASE).createStatement();
    
            // load promotions list
            ResultSet r1 = gameplay.executeQuery("select * from GovernorPromotions;");
            while (r1.next()) {
                String tag = r1.getString("GovernorPromotionType");
                GovernorPromotion promotion = new GovernorPromotion(tag);
                promotion.name = r1.getString("Name");
                promotion.description = r1.getString("Description");
                promotion.level = r1.getInt("Level") + 1;
                promotion.column = r1.getInt("Column") + 1;
                promotion.base = r1.getBoolean("BaseAbility");
            }
    
            // load other information
            for(Entry<String, GovernorPromotion> entry : promotions.entrySet()) {
                String tag = entry.getKey();
                GovernorPromotion promotion = entry.getValue();

                // load governor
                ResultSet r2 = gameplay.executeQuery("select * from GovernorPromotionSets where GovernorPromotion = \"" + tag + "\";");
                if (r2.next()) {
                    promotion.governor = r2.getString("GovernorType");
                }

                // load prereq promotions
                ResultSet r3 = gameplay.executeQuery("select * from GovernorPromotionPrereqs where GovernorPromotionType = \"" + tag + "\";");
                while (r3.next()) {
                    promotion.prereqs.add(r3.getString("PrereqGovernorPromotion"));
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading governor promotions.");
            System.err.println(e.getClass().getName() + e.getMessage());
        } finally {
            try {
                if (gameplay != null) gameplay.close();
            } catch (Exception e) {
            }
        }
    }
}
