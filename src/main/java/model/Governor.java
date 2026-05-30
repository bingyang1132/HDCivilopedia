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

import model.abstracts.WritableWithIcon;
import tools.ImageEditor;
import tools.Tools;

public class Governor extends WritableWithIcon {
    
    public static final Map<String, Governor> governors = new HashMap<>();

    public String description;
    public List<String> promotions = new ArrayList<>();

    public Governor (String tag) {
        super(tag);
        governors.put(tag, this);
    }
    
    // load governors from database
    public static void load () {
        Statement gameplay = null;
        try {
            gameplay = DriverManager.getConnection(Tools.GAMEPLAY_DATABASE).createStatement();

            // load belief list
            ResultSet r1 = gameplay.executeQuery("select * from Governors;");
            while (r1.next()) {
                String tag = r1.getString("GovernorType");
                Governor governor = new Governor(tag);
                governor.name = r1.getString("Name");
                governor.description = r1.getString("Description");
            }
    
            // load other information
            for(Entry<String, Governor> entry : governors.entrySet()) {
                String tag = entry.getKey();
                Governor governor = entry.getValue();

                // load promotions
                ResultSet r2 = gameplay.executeQuery("select * from GovernorPromotionSets where GovernorType = \"" + tag + "\";");
                while (r2.next()) {
                    governor.promotions.add(r2.getString("GovernorPromotion"));
                }

                // load icon
                String iconString = "ICON_" + tag;
                BufferedImage icon = Tools.getImage(iconString);
                if (icon != null) {
                    String path = iconString + ".png";
                    governor.icon = Tools.IMAGE_URL + "/" + path;
                    ImageEditor.saveImage(icon, path);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading governors.");
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

        object.remove("portrait");

        JSONArray leftColumnItems = new JSONArray();

        JSONArray contents = new JSONArray();
        for (String p : promotions) {
            GovernorPromotion promotion = GovernorPromotion.promotions.get(p);
            if (promotion != null) {
                contents.add(Tools.getPromotion(promotion.level, promotion.column, promotion.base ? icon : null, "", Tools.getTextWithAlt(promotion.name, language), Tools.getTextWithAlt(promotion.description, language)));
                for (String pr : promotion.prereqs) {
                    GovernorPromotion prereq = GovernorPromotion.promotions.get(pr);
                    if (prereq != null) {
                        contents.add(Tools.getLine(prereq.level, promotion.level, prereq.column, promotion.column));
                    }
                }
            }
        }
        leftColumnItems.add(Tools.getGovernorBox(contents));

        object.put("leftColumnItems", leftColumnItems);

        return object;
    }

    @Override
    public String getChapter() {
        return "governors";
    }

    @Override
    public String getTagPrefix() {
        return "GOVERNOR_";
    }

    @Override
    public String getFolder() {
        return "Governors";
    }

    @Override
    public String getFolderName(String language) {
        return Tools.getControlText("Governors", language);
    }

    @Override
    public String getCat() {
        return "总督改动";
    }
    
    @Override
    public int getCatOrder() {
        return -900;
    }
}
