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

public class Project extends UnlockableWithIcon {

    public static final Map<String, Project> projects = new HashMap<>();

    public String description;

    public String trait;
    public String belongs;

    public int cost;
    public String costProgressionModel;
    public int costProgressionParam;
    public String prereqDistrict;
    public String prereqBuilding;
    public String createBuilding;

    public String conversionYieldType;
    public int conversionPercentage;
    public List<GreatPersonPoints> greatPersonPoints = new ArrayList<>();
    public Map<String, Integer> resourceCosts = new HashMap<>();
    public List<String> prereqProjects = new ArrayList<>();
    public List<String> consumeBuilding = new ArrayList<>();

    static class GreatPersonPoints {
        String greatPersonClassType;
        int points;
        String pointProgressionModel;
        int pointprogressionParam;
    }

    public Project (String tag) {
        super(tag);
        projects.put(tag, this);
    }

    // load projects from database
    public static void load () {
        Statement gameplay = null;
        try {
            gameplay = DriverManager.getConnection(Tools.GAMEPLAY_DATABASE).createStatement();
    
            // load project list
            ResultSet r1 = gameplay.executeQuery("select * from Projects;");
            while (r1.next()) {
                String tag = r1.getString("ProjectType");
                Project project = new Project(tag);
                project.name = r1.getString("ShortName");
                project.description = r1.getString("Description");
                project.cost = r1.getInt("Cost");
                project.costProgressionModel = r1.getString("CostProgressionModel");
                project.costProgressionParam = r1.getInt("CostProgressionParam1");
                project.prereqTech = r1.getString("PrereqTech");
                project.prereqCivic = r1.getString("PrereqCivic");
                project.prereqDistrict = r1.getString("PrereqDistrict");
                project.prereqBuilding = r1.getString("RequiredBuilding");
                if (project.prereqBuilding != null && project.prereqBuilding.startsWith("BUILDING_CITY_POLICY_")) {
                    project.prereqBuilding = null;
                }
            }
    
            // load other information, i.e. icons
            for(Entry<String, Project> entry : projects.entrySet()) {
                String tag = entry.getKey();
                Project project = entry.getValue();

                // load prereq projects
                ResultSet r2 = gameplay.executeQuery("select * from ProjectPrereqs where ProjectType = \"" + tag + "\";");
                while (r2.next()) {
                    project.prereqProjects.add(r2.getString("PrereqProjectType"));
                }

                // load project building cost
                ResultSet r3 = gameplay.executeQuery("select * from Project_BuildingCosts where ProjectType = \"" + tag + "\";");
                while (r3.next()) {
                    String consume = r3.getString("ConsumedBuildingType");
                    if (consume != null && !consume.startsWith("BUILDING_CITY_POLICY_")) {
                        project.consumeBuilding.add(consume);
                    }
                }

                // load project great person points
                ResultSet r4 = gameplay.executeQuery("select * from Project_GreatPersonPoints where ProjectType = \"" + tag + "\";");
                while (r4.next()) {
                    GreatPersonPoints p = new GreatPersonPoints();
                    p.greatPersonClassType = r4.getString("GreatPersonClassType");
                    p.points = r4.getInt("Points");
                    p.pointProgressionModel = r4.getString("PointProgressionModel");
                    p.pointprogressionParam = r4.getInt("PointProgressionParam1");
                    project.greatPersonPoints.add(p);
                }

                // load resource costs
                ResultSet r5 = gameplay.executeQuery("select * from Project_ResourceCosts where ProjectType = \"" + tag + "\";");
                while (r5.next()) {
                    project.resourceCosts.put(r5.getString("ResourceType"), r5.getInt("StartProductionCost"));
                }

                // load production conversion
                ResultSet r6 = gameplay.executeQuery("select * from Project_YieldConversions where ProjectType = \"" + tag + "\";");
                if (r6.next()) {
                    project.conversionYieldType = r6.getString("YieldType");
                    project.conversionPercentage = r6.getInt("PercentOfProductionRate");
                }

                // load xp2 information
                ResultSet r7 = gameplay.executeQuery("select * from Projects_XP2 where ProjectType = \"" + tag + "\";");
                if (r7.next()) {
                    String prereqBuilding = r7.getString("RequiredBuilding");
                    if (prereqBuilding != null && !prereqBuilding.startsWith("BUILDING_CITY_POLICY_")) {
                        project.prereqBuilding = prereqBuilding;
                    }
                    project.createBuilding = r7.getString("CreateBuilding");
                    if (project.createBuilding != null && project.createBuilding.startsWith("BUILDING_CITY_POLICY_")) {
                        project.createBuilding = null;
                    }
                }

                // load trait
                String modifierID = null;
                ResultSet r8 = gameplay.executeQuery("select * from ModifierArguments where Name = \"ProjectType\" and Value = \"" + tag + "\";");
                if (r8.next()) {
                    modifierID = r8.getString("ModifierId");
                }
                if (modifierID != null) {
                    ResultSet r9 = gameplay.executeQuery("select * from TraitModifiers where ModifierID = \"" + modifierID + "\";");
                    if (r9.next()) {
                        project.trait = r9.getString("TraitType");
                    }
                }

                // load project icon
                String iconString = "ICON_" + tag;
                BufferedImage icon = Tools.getImage(iconString);
                if (icon != null) {
                    String path = iconString + ".png";
                    project.icon = Tools.IMAGE_URL + "/" + path;
                    ImageEditor.saveImage(icon, path);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading projects.");
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

        JSONArray traitContents = new JSONArray();

        if (belongs != null) {
            traitContents.add(Tools.getSeparator());
            traitContents.add(Tools.getHeader(Tools.getControlText("Unique To", language)));
            Civilization civilization = Civilization.civilizations.get(belongs);
            if (civilization != null) {
                traitContents.add(civilization.getIconLabel(language));
            }
            Leader leader = Leader.leaders.get(belongs);
            if (leader != null) {
                traitContents.add(leader.getIconLabel(language));
            }
        }

        if (conversionYieldType != null) {
            traitContents.add(Tools.getSeparator());
            traitContents.add(Tools.getLabel(Tools.getControlText("Conversion 1", language) + conversionPercentage + Tools.getControlText("Conversion 2", language) + Tools.getYield(conversionYieldType, language)));
        }

        if (createBuilding != null || greatPersonPoints.size() > 0) {
            traitContents.add(Tools.getSeparator());
            traitContents.add(Tools.getHeader(Tools.getControlText("Provides Upon Complection", language)));
            if (createBuilding != null) {
                Building building = Building.buildings.get(createBuilding);
                if (building != null) {
                    traitContents.add(building.getIconLabel(language));
                }
            }
            for (GreatPersonPoints p : greatPersonPoints) {
                String text = "";
                text += p.points;
                if (p.pointProgressionModel.equals("COST_PROGRESSION_GAME_PROGRESS") && p.pointprogressionParam != 100) {
                    text += " ~ ";
                    text += p.points * p.pointprogressionParam / 100;
                }
                String type = p.greatPersonClassType.substring("GREAT_PERSON_CLASS_".length(), p.greatPersonClassType.length());
                text += " [ICON_GREAT" + type + "] " + Tools.getTextWithAlt("LOC_" + p.greatPersonClassType + "_NAME", language);
                text += Tools.getControlText("points", language);
                traitContents.add(Tools.getLabel(text));
            }
        }
        if (consumeBuilding.size() > 0) {
            traitContents.add(Tools.getHeader(Tools.getControlText("Removes Upon Complection", language)));
            for (String b : consumeBuilding) {
                Building building = Building.buildings.get(b);
                if (building != null) {
                    traitContents.add(building.getIconLabel(language));
                }
            }
        }

        if (traitContents.size() > 0) {
            rightColumnItems.add(Tools.getStatbox(Tools.getControlText("Traits", language), traitContents));
        }

        JSONArray requirementContents = new JSONArray();
        if (prereqDistrict != null || prereqBuilding != null) {
            requirementContents.add(Tools.getSeparator());
        }
        if (prereqDistrict != null) {
            District district = District.distincts.get(prereqDistrict);
            if (district != null) {
                requirementContents.add(Tools.getHeader(Tools.getControlText("District", language)));
                requirementContents.add(district.getIconLabel(language));
            }
        }
        if (prereqBuilding != null) {
            Building building = Building.buildings.get(prereqBuilding);
            if (building != null) {
                requirementContents.add(Tools.getHeader(Tools.getControlText("Building", language)));
                requirementContents.add(building.getIconLabel(language));
            }
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

        if (prereqProjects.size() > 0) {
            requirementContents.add(Tools.getSeparator());
            requirementContents.add(Tools.getHeader(Tools.getControlText("Project", language)));
            for (String p : prereqProjects) {
                Project project = projects.get(p);
                requirementContents.add(project.getIconLabel(language));
            }
        }

        if (cost > 0 || resourceCosts.size() > 0) {
            requirementContents.add(Tools.getSeparator());
        }
        if (cost > 0) {
            requirementContents.add(Tools.getHeader(Tools.getControlText("Production Cost", language)));
            String text = "";
            text += cost;
            if (costProgressionModel.equals("COST_PROGRESSION_GAME_PROGRESS") && costProgressionParam != 100) {
                text += " ~ ";
                text += cost * costProgressionParam / 100;
            }
            text += Tools.getYield("PRODUCTION", language);
            requirementContents.add(Tools.getLabel(text));
            if (requirementContents.size() > 0) {
                rightColumnItems.add(Tools.getStatbox(Tools.getControlText("Requirements", language), requirementContents));
            }
        }
        if (resourceCosts.size() > 0) {
            requirementContents.add(Tools.getHeader(Tools.getControlText("Strategic Resource", language)));
            for (Entry<String, Integer> entry : resourceCosts.entrySet()) {
                requirementContents.add(Tools.getLabel(entry.getValue() + Tools.getResource(entry.getKey(), language)));
            }
        }
        return object;
    }

    @Override
    public String getChapter() {
        return "historic_moments";
    }

    @Override
    public String getFolder() {
        if (belongs != null) {
            return "unique";
        }
        if (tag.startsWith("PROJECT_CREATE_CORPORATION_PRODUCT_")) {
            return "corporation_product";
        }
        if (tag.startsWith("PROJECT_ENHANCE_") || tag.contains("BREAD_AND_CIRCUSES")) {
            return "enhance";
        }
        if (tag.startsWith("PROJECT_CITY_POLICY_")) {
            return "city_policy";
        }
        if (tag.startsWith("PROJECT_GRANT_RESOURCE_")) {
            return "grant_resource";
        }
        if (tag.startsWith("PROJECT_REMOVE_")) {
            return "remove";
        }
        if (tag.startsWith("PROJECT_CONVERT_") || tag.startsWith("PROJECT_DECOMMISSION_")) {
            return "convert";
        }
        if (prereqDistrict != null && prereqDistrict.equals("DISTRICT_SPACEPORT")) {
            return "space";
        }
        return "other";
    }

    @Override
    public int getFolderOrder() {
        if (belongs != null) {
            return 2;
        }
        if (tag.startsWith("PROJECT_CREATE_CORPORATION_PRODUCT_")) {
            return 8;
        }
        if (tag.startsWith("PROJECT_ENHANCE_") || tag.contains("BREAD_AND_CIRCUSES")) {
            return 4;
        }
        if (tag.startsWith("PROJECT_CITY_POLICY_")) {
            return 1;
        }
        if (tag.startsWith("PROJECT_GRANT_RESOURCE_")) {
            return 6;
        }
        if (tag.startsWith("PROJECT_REMOVE_")) {
            return 7;
        }
        if (tag.startsWith("PROJECT_CONVERT_") || tag.startsWith("PROJECT_DECOMMISSION_")) {
            return 5;
        }
        if (prereqDistrict != null && prereqDistrict.equals("DISTRICT_SPACEPORT")) {
            return 0;
        }
        return 3;
    }
    
    @Override
    public String getFolderName(String language) {
        if (belongs != null) {
            return Tools.getControlText("Unique", language);
        }
        if (tag.startsWith("PROJECT_CREATE_CORPORATION_PRODUCT_")) {
            return Tools.getControlText("Corporation Product", language);
        }
        if (tag.startsWith("PROJECT_ENHANCE_") || tag.contains("BREAD_AND_CIRCUSES")) {
            return Tools.getControlText("Enhance", language);
        }
        if (tag.startsWith("PROJECT_CITY_POLICY_")) {
            return Tools.getControlText("City Policy", language);
        }
        if (tag.startsWith("PROJECT_GRANT_RESOURCE_")) {
            return Tools.getControlText("Grant Resource", language);
        }
        if (tag.startsWith("PROJECT_REMOVE_")) {
            return Tools.getControlText("District Removement", language);
        }
        if (tag.startsWith("PROJECT_CONVERT_") || tag.startsWith("PROJECT_DECOMMISSION_")) {
            return Tools.getControlText("Building Conversion", language);
        }
        if (prereqDistrict != null && prereqDistrict.equals("DISTRICT_SPACEPORT")) {
            return Tools.getControlText("Space", language);
        }
        return Tools.getControlText("Other Projects", language);
    }

    @Override
    public int getOrder() {
        if (tag.startsWith("PROJECT_ENHANCE_") || tag.startsWith("PROJECT_REMOVE_")) {
            if (prereqDistrict != null) {
                District district = District.distincts.get(prereqDistrict);
                return district.getOrder();
            }
        }
        if (prereqDistrict != null && prereqDistrict.equals("DISTRICT_SPACEPORT")) {
            return cost;
        }
        return super.getOrder();
    }

    @Override
    public String getTagPrefix() {
        return "PROJECT_";
    }
    
    @Override
    public String getTitle(String language) {
        String title = super.getTitle(language);
        if (title.contains("[") && title.contains("]")) {
			if (title.length() >= title.indexOf("]") + 2) {
				title = title.substring(title.indexOf("]") + 2, title.length());
			} else {
				title = title.substring(title.indexOf("]"), title.length());
			}
        }
        return title;
    }

    @Override
    public String getCat() {
        return "项目改动";
    }

    @Override
    public int getCatOrder() {
        return -800;
    }
}
