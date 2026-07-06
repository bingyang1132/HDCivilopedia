package model;

import java.awt.image.BufferedImage;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import model.abstracts.WritableWithIcon;
import tools.ImageEditor;
import tools.Tools;

public class GreatPerson extends WritableWithIcon {
    
    public static final Map<String, GreatPerson> greatpeople = new HashMap<>();

    public String era;
    public String cla;
    public int actionCharges;
    public String activeNameOverride;
    public String activeDecriptionOverride;
    public String passiveNameOverride;
    public String passiveDecriptionOverride;

    public List<Modifier> activeModifiers = new ArrayList<>();
    public List<Modifier> passiveModifiers = new ArrayList<>();
    public List<GreatWork> greatWorks = new ArrayList<>();

    static class Modifier {
        String tag;
        String text;
    }

    static class GreatWork {
        String tag;
        String type;
        String name;
        String typeName;
        String icon;
    }

    public GreatPerson (String tag) {
        super(tag);
        greatpeople.put(tag, this);
    }

    // load great people
    public static void load () {
        Statement gameplay = null;
        try {
            gameplay = DriverManager.getConnection(Tools.GAMEPLAY_DATABASE).createStatement();
    
            // load great people list
            ResultSet r1 = gameplay.executeQuery("select * from GreatPersonIndividuals where GreatPersonClassType != \"GREAT_PERSON_CLASS_PRODUCT\";");
            while (r1.next()) {
                String tag = r1.getString("GreatPersonIndividualType");
                GreatPerson greatPerson = new GreatPerson(tag);
                greatPerson.name = r1.getString("Name");
                greatPerson.era = r1.getString("EraType");
                greatPerson.cla = r1.getString("GreatPersonClassType");
                greatPerson.actionCharges = r1.getInt("ActionCharges");
                greatPerson.activeNameOverride = r1.getString("ActionNameTextOverride");
                greatPerson.activeDecriptionOverride = r1.getString("ActionEffectTextOverride");
                greatPerson.passiveNameOverride = r1.getString("BirthNameTextOverride");
                greatPerson.passiveDecriptionOverride = r1.getString("BirthEffectTextOverride");
            }
    
            // load other information
            for(Entry<String, GreatPerson> entry : greatpeople.entrySet()) {
                String tag = entry.getKey();
                GreatPerson person = entry.getValue();

                // load active abilities
                ResultSet r2 = gameplay.executeQuery("select * from GreatPersonIndividualActionModifiers where GreatPersonIndividualType = \"" + tag + "\";");
                while (r2.next()) {
                    Modifier modifier = new Modifier();
                    modifier.tag = r2.getString("ModifierId");
                    person.activeModifiers.add(modifier);
                }

                Set<Modifier> remove = new HashSet<>();
                for (Modifier modifier : person.activeModifiers) {
                    ResultSet r3 = gameplay.executeQuery("select * from ModifierStrings where ModifierId = \"" + modifier.tag + "\" and Context = \"Summary\";");
                    if (r3.next()) {
                        modifier.text = r3.getString("Text");
                    }
                    if (modifier.text == null) {
                        remove.add(modifier);
                    }
                }
                person.activeModifiers.removeAll(remove);

                remove = new HashSet<>();
                // load passive abilities
                ResultSet r4 = gameplay.executeQuery("select * from GreatPersonIndividualBirthModifiers where GreatPersonIndividualType = \"" + tag + "\";");
                while (r4.next()) {
                    Modifier modifier = new Modifier();
                    modifier.tag = r4.getString("ModifierId");
                    person.passiveModifiers.add(modifier);
                }

                for (Modifier modifier : person.passiveModifiers) {
                    ResultSet r5 = gameplay.executeQuery("select * from ModifierStrings where ModifierId = \"" + modifier.tag + "\" and Context = \"Summary\";");
                    if (r5.next()) {
                        modifier.text = r5.getString("Text");
                    }
                    if (modifier.text == null) {
                        remove.add(modifier);
                    }
                }
                person.passiveModifiers.removeAll(remove);

                // load great works
                ResultSet r6 = gameplay.executeQuery("select * from GreatWorks where GreatPersonIndividualType = \"" + tag + "\";");
                while (r6.next()) {
                    GreatWork work = new GreatWork();
                    work.tag = r6.getString("GreatWorkType");
                    work.type = r6.getString("GreatWorkObjectType");
                    work.name = r6.getString("Name");
                    person.greatWorks.add(work);
                }

                for (GreatWork work : person.greatWorks) {
                    ResultSet r7 = gameplay.executeQuery("select * from GreatWorkObjectTypes where GreatWorkObjectType = \"" + work.type + "\";");
                    if (r7.next()) {
                        work.typeName = r7.getString("Name");
                        work.icon = r7.getString("IconString");
                    }
                }

                // load great person icon
                String iconString = "ICON_" + tag;
                BufferedImage icon = Tools.getImage(iconString);
                if (icon != null) {
                    String path = iconString + ".png";
                    person.icon = Tools.IMAGE_URL + "/" + path;
                    ImageEditor.saveImage(icon, path);
                }
                
            }
        } catch (Exception e) {
            System.err.println("Error loading great people.");
            System.err.println(e.getClass().getName() + " " + e.getMessage());
        } finally {
            try {
                if(gameplay != null) gameplay.close();
            } catch (Exception e) {
            }
        }
    }

    @Override
    public JSONObject toJson(String language) {
        JSONObject object = super.toJson(language);

        JSONArray leftColumnItems = new JSONArray();
        object.put("leftColumnItems", leftColumnItems);

        JSONArray rightColumnItems = new JSONArray();
        JSONArray contents = new JSONArray();
        rightColumnItems.add(Tools.getStatbox(Tools.getControlText("Traits", language), contents));
        object.put("rightColumnItems", rightColumnItems);

        contents.add(Tools.getSeparator());
        contents.add(Tools.getLabel(Tools.getText("LOC_" + era + "_NAME", language)));
        contents.add(Tools.getLabel(Tools.getText("LOC_" + cla + "_NAME", language)));

        if (greatWorks.size() > 0) {
            leftColumnItems.add(Tools.getHeader(Tools.getText("LOC_UI_PEDIA_GREAT_WORKS", language)));
            String text = "";
            for (GreatWork work : greatWorks) {
                text += "[ICON_BULLET]" + work.icon + " " + Tools.getText(work.typeName, language) + Tools.getControlText(": ", language) + Tools.getText(work.name, language) + "[NEWLINE]";
            }
            text += Tools.getText("LOC_GREATPERSON_ACTION_USAGE_CREATE_GREAT_WORK", language);
            leftColumnItems.add(Tools.getBody("", text));
        }

        boolean hasActive = activeModifiers.size() > 0 || activeDecriptionOverride != null;
        boolean hasPassive = passiveModifiers.size() > 0 || passiveDecriptionOverride != null;

        if (cla.equals("GREAT_PERSON_CLASS_PROPHET") || hasActive || hasPassive) {
            leftColumnItems.add(Tools.getHeader(Tools.getText("LOC_UI_PEDIA_UNIQUE_ABILITY", language)));
            if (cla.equals("GREAT_PERSON_CLASS_PROPHET")) {
                leftColumnItems.add(Tools.getBody("", Tools.getText("LOC_GREATPERSON_ACTION_USAGE_FOUND_RELIGION", language)));
            }

            if (hasActive) {
                String action = null;
                if (activeNameOverride != null) {
                    action = Tools.getText(activeNameOverride, language);
                }
                if (action == null) {
                    action = Tools.getText("LOC_GREATPERSON_ACTION_NAME_DEFAULT", language);
                }
                String title = action + Tools.getControlText(" (", language) + actionCharges + " " + Tools.getControlText("charge", language);
                if (actionCharges >= 2 && language.equals("en_US")) {
                    title += "s";
                }
                title += Tools.getControlText(") ", language);

                String text = null;
                if (activeDecriptionOverride != null) {
                    text = Tools.getText(activeDecriptionOverride, language);
                }
                if (text == null) {
                    text = "";
                    for (Modifier modifier : activeModifiers) {
                        text += Tools.getText(modifier.text, language, modifier.tag) + "[NEWLINE]";
                    }
                }
                leftColumnItems.add(Tools.getBody(title, text));
            }

            if (hasPassive) {
                String title = null;
                if (passiveNameOverride != null) {
                    title = Tools.getText(passiveNameOverride, language);
                }
                if (title == null) {
                    title = Tools.getText("LOC_GREATPERSON_PASSIVE_NAME_DEFAULT", language);
                }

                String text = null;
                if (passiveDecriptionOverride != null) {
                    text = Tools.getText(passiveDecriptionOverride, language);
                }
                if (text == null) {
                    text = "";
                    for (Modifier modifier : passiveModifiers) {
                        text += Tools.getText(modifier.text, language, modifier.tag) + "[NEWLINE]";
                    }
                }
                leftColumnItems.add(Tools.getBody(title, text));
            }
        }

        // 历史背景 from wikipedia (需求2)
        String wikiHistory = Tools.getWikiHistory(tag, language);
        if (wikiHistory != null) {
            leftColumnItems.add(Tools.getHeader(Tools.getControlText("History", language)));
            leftColumnItems.add(Tools.getBody(null, wikiHistory));
        }

        return object;
    }

    @Override
    public int getOrder() {
        Statement gameplay = null;
        try {
            gameplay = DriverManager.getConnection(Tools.GAMEPLAY_DATABASE).createStatement();
            ResultSet r = gameplay.executeQuery("select * from Eras where EraType = \"" + era + "\";");
            return r.getInt("ChronologyIndex");
        } catch (Exception e) {
        } finally {
            if (gameplay != null) {
                try {
                    gameplay.close();
                } catch (Exception e) {
                }
            }
        }
        return super.getOrder();
    }

    @Override
    public String getChapter() {
        return "greatpeople";
    }

    @Override
    public String getTagPrefix() {
        return "GREAT_PERSON_INDIVIDUAL_";
    }

    @Override
    public String getFolder() {
        return cla;
    }

    @Override
    public String getFolderName(String language) {
		if (cla.equals("GREAT_PERSON_CLASS_KEQING")) {
			return Tools.getText("LOC_UNIT_KEQING_NAME", language);
		}
        return Tools.getText("LOC_" + cla + "_NAME", language);
    }

    @Override
    public String getCat() {
        return "伟人改动";
    }
    
    @Override
    public int getCatOrder() {
        return -1500;
    }
}
