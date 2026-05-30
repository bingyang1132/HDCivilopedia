package model;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.alibaba.fastjson.JSONObject;

import tools.Tools;

public class Trait {
    public static Map<String, Trait> traits = new HashMap<>();

    public String tag;
    public String name;
    public String name_alt;
    public String description;
    public String description_alt;
    public String icon;
    public String belongs;

    public boolean containsUP;

    public String pointsTo;

    public int type;
    public static final int LA = 1;
    public static final int UA = 2;
    public static final int UU = 3;
    public static final int UD = 4;
    public static final int UB = 5;
    public static final int UI = 6;

    public String getTypeAsString () {
        switch (type) {
            case LA : return "LA";
            case UA : return "UA";
            case UU : return "UU";
            case UD : return "UD";
            case UB : return "UB";
            case UI : return "UI";
        }
        return "Undefined";
    }

    public static List<String> sort(List<String> trs) {
        List<String> sorted = new ArrayList<>();
        for (int type = 1; type <= 6; type++) {
            for (String tr : trs) {
                Trait trait = traits.get(tr);
                if(trait.type == type) {
                    sorted.add(tr);
                }
            }
        }
        return sorted;
    }

    public Trait (String tag) {
        this.tag = tag;
        traits.put(tag, this);
    }

    public String getName (String language) {
        String s3 = null;
        switch (type) {
            case UB : {
                Building building = Building.buildings.get(pointsTo);
                if (building != null) {
                    s3 = building.getTitle(language);
                }
                break;
            }
            case UD : {
                District district = District.distincts.get(pointsTo);
                if (district != null) {
                    s3 = district.getTitle(language);
                }
                break;
            }
            case UI : {
                Improvement improvement = Improvement.improvements.get(pointsTo);
                if (improvement != null) {
                    s3 = improvement.getTitle(language);
                }
                break;
            }
            case UU : {
                Unit unit = Unit.units.get(pointsTo);
                if (unit != null) {
                    s3 = unit.getTitle(language);
                }
                break;
            }
        }
        if (s3 != null) {
            return s3;
        }
        String s2 = Tools.getText(name_alt, language);
        if (s2 != null) {
            return s2;
        }
        String s1 = Tools.getText(name, language);
        if (s1 != null) {
            return s1;
        }
        if (name_alt != null) {
            return "[" + language + ":" + name_alt + "]";
        }
        if (name != null) {
            return "[" + language + ":" + name + "]";
        }
        return null;
    }

    public String getDescription (String language) {
        String s2 = Tools.getText(description_alt, language);
        if (s2 != null) {
            return s2;
        }
        String s1 = Tools.getText(description, language);
        if (s1 != null) {
            return s1;
        }
        if (description_alt != null) {
            return "[" + language + ":" + description_alt + "]";
        }
        if (description != null) {
            return "[" + language + ":" + description + "]";
        }
        return null;
    }

    public String getLink (String language) {
        switch (type) {
            case UB : {
                Building building = Building.buildings.get(pointsTo);
                if (building != null) {
                    return building.getLink(language);
                }
                break;
            }
            case UD : {
                District district = District.distincts.get(pointsTo);
                if (district != null) {
                    return district.getLink(language);
                }
                break;
            }
            case UI : {
                Improvement improvement = Improvement.improvements.get(pointsTo);
                if (improvement != null) {
                    return improvement.getLink(language);
                }
                break;
            }
            case UU : {
                Unit unit = Unit.units.get(pointsTo);
                if (unit != null) {
                    return unit.getLink(language);
                }
                break;
            }
        }
        return null;
    }

    public JSONObject getIconLabel (String language) {
        return Tools.getIconlabel(getLink(language), icon, "ICON_" + pointsTo, getName(language));
    }

    public static void load () {
        Statement gameplay = null;
        try {
            gameplay = DriverManager.getConnection(Tools.GAMEPLAY_DATABASE).createStatement();
            
            // load trait list
            ResultSet r1 = gameplay.executeQuery("select * from Traits;");
            while (r1.next()) {
                String tag = r1.getString("TraitType");
                Trait trait = new Trait(tag);
                trait.name = r1.getString("Name");
                trait.description = r1.getString("Description");
            }
        } catch (Exception e) {
            System.err.println("Error loading traits.");
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

    // this method shold be called after all other models are loaded, which links information
    public static void linkData () {
        for (Entry<String, Civilization> entry : Civilization.civilizations.entrySet()) {
            Civilization civilization = entry.getValue();
            for (String tr : civilization.traits) {
                Trait trait = traits.get(tr);
                if (trait != null) {
                    trait.type = UA;
                    trait.icon = civilization.icon;
                    trait.belongs = entry.getKey();
                }
            }
        }

        for (Entry<String, Leader> entry : Leader.leaders.entrySet()) {
            Leader leader = entry.getValue();
            for (String tr : leader.traits) {
                Trait trait = traits.get(tr);
                if (trait != null) {
                    trait.type = LA;
                    trait.icon = leader.icon;
                    trait.pointsTo = entry.getKey();
                    trait.belongs = entry.getKey();
                }
            }
        }

        for (Entry<String, CityState> entry : CityState.cityStates.entrySet()) {
            CityState cityState = entry.getValue();
            for (String tr : cityState.traits) {
                Trait trait = traits.get(tr);
                if (trait != null) {
                    trait.pointsTo = entry.getKey();
                    trait.belongs = entry.getKey();
                }
            }
        }

        for (Entry<String, Unit> entry : Unit.units.entrySet()) {
            Unit unit = entry.getValue();
            if (unit.trait != null) {
                Trait trait = traits.get(unit.trait);
                if (trait != null) {
                    trait.type = UU;
                    trait.icon = unit.icon;
                    trait.name_alt = unit.name;
                    trait.description_alt = unit.description;
                    trait.pointsTo = entry.getKey();
                    unit.belongs = trait.belongs;
                }
            }
        }
        
        for (Entry<String, District> entry : District.distincts.entrySet()) {
            District district = entry.getValue();
            if(district.trait != null) {
                Trait trait = traits.get(district.trait);
                if (trait != null) {
                    trait.type = UD;
                    trait.icon = district.icon;
                    trait.name_alt = district.name;
                    trait.description_alt = district.description;
                    trait.pointsTo = entry.getKey();
                    district.belongs = trait.belongs;
                }
            }
        }

        for (Entry<String, Building> entry : Building.buildings.entrySet()) {
            Building building = entry.getValue();
            if(building.trait != null) {
                Trait trait = traits.get(building.trait);
                if (trait != null) {
                    trait.type = UB;
                    trait.icon = building.icon;
                    trait.name_alt = building.name;
                    trait.description_alt = building.description;
                    trait.pointsTo = entry.getKey();
                    building.belongs = trait.belongs;
                }
            }
        }

        for (Entry<String, Improvement> entry : Improvement.improvements.entrySet()) {
            Improvement improvement = entry.getValue();
            if(improvement.trait != null) {
                Trait trait = traits.get(improvement.trait);
                if (trait != null) {
                    trait.type = UI;
                    trait.icon = improvement.icon;
                    trait.name_alt = improvement.name;
                    trait.description_alt = improvement.description;
                    trait.pointsTo = entry.getKey();
                    improvement.belongs = trait.belongs;
                }
            }
        }

        for (Entry<String, Project> entry : Project.projects.entrySet()) {
            Project project = entry.getValue();
            if (project.trait != null) {
                Trait trait = traits.get(project.trait);
                if (trait != null) {
                    trait.containsUP = true;
                    trait.icon = project.icon;
                    trait.pointsTo = entry.getKey();
                    project.belongs = trait.belongs;
                }
            }
        }
    }
}
