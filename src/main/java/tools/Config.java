package tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * Machine-specific paths, read from {@code config.properties} in the working directory.
 *
 * These used to be hardcoded in {@link Constants}, which made moving the project to another
 * machine a matter of editing a source file — including a hand-maintained list of 83 absolute
 * texture folders, of which 31 had silently gone stale by the time anyone checked. Now there
 * are four roots here and everything else is derived or discovered.
 *
 * The file is git-ignored; {@code config.example.properties} is the tracked template. If it is
 * missing, the defaults below apply, so an existing checkout keeps working untouched.
 */
public class Config {

    private static final Properties PROPS = load();

    private static Properties load () {
        Properties p = new Properties();
        File file = new File("config.properties");
        if (!file.exists()) {
            System.out.println("[CONFIG] config.properties not found, using built-in defaults"
                    + " (copy config.example.properties to config.properties to change them)");
            return p;
        }
        try (InputStream in = new FileInputStream(file)) {
            p.load(in);
        } catch (Exception e) {
            System.out.println("[CONFIG] failed to read config.properties: " + e.getMessage());
        }
        return p;
    }

    /** Trailing slashes are trimmed so callers can concatenate "/..." unconditionally. */
    public static String get (String key, String fallback) {
        String v = PROPS.getProperty(key);
        if (v == null || v.trim().isEmpty()) {
            return fallback;
        }
        v = v.trim().replace('\\', '/');
        while (v.endsWith("/")) {
            v = v.substring(0, v.length() - 1);
        }
        return v;
    }

    /** A path under the user's home, so the built-in defaults carry nobody's username. */
    public static String underHome (String suffix) {
        return System.getProperty("user.home").replace('\\', '/') + suffix;
    }

    /** Same for %LOCALAPPDATA%, where the game keeps its cache. */
    public static String underLocalAppData (String suffix) {
        String base = System.getenv("LOCALAPPDATA");
        if (base == null || base.trim().isEmpty()) {
            base = System.getProperty("user.home") + "/AppData/Local";
        }
        return base.replace('\\', '/') + suffix;
    }

    /** Warns about roots that do not exist — a stale path here costs whole atlases of icons. */
    public static void report () {
        String[][] roots = {
                { "steam.folder", Constants.STEAM_FOLDER },
                { "mods.folder", Constants.MODS_FOLDER },
                { "hd.mod", Constants.HD_MOD },
                { "cache.source", Constants.DATABASES_SOURCE },
        };
        for (String[] r : roots) {
            if (!new File(r[1]).isDirectory()) {
                System.out.println("[CONFIG] " + r[0] + " does not exist: " + r[1]);
            }
        }
    }
}
