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

public class Civilization extends WritableWithIcon {

    public static final Map<String, Civilization> civilizations = new HashMap<>();

    public List<String> leaders = new ArrayList<>();
    public List<String> traits = new ArrayList<>();

    public Map<String, Integer> startBiasFeatures = new HashMap<>();
    public Map<String, Integer> startBiasResources = new HashMap<>();
    public Map<String, Integer> startBiasTerrains = new HashMap<>();
    public int startBiasRivers;

    public Civilization (String tag) {
        super(tag);
        civilizations.put(tag, this);
    }

    // used for searching civi color in leader tabel (color is attached to leaders)
    // returns "(" + Leaders.get(1) + ", " + Leaders.get(2) + ", " + ... + ")"
    public String getLeadersCollection () {
        String s = "";
        for (String st : leaders) {
            s += "\"" + st + "\", ";
        }
        s = s.substring(0, s.length() - 2);
        return s;
    }

    // load civilization list from database
    public static void load () {
        Statement gameplay = null;
        Statement extra = null;
        try {
            gameplay = DriverManager.getConnection(Tools.GAMEPLAY_DATABASE).createStatement();
            extra = DriverManager.getConnection(Tools.EXTRA_DATABASE).createStatement();
    
            // load civilization list
            ResultSet r1 = gameplay.executeQuery("select * from Civilizations;");
            while (r1.next()) {
                String civiLevel = r1.getString("StartingCivilizationLevelType");
                if(civiLevel.equals("CIVILIZATION_LEVEL_FULL_CIV")) {
                    String tag = r1.getString("CivilizationType");
                    Civilization civilization = new Civilization(tag);
                    civilization.name = r1.getString("Name");
                }
            }
    
            // load other information
            for(Entry<String, Civilization> entry : civilizations.entrySet()) {
                String tag = entry.getKey();
                Civilization civilization = entry.getValue();
    
                // load leaders
                ResultSet r2 = gameplay.executeQuery("select * from CivilizationLeaders where CivilizationType = \"" + tag + "\" order by LeaderType;");
                while (r2.next()) {
                    String leader = r2.getString("LeaderType");
                    civilization.leaders.add(leader);
                }
    
                // load traits
                ResultSet r3 = gameplay.executeQuery("select * from CivilizationTraits where CivilizationType = \"" + tag + "\" order by TraitType;");
                while (r3.next()) {
                    String trait = r3.getString("TraitType");
                    civilization.traits.add(trait);
                }

                // load start bias
                ResultSet r4 = gameplay.executeQuery("select * from StartBiasFeatures where CivilizationType = \"" + tag + "\";");
                while (r4.next()) {
                    civilization.startBiasFeatures.put(r4.getString("FeatureType"), r4.getInt("Tier"));
                }
                ResultSet r5 = gameplay.executeQuery("select * from StartBiasResources where CivilizationType = \"" + tag + "\";");
                while (r5.next()) {
                    civilization.startBiasResources.put(r5.getString("ResourceType"), r5.getInt("Tier"));
                }
                ResultSet r6 = gameplay.executeQuery("select * from StartBiasTerrains where CivilizationType = \"" + tag + "\";");
                while (r6.next()) {
                    civilization.startBiasTerrains.put(r6.getString("TerrainType"), r6.getInt("Tier"));
                }
                ResultSet r7 = gameplay.executeQuery("select * from StartBiasRivers where CivilizationType = \"" + tag + "\";");
                if (r7.next()) {
                    civilization.startBiasRivers = r7.getInt("Tier");
                }
    
                // load civi icon
                String iconString = "ICON_" + tag;
                BufferedImage icon = Tools.getImage(iconString);
                if (icon != null) {
                    String path = iconString + ".png";
                    civilization.icon = Tools.IMAGE_URL + "/" + path;
                    ImageEditor.saveImage(icon, path);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading civilizations.");
            System.err.println(e.getClass().getName() + " " + e.getMessage());
        } finally {
            try {
                if(gameplay != null) gameplay.close();
                if(extra != null) extra.close();
            } catch (Exception e) {
            }
        }
    }

    // Convert information into json page
    @Override
    public JSONObject toJson (String language) {
        JSONObject object = super.toJson(language);
        
        JSONArray leftColumnItems = new JSONArray();
        leftColumnItems.add(Tools.getHeader(Tools.getControlText("UA", language)));
        object.put("leftColumnItems", leftColumnItems);

        JSONArray rightColumnItems = new JSONArray();
        JSONArray contents = new JSONArray();
        rightColumnItems.add(Tools.getStatbox(Tools.getControlText("Traits", language), contents));
        object.put("rightColumnItems", rightColumnItems);

        contents.add(Tools.getSeparator());
        contents.add(Tools.getHeader(Tools.getControlText("Leaders", language)));
        for (String l : leaders) {
            Leader leader = Leader.leaders.get(l);
            if (leader != null) {
                contents.add(leader.getIconLabel(language));
            }
        }

        traits = Trait.sort(traits);
        boolean[] written = new boolean[7];
        for (String t : traits) {
            Trait trait = Trait.traits.get(t);
            if (trait == null || (trait.name == null && trait.name_alt == null)) {
                continue;
            }
            int type = trait.type;
            if (type == Trait.UA) {
                leftColumnItems.add(Tools.getBody(trait.getName(language), trait.getDescription(language)));
            } else {
                if (!written[type]) {
                    contents.add(Tools.getSeparator());
                    contents.add(Tools.getHeader(Tools.getControlText(trait.getTypeAsString(), language)));
                    written[type] = true;
                }
                contents.add(trait.getIconLabel(language));
            }
            if (trait.containsUP) {
                if (!written[0]) {
                    contents.add(Tools.getSeparator());
                    contents.add(Tools.getHeader(Tools.getControlText("UP", language)));
                    written[0] = true;
                }
                Project project = Project.projects.get(trait.pointsTo);
                contents.add(project.getIconLabel(language));
            }
        }

        if (startBiasFeatures.size() > 0 || startBiasResources.size() > 0 || startBiasTerrains.size() > 0 || startBiasRivers > 0) {
            contents.add(Tools.getSeparator());
            contents.add(Tools.getHeader(Tools.getControlText("Start Bias", language)));
            for (Entry<String, Integer> entry : startBiasTerrains.entrySet()) {
                Terrain terrain = Terrain.terrains.get(entry.getKey());
                JSONObject iconLabel = terrain.getIconLabel(language);
                iconLabel.put("num", entry.getValue());
                contents.add(iconLabel);
            }
            for (Entry<String, Integer> entry : startBiasFeatures.entrySet()) {
                Feature feature = Feature.features.get(entry.getKey());
                JSONObject iconLabel = feature.getIconLabel(language);
                iconLabel.put("num", entry.getValue());
                contents.add(iconLabel);
            }
            for (Entry<String, Integer> entry : startBiasResources.entrySet()) {
                Resource resource = Resource.resources.get(entry.getKey());
                JSONObject iconLabel = resource.getIconLabel(language);
                iconLabel.put("num", entry.getValue());
                contents.add(iconLabel);
            }
            if (startBiasRivers > 0) {
                JSONObject iconLabel = Tools.getIconlabel("", "", "", startBiasRivers + "", Tools.getControlText("River", language));
                contents.add(iconLabel);
            }
        }

        return object;
    }

    @Override
    public String getChapter() {
        return "civilizations";
    }

    @Override
    public String getTagPrefix() {
        return "CIVILIZATION_";
    }

    @Override
    public String getFolder() {
        return "civilizations";
    }

    @Override
    public String getFolderName(String language) {
        return Tools.getControlText("Civilizations", language);
    }
    
    @Override
    public String getCat() {
        return "文明&领袖改动";
    }

    @Override
    public int getCatOrder() {
        return -2000;
    }
    
}
