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

public class Resource extends WritableWithIcon {

    public static final Map<String, Resource> resources = new HashMap<>();

    public String resourceClassType;
    public String prereqTech;
    public String prereqCivic;

    public int improvedExtractionRate;
    public String harvestPrereqTech;
	public List<String> categories = new ArrayList<>();

    public Map<String, Integer> yields = new HashMap<>();
    public Map<String, Integer> harvestYields = new HashMap<>();
    public List<String> improvedBy = new ArrayList<>();
    public List<String> validFeatures = new ArrayList<>();
    public List<String> validTerrains = new ArrayList<>();

    public Resource (String tag) {
        super(tag);
        resources.put(tag, this);
    }

    // load resources from database
    public static void load () {
        Statement gameplay = null;
        try {
            gameplay = DriverManager.getConnection(Tools.GAMEPLAY_DATABASE).createStatement();
    
            // load resource list
            ResultSet r1 = gameplay.executeQuery("select * from Resources where exists (select * from Resource_ValidFeatures where ResourceType = Resources.ResourceType) or exists (select * from Resource_ValidTerrains where ResourceType = Resources.ResourceType);");
            while (r1.next()) {
                String tag = r1.getString("ResourceType");
                Resource resource = new Resource(tag);
                resource.name = r1.getString("Name");
                resource.resourceClassType = r1.getString("ResourceClassType");
                resource.prereqCivic = r1.getString("PrereqCivic");
                resource.prereqTech = r1.getString("PrereqTech");
            }
    
            // load other information, i.e. icons
            for(Entry<String, Resource> entry : resources.entrySet()) {
                String tag = entry.getKey();
                Resource resource = entry.getValue();

                // load accumulate rate
                ResultSet r2 = gameplay.executeQuery("select * from Resource_Consumption where ResourceType = \"" + tag + "\";");
                if (r2.next()) {
                    resource.improvedExtractionRate = r2.getInt("ImprovedExtractionRate");
                }

                // load harvest preTech and yields
                ResultSet r3 = gameplay.executeQuery("select * from Resource_Harvests where ResourceType = \"" + tag + "\";");
                while (r3.next()) {
                    resource.harvestPrereqTech = r3.getString("PrereqTech");
                    resource.harvestYields.put(r3.getString("YieldType"), r3.getInt("Amount"));
                }

                // load yields
                ResultSet r4 = gameplay.executeQuery("select * from Resource_YieldChanges where ResourceType = \"" + tag + "\";");
                while (r4.next()) {
                    resource.yields.put(r4.getString("YieldType"), r4.getInt("YieldChange"));
                }

                // load valid features
                ResultSet r5 = gameplay.executeQuery("select * from Resource_ValidFeatures where ResourceType = \"" + tag + "\";");
                while (r5.next()) {
                    resource.validFeatures.add(r5.getString("FeatureType"));
                }

                // load valid terrains
                ResultSet r6 = gameplay.executeQuery("select * from Resource_ValidTerrains where ResourceType = \"" + tag + "\";");
                while (r6.next()) {
                    resource.validTerrains.add(r6.getString("TerrainType"));
                }

                // load improvement
                ResultSet r7 = gameplay.executeQuery("select * from Improvement_ValidResources where ResourceType = \"" + tag + "\" and ImprovementType != \"IMPROVEMENT_MOAI\";");
                while (r7.next()) {
                    resource.improvedBy.add(r7.getString("ImprovementType"));
                }

                // load categories (a resource can belong to several monopoly categories)
                ResultSet r8 = gameplay.executeQuery("select * from HD_Monopoly_Resource_Categories where ResourceType = \"" + tag + "\";");
                while (r8.next()) {
                    resource.categories.add(r8.getString("Category"));
                }

                // load improvement icon
                String iconString = "ICON_" + tag;
                BufferedImage icon = Tools.getImage(iconString);
                if (icon != null) {
                    String path = iconString + ".png";
                    resource.icon = Tools.IMAGE_URL + "/" + path;
                    ImageEditor.saveImage(icon, path);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading resources.");
            System.err.println(e.getClass().getName() + e.getMessage());
        } finally {
            try {
                if (gameplay != null) {
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
        object.put("leftColumnItems", leftColumnItems);

        // the industries & corporations this resource belongs to, with their effects
        for (String cat : categories) {
            ResourceCategory resourceCategory = ResourceCategory.categories.get(cat);
            if (resourceCategory != null) {
                leftColumnItems.add(Tools.getHeader(resourceCategory.getLinkedTitle(language)));
                leftColumnItems.add(Tools.getBody(Tools.getControlText("IndustryEffect", language), Tools.getText(resourceCategory.industryEffect, language)));
                leftColumnItems.add(Tools.getBody(Tools.getControlText("CorporationEffect", language), Tools.getText(resourceCategory.corporationEffect, language)));
            }
        }

        JSONArray rightColumnItems = new JSONArray();
        object.put("rightColumnItems", rightColumnItems);

        JSONArray traitContents = new JSONArray();
        rightColumnItems.add(Tools.getStatbox(Tools.getControlText("Traits", language), traitContents));

        // industries & corporations shown in their own sidebar section (not under Traits)
        JSONArray categoryContents = new JSONArray();
        if (!categories.isEmpty()) {
            categoryContents.add(Tools.getSeparator());
            for (String cat : categories) {
                ResourceCategory resourceCategory = ResourceCategory.categories.get(cat);
                if (resourceCategory != null) {
                    categoryContents.add(Tools.getLabel(resourceCategory.getLinkedTitle(language)));
                }
            }
            rightColumnItems.add(Tools.getStatbox(Tools.getControlText("ResourceCategory", language), categoryContents));
        }

        traitContents.add(Tools.getSeparator());
        traitContents.add(Tools.getLabel(getFolderName(language)));

        if (yields.size() > 0) {
            for (Entry<String, Integer> entry : yields.entrySet()) {
                traitContents.add(Tools.getLabel("+" + entry.getValue() + Tools.getYield(entry.getKey(), language)));
            }
        }

        if (improvedExtractionRate > 0) {
            traitContents.add(Tools.getLabel("+" + improvedExtractionRate + " [ICON_" + tag + "] " + getTitle(language)));
        }

        if (improvedBy.size() > 0) {
            traitContents.add(Tools.getSeparator());
            traitContents.add(Tools.getHeader(Tools.getControlText("Improved By", language)));
            for (String i : improvedBy) {
                Improvement improvement = Improvement.improvements.get(i);
                traitContents.add(improvement.getIconLabel(language));
            }
        }

        if (harvestYields.size() > 0) {
            traitContents.add(Tools.getSeparator());
            traitContents.add(Tools.getHeader(Tools.getControlText("Harvest", language)));
            for (Entry<String, Integer> entry : harvestYields.entrySet()) {
                traitContents.add(Tools.getLabel("+" + entry.getValue() + Tools.getYield(entry.getKey(), language)));
            }
            if (harvestPrereqTech != null) {
                Technology technology = Technology.technologies.get(harvestPrereqTech);
                if (technology != null) {
                    traitContents.add(Tools.getHeader(Tools.getControlText("Requires", language)));
                    traitContents.add(technology.getIconLabel(language));
                }
            }
        }

        JSONArray requirementContents = new JSONArray();
        rightColumnItems.add(Tools.getStatbox(Tools.getControlText("Requirements", language), requirementContents));
        
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
        requirementContents.add(Tools.getHeader(Tools.getControlText("Placement", language)));
        for (String t : validTerrains) {
            Terrain terrain = Terrain.terrains.get(t);
            requirementContents.add(terrain.getIconLabel(language));
        }
        for (String f : validFeatures) {
            Feature feature = Feature.features.get(f);
            requirementContents.add(feature.getIconLabel(language));
        }       

        return object;
    }

    @Override
    public String getChapter() {
        return "resources";
    }

    @Override
    public String getFolder() {
        if (resourceClassType.equals("RESOURCECLASS_LUXURY")) {
            for (String i : improvedBy) {
                Improvement improvement = Improvement.improvements.get(i);
                return improvement.getEnglishTitle();
            }
        }
        return resourceClassType.substring("RESOURCECLASS_".length(), resourceClassType.length()).toLowerCase();
    }

    @Override
    public String getFolderName(String language) {
        if (resourceClassType.equals("RESOURCECLASS_LUXURY")) {
            for (String i : improvedBy) {
                Improvement improvement = Improvement.improvements.get(i);
                return improvement.getTitle(language) + Tools.getControlText("Luxuries", language);
            }
        }
        return Tools.getText("LOC_" + resourceClassType + "_NAME", language) + Tools.getControlText("Resources", language);
    }

    @Override
    public String getTagPrefix() {
        return "RESOURCE_";
    }
    
    @Override
    public String getCat() {
        return "地形, 地貌, 资源&自然奇观改动";
    }
    
    @Override
    public int getCatOrder() {
        return -1100;
    }
}
