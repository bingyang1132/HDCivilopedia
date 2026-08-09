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

    static List<Writable> targets(int limitPerType) {
        List<Writable> targets = new ArrayList<>();
        addAll(targets, Civilization.civilizations.values(), limitPerType);
        addAll(targets, Leader.leaders.values(), limitPerType);
        addAll(targets, GreatPerson.greatpeople.values(), limitPerType);
        addAll(targets, Wonder.wonders.values(), limitPerType);
        return targets;
    }

    public static void fetchAll(int limitPerType) {
        List<Writable> targets = targets(limitPerType);

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
                // SKIP sentinel: drop any wiki file so the page falls back to game history
                if (override != null && override.trim().equalsIgnoreCase("SKIP")) {
                    if (out.exists()) {
                        out.delete();
                    }
                    skip++;
                    continue;
                }
                // PIN sentinel: the file is hand-maintained, never fetch or overwrite it. Used
                // where the source is not a wiki we can call — 中国 takes its lead from Baidu
                // Baike, which 403s every request from Java (WAF fingerprinting, not headers;
                // the same request from curl returns 200). A missing PIN file is reported as a
                // failure rather than silently falling back to the Wikipedia article.
                if (override != null && override.trim().equalsIgnoreCase("PIN")) {
                    if (out.exists()) {
                        skip++;
                    } else {
                        failures.add(language + "\t" + w.tag + "\t(PIN, but the file is missing)");
                        fail++;
                    }
                    continue;
                }
                // already fetched and no correction pending -> skip (resumable)
                if (out.exists() && override == null) {
                    skip++;
                    continue;
                }
                boolean longIntro = w instanceof Civilization;
                String name = cleanName(w.getTitle(language));
                if ((override == null || override.isEmpty()) && name.isEmpty()) {
                    failures.add(language + "\t" + w.tag + "\t(no name)");
                    fail++;
                    continue;
                }
                JSONObject result = (override != null && !override.isEmpty())
                        ? resolveOverride(wl, override, longIntro)
                        : autoFetch(wl, name, longIntro, w instanceof Leader);
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

    /**
     * Re-fetches cached entries in place so a change to what we ask Wikipedia for (the zh
     * variant, see acceptLang) reaches text that is already on disk — fetchAll skips existing
     * files by design, so it would never pick that up.
     *
     * It deliberately does NOT re-resolve names: each entry is re-fetched by the exact article
     * its cache file already records, so this can only change the wording of an entry, never
     * which article it points at. An entry is left untouched if the fetch fails (the old text
     * stays), if it is PINned or carries a "source" (Baidu, hand-translated — there is no API
     * behind those), or if its text comes back identical.
     */
    public static void refreshAll() {
        List<Writable> targets = targets(0);
        Map<String, String> overrides = readOverrides();
        // reported on stdout, not into _failures.tsv: that file is fetchAll's list of entries
        // with no article yet, and a refresh miss is transient — the old text is still there,
        // and re-running the command retries it
        List<String> failures = new ArrayList<>();
        int ok = 0, same = 0, skip = 0, fail = 0;
        for (String language : LANGUAGES) {
            // only the languages we ask a variant for need this; re-fetching en would just pull
            // in unrelated upstream edits
            if (acceptLang(wikiLang(language)) == null) {
                continue;
            }
            for (Writable w : targets) {
                File out = new File("manual/wiki/" + language + "/" + w.tag + ".json");
                String override = overrides.get(language + "\t" + w.tag);
                if (!out.exists() || (override != null && override.trim().equalsIgnoreCase("PIN"))) {
                    skip++;
                    continue;
                }
                JSONObject old;
                try {
                    old = Tools.readJSON(out);
                } catch (Exception e) {
                    old = null;
                }
                String url = old == null ? null : old.getString("url");
                String host = hostOf(url);
                if (host == null || !host.endsWith("wikipedia.org") || old.getString("source") != null) {
                    skip++;
                    continue;
                }
                String wl = host.substring(0, host.indexOf('.'));
                JSONObject fresh = extract(wl, titleFromOverride(url), w instanceof Civilization);
                sleep(120);
                if (fresh == null) {
                    failures.add(language + " " + w.tag);
                    fail++;
                    continue;
                }
                if (fresh.getString("text").equals(old.getString("text"))) {
                    same++;
                    continue;
                }
                writeJson(fresh, out);
                ok++;
            }
        }
        System.out.println("wiki refresh done: updated=" + ok + " unchanged=" + same
                + " skipped=" + skip + " failed=" + fail);
        if (!failures.isEmpty()) {
            System.out.println("  kept the old text for these, re-run to retry: " + failures);
        }
    }

    static String hostOf(String url) {
        if (url == null || !url.contains("://")) {
            return null;
        }
        try {
            return new URL(url).getHost();
        } catch (Exception e) {
            return null;
        }
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

    // an override value is either a bare article title (use the row's language wiki) or a full
    // URL. For a wikipedia URL the wiki language comes from the host (zh./en.…); non-wikipedia
    // hosts are unsupported and return null so the entry is logged. (Baidu Baike specifically
    // cannot be fetched from Java at all — see the PIN sentinel in fetchAll.)
    static JSONObject resolveOverride(String rowWl, String override, boolean longIntro) {
        String s = override.trim();
        if (s.contains("://")) {
            try {
                String host = new URL(s).getHost();
                if (host == null || !host.endsWith("wikipedia.org")) {
                    return null;
                }
                String sub = host.substring(0, host.indexOf('.'));
                return extract(sub, titleFromOverride(s), longIntro);
            } catch (Exception e) {
                return null;
            }
        }
        return extract(rowWl, s, longIntro);
    }

    // an override value may be an exact article title or a full wiki URL; extract the title
    static String titleFromOverride(String override) {
        String s = override.trim();
        int i = s.indexOf("/wiki/");
        if (i >= 0) {
            s = s.substring(i + "/wiki/".length());
            int cut = s.indexOf('#');
            if (cut >= 0) {
                s = s.substring(0, cut);
            }
            cut = s.indexOf('?');
            if (cut >= 0) {
                s = s.substring(0, cut);
            }
            try {
                s = java.net.URLDecoder.decode(s, "UTF-8");
            } catch (Exception e) {
            }
        }
        return s;
    }

    static String cleanName(String s) {
        if (s == null) {
            return "";
        }
        return s.replaceAll("\\[[^\\]]*\\]", "").replaceAll("\\s+", " ").trim();
    }

    // resolve strictly by the exact article title (the localized name). The old search
    // fallback returned garbage for obscure names (an Icelandic lawspeaker -> a footballer),
    // so a miss now returns null and the page falls back to the game's own history.
    //
    // Leaders get one extra attempt: their display name carries the persona epithet
    // (嬴政（受命于天）, Victoria (Age of Steam)), while the article is under the bare name —
    // and both personas of a leader legitimately share that one article. This retry is limited
    // to leaders on purpose: for great people a bare-name guess is exactly how the old search
    // fallback produced wrong matches.
    static JSONObject autoFetch(String wl, String name, boolean longIntro, boolean persona) {
        JSONObject o = extract(wl, name, longIntro);
        if (o != null || !persona) {
            return o;
        }
        String bare = dropEpithet(name);
        return bare.isEmpty() || bare.equals(name) ? null : extract(wl, bare, longIntro);
    }

    static String dropEpithet(String name) {
        return name.replaceAll("（[^（）]*）", "").replaceAll("\\s*\\([^()]*\\)", "").trim();
    }

    // short lead (REST summary) or, for longIntro, the full lead section via the query API.
    // summary() validates the article is a real page (not a disambiguation) and resolves
    // redirects; longExtract() then pulls the fuller intro for that canonical title.
    static JSONObject extract(String wl, String title, boolean longIntro) {
        JSONObject s = summary(wl, title);
        if (s == null) {
            return null;
        }
        if (!longIntro) {
            return s;
        }
        JSONObject lng = longExtract(wl, s.getString("title"));
        return lng != null ? lng : s;
    }

    static JSONObject longExtract(String wl, String title) {
        try {
            JSONObject d = getJson("https://" + wl + ".wikipedia.org/w/api.php?action=query&prop=extracts&exintro=1&explaintext=1&redirects=1&format=json&titles="
                    + enc(title), acceptLang(wl));
            if (d == null) {
                return null;
            }
            JSONObject pages = d.getJSONObject("query").getJSONObject("pages");
            for (String k : pages.keySet()) {
                JSONObject p = pages.getJSONObject(k);
                String ex = p.getString("extract");
                if (ex == null || ex.trim().isEmpty()) {
                    return null;
                }
                JSONObject out = new JSONObject(true);
                out.put("title", p.getString("title"));
                out.put("url", "https://" + wl + ".wikipedia.org/wiki/" + enc(p.getString("title").replace(" ", "_")));
                out.put("text", stripRefs(ex.trim()));
                return out;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    // drop wiki reference markers like [8], [a], [注 3]
    static String stripRefs(String s) {
        return s.replaceAll("\\[\\d+\\]", "").replaceAll("\\[[a-zA-Z]\\]", "").replaceAll("\\[注[^\\]]*\\]", "");
    }

    // zh.wikipedia serves mixed/traditional text by default; request the mainland variant.
    // zh-cn rather than zh-hans: zh-hans converts characters only, so regional terms come
    // through in their Taiwan form (鄂图曼帝国, 大部份). zh-cn converts those too.
    static String acceptLang(String wl) {
        return "zh".equals(wl) ? "zh-cn" : null;
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
