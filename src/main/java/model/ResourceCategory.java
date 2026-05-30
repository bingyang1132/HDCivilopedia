package model;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import model.abstracts.Writable;
import tools.Tools;

public class ResourceCategory extends Writable {
	
    public static final Map<String, ResourceCategory> categories = new HashMap<>();

	public String industryEffect;
	public String corporationEffect;
	public List<String> resources = new ArrayList<>();

    public ResourceCategory (String tag) {
        super(tag);
        categories.put(tag, this);
    }

    // load resource categories classes
    public static void load () {
        Statement gameplay = null;
        try {
            gameplay = DriverManager.getConnection(Tools.GAMEPLAY_DATABASE).createStatement();
    
            // load resource categoty list
            ResultSet r1 = gameplay.executeQuery("select * from HDMonopolyResourceEffects where Category not in ('KHALIFA', 'PORCELAIN', 'AIRPORT_DRINK', 'AIRPORT_USING', 'AIRPORT_FOOD');");
            while (r1.next()) {
                String tag = r1.getString("Category");
				ResourceCategory category;
				if (categories.containsKey(tag)) {
					category = categories.get(tag);
				} else {
					category = new ResourceCategory(tag);
					category.name = "LOC_HD_PEDIA_CATEGORY_" + tag + "_NAME";
					category.industryEffect = "LOC_" + r1.getString("IndustryEffect") + "_DESCRIPTION";
					category.corporationEffect = "LOC_" + r1.getString("CorporationEffect") + "_DESCRIPTION";
				}
				category.resources.add(r1.getString("ResourceType"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error loading resource categories.");
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
        leftColumnItems.add(Tools.getHeader(Tools.getControlText("IndustryEffect", language)));
		leftColumnItems.add(Tools.getBody("", Tools.getText(industryEffect, language)));
        leftColumnItems.add(Tools.getHeader(Tools.getControlText("CorporationEffect", language)));
		leftColumnItems.add(Tools.getBody("", Tools.getText(corporationEffect, language)));
        object.put("leftColumnItems", leftColumnItems);

        JSONArray rightColumnItems = new JSONArray();
        JSONArray contents = new JSONArray();
        rightColumnItems.add(Tools.getStatbox(Tools.getControlText("resources", language), contents));
        object.put("rightColumnItems", rightColumnItems);

        contents.add(Tools.getSeparator());
		for (String resource : resources) {
			Resource resourceObject = Resource.resources.get(resource);
			if (resourceObject != null) {
				contents.add(resourceObject.getIconLabel(language));
			} else {
				System.out.println(resource);
			}
		}

        return object;
    }

    @Override
    public String getChapter() {
        return "resources";
    }

    @Override
    public String getTagPrefix() {
        return "";
    }

    @Override
    public String getFolder() {
        return "ResourceCategory";
    }

    @Override
    public String getFolderName(String language) {
        return Tools.getControlText("ResourceCategory", language);
    }

    @Override
    public String getCat() {
        return "行业与公司模式改动";
    }

    @Override
    public int getCatOrder() {
        return -1500;
    }
}
