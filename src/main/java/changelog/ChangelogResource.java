package changelog;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.alibaba.fastjson.JSONObject;

import model.Project;
import model.abstracts.Main;
import model.abstracts.Writable;
import tools.Tools;

public class ChangelogResource {

    public static Map<String, String> nicks = getNicks();
    public static Map<String, String> icons = getIcons();

    public static Map<String, String> getNicks() {
        Map<String, String> nicks = new HashMap<>();
        nicks.put("宜居", "宜居度");
        nicks.put("防空力", "防空力量");
        nicks.put("轰炸力", "轰炸攻击力");
        nicks.put("琴", "文化值");
        nicks.put("文化", "文化值");
        nicks.put("鸽", "信仰");
        nicks.put("信仰值", "信仰");
        nicks.put("外交点", "外交支持");
        nicks.put("粮食", "食物");
        nicks.put("粮", "食物");
        nicks.put("金", "金币");
        nicks.put("总督点", "总督头衔");
        nicks.put("提督", "海军统帅");
        nicks.put("大艺", "大艺术家");
        nicks.put("大工", "大工程师");
        nicks.put("大军", "大将军");
        nicks.put("大商", "大商人");
        nicks.put("大音", "大音乐家");
        nicks.put("大仙", "大预言家");
        nicks.put("大科", "大科学家");
        nicks.put("大作", "大作家");
        nicks.put("速", "移动力");
        nicks.put("锤", "生产力");
        nicks.put("远程力", "远程战斗力");
        nicks.put("瓶", "科技值");
        nicks.put("科技", "科技值");
        nicks.put("力", "战斗力");
        nicks.put("箱", "旅游业绩");
        nicks.put("业绩", "旅游业绩");
        nicks.put("商路", "贸易路线");
        nicks.put("电", "电力");
        nicks.put("工人", "建造者");
        nicks.put("移民", "开拓者");
        nicks.put("公民", "市民");
        return nicks;
    }

    public static Map<String, String> getIcons () {
        Map<String, String> icons = new HashMap<>();
        icons.put("宜居度", "ICON_AMENITIES");
        icons.put("防空力量", "ICON_ANTIAIR_LARGE");
        icons.put("轰炸攻击力", "ICON_BOMBARD");
        icons.put("劳动力", "ICON_CHARGES");
        icons.put("首都", "ICON_CAPITAL");
        icons.put("人口", "ICON_CITIZEN");
        icons.put("市民", "ICON_CITIZEN");
        icons.put("鼓舞", "ICON_CIVICBOOSTED");
        icons.put("文化值", "ICON_CULTURE");
        icons.put("区域", "ICON_DISTRICT");
        icons.put("使者", "ICON_ENVOY");
        icons.put("信仰", "ICON_FAITH");
        icons.put("外交支持", "ICON_FAVOR");
        icons.put("食物", "ICON_FOOD");
        icons.put("金币", "ICON_GOLD");
        icons.put("总督头衔", "ICON_GOVERNOR");
        icons.put("总督", "ICON_GOVERNOR");
        icons.put("海军统帅", "ICON_GREATADMIRAL");
        icons.put("大艺术家", "ICON_GREATARTIST");
        icons.put("大工程师", "ICON_GREATENGINEER");
        icons.put("大将军", "ICON_GREATGENERAL");
        icons.put("大商人", "ICON_GREATMERCHANT");
        icons.put("大音乐家", "ICON_GREATMUSICIAN");
        icons.put("伟人", "ICON_GREATPERSON");
        icons.put("大预言家", "ICON_GREATPROPHET");
        icons.put("大科学家", "ICON_GREATSCIENTIST");
        icons.put("文物", "ICON_GREATWORK_ARTIFACT");
        icons.put("艺术", "ICON_GREATWORK_LANDSCAPE");
        icons.put("音乐", "ICON_GREATWORK_MUSIC");
        icons.put("肖像", "ICON_GREATWORK_PORTRAIT");
        icons.put("遗物", "ICON_GREATWORK_RELIC");
        icons.put("宗教艺术", "ICON_GREATWORK_RELIGIOUS");
        icons.put("雕塑", "ICON_GREATWORK_SCULPTURE");
        icons.put("著作", "ICON_GREATWORK_WRITING");
        icons.put("大作家", "ICON_GREATWRITER");
        icons.put("不满", "ICON_GRIEVANCE");
        icons.put("住房", "ICON_HOUSING");
        icons.put("移动力", "ICON_MOVEMENT");
        icons.put("电力", "ICON_POWER");
        icons.put("生产力", "ICON_PRODUCTION");
        icons.put("远程战斗力", "ICON_RANGED");
        icons.put("科技值", "ICON_SCIENCE");
        icons.put("战斗力", "ICON_STRENGTH");
        icons.put("尤里卡", "ICON_TECHBOOSTED");
        icons.put("旅游业绩", "ICON_TOURISM");
        icons.put("贸易路线", "ICON_TRADEROUTE");
        icons.put("贸易站", "ICON_TRADINGPOST");
        icons.put("回合", "ICON_TURN");
        icons.put("外交能见度", "ICON_VISLIMITED");
        icons.put("奇观", "ICON_WONDER");
        return icons;
    }

    public static JSONObject generateResourceFile () {
        JSONObject object = new JSONObject();
        for (Entry<String, String> entry : icons.entrySet()) {
            JSONObject item = new JSONObject();
            item.put("abs", "<img src=\"https://civ6hd.com/icons/" + entry.getValue() + ".png\" alt=\"" + entry.getValue() + "\" style=\"vertical-align:middle;margin-bottom:4px;width:22px;height:22px;\"/>" + entry.getKey());
            item.put("rel", "<img src=\"" + Tools.IMAGE_URL + "/" + entry.getValue() + ".png\" alt=\"" + entry.getValue() + "\" style=\"vertical-align:middle;margin-bottom:4px;width:22px;height:22px;\"/>" + entry.getKey());
            object.put(entry.getKey(), item);
        }
        for (Entry<String, Writable> entry : Main.WRITABLES_ZHINDEX.entrySet()) {
            JSONObject item = new JSONObject();
            item.put("abs", "<a href=\"" + entry.getValue().getAbsLink("zh_Hans_CN") + "\">" + entry.getKey() + "</a>");
            item.put("rel", "<a href=\"" + entry.getValue().getLink("zh_Hans_CN") + "\">" + entry.getKey() + "</a>");
            item.put("defaultCat", entry.getValue().getCat());
            item.put("defaultCatOrder", entry.getValue().getCatOrder());
            object.put(entry.getKey(), item);
        }
        for (Entry<String, Writable> entry : Main.WRITABLES_ZHINDEX.entrySet()) {
            if (entry.getValue() instanceof Project) {
                Project project = (Project) entry.getValue();
                if (project.tag.startsWith("PROJECT_CITY_POLICY_ENABLE_")) {
                    JSONObject item = new JSONObject();
                    item.put("abs", ("<a href=\"" + entry.getValue().getAbsLink("zh_Hans_CN") + "\">" + entry.getKey() + "</a>").replaceAll("启用 ", ""));
                    item.put("abs", ("<a href=\"" + entry.getValue().getAbsLink("zh_Hans_CN") + "\">" + entry.getKey() + "</a>").replaceAll("启用 ", ""));
                    item.put("rel", ("<a href=\"" + entry.getValue().getLink("zh_Hans_CN") + "\">" + entry.getKey() + "</a>"));
                    item.put("defaultCat", "城市政策改动");
                    item.put("defaultCatOrder", entry.getValue().getCatOrder());
                    object.put(entry.getKey().replaceAll("启用 ", ""), item);
                }
            }
        }
        for (Entry<String, String> entry : nicks.entrySet()) {
            object.put(entry.getKey(), object.getJSONObject(entry.getValue()));
        }
        File file = new File("output/icons");
        for (File f : file.listFiles()) {
            String s = f.getName().replaceAll(".png", "");
            JSONObject item = new JSONObject();
            item.put("abs", "<img src=\"https://civ6hd.com/icons/" + s + ".png\" alt=\"" + s + "\" style=\"vertical-align:middle;margin-bottom:4px;width:22px;height:22px;\"/>");
            item.put("rel", "<img src=\"" + Tools.IMAGE_URL + "/" + s + ".png\" alt=\"" + s + "\" style=\"vertical-align:middle;margin-bottom:4px;width:22px;height:22px;\"/>");
            object.put(s, item);
        }
        return object;
    }

    public static void saveResourceFile () throws Exception {
        JSONObject resource = generateResourceFile();
        File resouceFile = new File("ChangelogResouce.json");
        if (resouceFile.exists()) {
            resouceFile.delete();
        }
        resouceFile.createNewFile();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(resouceFile), "UTF-8"));
        writer.write(resource.toString());
        writer.flush();
        writer.close();
    }
}