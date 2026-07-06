package tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import model.Civilization;
import model.GreatPerson;
import model.Leader;
import model.Wonder;
import model.abstracts.Writable;

/**
 * Fetches a short real-world history blurb for civilizations, leaders, great people and
 * wonders from Wikipedia (需求2) and caches one file per entity per language under
 * manual/wiki/{language}/{TAG}.json ({title, url, text}). The models read those files at
 * generation time and show them as a 历史背景 section. Run via: Main "wiki" [limitPerType].
 *
 * It resolves each entity by its localized display name (auto-match). Entries that 404 or
 * hit a disambiguation page are written to manual/wiki/_failures.tsv for manual fixing;
 * already-fetched files are skipped, so the command is resumable and re-runnable.
 */
public class WikiFetcher {

    static final String UA = "HDCivilopedia/1.0 (https://civ6hd.com; wiki history import)";
    static final String[] LANGUAGES = { "zh_Hans_CN", "en_US" };

    static String wikiLang(String language) {
        return language.startsWith("zh") ? "zh" : "en";
    }

    public static void fetchAll(int limitPerType) {
        List<Writable> targets = new ArrayList<>();
        addAll(targets, Civilization.civilizations.values(), limitPerType);
        addAll(targets, Leader.leaders.values(), limitPerType);
        addAll(targets, GreatPerson.greatpeople.values(), limitPerType);
        addAll(targets, Wonder.wonders.values(), limitPerType);

        // manual corrections for entries that auto-matching missed (see _failures.tsv);
        // key = "language\ttag", value = exact wikipedia article title to use
        Map<String, String> overrides = readOverrides();

        List<String> failures = new ArrayList<>();
        int ok = 0, skip = 0, fail = 0;
        for (String language : LANGUAGES) {
            String wl = wikiLang(language);
            for (Writable w : targets) {
                File out = new File("manual/wiki/" + language + "/" + w.tag + ".json");
                String override = overrides.get(language + "\t" + w.tag);
                // already fetched and no correction pending -> skip (resumable)
                if (out.exists() && override == null) {
                    skip++;
                    continue;
                }
                String name = cleanName(w.getTitle(language));
                if ((override == null || override.isEmpty()) && name.isEmpty()) {
                    failures.add(language + "\t" + w.tag + "\t(no name)");
                    fail++;
                    continue;
                }
                JSONObject result = (override != null && !override.isEmpty())
                        ? summary(wl, override)
                        : fetch(wl, name);
                sleep(120);
                if (result == null) {
                    failures.add(language + "\t" + w.tag + "\t" + (override != null && !override.isEmpty() ? override : name));
                    fail++;
                    continue;
                }
                writeJson(result, out);
                ok++;
            }
        }
        writeFailures(failures);
        System.out.println("wiki fetch done: ok=" + ok + " skipped=" + skip + " failed=" + fail
                + " (see manual/wiki/_failures.tsv)");
    }

    static void addAll(List<Writable> targets, java.util.Collection<? extends Writable> src, int limit) {
        int n = 0;
        for (Writable w : src) {
            if (limit > 0 && n >= limit) {
                break;
            }
            targets.add(w);
            n++;
        }
    }

    static String cleanName(String s) {
        if (s == null) {
            return "";
        }
        return s.replaceAll("\\[[^\\]]*\\]", "").replaceAll("\\s+", " ").trim();
    }

    // resolve via REST summary on the name; on miss/disambiguation, search then summary
    static JSONObject fetch(String wl, String name) {
        JSONObject r = summary(wl, name);
        if (r != null) {
            return r;
        }
        String best = searchTitle(wl, name);
        if (best != null && !best.equals(name)) {
            r = summary(wl, best);
            if (r != null) {
                return r;
            }
        }
        return null;
    }

    // zh.wikipedia serves mixed/traditional text by default; request the Simplified variant
    static String acceptLang(String wl) {
        return "zh".equals(wl) ? "zh-hans" : null;
    }

    static JSONObject summary(String wl, String title) {
        try {
            JSONObject d = getJson("https://" + wl + ".wikipedia.org/api/rest_v1/page/summary/" + enc(title), acceptLang(wl));
            if (d == null || "disambiguation".equals(d.getString("type"))) {
                return null;
            }
            String extract = d.getString("extract");
            if (extract == null || extract.trim().isEmpty()) {
                return null;
            }
            JSONObject out = new JSONObject(true);
            out.put("title", d.getString("title"));
            JSONObject cu = d.getJSONObject("content_urls");
            if (cu != null && cu.getJSONObject("desktop") != null) {
                out.put("url", cu.getJSONObject("desktop").getString("page"));
            }
            out.put("text", extract.trim());
            return out;
        } catch (Exception e) {
            return null;
        }
    }

    static String searchTitle(String wl, String name) {
        try {
            JSONObject d = getJson("https://" + wl + ".wikipedia.org/w/api.php?action=query&list=search&srsearch="
                    + enc(name) + "&srlimit=1&format=json", acceptLang(wl));
            if (d == null) {
                return null;
            }
            JSONArray hits = d.getJSONObject("query").getJSONArray("search");
            if (hits.isEmpty()) {
                return null;
            }
            return hits.getJSONObject(0).getString("title");
        } catch (Exception e) {
            return null;
        }
    }

    static JSONObject getJson(String urlStr, String acceptLang) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
        conn.setRequestProperty("User-Agent", UA);
        conn.setRequestProperty("Accept", "application/json");
        if (acceptLang != null) {
            conn.setRequestProperty("Accept-Language", acceptLang);
        }
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(15000);
        if (conn.getResponseCode() != 200) {
            conn.disconnect();
            return null;
        }
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        }
        conn.disconnect();
        return JSON.parseObject(sb.toString());
    }

    static String enc(String s) throws IOException {
        return URLEncoder.encode(s, "UTF-8").replace("+", "%20");
    }

    static void writeJson(JSONObject o, File f) {
        try {
            f.getParentFile().mkdirs();
            try (OutputStream out = new FileOutputStream(f)) {
                out.write(JSON.toJSONString(o).getBytes("UTF-8"));
            }
        } catch (Exception e) {
            System.err.println("failed to write " + f + ": " + e.getMessage());
        }
    }

    // reads manual/wiki/_overrides.tsv: lines "language<TAB>tag<TAB>wikiTitle" (# comments allowed)
    static Map<String, String> readOverrides() {
        Map<String, String> map = new HashMap<>();
        File f = new File("manual/wiki/_overrides.tsv");
        if (!f.exists()) {
            return map;
        }
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new java.io.FileInputStream(f), "UTF-8"))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#") || line.startsWith("language\t")) {
                    continue;
                }
                String[] parts = line.split("\t");
                if (parts.length >= 3 && !parts[2].trim().isEmpty()) {
                    map.put(parts[0].trim() + "\t" + parts[1].trim(), parts[2].trim());
                }
            }
        } catch (Exception e) {
            System.err.println("failed to read overrides: " + e.getMessage());
        }
        return map;
    }

    static void writeFailures(List<String> failures) {
        if (failures.isEmpty()) {
            return;
        }
        try {
            File f = new File("manual/wiki/_failures.tsv");
            f.getParentFile().mkdirs();
            StringBuilder sb = new StringBuilder("language\ttag\tname\n");
            for (String line : failures) {
                sb.append(line).append("\n");
            }
            try (OutputStream out = new FileOutputStream(f)) {
                out.write(sb.toString().getBytes("UTF-8"));
            }
        } catch (Exception e) {
            System.err.println("failed to write failures: " + e.getMessage());
        }
    }

    static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
        }
    }
}
