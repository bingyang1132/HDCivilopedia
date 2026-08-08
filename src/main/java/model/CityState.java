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

import model.abstracts.Writable;
import model.abstracts.WritableWithIcon;
import tools.ImageEditor;
import tools.Tools;

public class CityState extends WritableWithIcon {

    public static final Map<String, CityState> cityStates = new HashMap<>();
	
	public static final Map<String, CityStateType> cityStateTypes = new HashMap<>();

    public String inheritLeader;
    public List<String> traits = new ArrayList<>();

    public String description;

    public CityState (String tag) {
        super(tag);
        cityStates.put(tag, this);
    }

    // load city-states list from database
    public static void load () {
        Statement gameplay = null;
        try {
            gameplay = DriverManager.getConnection(Tools.GAMEPLAY_DATABASE).createStatement();
    
            // load city-states list
            ResultSet r1 = gameplay.executeQuery("select * from Civilizations;");
            while (r1.next()) {
                String civiLevel = r1.getString("StartingCivilizationLevelType");
                if(civiLevel.equals("CIVILIZATION_LEVEL_CITY_STATE")) {
                    String tag = r1.getString("CivilizationType");
                    CityState cityState = new CityState(tag);
                    cityState.name = r1.getString("Name");
                }
            }
    
            // load other information
            for(Entry<String, CityState> entry : cityStates.entrySet()) {
                String tag = entry.getKey();
                CityState state = entry.getValue();
                String leader = null;

                // load leaders
                ResultSet r2 = gameplay.executeQuery("select * from CivilizationLeaders where CivilizationType = \"" + tag + "\" order by LeaderType;");
                if (r2.next()) {
                    leader = r2.getString("LeaderType");
                }

                // load leader links
                ResultSet r3 = gameplay.executeQuery("select * from Leaders where LeaderType = \"" + leader + "\"");
                if (r3.next()) {
                    state.inheritLeader = r3.getString("InheritFrom");
                }

                // load traits
                ResultSet r4 = gameplay.executeQuery("select * from LeaderTraits where LeaderType = \"" + state.inheritLeader + "\";");
                while (r4.next()) {
                    String trait = r4.getString("TraitType");
                    state.traits.add(trait);
                }

                ResultSet r5 = gameplay.executeQuery("select * from LeaderTraits where LeaderType = \"" + leader + "\";");
                while (r5.next()) {
                    String trait = r5.getString("TraitType");
                    state.traits.add(trait);
                }
                
                // load city-state icon
                String iconString = "ICON_" + tag;
                BufferedImage icon = Tools.getImage(iconString);
                if (icon != null) {
                    String path = iconString + ".png";
                    state.icon = Tools.IMAGE_URL + "/" + path;
                    ImageEditor.saveImage(icon, path);
                }
            }

			// load city-state type bonus table
			ResultSet r6 = gameplay.executeQuery("select * from CSE_ClassTypes;");
            while (r6.next()) {
				String traitType = r6.getString("TraitType");
				CityStateType type = new CityStateType();
				cityStateTypes.put(traitType, type);
				type.bonuses.add(r6.getString("SmallBonus"));
				type.bonuses.add(r6.getString("MediumBonus"));
				type.bonuses.add(r6.getString("LargeBonus"));
				type.bonuses.add(r6.getString("LargestBonus"));
				type.items.add(new ArrayList<>());
				type.items.add(new ArrayList<>());
				type.items.add(new ArrayList<>());
				type.items.add(new ArrayList<>());
            }
			
			// load buffed items
			ResultSet r7 = gameplay.executeQuery("select * from HD_CityStateBuffedObjects;");
            while (r7.next()) {
				String traitType = r7.getString("TraitType");
				CityStateType type = cityStateTypes.get(traitType);
				String level = r7.getString("Level");
				String item = r7.getString("ObjectType");
				List<String> list = null;
				switch (level) {
					case "SMALL" : {
						list = type.items.get(0);
						break;
					}
					case "MEDIUM" : {
						list = type.items.get(1);
						break;
					}
					case "LARGE" : {
						list = type.items.get(2);
						break;
					}
					case "LARGEST" : {
						list = type.items.get(3);
						break;
					}
				}
				if (list != null && !list.contains(item)) {
					list.add(item);
				}
            }
        } catch (Exception e) {
            System.err.println("Error loading city-states.");
            System.err.println(e.getClass().getName() + " " + e.getMessage());
        } finally {
            try {
                if(gameplay != null) gameplay.close();
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
        rightColumnItems.add(Tools.getStatbox(Tools.getControlText("CityStateType", language), contents));
        object.put("rightColumnItems", rightColumnItems);

        contents.add(Tools.getSeparator());
        contents.add(Tools.getLabel(Tools.getControlText(inheritLeader, language)));
		
		
        for (String t : traits) {
			Trait trait = Trait.traits.get(t);

			CityStateType stateType = cityStateTypes.get(t);
			if (stateType != null) {
				String typeBonus = "";
				for (int i = 0; i < stateType.bonuses.size(); i++) {
					typeBonus += stateType.getBonus(i, language);
					if (i < stateType.bonuses.size() - 1) {
						typeBonus += "[NEWLINE]";
					}
				}
				leftColumnItems.add(Tools.getBody(trait.getName(language), typeBonus));
			} else {
				leftColumnItems.add(Tools.getBody(trait.getName(language), trait.getDescription(language)));
			}

            int type = trait.type;
            if (type > 0) {
                contents.add(Tools.getSeparator());
                contents.add(Tools.getHeader(Tools.getControlText(trait.getTypeAsString(), language)));
                contents.add(trait.getIconLabel(language));
            }
        }

        // The four resources a suzerain gets from this city-state. They are a property of the
        // city-state *type*, so every city-state of a type lists the same four -- which is what
        // a reader looking at one particular city-state wants to know.
        JSONArray resourceContents = new JSONArray();
        for (Resource resource : Resource.resources.values()) {
            if (getFolder().equalsIgnoreCase(resource.cityStateType)) {
                resourceContents.add(resource.getIconLabel(language));
            }
        }
        if (!resourceContents.isEmpty()) {
            rightColumnItems.add(Tools.getStatbox(
                    Tools.getControlText("CityStateResources", language), resourceContents));
        }

        return object;
    }

    @Override
    public String getChapter() {
        return "citystates";
    }

    @Override
    public String getTagPrefix() {
        return "CIVILIZATION_";
    }

    @Override
    public String getFolder () {
        return inheritLeader.substring("LEADER_MINOR_CIV_".length(), inheritLeader.length()).toLowerCase();
    }

    @Override
    public String getFolderName(String language) {
        return Tools.getControlText(inheritLeader, language);
    }

    @Override
    public String getCat() {
        return "城邦改动";
    }

    @Override
    public int getCatOrder() {
        return -1900;
    }
}

class CityStateType {
	List<String> bonuses = new ArrayList<>();
	List<List<String>> items = new ArrayList<>();

	String getBonus (int index, String language) {
		String raw = Tools.getText(bonuses.get(index), language);
		String itemString = "";
		for (int i = 0; i < items.get(index).size(); i++) {
			String item = items.get(index).get(i);
			Writable writable = null;
			if (District.distincts.containsKey(item)) {
				writable = District.distincts.get(item);
			} else {
				writable = Building.buildings.get(item);
			}
			if (writable != null) {
				itemString += writable.getLinkedTitle(language);
				if (i < items.get(index).size() - 2) {
					itemString += Tools.getText("LOC_COMMA", language);
				} else if (i == items.get(index).size() - 2) {
					itemString += Tools.getText("LOC_AND", language);
				}
			} else {
				System.out.println(item);
			}
		}
		return raw.replace("*", itemString);
	}
}