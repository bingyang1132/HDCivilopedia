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

public class Leader extends WritableWithIcon {
    
    public static final Map<String, Leader> leaders = new HashMap<>();

    public String quote;
    public String civilization;
    public List<String> traits = new ArrayList<>();

    public Leader (String tag) {
        super(tag);
        leaders.put(tag, this);
    }

    // load leader list from database
    public static void load () {
        Statement gameplay = null;
        try {
            gameplay = DriverManager.getConnection(Tools.GAMEPLAY_DATABASE).createStatement();
    
            // load leaders list
            ResultSet r1 = gameplay.executeQuery("select * from Leaders;");
            while (r1.next()) {
                String inherit = r1.getString("InheritFrom");
                if(inherit != null && inherit.equals("LEADER_DEFAULT")) {
                    String tag = r1.getString("LeaderType");
                    Leader leader = new Leader(tag);
                    leader.name = r1.getString("Name");
                }
            }
    
            // load other information
            for(Entry<String, Leader> entry : leaders.entrySet()) {
                String tag = entry.getKey();
                Leader leader = entry.getValue();

                // load quotes
                ResultSet r2 = gameplay.executeQuery("select * from LeaderQuotes where LeaderType = \"" + tag + "\";");
                if(r2.next()) {
                    leader.quote = r2.getString("Quote");
                }
    
                // load civis
                ResultSet r3 = gameplay.executeQuery("select * from CivilizationLeaders where LeaderType = \"" + tag + "\";");
                if (r3.next()) {
                    String civi = r3.getString("CivilizationType");
                    leader.civilization = civi;
                }
    
                // load traits
                ResultSet r4 = gameplay.executeQuery("select * from LeaderTraits where LeaderType = \"" + tag + "\" order by TraitType;");
                while (r4.next()) {
                    String trait = r3.getString("TraitType");
                    leader.traits.add(trait);
                }

                // load civi icon
                String iconString = "ICON_" + tag;
                BufferedImage icon = Tools.getImage(iconString);
                if (icon != null) {
                    String path = iconString + ".png";
                    leader.icon = Tools.IMAGE_URL + "/" + path;
                    ImageEditor.saveImage(icon, path);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading leaders.");
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
        leftColumnItems.add(Tools.getHeader(Tools.getControlText("UA", language)));
        object.put("leftColumnItems", leftColumnItems);

        JSONArray rightColumnItems = new JSONArray();
        JSONArray contents = new JSONArray();
        rightColumnItems.add(Tools.getStatbox(Tools.getControlText("Traits", language), contents));
        object.put("rightColumnItems", rightColumnItems);

        contents.add(Tools.getSeparator());
        contents.add(Tools.getHeader(Tools.getControlText("Civilizations", language)));
        Civilization civi = Civilization.civilizations.get(civilization);
        
        if (civi != null) {
            contents.add(civi.getIconLabel(language));
        }

        traits = Trait.sort(traits);
        boolean[] written = new boolean[7];
        for (String t : traits) {
            Trait trait = Trait.traits.get(t);
            if (trait == null || (trait.name == null && trait.name_alt == null)) {
                continue;
            }
            int type = trait.type;
            if (type == Trait.LA) {
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

        // 历史背景 from wikipedia (需求2)
        String wikiHistory = Tools.getWikiHistory(tag, language);
        if (wikiHistory != null) {
            leftColumnItems.add(Tools.getHeader(Tools.getControlText("History", language)));
            leftColumnItems.add(Tools.getBody(null, wikiHistory));
        }

        return object;
    }

    @Override
    public String getChapter() {
        return "civilizations";
    }

    @Override
    public String getTagPrefix() {
        return "LEADER_";
    }

    @Override
    public String getFolder() {
        return "leaders";
    }

    @Override
    public String getFolderName(String language) {
        return Tools.getControlText("Leaders", language);
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
