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

public class Belief extends WritableWithIcon {

    public static final Map<String, Belief> beliefs = new HashMap<>();

    public String description;
    public String beliefClassType;

    public Belief (String tag) {
        super(tag);
        beliefs.put(tag, this);
    }

    // load beliefs from database 
    public static void load () {
        Statement gameplay = null;
        try {
            gameplay = DriverManager.getConnection(Tools.GAMEPLAY_DATABASE).createStatement();

            // load belief list
            ResultSet r1 = gameplay.executeQuery("select * from Beliefs;");
            while (r1.next()) {
                String tag = r1.getString("BeliefType");
                Belief belief = new Belief(tag);
                belief.name = r1.getString("Name");
                belief.description = r1.getString("Description");
                belief.beliefClassType = r1.getString("BeliefClassType");
            }
    
            // load other information, i.e. icons
            for(Entry<String, Belief> entry : beliefs.entrySet()) {
                String tag = entry.getKey();
                Belief belief = entry.getValue();

                // load icon
                String iconString = "ICON_" + tag;
                BufferedImage icon = Tools.getImage(iconString);
                if (icon != null) {
                    String path = iconString + ".png";
                    belief.icon = Tools.IMAGE_URL + "/" + path;
                    ImageEditor.saveImage(icon, path);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading beliefs.");
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
        object.put("leftColumnItems", leftColumnItems);

        JSONArray rightColumnItems = new JSONArray();
        object.put("rightColumnItems", rightColumnItems);

        JSONArray traitContents = new JSONArray();
        rightColumnItems.add(Tools.getStatbox(Tools.getControlText("Traits", language), traitContents));

        traitContents.add(Tools.getSeparator());
        traitContents.add(Tools.getHeader(Tools.getControlText("Belief Class", language)));
        traitContents.add(Tools.getLabel(getFolderName(language)));

        if (beliefClassType.equals("BELIEF_CLASS_WORSHIP")) {
            String build = "BUILDING_" + tag.replaceAll("BELIEF_", "").replaceAll("JNR_", "");
            Building building = Building.buildings.get(build);
            if (building != null) {
                traitContents.add(Tools.getSeparator());
                traitContents.add(Tools.getHeader(Tools.getControlText("UB", language)));
                traitContents.add(building.getIconLabel(language));
            }
        }

        return object;
    }

    @Override
    public String getChapter() {
        return "religions";
    }

    @Override
    public String getFolder() {
        return beliefClassType.substring("BELIEF_CLASS_".length(), beliefClassType.length());
    }

    @Override
    public String getFolderName(String language) {
        return Tools.getText("LOC_" + beliefClassType + "_NAME", language) + Tools.getControlText("Belief", language) + (language.equals("en_US") ? "s" : "");
    }

    @Override
    public String getTagPrefix() {
        return "BELIEF_";
    }

    @Override
    public String getCat() {
        return "信条改动";
    }
    
    @Override
    public int getCatOrder() {
        return -1200;
    }
}
