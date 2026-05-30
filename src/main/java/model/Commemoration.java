package model;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import model.abstracts.Writable;
import tools.Tools;

public class Commemoration extends Writable {

    public static final Map<String, Commemoration> commemorations = new HashMap<>();

    public String minimumGameEra;
    public String maximumGameEra;
    public String goldenAgeBonusDescription;
    public String normalAgeBonusDescription;
    public String darkAgeBonusDescription;

    public Commemoration (String tag) {
        super(tag);
        commemorations.put(tag, this);
    }

    public static void load () {
        Statement gameplay = null;
        try {
            gameplay = DriverManager.getConnection(Tools.GAMEPLAY_DATABASE).createStatement();

            // load commemoration list
            ResultSet r1 = gameplay.executeQuery("select * from CommemorationTypes;");
            while (r1.next()) {
                String tag = r1.getString("CommemorationType");
                Commemoration commemoration = new Commemoration(tag);
                commemoration.name = r1.getString("CategoryDescription");
                commemoration.minimumGameEra = r1.getString("MinimumGameEra");
                commemoration.maximumGameEra = r1.getString("MaximumGameEra");
                commemoration.goldenAgeBonusDescription = r1.getString("GoldenAgeBonusDescription");
                commemoration.normalAgeBonusDescription = r1.getString("NormalAgeBonusDescription");
                commemoration.darkAgeBonusDescription = r1.getString("DarkAgeBonusDescription");
            }
        } catch (Exception e) {
            System.err.println("Error loading commemorations.");
            System.err.println(e.getClass().getName() + " " + e.getMessage());
        } finally {
            try {
                if(gameplay != null) {
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
        if (goldenAgeBonusDescription != null) {
            leftColumnItems.add(Tools.getHeader(Tools.getControlText("Golden Age", language)));
            leftColumnItems.add(Tools.getBody(null, Tools.getTextWithAlt(goldenAgeBonusDescription, language)));
        }
        if (normalAgeBonusDescription != null) {
            leftColumnItems.add(Tools.getHeader(Tools.getControlText("Normal Age", language)));
            leftColumnItems.add(Tools.getBody(null, Tools.getTextWithAlt(normalAgeBonusDescription, language)));
        }
        if (darkAgeBonusDescription != null) {
            leftColumnItems.add(Tools.getHeader(Tools.getControlText("Dark Age", language)));
            leftColumnItems.add(Tools.getBody(null, Tools.getTextWithAlt(darkAgeBonusDescription, language)));
        }
        object.put("leftColumnItems", leftColumnItems);

        JSONArray rightColumnItems = new JSONArray();
        object.put("rightColumnItems", rightColumnItems);

        JSONArray requirementContents = new JSONArray();
        rightColumnItems.add(Tools.getStatbox(Tools.getControlText("Requirements", language), requirementContents));

        Era st = Era.eras.get(minimumGameEra);
        Era ed = null;
        if (maximumGameEra != null) {
            ed = Era.eras.get(maximumGameEra);
        }
        requirementContents.add(Tools.getSeparator());

        int edc = ed == null ? Era.eraList.length - 1 : ed.chronologyIndex;
        for (int c = st.chronologyIndex; c <= edc; c++) {
            Era era = Era.eraList[c];
            requirementContents.add(Tools.getLabel(era.getTitle(language)));
        }

        return object;
    }

    @Override
    public String getChapter() {
        return "governments";
    }

    @Override
    public String getFolder() {
        return "commemorations";
    }

    @Override
    public String getFolderName(String language) {
        return Tools.getControlText("Commemorations", language);
    }

    @Override
    public String getTagPrefix() {
        return "COMMEMORATION_";
    }
    
    @Override
    public int getOrder() {
        Era st = Era.eras.get(minimumGameEra);
        Era ed = null;
        if (maximumGameEra != null) {
            ed = Era.eras.get(maximumGameEra);
        }
        return st.chronologyIndex * 16 + (ed == null ? 15 : ed.chronologyIndex);
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
