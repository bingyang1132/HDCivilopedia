package changelog;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import tools.Tools;

public class Changelog {

    public static JSONObject resource = readResouceFile();
    public static JSONObject readResouceFile () {
        try {
            File file = new File ("ChangelogResouce.json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
            String text = reader.readLine();
            reader.close();
            return (JSONObject) JSON.parse(text);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String version;
    public String versionRegExp;
    public List<ChangelogItem> items = new ArrayList<>();
    public Map<String, Cat> cats = new HashMap<>();

    public Changelog (String version, String versionRegExp) {
        this.version = version;
        this.versionRegExp = versionRegExp;
    }

    public JSONObject toJsonObject (String version) {
        JSONObject object = new JSONObject();
        object.put("language", "zh_Hans");
        object.put("title", version + "更新日志");
        JSONArray left = new JSONArray();
        List<Cat> catList = new ArrayList<>();
        for (Cat cat : cats.values()) {
            catList.add(cat);
        }
        catList.sort(new Comparator<Cat>() {
            public int compare(Cat o1, Cat o2) {
                Cat c1 = (Cat) o1;
                Cat c2 = (Cat) o2;
                return c1.order == c2.order ? (Integer.compare(c1.absOrder, c2.absOrder)) : Integer.compare(c1.order, c2.order);
            };
        });
        for (int i = 0; i < catList.size(); i++) {
            Cat cat = catList.get(i);
            left.add(Tools.getHeader(cat.name));

            String s = "<ul>";
            List<Subcat> subcatList = new ArrayList<>();
            for (Subcat subcat : cat.subcats.values()) {
                subcatList.add(subcat);
            }
            subcatList.sort(new Comparator<Subcat>() {
                public int compare(Subcat o1, Subcat o2) {
                    Subcat c1 = (Subcat) o1;
                    Subcat c2 = (Subcat) o2;
                    return c1.order == c2.order ? (Integer.compare(c1.absOrder, c2.absOrder)) : Integer.compare(c1.order, c2.order);
                };
            });

            for (Subcat subcat : subcatList) {
                s += "<li>";
                //s += (j + 1) + ".";
                s += subcat.name + "<ul>";
                
                List<Subsubcat> subsubcatList = new ArrayList<>();
                for (Subsubcat subsubcat : subcat.subsubcats.values()) {
                    subsubcatList.add(subsubcat);
                }
                subsubcatList.sort(new Comparator<Subsubcat>() {
                    public int compare(Subsubcat o1, Subsubcat o2) {
                        Subsubcat c1 = (Subsubcat) o1;
                        Subsubcat c2 = (Subsubcat) o2;
                        return c1.order == c2.order ? (Integer.compare(c1.absOrder, c2.absOrder)) : Integer.compare(c1.order, c2.order);
                    };
                });

                for (Subsubcat subsubcat : subsubcatList) {
                    s += "<li>";
                    //s += (j + 1) + ".";
                    s += subsubcat.name + "<ul>";
                    for (ChangelogItem item : subsubcat.items) {
                        s += "<li>";
                        //s += "(" + (k + 1) + ").";
                        s += item.text + "</li>";
                    }
                    s += "</ul></li>";
                }
                for (ChangelogItem item : subcat.items) {
                    s += "<li>";
                    //s += "(" + (k + 1) + ").";
                    s += item.text + "</li>";
                }
                s += "</ul></li>";
            }
            for (ChangelogItem item : cat.items) {
                s += "<li>";
                //s += (j + 1) + ".";
                s += item.text + "</li>";
            }
            s += "</ul>";

            left.add(Tools.getBody("", s));
        }
        object.put("leftColumnItems", left);
        return object;
    }

    public void readFrom (File file, String mod) throws Exception {
        String author = file.getName().replaceAll(".txt", "");
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        String line;
        boolean on = false;
        int order = 0;
        Cat cat = null;
        Subcat subcat = null;
        Subsubcat subsubcat = null;
        while ((line = reader.readLine()) != null) {
            try {
                int comment = line.indexOf("//");
                if (comment != -1) {
                    line = line.substring(0, comment);
                }
                if (line.matches("\\s*")) {
                    continue;
                }
                if (line.startsWith("version ")) {
                    String vers = line.substring("version ".length());
                    on = vers.matches(versionRegExp);
                    continue;
                }
                if (!on) {
                    continue;
                }
                if (line.startsWith("order ")) {
                    order = Integer.parseInt(line.substring("order ".length()));
                    continue;
                }
                if (line.startsWith("cat ")) {
                    String c = line.substring("cat ".length());
                    int o = order;
                    if (c.startsWith("{")) {
                        o = Integer.parseInt(c.substring(1, c.indexOf("}")));
                        c = c.substring(c.indexOf("}") + 1);
                    }
                    if (cats.containsKey(c)) {
                        cat = cats.get(c);
                    } else {
                        cat = new Cat();
                        cat.name = c;
                        cats.put(c, cat);
                    }
                    if (o != 0) {
                        cat.order = o;
                    }
                    continue;
                }
                if (line.startsWith("subcat ")) {
                    String sc = line.substring("subcat ".length());
                    int o = order;
                    if (sc.startsWith("{")) {
                        o = Integer.parseInt(sc.substring(1, sc.indexOf("}")));
                        sc = sc.substring(sc.indexOf("}") + 1);
                    }
                    if (cat.subcats.containsKey(sc)) {
                        subcat = cat.subcats.get(sc);
                    } else {
                        subcat = new Subcat();
                        subcat.name = sc;
                        cat.subcats.put(sc, subcat);
                    }
                    if (o != 0) {
                        subcat.order = o;
                    }
                    continue;
                }
                if (line.startsWith("subsubcat ")) {
                    String ssc = line.substring("subsubcat ".length());
                    int o = order;
                    if (ssc.startsWith("{")) {
                        o = Integer.parseInt(ssc.substring(1, ssc.indexOf("}")));
                        ssc = ssc.substring(ssc.indexOf("}") + 1);
                    }
                    if (subcat.subsubcats.containsKey(ssc)) {
                        subsubcat = subcat.subsubcats.get(ssc);
                    } else {
                        subsubcat = new Subsubcat();
                        subsubcat.name = ssc;
                        subcat.subsubcats.put(ssc, subsubcat);
                    }
                    if (o != 0) {
                        subsubcat.order = o;
                    }
                    continue;
                }
                if (line.startsWith("end ")) {
                    String endItem = line.substring("end ".length());
                    if (endItem.equals("order")) {
                        order = 0;
                    }
                    if (endItem.equals("cat")) {
                        cat = null;
                    }
                    if (endItem.equals("subcat")) {
                        subcat = null;
                    }
                    if (endItem.equals("subsubcat")) {
                        subsubcat = null;
                    }
                    continue;
                }

                int rorder = order;
                Cat rcat = cat;
                Subcat rsubcat = subcat;
                Subsubcat rsubsubcat = subsubcat;
                String[] exps;

                if (line.startsWith("{")) {
                    exps = line.substring(1, line.indexOf("}")).split(",");
                    line = line.substring(line.indexOf("}") + 1);
                } else {
                    exps = new String[0];
                }

                JSONObject firstReplace = null;
                int l = 0;
                int r = -1;
                String s1 = "";
                while ((l = line.indexOf("[", r)) != -1) {
                    s1 += line.substring(r + 1, l);
                    r = line.indexOf("]", l);
                    String item = line.substring(l + 1, r);
                    if (resource.containsKey(item)) {
                        JSONObject replace = resource.getJSONObject(item);
                        s1 += replace.getString(mod);
                        if (firstReplace == null && replace.containsKey("defaultCat")) {
                            firstReplace = replace;
                        }
                    } else {
                        System.out.println("Warning : Can't analyze [" + item + "] in " + file.getName() + ".");
                        s1 += item;
                    }
                }
                s1 += line.substring(r + 1, line.length());
                line = s1;
                
                for (String exp : exps) {
                    String[] spl = exp.split("=");
                    if (spl[0].equals("order")) {
                        rorder = Integer.parseInt(spl[1]);
                    }
                    if (spl[0].equals("cat")) {
                        if (!cats.containsKey(spl[1])) {
                            rcat = new Cat();
                            rcat.name = spl[1];
                            cats.put(spl[1], rcat);
                        } else {
                            rcat = cats.get(spl[1]);
                        }
                    }
                }
                if (rcat == null) {
                    String catString = "其它改动";
                    int catOrder = 1000;
                    if (firstReplace != null) {
                        catString = firstReplace.getString("defaultCat");
                        catOrder = firstReplace.getInteger("defaultCatOrder");
                    }
                    if (cats.containsKey(catString)) {
                        rcat = cats.get(catString);
                        if (rcat.order == 0) {
                            rcat.order = catOrder;
                        }
                    } else {
                        rcat = new Cat();
                        rcat.name = catString;
                        rcat.order = catOrder;
                        cats.put(catString, rcat);
                    }
                }
                for (String exp : exps) {
                    String[] spl = exp.split("=");
                    if (spl[0].equals("subcat")) {
                        if (!rcat.subcats.containsKey(spl[1])) {
                            rsubcat = new Subcat();
                            rsubcat.name = spl[1];
                            rcat.subcats.put(spl[1], rsubcat);
                        } else {
                            rsubcat = rcat.subcats.get(spl[1]);
                        }
                    }
                }
                for (String exp : exps) {
                    String[] spl = exp.split("=");
                    if (spl[0].equals("subsubcat")) {
                        if (!rsubcat.subsubcats.containsKey(spl[1])) {
                            rsubsubcat = new Subsubcat();
                            rsubsubcat.name = spl[1];
                            rsubcat.subsubcats.put(spl[1], rsubsubcat);
                        } else {
                            rsubsubcat = rsubcat.subsubcats.get(spl[1]);
                        }
                    }
                }
                ChangelogItem item = new ChangelogItem();
                item.author = author;
                item.order = rorder;
                item.cat = rcat.name;
                if (rsubcat != null) {
                    item.subcat = rsubcat.name;
                }
                if (rsubsubcat != null) {
                    item.subsubcat = rsubsubcat.name;
                }
                item.text = line;

                if (rsubsubcat != null) {
                    rsubsubcat.items.add(item);
                } else if (rsubcat != null) {
                    rsubcat.items.add(item);
                } else {
                    rcat.items.add(item);
                }
                items.add(item);
            } catch (Exception e) {
                System.out.println(line);
                e.printStackTrace();
            }
        }
        reader.close();
    }

    //public static String[] chNum = new String[]{"一", "二", "三", "四", "五", "六", "七", "八", "九", "十", "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十", "二十一", "二十二", "二十三", "二十四", "二十五", "二十六", "二十七", "二十八", "二十九", "三十"};
    public String toHtml () {
        String s = "";
        s += "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><title>" + version + " Changelog 预览</title></head><body style=\"width:100%;display:flex;justify-content:center;\"><div style=\"max-width:700px;\">";
        List<Cat> catList = new ArrayList<>();
        for (Cat cat : cats.values()) {
            catList.add(cat);
        }
        catList.sort(new Comparator<Cat>() {
            public int compare(Cat o1, Cat o2) {
                Cat c1 = (Cat) o1;
                Cat c2 = (Cat) o2;
                return c1.order == c2.order ? (Integer.compare(c1.absOrder, c2.absOrder)) : Integer.compare(c1.order, c2.order);
            };
        });
        for (int i = 0; i < catList.size(); i++) {
            Cat cat = catList.get(i);
            s += "<h3>";
            //s += chNum[i] + "、";
            s += cat.name + "</h3><ul>";

            List<Subcat> subcatList = new ArrayList<>();
            for (Subcat subcat : cat.subcats.values()) {
                subcatList.add(subcat);
            }
            subcatList.sort(new Comparator<Subcat>() {
                public int compare(Subcat o1, Subcat o2) {
                    Subcat c1 = (Subcat) o1;
                    Subcat c2 = (Subcat) o2;
                    return c1.order == c2.order ? (Integer.compare(c1.absOrder, c2.absOrder)) : Integer.compare(c1.order, c2.order);
                };
            });

            int j = 0;
            for (; j < subcatList.size(); j++) {
                Subcat subcat = subcatList.get(j);
                s += "<li>";
                //s += (j + 1) + ".";
                s += subcat.name + "<ul>";
                
                List<Subsubcat> subsubcatList = new ArrayList<>();
                for (Subsubcat subsubcat : subcat.subsubcats.values()) {
                    subsubcatList.add(subsubcat);
                }
                subsubcatList.sort(new Comparator<Subsubcat>() {
                    public int compare(Subsubcat o1, Subsubcat o2) {
                        Subsubcat c1 = (Subsubcat) o1;
                        Subsubcat c2 = (Subsubcat) o2;
                        return c1.order == c2.order ? (Integer.compare(c1.absOrder, c2.absOrder)) : Integer.compare(c1.order, c2.order);
                    };
                });

                int k = 0;
                for (; k < subsubcatList.size(); k++) {
                    Subsubcat subsubcat = subsubcatList.get(k);
                    s += "<li>";
                    //s += (j + 1) + ".";
                    s += subsubcat.name + "<ul>";
                    for (ChangelogItem item : subsubcat.items) {
                        s += "<li>";
                        //s += "(" + (k + 1) + ").";
                        s += item.text + "</li>";
                        j ++;
                    }
                    s += "</ul></li>";
                }
                for (ChangelogItem item : subcat.items) {
                    s += "<li>";
                    //s += "(" + (k + 1) + ").";
                    s += item.text + "</li>";
                    k ++;
                }
                s += "</ul></li>";
            }
            for (ChangelogItem item : cat.items) {
                s += "<li>";
                //s += (j + 1) + ".";
                s += item.text + "</li>";
                j ++;
            }
            s += "</ul>";
        }
        s += "</div></body></html>";
        return s;
    }

    public void writeHtml () throws Exception {
        String fileName = version + ".html";
        File file = new File(fileName);
        if (file.exists()) {
            file.delete();
        }
        file.createNewFile();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
        writer.write(toHtml());
        writer.flush();
        writer.close();
    }

    public static void buildChangelog (String version) throws Exception {
        Changelog changelog = new Changelog(version, version + "\\S*");
        File logs = new File(new File("").getAbsolutePath());
        for (File f : logs.listFiles()) {
            if (!f.getName().endsWith(".txt") || f.getName().equals("changelog_old.txt")) continue;
            changelog.readFrom(f, "abs");
        }
        changelog.writeHtml();
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("please specify version.");
        } else {
            buildChangelog(args[0]);
        }
    }
}

class ChangelogItem {
    @Override
    public java.lang.String toString() {
        return "author = " + author + ", cat = " + cat + ", subcat = " + subcat + ", subsubcat = " + subsubcat + ", order = " + order + "\n" + text;
    }
    String text;
    String author;
    String cat;
    String subcat;
    String subsubcat;
    int order;
}

class Cat {
    String name;
    int order;
    Map<String, Subcat> subcats = new HashMap<>();
    List<ChangelogItem> items = new ArrayList<>();
    static int o = 0;
    int absOrder;
    Cat () {
        absOrder = o++;
    }
}

class Subcat {
    String name;
    int order;
    Map<String, Subsubcat> subsubcats = new HashMap<>();
    List<ChangelogItem> items = new ArrayList<>();

    static int o = 0;
    int absOrder;
    Subcat () {
        absOrder = o++;
    }
}

class Subsubcat {
    String name;
    int order;
    List<ChangelogItem> items = new ArrayList<>();

    static int o = 0;
    int absOrder;
    Subsubcat () {
        absOrder = o++;
    }
}