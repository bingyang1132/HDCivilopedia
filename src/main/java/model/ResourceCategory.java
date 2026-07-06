package model;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import model.abstracts.Writable;
import tools.Tools;

public class ResourceCategory extends Writable {
	
    public static final Map<String, ResourceCategory> categories = new HashMap<>();

	public String industryEffect;
	public String corporationEffect;
	public List<String> resources = new ArrayList<>();
	public Map<String, Integer> productYields = new LinkedHashMap<>();

    public ResourceCategory (String tag) {
        super(tag);
        categories.put(tag, this);
    }

    // load resource categories classes
    public static void load () {
        Statement gameplay = null;
        try {
            gameplay = DriverManager.getConnection(Tools.GAMEPLAY_DATABASE).createStatement();

            // load the monopoly category list. Only categories that define both an
            // industry and a corporation effect are real monopoly categories; the
            // product-only categories (BAVARIA_*, TOYS, COSMETICS, ...) carry no
            // effect and are skipped.
            ResultSet r1 = gameplay.executeQuery("select * from HD_Monopoly_Categories where IndustryEffect is not null and IndustryEffect <> '' and CorporationEffect is not null and CorporationEffect <> '';");
            while (r1.next()) {
                String tag = r1.getString("Category");
                ResourceCategory category = new ResourceCategory(tag);
                category.name = "LOC_RESOURCE_CLASSIFICATION_HD_" + tag + "_NAME";
                category.industryEffect = "LOC_" + r1.getString("IndustryEffect") + "_DESCRIPTION";
                category.corporationEffect = "LOC_" + r1.getString("CorporationEffect") + "_DESCRIPTION";
            }

            // resources belonging to each category (many-to-many)
            ResultSet r2 = gameplay.executeQuery("select * from HD_Monopoly_Resource_Categories;");
            while (r2.next()) {
                ResourceCategory category = categories.get(r2.getString("Category"));
                if (category != null) {
                    category.resources.add(r2.getString("ResourceType"));
                }
            }

            // product yields granted by each category's products
            ResultSet r3 = gameplay.executeQuery("select * from HD_ProductYields;");
            while (r3.next()) {
                ResourceCategory category = categories.get(r3.getString("Category"));
                if (category != null) {
                    category.productYields.put(r3.getString("YieldType"), r3.getInt("YieldChange"));
                }
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

        if (!productYields.isEmpty()) {
            JSONArray yieldContents = new JSONArray();
            yieldContents.add(Tools.getSeparator());
            for (Entry<String, Integer> entry : productYields.entrySet()) {
                yieldContents.add(Tools.getLabel(Tools.signed(entry.getValue()) + Tools.getYield(entry.getKey(), language)));
            }
            rightColumnItems.add(Tools.getStatbox(Tools.getControlText("ProductYields", language), yieldContents));
        }

        JSONArray contents = new JSONArray();
        contents.add(Tools.getSeparator());
		for (String resource : resources) {
			Resource resourceObject = Resource.resources.get(resource);
			if (resourceObject != null) {
				contents.add(resourceObject.getIconLabel(language));
			}
		}
        rightColumnItems.add(Tools.getStatbox(Tools.getControlText("resources", language), contents));
        object.put("rightColumnItems", rightColumnItems);

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
    public int getFolderOrder() {
        return -100;
    }

    @Override
    public String getFolderName(String language) {
        return Tools.colorText(Tools.getControlText("ResourceCategory", language), "#5B9797");
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
