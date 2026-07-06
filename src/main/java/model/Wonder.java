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

import model.abstracts.UnlockableWithIcon;
import tools.ImageEditor;
import tools.Tools;

public class Wonder extends UnlockableWithIcon {
    
    public static final Map<String, Wonder> wonders = new HashMap<>();

    public String description;
    public String quoteTag;

    public int cost;
    public int housing;
    public int entertainment;
    public int regionalRange;
    public String obsoleteEra;

    public Map<String, Integer> yieldChanges = new HashMap<>();
    public Map<String, Integer> greatWorks = new HashMap<>();
    public Map<String, Integer> greatPersonPoints = new HashMap<>();

    public int tourism;

    public Wonder (String tag) {
        super(tag);
        wonders.put(tag, this);
    }

    // load units from database
    public static void load () {
        Statement gameplay = null;
        Statement text = null;
        try {
            gameplay = DriverManager.getConnection(Tools.GAMEPLAY_DATABASE).createStatement();
            text = DriverManager.getConnection(Tools.TEXT_DATABASE).createStatement();
    
            // load units list
            ResultSet r1 = gameplay.executeQuery("select * from Buildings where InternalOnly = \"0\" and IsWonder = \"1\";");
            while (r1.next()) {
                String tag = r1.getString("BuildingType");
                if (tag.endsWith("_INTERNAL")) {
                    continue;
                }
                Wonder wonder = new Wonder(tag);
                wonder.name = r1.getString("Name");
                wonder.description = r1.getString("Description");
                
                wonder.prereqTech = r1.getString("PrereqTech");
                wonder.prereqCivic = r1.getString("prereqCivic");
                wonder.cost = r1.getInt("Cost");
                wonder.housing = r1.getInt("Housing");
                wonder.entertainment = r1.getInt("Entertainment");
                wonder.regionalRange = r1.getInt("RegionalRange");
                wonder.quoteTag = r1.getString("Quote");
            }
    
            // load other information, i.e. icons
            for(Entry<String, Wonder> entry : wonders.entrySet()) {
                String tag = entry.getKey();
                Wonder wonder = entry.getValue();

                // load yields
                ResultSet r2 = gameplay.executeQuery("select * from Building_YieldChanges where BuildingType = \"" + tag + "\";");
                while (r2.next()) {
                    wonder.yieldChanges.put(r2.getString("YieldType"), r2.getInt("YieldChange"));
                }

                // load great work slots
                ResultSet r3 = gameplay.executeQuery("select * from Building_GreatWorks where BuildingType = \"" + tag + "\";");
                while (r3.next()) {
                    wonder.greatWorks.put(r3.getString("GreatWorkSlotType"), r3.getInt("NumSlots"));
                }

                // load great person points
                ResultSet r4 = gameplay.executeQuery("select * from Building_GreatPersonPoints where BuildingType = \"" + tag + "\";");
                while (r4.next()) {
                    wonder.greatPersonPoints.put(r4.getString("GreatPersonClassType"), r4.getInt("PointsPerTurn"));
                }

                // load tourism
                wonder.tourism = 1000;

                // load wonder icon
                String iconString = "ICON_" + tag;
                BufferedImage icon = Tools.getImage(iconString);
                if (icon != null) {
                    String path = iconString + ".png";
                    wonder.icon = Tools.IMAGE_URL + "/" + path;
                    ImageEditor.saveImage(icon, path);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading wonders.");
            System.err.println(e.getClass().getName() + e.getMessage());
        } finally {
            try {
                if (gameplay != null) gameplay.close();
                if (text != null) text.close();
            } catch (Exception e) {
            }
        }
    }
    
    // Convert information into json page
    @Override
    public JSONObject toJson (String language) {
        JSONObject object = super.toJson(language);
        
        JSONArray leftColumnItems = new JSONArray();
        if (description != null) {
            leftColumnItems.add(Tools.getHeader(Tools.getControlText("Description", language)));
            leftColumnItems.add(Tools.getBody(null, Tools.getTextWithAlt(description, language)));
        }

        // 引言 (flavor quote): resolve the Quote LOC key from the Buildings table in the page
        // language. The key often differs from the building tag (e.g. BUILDING_TEMPLE_ARTEMIS ->
        // LOC_BUILDING_TEMPLE_OF_ARTEMIS_QUOTE), so we must read it rather than derive it. The
        // author is embedded after a [NEWLINE].
        if (quoteTag != null) {
            String quote = Tools.getText(quoteTag, language);
            if (quote != null) {
                leftColumnItems.add(Tools.getHeader(Tools.getControlText("Quotes", language)));
                leftColumnItems.add(Tools.getQuote(quote));
            }
        }

        object.put("leftColumnItems", leftColumnItems);

        JSONArray rightColumnItems = new JSONArray();
        object.put("rightColumnItems", rightColumnItems);

        JSONArray traitContents = new JSONArray();

        if (regionalRange > 0) {
            traitContents.add(Tools.getSeparator());
            traitContents.add(Tools.getLabel(Tools.getControlText("regional 1", language) + regionalRange + Tools.getControlText("regional 2", language)));
        }
        
        if (housing > 0 || entertainment > 0 || yieldChanges.size() > 0 || greatWorks.size() > 0 || greatPersonPoints.size() > 0) {
            traitContents.add(Tools.getSeparator());
            
            for (Entry<String, Integer> entry : yieldChanges.entrySet()) {
                traitContents.add(Tools.getLabel("+" + entry.getValue() + Tools.getYield(entry.getKey(), language)));
            }

            for (Entry<String, Integer> entry : greatPersonPoints.entrySet()) {
                String tag = entry.getKey();
                String type = tag.substring(19, tag.length());
                int value = entry.getValue();
                traitContents.add(Tools.getLabel("+" + value + " [ICON_GREAT" + type + "] " + Tools.getTextWithAlt("LOC_" + tag + "_NAME", language) + Tools.getControlText("points", language)));
            }

            if (housing != 0) {
                traitContents.add(Tools.getLabel("+" + housing + " [ICON_HOUSING] " + Tools.getControlText("housing", language)));
            }
            if (entertainment != 0) {
                traitContents.add(Tools.getLabel("+" + entertainment + " [ICON_AMENITIES] " + Tools.getControlText("amenity", language)));
            }

            for (Entry<String, Integer> entry : greatWorks.entrySet()) {
                traitContents.add(Tools.getLabel("+" + entry.getValue() + Tools.getControlText(entry.getKey(), language)));
            }

        }
        if (tourism > 0) {
            traitContents.add(Tools.getSeparator());
            traitContents.add(Tools.getLabel(Tools.getControlText("Tourism from Rock Concerts 1", language) + "+" + tourism + Tools.getControlText("Tourism from Rock Concerts 2", language)));
        }

        if (traitContents.size() > 0) {
            rightColumnItems.add(Tools.getStatbox(Tools.getControlText("Traits", language), traitContents));
        }

        JSONArray requirementContents = new JSONArray();
        rightColumnItems.add(Tools.getStatbox(Tools.getControlText("Requirements", language), requirementContents));
        if (obsoleteEra != null && !obsoleteEra.equals("NO_ERA")) {
            requirementContents.add(Tools.getSeparator());
            requirementContents.add(Tools.getHeader(Tools.getControlText("ObsoleteEra", language)));
            requirementContents.add(Tools.getLabel(Tools.getText("LOC_" + obsoleteEra + "_NAME", language)));
        }
        if (prereqTech != null) {
            Technology tech = Technology.technologies.get(prereqTech);
            if (tech != null) {
                requirementContents.add(Tools.getSeparator());
                requirementContents.add(Tools.getHeader(Tools.getControlText("Technology", language)));
                requirementContents.add(tech.getIconLabel(language));
            }
        }
        if (prereqCivic != null) {
            Civic civic = Civic.civics.get(prereqCivic);
            if (civic != null) {
                requirementContents.add(Tools.getSeparator());
                requirementContents.add(Tools.getHeader(Tools.getControlText("Civic", language)));
                requirementContents.add(civic.getIconLabel(language));
            }
        }
        requirementContents.add(Tools.getSeparator());
        requirementContents.add(Tools.getHeader(Tools.getControlText("Production Cost", language)));
        requirementContents.add(Tools.getLabel(cost + Tools.getYield("YIELD_PRODUCTION", language)));
        return object;
    }

    @Override
    public int getOrder() {
        return cost;
    }

    @Override
    public String getChapter() {
        return "wonders";
    }

    @Override
    public String getTagPrefix() {
        if (tag.startsWith("BUILDING_")) {
            return "BUILDING_";
        } else {
            return "";
        }
    }

    // derive the wonder's era from its prerequisite tech (preferred) or civic
    private String getEra() {
        if (prereqTech != null) {
            Technology tech = Technology.technologies.get(prereqTech);
            if (tech != null && tech.era != null) {
                return tech.era;
            }
        }
        if (prereqCivic != null) {
            Civic civic = Civic.civics.get(prereqCivic);
            if (civic != null && civic.era != null) {
                return civic.era;
            }
        }
        return null;
    }

    @Override
    public String getFolder() {
        if (tag.startsWith("NAT_WONDER_CL_") || tag.startsWith("NAT_WON_CL_")) {
            return "nationalwonders";
        }
        String era = getEra();
        return era != null ? era : "wonders";
    }

    @Override
    public int getFolderOrder() {
        if (tag.startsWith("NAT_WONDER_CL_") || tag.startsWith("NAT_WON_CL_")) {
            return 1000;
        }
        String era = getEra();
        if (era != null && Era.eras.get(era) != null) {
            return Era.eras.get(era).chronologyIndex;
        }
        return 999;
    }

    @Override
    public String getFolderName(String language) {
        if (tag.startsWith("NAT_WONDER_CL_") || tag.startsWith("NAT_WON_CL_")) {
            return Tools.getControlText("National Wonders", language);
        }
        String era = getEra();
        if (era != null) {
            return Tools.getText("LOC_" + era + "_NAME", language);
        }
        return Tools.getControlText("Wonders", language);
    }

    @Override
    public String getCat() {
        return "奇观改动";
    }

    @Override
    public int getCatOrder() {
        return -1700;
    }
}
