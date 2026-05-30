package model;

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

import model.abstracts.Writable;
import tools.Tools;

public class UnitPromotionClass extends Writable {
    
    public static final Map<String, UnitPromotionClass> classes = new HashMap<>();

    public List<String> promotions = new ArrayList<>();

    public UnitPromotionClass (String tag) {
        super(tag);
        classes.put(tag, this);
    }

    // load unit promotion classes
    public static void load () {
        Statement gameplay = null;
        try {
            gameplay = DriverManager.getConnection(Tools.GAMEPLAY_DATABASE).createStatement();
    
            // load promotions list
            ResultSet r1 = gameplay.executeQuery("select * from UnitPromotionClasses;");
            while (r1.next()) {
                String tag = r1.getString("PromotionClassType");
                if (tag.equals("PROMOTION_CLASS_SUPPORT") || tag.equals("PROMOTION_CLASS_INQUISITOR") || tag.equals("")) {
                    continue;
                }
                UnitPromotionClass cla = new UnitPromotionClass(tag);
                cla.name = r1.getString("Name");
            }
    
            // load other information
            for(Entry<String, UnitPromotionClass> entry : classes.entrySet()) {
                String tag = entry.getKey();
                UnitPromotionClass cla = entry.getValue();

                // load promotions
                ResultSet r2 = gameplay.executeQuery("select * from UnitPromotions where PromotionClass = \"" + tag + "\";");
                while (r2.next()) {
                    cla.promotions.add(r2.getString("UnitPromotionType"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error loading unit promotion classes.");
            System.err.println(e.getClass().getName() + e.getMessage());
        } finally {
            try {
                if (gameplay != null) gameplay.close();
            } catch (Exception e) {
            }
        }
    }

    // Convert information into json page
    @Override
    public JSONObject toJson (String language) {
        JSONObject object = super.toJson(language);
        
        JSONArray leftColumnItems = new JSONArray();

        if (tag.equals("PROMOTION_CLASS_APOSTLE") || tag.equals("PROMOTION_CLASS_SPY") || tag.equals("PROMOTION_CLASS_ROCK_BAND") || tag.equals("PROMOTION_CLASS_GIANT_DEATH_ROBOT") || tag.equals("PROMOTION_CLASS_PLUM_INTERNAL_SECURITY_HD")) {
            for (String p : promotions) {
                UnitPromotion promotion = UnitPromotion.promotions.get(p);
                if (promotion != null) {
                    leftColumnItems.add(Tools.getBody(Tools.getTextWithAlt(promotion.name, language), Tools.getTextWithAlt(promotion.description, language)));
                }
            }
        } else {
            JSONArray contents = new JSONArray();
            for (String p : promotions) {
                UnitPromotion promotion = UnitPromotion.promotions.get(p);
                if (promotion != null) {
                    contents.add(Tools.getPromotion(promotion.level, promotion.column, promotion.icon, "", Tools.getTextWithAlt(promotion.name, language), Tools.getTextWithAlt(promotion.description, language)));
                    for (String pr : promotion.prereqs) {
                        UnitPromotion prereq = UnitPromotion.promotions.get(pr);
                        if (prereq != null) {
                            contents.add(Tools.getLine(prereq.level, promotion.level, prereq.column, promotion.column));
                        }
                    }
                }
            }
            leftColumnItems.add(Tools.getPromotionBox(contents));
        }

        object.put("leftColumnItems", leftColumnItems);
        return object;
    }

    @Override
    public String getChapter() {
        return "unitpromotions";
    }

    @Override
    public String getTagPrefix() {
        return "PROMOTION_CLASS_";
    }

    @Override
    public String getFolder() {
        return "unitpromotions";
    }

    @Override
    public String getFolderName(String language) {
        return Tools.getControlText("UnitPromotions", language);
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
