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

import model.abstracts.UnlockableWithIcon;
import tools.ImageEditor;
import tools.Tools;

public class Government extends UnlockableWithIcon {
    
    public static final Map<String, Government> goverments = new HashMap<>();

    public String inherent;
    public String accumulate;
    public int intolerance;
    public int influence;
    public int threshold;
    public int token;
    public String policy;
    public String tier;

    public int favor;
    public List<String> exclusive = new ArrayList<>();

    public Map<String, Integer> slots = new HashMap<>();

    public Government (String tag) {
        super(tag);
        goverments.put(tag, this);
    }

    // load governments from database
    public static void load () {
        Statement gameplay = null;
        try {
            gameplay = DriverManager.getConnection(Tools.GAMEPLAY_DATABASE).createStatement();
    
            // load governments list
            ResultSet r1 = gameplay.executeQuery("select * from Governments;");
            while (r1.next()) {
                String tag = r1.getString("GovernmentType");
                Government government = new Government(tag);
                government.name = r1.getString("Name");
                government.prereqCivic = r1.getString("PrereqCivic");
                government.inherent = r1.getString("InherentBonusDesc");
                government.accumulate = r1.getString("AccumulatedBonusDesc");
                government.intolerance = r1.getInt("OtherGovernmentIntolerance");
                government.influence = r1.getInt("InfluencePointsPerTurn");
                government.threshold = r1.getInt("InfluencePointsThreshold");
                government.token = r1.getInt("InfluenceTokensPerThreshold");
                government.policy = r1.getString("PolicyToUnlock");
                government.tier = r1.getString("Tier");
            }
    
            // load other information
            for(Entry<String, Government> entry : goverments.entrySet()) {
                String tag = entry.getKey();
                Government government = entry.getValue();

                // load card slots
                ResultSet r2 = gameplay.executeQuery("select * from Government_SlotCounts where GovernmentType = \"" + tag + "\";");
                while (r2.next()) {
                    government.slots.put(r2.getString("GovernmentSlotType"), r2.getInt("NumSlots"));
                }

                // load favor
                ResultSet r3 = gameplay.executeQuery("select * from Governments_XP2 where  GovernmentType = \"" + tag + "\";");
                if (r3.next()) {
                    government.favor = r3.getInt("Favor");
                }

                // load exclusive policy
                ResultSet r4 = gameplay.executeQuery("select * from Policy_GovernmentExclusives_XP2 where GovernmentType = \"" + tag + "\";");
                while (r4.next()) {
                    government.exclusive.add(r4.getString("PolicyType"));
                }

                // load icon
                String iconString = "ICON_" + tag;
                BufferedImage icon = Tools.getImage(iconString);
                if (icon != null) {
                    String path = iconString + ".png";
                    government.icon = Tools.IMAGE_URL + "/" + path;
                    ImageEditor.saveImage(icon, path);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading governments.");
            System.err.println(e.getClass().getName() + e.getMessage());
        } finally {
            try {
                if (gameplay != null) gameplay.close();
            } catch (Exception e) {
            }
        }
    }

    @Override
    public JSONObject toJson(String language) {
        JSONObject object = super.toJson(language);

        JSONArray leftColumnItems = new JSONArray();
        if (inherent != null || accumulate != null) {
            leftColumnItems.add(Tools.getHeader(Tools.getControlText("Description", language)));
            if (inherent != null) {
                leftColumnItems.add(Tools.getBody(Tools.getControlText("Inherent", language), Tools.getTextWithAlt(inherent, language)));
            }
            if (accumulate != null) {
                leftColumnItems.add(Tools.getBody(Tools.getControlText("Accumulate", language), Tools.getTextWithAlt(accumulate, language)));
            }
        }
        object.put("leftColumnItems", leftColumnItems);

        JSONArray rightColumnItems = new JSONArray();
        object.put("rightColumnItems", rightColumnItems);

        JSONArray traitContents = new JSONArray();
        rightColumnItems.add(Tools.getStatbox(Tools.getControlText("Traits", language), traitContents));

        traitContents.add(Tools.getSeparator());
        if (tier != null) {
            traitContents.add(Tools.getLabel(Tools.getControlText(tier, language)));
        }
        for (Entry<String, Integer> entry : slots.entrySet()) {
            String text = "";
            text += entry.getValue();
            text += " ";
            text += Tools.getText("LOC_" + entry.getKey() + "_NAME", language);
            text += Tools.getControlText("Slot", language);
            if (entry.getValue() > 1 && language.equals("en_US")) {
                text += "s";
            } 
            traitContents.add(Tools.getLabel(text));
        }

        if (favor > 0 || influence > 0 || intolerance > 0) {
            traitContents.add(Tools.getSeparator());
        }

        if (favor > 0) {
            traitContents.add(Tools.getLabel(Tools.getControlText("Favor 1", language) + favor + Tools.getControlText("Favor 2", language)));
        }

        if (influence > 0) {
            traitContents.add(Tools.getLabel(Tools.getControlText("Influence 1", language) + influence + Tools.getControlText("Influence 2", language)));
            traitContents.add(Tools.getLabel(Tools.getControlText("Envoy 1", language) + threshold + Tools.getControlText("Envoy 2", language) + token + Tools.getControlText("Envoy 3", language)));
        }

        if (intolerance > 0) {
            traitContents.add(Tools.getLabel(Tools.getControlText("Tolerance 1", language) + intolerance + Tools.getControlText("Tolerance 2", language)));
        }

        if (policy != null || exclusive.size() > 0) {
            traitContents.add(Tools.getSeparator());
        }

        if (exclusive.size() > 0) {
            traitContents.add(Tools.getHeader(Tools.getControlText("Exclusive Policy", language)));
            for (String p : exclusive) {
                Policy policy = Policy.policies.get(p);
                traitContents.add(policy.getIconLabel(language));
            }
        }

        if (policy != null) {
            Policy p = Policy.policies.get(policy);
            traitContents.add(Tools.getHeader(Tools.getControlText("Legacy Policy", language)));
            traitContents.add(p.getIconLabel(language));
        }

        if (prereqCivic != null) {
            JSONArray requirementContents = new JSONArray();
            rightColumnItems.add(Tools.getStatbox(Tools.getControlText("Requirements", language), requirementContents));
            Civic civic = Civic.civics.get(prereqCivic);
            if (civic != null) {
                requirementContents.add(Tools.getSeparator());
                requirementContents.add(Tools.getHeader(Tools.getControlText("Civic", language)));
                requirementContents.add(civic.getIconLabel(language));
            }
            
        }

        return object;
    }

    @Override
    public String getChapter() {
        return "governments";
    }

    @Override
    public String getFolder() {
        return "governments";
    }

    @Override
    public String getFolderName(String language) {
        return Tools.getControlText("Governments", language);
    }

    @Override
    public String getTagPrefix() {
        return "GOVERNMENT_";
    }

    @Override
    public String getCat() {
        return "政体&政策改动";
    }
    
    @Override
    public int getCatOrder() {
        return -1300;
    }
}
