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

public class Commemoration_CN extends Writable {

    public static final Map<String, Commemoration_CN> commemorationsCN = new HashMap<>();

    public String description;
    public java.util.List<String> belongs = new java.util.ArrayList<>(); // 是LeaderTyoe类型 name需要到Leaders里找对应的Name

    public Commemoration_CN(String tag) {
        super(tag);
        commemorationsCN.put(tag, this);
    }

    public static void load() {
        Statement gameplay = null;
        try {
            gameplay = DriverManager.getConnection(Tools.GAMEPLAY_DATABASE).createStatement();

            // 加载中国特色远古时代着力点
            ResultSet rs = gameplay.executeQuery("select * from China_AncientCommemorationTypes_HD");
            while (rs.next()) {
                String tag = rs.getString("AncientCommemorationType");
                Commemoration_CN commemoration = new Commemoration_CN(tag);
                commemoration.name = rs.getString("Name");
                commemoration.description = rs.getString("Description");

                // 创建新的statement来执行第二个查询
                Statement gameplay2 = null;
                gameplay2 = DriverManager.getConnection(Tools.GAMEPLAY_DATABASE).createStatement();
                ResultSet rs2 = gameplay2.executeQuery("select * from ChinaLeaders_AncientCommemorationTypes_HD where AncientCommemorationType = \"" + tag + "\"");

                while (rs2.next()) {
                   // leader的名字在LeaderType
                    String leaderTag = rs2.getString("LeaderType");
                    if (leaderTag != null) {
                        commemoration.belongs.add(leaderTag);
                        // debug
                        // System.out.println("Commemoration: " + commemoration.name + " belongs to " + leaderTag);
                    }
                }
                // 关闭第二个statement
                if (gameplay2!= null) {
                    gameplay2.close();
                }
            }

            // 找到leaders
        } catch (Exception e) {
            System.err.println("Error loading Chinese commemorations.");
            System.err.println(e.getClass().getName() + " " + e.getMessage());
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
        if (description != null) {
            leftColumnItems.add(Tools.getHeader(Tools.getControlText("Description", language)));
            leftColumnItems.add(Tools.getBody(null, Tools.getTextWithAlt(description, language)));
        }
        object.put("leftColumnItems", leftColumnItems);

        JSONArray rightColumnItems = new JSONArray();
        object.put("rightColumnItems", rightColumnItems);

        // 把对应领袖的超链接放到右栏
        // 创建内容
        JSONArray belongContent = new JSONArray();
        // "特属于"
        belongContent.add(Tools.getSeparator());
        belongContent.add(Tools.getHeader(Tools.getControlText("Unique To", language)));
        // 遍历belongs 加入leader
        for (String leaderTag : belongs) {
            Leader leader = Leader.leaders.get(leaderTag);  // 调用做好了的Leaders
            if (leader!= null) {
                belongContent.add(leader.getIconLabel(language));
            } 
            // debug
            // System.out.println("Commemoration: " + name + " belongs to " + leaderTag);
        }
        // 添加到右栏
        if (belongContent.size() > 0) {
            rightColumnItems.add(Tools.getStatbox(Tools.getControlText("Traits", language), belongContent));
        }


        return object;
    }

    @Override
    public String getChapter() {
        return "governments";
    }

    @Override
    public String getFolder() {
        return "commemorations_cn";
    }

    @Override
    public String getFolderName(String language) {
        return Tools.getControlText("Commemorations_CN", language);
    }

    @Override
    public String getTagPrefix() {
        return "COMMEMORATION_CN_";
    }

    @Override
    public int getOrder() {
        return 0; // 可以根据需要添加排序逻辑
    }

    @Override
    public String getCat() {
        return "中国特色远古时代着力点";
    }

    @Override
    public int getCatOrder() {
        return -1300; 
    }
}