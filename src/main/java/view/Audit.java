package view;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import tools.Constants;
import tools.Tools;

/**
 * Post-build sanity report.
 *
 * Nothing in this pipeline fails loudly: every layer catches and continues, so defects show up
 * as missing or stale output rather than a broken build. Three classes of them had been piling
 * up unnoticed for a long time — 1570 orphaned android pages, 256 icons that decoded fine but
 * were never wired to a page, 31 dead entries in a hand-maintained path list. None of it was
 * reported by anything; it all had to be dug out of the artifacts by hand.
 *
 * So this measures the artifacts after every run and compares against a committed baseline,
 * flagging metrics that got worse. Absolute numbers alone would be noise — several of these are
 * non-zero for reasons we understand and accept (see docs/known-issues.md); what matters is
 * whether they move in the wrong direction.
 *
 * {@code Main audit} runs it standalone, {@code Main audit save} rewrites the baseline.
 */
public class Audit {

    public static final File BASELINE = new File("manual/audit-baseline.json");

    /** Metrics where a higher number is worse. Anything not listed is informational. */
    private static final List<String> LOWER_IS_BETTER = java.util.Arrays.asList(
            "ddsFoldersDead", "iconlabelsNoSrc", "iconlabelTagsNoSrc", "iconlabelsNoSrcButPngExists",
            "androidPagesUnexpected", "searchEntriesNoIcon");

    public static void run (boolean save) throws Exception {
        Map<String, Integer> now = collect();

        JSONObject baseline = null;
        if (BASELINE.exists()) {
            baseline = Tools.readJSON(BASELINE);
        }

        System.out.println();
        System.out.println("[AUDIT] ------------------------------------------------------------");
        boolean worse = false;
        for (Entry<String, Integer> metric : now.entrySet()) {
            String key = metric.getKey();
            int value = metric.getValue();
            String note = "";
            if (baseline != null && baseline.containsKey(key)) {
                int was = baseline.getIntValue(key);
                int delta = value - was;
                if (delta != 0) {
                    boolean bad = LOWER_IS_BETTER.contains(key) == (delta > 0);
                    note = String.format("  %+d vs baseline %s", delta, bad ? "<== WORSE" : "(better)");
                    worse |= bad;
                }
            } else if (baseline != null) {
                note = "  (new metric)";
            }
            System.out.printf("[AUDIT] %-32s %8d%s%n", key, value, note);
        }
        if (baseline == null) {
            System.out.println("[AUDIT] no baseline yet - run `Main audit save` to record these numbers");
        } else if (worse) {
            System.out.println("[AUDIT] something regressed. See docs/known-issues.md for what each metric means.");
        }
        System.out.println("[AUDIT] ------------------------------------------------------------");

        if (save) {
            JSONObject out = new JSONObject(true);
            out.putAll(now);
            Page.writeTextToFile(JSON.toJSONString(out, true), BASELINE);
            System.out.println("[AUDIT] baseline written to " + BASELINE.getPath());
        }
    }

    private static Map<String, Integer> collect () throws Exception {
        Map<String, Integer> m = new LinkedHashMap<>();

        // 1. the hand-maintained texture path list rots whenever a mod folder is renamed, and a
        //    dead entry costs a whole atlas worth of icons without saying anything
        int dead = 0;
        for (String folder : Constants.DDS_FOLDERS) {
            if (!new File(folder).isDirectory()) {
                dead++;
            }
        }
        m.put("ddsFoldersDead", dead);
        m.put("ddsFoldersTotal", Constants.DDS_FOLDERS.size());

        // 2. an iconlabel carries the tag in `alt` but only renders when `src` is set, and `src`
        //    is only set when the DDS decoded at load time. A tag whose png is already sitting in
        //    output/icons but has no src is always a bug: the art exists and nothing points at it.
        //    Counted per rendered row, so an item cross-listed under several folders counts once
        //    per listing; `iconlabelTagsNoSrc` is the distinct-tag view of the same thing. Note
        //    fastjson writes a repeated object as {"$ref": ...} and resolves it on parse, so a
        //    reader that does not follow $ref undercounts these badly.
        int noSrc = 0;
        int noSrcButPng = 0;
        java.util.Set<String> distinct = new java.util.HashSet<>();
        for (String language : Page.LANGUAGES) {
            for (String chapter : Page.HEADERS) {
                File contents = new File("json/" + language + "/" + chapter + "/contents.json");
                if (!contents.exists()) {
                    continue;
                }
                List<String> alts = new ArrayList<>();
                collectIconlabelsWithoutSrc(Tools.readJSON(contents), alts);
                noSrc += alts.size();
                for (String alt : alts) {
                    distinct.add(alt);
                    if (new File("output/icons/" + alt + ".png").exists()) {
                        noSrcButPng++;
                    }
                }
            }
        }
        m.put("iconlabelsNoSrc", noSrc);
        m.put("iconlabelTagsNoSrc", distinct.size());
        m.put("iconlabelsNoSrcButPngExists", noSrcButPng);

        // 3. output/ is cleared every run and output_android/ mirrors it, so the two trees should
        //    differ only by android's extra toc.html per chapter plus its start page
        int desktop = countHtml(new File("output"));
        int android = countHtml(new File("output_android"));
        int expected = Page.LANGUAGES.length * (Page.HEADERS.length + 1);
        m.put("pagesDesktop", desktop);
        m.put("pagesAndroid", android);
        m.put("androidPagesUnexpected", Math.abs(android - desktop - expected));

        // 4. the user-visible version of metric 2: how many search hits come up with no icon
        int noIcon = 0;
        int total = 0;
        for (String language : Page.LANGUAGES) {
            File data = new File("output/" + language + "/search-data.js");
            if (!data.exists()) {
                continue;
            }
            String js = Tools.readFromFile(data);
            int eq = js.indexOf('=');
            int end = js.lastIndexOf(';');
            if (eq < 0 || end <= eq) {
                continue;
            }
            JSONArray entries = JSON.parseArray(js.substring(eq + 1, end));
            for (Object o : entries) {
                total++;
                String icon = ((JSONObject) o).getString("i");
                if (icon == null || icon.isEmpty()) {
                    noIcon++;
                }
            }
        }
        m.put("searchEntriesNoIcon", noIcon);
        m.put("searchEntriesTotal", total);

        return m;
    }

    private static void collectIconlabelsWithoutSrc (Object node, List<String> alts) {
        if (node instanceof JSONObject) {
            JSONObject object = (JSONObject) node;
            JSONObject iconlabel = object.getJSONObject("iconlabel");
            if (iconlabel != null) {
                String src = iconlabel.getString("src");
                String alt = iconlabel.getString("alt");
                if ((src == null || src.isEmpty()) && alt != null && alt.startsWith("ICON_")) {
                    alts.add(alt);
                }
            }
            for (Object value : object.values()) {
                collectIconlabelsWithoutSrc(value, alts);
            }
        } else if (node instanceof JSONArray) {
            for (Object value : (JSONArray) node) {
                collectIconlabelsWithoutSrc(value, alts);
            }
        }
    }

    private static int countHtml (File folder) {
        if (!folder.isDirectory()) {
            return 0;
        }
        File[] children = folder.listFiles();
        if (children == null) {
            return 0;
        }
        int count = 0;
        for (File child : children) {
            if (child.isDirectory()) {
                count += countHtml(child);
            } else if (child.getName().endsWith(".html")) {
                count++;
            }
        }
        return count;
    }
}
