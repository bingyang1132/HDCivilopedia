package tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.alibaba.fastjson.JSONObject;

/**
 * Snapshots each run's artifacts into {@code archive.folder/HDCivilopedia_<yyyyMMdd>}, one per day.
 *
 * The point is the changelog rebuild (see docs/roadmap.md): diffing two snapshots gives the
 * player-visible change set by construction — a pure refactor produces an empty diff, a
 * "+2 production" produces exactly one line. That only works if snapshots actually exist, and
 * archiving by hand had produced 20 of them in two years, HTML only.
 *
 * What goes in:
 * <ul>
 * <li>{@code json/} verbatim — the diff source of record. HTML diffs badly (one number moving
 *     drags a pile of {@code <span>}/{@code <a>}/table markup with it); the 20 legacy archives
 *     are HTML-only and have to be read through a strip-tags pass, new ones need not be.</li>
 * <li>{@code output/} and {@code output_android/} zipped, minus {@link #SKIP}. HTML compresses
 *     about 15:1, so the pair costs ~46 MB instead of ~690 MB.</li>
 * <li>{@code manifest.json}, which records the generator's own commit. Two snapshots can differ
 *     because the mod changed or because the generator changed, and the diff cannot tell those
 *     apart on its own.</li>
 * </ul>
 *
 * Failures throw rather than being logged and swallowed: silent accumulation of missing artifacts
 * is exactly the failure mode this project keeps getting bitten by (docs/known-issues.md).
 */
public class Archive {

    /**
     * Top-level folders left out of the zips: 122 MB of binaries that are byte-identical from one
     * run to the next. {@code css/} is kept — it is 1.4 MB and the pages need it to render.
     */
    private static final List<String> SKIP = Arrays.asList("icons", "images");

    public static void run () throws IOException {
        String root = Config.get("archive.folder", "");
        if (root.isEmpty()) {
            System.out.println("[ARCHIVE] archive.folder is not set in config.properties, skipping");
            return;
        }
        File base = new File(root);
        if (!base.isDirectory()) {
            System.out.println("[ARCHIVE] archive.folder does not exist, skipping: " + root);
            return;
        }

        String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
        // One directory per day, the last run of the day wins, named the way the hand-made
        // archives already are (HDCivilopedia_20260807) so the whole history reads as one series
        // and the diff script needs no special case for which half it is looking at.
        File dir = new File(base, "HDCivilopedia_" + date);
        if (!dir.isDirectory() && !dir.mkdirs()) {
            throw new IOException("could not create " + dir.getPath());
        }
        // only ever remove what this class writes. Sharing the naming means the target can be a
        // hand-made release archive with a zip and an apk in it, and a recursive wipe of the day's
        // folder would take those with it; anything else already in there is left alone.
        delete(new File(dir, "json"));
        delete(new File(dir, "output-html.zip"));
        delete(new File(dir, "output_android-html.zip"));
        delete(new File(dir, "manifest.json"));

        long start = System.nanoTime();
        int json = copy(new File("json"), new File(dir, "json"));
        int desktop = zip(new File("output"), new File(dir, "output-html.zip"));
        int android = zip(new File("output_android"), new File(dir, "output_android-html.zip"));

        JSONObject manifest = new JSONObject(true);
        manifest.put("date", date);
        manifest.put("generatedAt", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        manifest.put("pediaCommit", head());
        manifest.put("jsonFiles", json);
        manifest.put("outputFiles", desktop);
        manifest.put("androidFiles", android);
        Tools.writeJson(manifest, new File(dir, "manifest.json"));

        System.out.printf("[ARCHIVE] %s  json=%d output=%d android=%d  (%.1fs, %d MB)%n",
                dir.getPath(), json, desktop, android, (System.nanoTime() - start) / 1e9,
                size(dir) / (1024 * 1024));
    }

    /** Copies a tree, returning the number of files written. */
    private static int copy (File from, File to) throws IOException {
        if (!from.exists()) {
            return 0;
        }
        if (from.isDirectory()) {
            if (!to.isDirectory() && !to.mkdirs()) {
                throw new IOException("could not create " + to.getPath());
            }
            int count = 0;
            for (File child : children(from)) {
                count += copy(child, new File(to, child.getName()));
            }
            return count;
        }
        try (InputStream in = new FileInputStream(from); OutputStream out = new FileOutputStream(to)) {
            pipe(in, out);
        }
        return 1;
    }

    /** Zips a tree minus {@link #SKIP}, returning the number of entries written. */
    private static int zip (File from, File target) throws IOException {
        if (!from.isDirectory()) {
            return 0;
        }
        try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(target))) {
            int count = 0;
            for (File child : children(from)) {
                if (SKIP.contains(child.getName())) {
                    continue;
                }
                count += zipInto(out, child, child.getName());
            }
            return count;
        }
    }

    private static int zipInto (ZipOutputStream out, File file, String path) throws IOException {
        if (file.isDirectory()) {
            int count = 0;
            for (File child : children(file)) {
                count += zipInto(out, child, path + "/" + child.getName());
            }
            return count;
        }
        ZipEntry entry = new ZipEntry(path);
        entry.setTime(file.lastModified());
        out.putNextEntry(entry);
        try (InputStream in = new FileInputStream(file)) {
            pipe(in, out);
        }
        out.closeEntry();
        return 1;
    }

    /**
     * The commit the generator was at. Read straight out of {@code .git} rather than by shelling
     * out; a packed HEAD or a missing .git just yields null, which is recorded as such.
     */
    private static String head () {
        try {
            String pointer = Tools.readFromFile(new File(".git/HEAD")).trim();
            if (!pointer.startsWith("ref:")) {
                return pointer;                                  // detached HEAD
            }
            File ref = new File(".git/" + pointer.substring(4).trim());
            return ref.exists() ? Tools.readFromFile(ref).trim() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private static void delete (File file) throws IOException {
        if (!file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            for (File child : children(file)) {
                delete(child);
            }
        }
        if (!file.delete()) {
            throw new IOException("could not delete " + file.getPath());
        }
    }

    private static long size (File file) {
        if (!file.isDirectory()) {
            return file.length();
        }
        long total = 0;
        for (File child : children(file)) {
            total += size(child);
        }
        return total;
    }

    private static File[] children (File folder) {
        File[] children = folder.listFiles();
        return children == null ? new File[0] : children;
    }

    private static void pipe (InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1 << 16];
        int read;
        while ((read = in.read(buffer)) > 0) {
            out.write(buffer, 0, read);
        }
    }
}
