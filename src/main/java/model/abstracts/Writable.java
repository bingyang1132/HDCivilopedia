package model.abstracts;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import tools.Tools;

public abstract class Writable {
    
    public String tag;
    public String name;

    public Writable (String tag) {
        this.tag = tag;
        Main.WRITABLES.add(this);
    }

    public abstract String getChapter ();
    public abstract String getTagPrefix ();
    public abstract String getFolder ();
    public abstract String getFolderName (String language);
    public abstract String getCat ();
    public abstract int getCatOrder ();
    
    public String getTitle (String language) {
        String title = Tools.getTextWithAlt(name, language);
        if (language.equals("zh_Hans_CN")) {
            Main.WRITABLES_ZHINDEX.put(title, this);
        }
        return title;
    }
    
    public String getLink (String language) {
        return Tools.LINK_URL + "/" + language + "/" + getChapter() + "/" + getFolder() + "/" + getEnglishTitle() + ".html";
    }

    public String getAbsLink (String language) {
        return "https://civ6hd.com/" + language + "/" + getChapter() + "/" + getFolder() + "/" + getEnglishTitle() + ".html";
    }
    
    public String getEnglishTitle () {
        //return tag.substring(getTagPrefix().length(), tag.length()).toLowerCase();
        return tag;
    }
    
    public String getLinkedTitle (String language) {
        String title = getTitle(language);
        String link = getLink(language);
        return "<a href=\"" + link + "\">" + title + "</a>";
    }

    public JSONObject toJson (String language) {
        JSONObject object = new JSONObject();
        object.put("title", getTitle(language));
        object.put("language", language);
        return object;
    }

    public int getOrder () {
        return 0;
    }

    public int getFolderOrder () {
        return 0;
    }

    static final List<String> STRINGS_TO_REMOVE = Arrays.asList(
        " [ICON_Damaged] ",
        " [ICON_Barbarian] ",
        " [ICON_GreatWork_Product] "
    );

    static Object cleanJson(Object obj) {
        if (obj instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) obj;
            JSONObject cleaned = new JSONObject();
            for (String key : jsonObject.keySet()) {
                cleaned.put(key, cleanJson(jsonObject.get(key)));
            }
            return cleaned;
        } else if (obj instanceof JSONArray) {
            JSONArray array = (JSONArray) obj;
            JSONArray cleanedArray = new JSONArray();
            for (Object item : array) {
                cleanedArray.add(cleanJson(item));
            }
            return cleanedArray;
        } else if (obj instanceof String) {
            String str = (String) obj;
            for (String bad : STRINGS_TO_REMOVE) {
                str = str.replace(bad, "");
            }
            return str;
        } else {
            return obj;
        }
    }

    
    public void writeJSON (String language) {
        try {
            File chapterFolder = new File("json/" + language + "/" + getChapter());
            if (!chapterFolder.exists()) {
                chapterFolder.mkdirs();
            }
            File contentsFile = new File(chapterFolder, "contents.json");
            JSONObject contents;
            if (contentsFile.exists()) {
                contents = Tools.readJSON(contentsFile);
            } else {
                contents = new JSONObject();
                contents.put("path", language + "/" + getChapter());
                contents.put("name", Tools.getControlText(getChapter(), language));
                contents.put("folders", new JSONArray());
            }
            JSONArray folders = contents.getJSONArray("folders");

            File subFolder = new File(chapterFolder, getFolder());
            if (!subFolder.exists()) {
                subFolder.mkdirs();
            }
            JSONObject folderObject = null;
            for (Object f : folders) {
                JSONObject folder = (JSONObject) f;
                if (getFolder().equals(folder.getString("path"))) {
                    folderObject = folder;
                    break;
                }
            }
            if (folderObject == null) {
                folderObject = new JSONObject();
                folders.add(folderObject);
                folderObject.put("path", getFolder());
                folderObject.put("name", getFolderName(language));
                folderObject.put("files", new JSONArray());
                folderObject.put("order", getFolderOrder());
            }

            File target = new File (subFolder, getEnglishTitle() + ".json");
            // Tools.writeJson(toJson(language), target);
            Tools.writeJson((JSONObject) cleanJson(toJson(language)), target);

            JSONObject fileObject = new JSONObject();
            folderObject.getJSONArray("files").add(fileObject);
            fileObject.put("name", getTitle(language));
            fileObject.put("path", getEnglishTitle() + ".json");
            fileObject.put("order", getOrder());
            if (this instanceof WritableWithIcon) {
                fileObject.put("iconlabel", ((WritableWithIcon) this).getIconLabel(language));
            }

            Tools.writeJson(contents, contentsFile);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error writing jsons.");
            System.err.println(e.getClass().getName() + " " + e.getMessage());
        }
    }

}
