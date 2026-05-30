package model;

import java.awt.image.BufferedImage;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import model.abstracts.UnlockableWithIcon;
import tools.ImageEditor;
import tools.Tools;

public class Policy extends UnlockableWithIcon {

    public static final Map<String, Policy> policies = new HashMap<>();

    public String description;
    public String slotType;

    public boolean darkAge;
    public String minimumGameEra;
    public String maximumGameEra;

    public String legacyGovernment;
    public String exclusiveGovernment;

    public List<String> obsolete = new ArrayList<>();

    public Policy (String tag) {
        super(tag);
        policies.put(tag, this);
    }

    // load policies from database
    public static void load () {
        Statement gameplay = null;
        try {
            gameplay = DriverManager.getConnection(Tools.GAMEPLAY_DATABASE).createStatement();
    
            // load policies list
            ResultSet r1 = gameplay.executeQuery("select * from Policies;");
            while (r1.next()) {
                String tag = r1.getString("PolicyType");
                Policy policy = new Policy(tag);
                policy.name = r1.getString("Name");
                policy.description = r1.getString("Description");
                policy.prereqCivic = r1.getString("PrereqCivic");
                policy.prereqTech = r1.getString("PrereqTech");
                policy.slotType = r1.getString("GovernmentSlotType");
            }
    
            // load other information
            for(Entry<String, Policy> entry : policies.entrySet()) {
                String tag = entry.getKey();
                Policy policy = entry.getValue();

                // load dark age requirements
                ResultSet r2 = gameplay.executeQuery("select * from Policies_XP1 where PolicyType = \"" + tag + "\";");
                if (r2.next()) {
                    policy.darkAge = r2.getBoolean("RequiresDarkAge");
                    policy.minimumGameEra = r2.getString("MinimumGameEra");
                    policy.maximumGameEra = r2.getString("MaximumGameEra");
                }

                // load legacy government
                ResultSet r3 = gameplay.executeQuery("select * from Governments where PolicyToUnlock = \"" + tag + "\";");
                if (r3.next()) {
                    policy.legacyGovernment = r3.getString("GovernmentType");
                }

                // load exclusive government
                ResultSet r4 = gameplay.executeQuery("select * from Policy_GovernmentExclusives_XP2 where PolicyType = \"" + tag + "\";");
                if (r4.next()) {
                    policy.exclusiveGovernment = r4.getString("GovernmentType");
                }

                // load obsolete policies
                ResultSet r5 = gameplay.executeQuery("select * from ObsoletePolicies where PolicyType = \"" + tag + "\";");
                while (r5.next()) {
                    String obs = r5.getString("ObsoletePolicy");
                    if (obs != null) {
                        policy.obsolete.add(obs);
                    }
                }

                // load policy icon
                String iconString = "ICON_POLICY_" + policy.slotType.substring("SLOT_".length(), policy.slotType.length());
                BufferedImage icon = Tools.getImage(iconString);
                if (icon != null) {
                    String path = iconString + ".png";
                    policy.icon = Tools.IMAGE_URL + "/" + path;
                    ImageEditor.saveImage(icon, path);
                }
            }

            for(Entry<String, Policy> entry : policies.entrySet()) {
                Policy policy = entry.getValue();
                Set<String> remove = new HashSet<>();
                for (String p : policy.obsolete) {
                    Policy po = policies.get(p);
                    if (po == null) {
                        remove.add(p);
                    }
                }
                policy.obsolete.removeAll(remove);
            }
        } catch (Exception e) {
            System.err.println("Error loading policies.");
            e.printStackTrace();
            System.exit(0);
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
        if (description != null) {
            leftColumnItems.add(Tools.getHeader(Tools.getControlText("Description", language)));
            leftColumnItems.add(Tools.getBody("", Tools.getText(description, language)));
        }
        object.put("leftColumnItems", leftColumnItems);

        JSONArray rightColumnItems = new JSONArray();
        object.put("rightColumnItems", rightColumnItems);

        if (prereqTech != null || prereqCivic != null || exclusiveGovernment != null || legacyGovernment != null || darkAge || obsolete.size() > 0) {
            JSONArray requirementContents = new JSONArray();
            rightColumnItems.add(Tools.getStatbox(Tools.getControlText("Requirements", language), requirementContents));
            requirementContents.add(Tools.getSeparator());
            if (prereqTech != null) {
                Technology tech = Technology.technologies.get(prereqTech);
                if (tech != null) {
                    requirementContents.add(Tools.getHeader(Tools.getControlText("Technology", language)));
                    requirementContents.add(tech.getIconLabel(language));
                }
            }
            if (prereqCivic != null) {
                Civic civic = Civic.civics.get(prereqCivic);
                if (civic != null) {
                    requirementContents.add(Tools.getHeader(Tools.getControlText("Civic", language)));
                    requirementContents.add(civic.getIconLabel(language));
                }
            }
            if (exclusiveGovernment != null) {
                Government government = Government.goverments.get(exclusiveGovernment);
                requirementContents.add(Tools.getHeader(Tools.getControlText("Government", language)));
                requirementContents.add(government.getIconLabel(language));
            }
            if (legacyGovernment != null) {
                Government government = Government.goverments.get(legacyGovernment);
                requirementContents.add(Tools.getHeader(Tools.getControlText("Legacy From", language)));
                requirementContents.add(government.getIconLabel(language));
            }
            if (darkAge) {
                requirementContents.add(Tools.getHeader(Tools.getControlText("Dark Age", language)));
                requirementContents.add(Tools.getLabel(Tools.getControlText("Min Era", language) + Tools.getText("LOC_" + minimumGameEra + "_NAME", language) + Tools.getControlText("Max Era", language) + Tools.getText("LOC_" + maximumGameEra + "_NAME", language)));
            }
            if (obsolete.size() > 0) {
                requirementContents.add(Tools.getHeader(Tools.getControlText("Obsolete With", language)));
                for (String p : obsolete) {
                    Policy policy = policies.get(p);
                    requirementContents.add(policy.getIconLabel(language));
                }
            }
        }

        return object;
    }

    @Override
    public String getChapter() {
        return "governments";
    }

    @Override
    public String getTagPrefix() {
        return "POLICY_";
    }

    @Override
    public String getFolder() {
        if (darkAge) {
            return "darkage";
        }
        if (legacyGovernment != null) {
            return "legacy";
        }
        return slotType.substring("SLOT_".length(), slotType.length()).toLowerCase();
    }

    @Override
    public String getFolderName(String language) {
        if (darkAge) {
            return Tools.getControlText("Dark Age Policies", language);
        }
        if (legacyGovernment != null) {
            return Tools.getControlText("Legacy Policies", language);
        }
        return Tools.getText("LOC_" + slotType + "_NAME", language) + Tools.getControlText("Policies", language);
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
