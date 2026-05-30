package model.abstracts;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;

import changelog.Changelog;
import changelog.ChangelogResource;
import load.Init;
import view.Page;
import model.*;
import tools.Tools;

public class Main {

    public static final List<Writable> WRITABLES = new ArrayList<>();
    public static final Map<String, Writable> WRITABLES_ZHINDEX = new HashMap<>();

    public static void load () {
        Era.load();
        Civilization.load();
        Leader.load();
        Trait.load();
        Unit.load();
        District.load();
        Building.load(); 
        Improvement.load();
        CityState.load();
        YieldChange.load();
        Technology.load();
        Civic.load();
        Wonder.load();
        UnitPromotion.load();
        UnitPromotionClass.load();
        GreatPerson.load();
        Government.load();
        Policy.load();
        Commemoration.load();
        Commemoration_CN.load();
        Belief.load();
        Terrain.load();
        Feature.load();
        NaturalWonder.load();
		ResourceCategory.load();
        Resource.load();
        Governor.load();
        GovernorPromotion.load();
        Project.load();

        Trait.linkData();
        District.linkData();
        Unit.linkData();
    }

    public static void write (String language) {
        for (Writable writable : WRITABLES) {
            writable.writeJSON(language);
        }
    }

    public static void writeAll () {
        for (String language : Page.LANGUAGES) {
            write(language);
        }
    }

    public static void buildChangelog (String version, String output) throws Exception {
        System.out.println("loading changelog txt");
        Changelog changelog = new Changelog(version, version + "\\S*");
        // File logs = new File("C:/Users/1132/Documents/My Games/Sid Meier's Civilization VI/Mods/civ6-harmony-in-diversity/Changelog");
        // use constant path from Tools.CHANGELOG
        File logs = new File(Tools.CHANGELOG);
        if (!logs.exists()) {
            System.out.println("[WARNING]Changelog folder not found");
            return;
        }        
        for (File f : logs.listFiles()) {
            if (!f.getName().endsWith(".txt") || f.getName().equals("changelog_old.txt")) continue;
            changelog.readFrom(f, "rel");
        }
        System.out.println("converting changelog into json");
        JSONObject object = changelog.toJsonObject(output);
        System.out.println("writing changelog json to manual/json/zh_Hans_CN/concepts/updates/" + output + ".json");
        File file = new File("manual/json/zh_Hans_CN/concepts/updates/" + output + ".json");
        if (file.exists()) {
            file.delete();
        }
        file.createNewFile();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
        writer.write(object.toString());
        // System.out.println("output written to");
        writer.flush();
        writer.close();
    }

    public static void main(String[] args) throws Exception {
        if (args.length > 0) {
            String cmd = args[0];
            if (cmd.equals("page")) {
                System.out.println("converting json into html...");
                Page.convertAll();
            } else if (cmd.equals("init")) {
                Init.main(null);
            } else if (cmd.equals("icons")) {
                Init.initIcons();
            } else if (cmd.equals("changelog")) {
                System.out.println("loading...");
                load();

                Page.deleteFiles(new File("json")); 
                System.out.println("writing json...");
                writeAll();
            } else if (cmd.equals("build")) {
                buildChangelog(args[1], args.length >= 3 ? args[2] : "v" + args[1]);
            } else if (cmd.equals("after_init")) {
                System.out.println("icons...");
                Init.initIcons();

                System.out.println("loading...");
                load();

                Page.deleteFiles(new File("json"));

                System.out.println("writing json...");
                writeAll();

                System.out.println("saving changelog resource...");
                ChangelogResource.saveResourceFile();

                System.out.println("converting json into html...");
                Page.convertAll();
            } else {
                System.out.println("unknown command");
                
                System.out.println("saving changelog resource...");
                ChangelogResource.saveResourceFile();
            }
        } else {
            // one-time run
            System.out.println("initializing...");
            Init.main(null);

            System.out.println("icons...");
            Init.initIcons();

            System.out.println("loading content...");
            load();

            Page.deleteFiles(new File("json"));

            System.out.println("writing json...");
            writeAll();
            
            // 初始化changelog需要手动 args: build <version> <output>[optional]
            System.out.println("saving changelog resource...");
            ChangelogResource.saveResourceFile();

            System.out.println("converting json into html...");
            Page.convertAll();


            // System.out.println("loading...");
            // load();
            
            // Page.deleteFiles(new File("json"));
            // System.out.println("writing json...");
            // writeAll();
    
            // System.out.println("converting json into html...");
            // Page.convertAll();
    
            // System.out.println("writing tag excel...");
            // Tools.writeExcel();
    
            // System.out.println("saving changelog resource...");
            // ChangelogResource.saveResourceFile();

            System.out.println("done");
        }
    }
}
    