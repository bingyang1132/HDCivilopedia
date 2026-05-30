package model;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import tools.Tools;

public class Era {
    
    public static final Map<String, Era> eras = new HashMap<>();
    public static Era[] eraList;

    public String tag;
    public String name;
    public int chronologyIndex;

    public Era (String tag) {
        this.tag = tag;
        eras.put(tag, this);
    }

    public static void load () {
        Statement gameplay = null;
        try {
            gameplay = DriverManager.getConnection(Tools.GAMEPLAY_DATABASE).createStatement();

            // load era list
            int maxIndex = 0;
            ResultSet r1 = gameplay.executeQuery("select * from Eras;");
            while (r1.next()) {
                String tag = r1.getString("EraType");
                Era era = new Era(tag);
                era.name = r1.getString("Name");
                era.chronologyIndex = r1.getInt("ChronologyIndex");
                if (era.chronologyIndex > maxIndex) {
                    maxIndex = era.chronologyIndex;
                }
            }

            // order eras
            eraList = new Era[maxIndex + 1];
            for (Entry<String, Era> entry : eras.entrySet()) {
                Era era = entry.getValue();
                eraList[era.chronologyIndex] = era;
            }
        } catch (Exception e) {
            System.err.println("Error loading eras.");
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

    public String getTitle (String language) {
        return Tools.getTextWithAlt(name, language);
    }
}
