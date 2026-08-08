package tools;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Lazily discovered texture folders.
 *
 * Kept out of {@link Constants} on purpose: touching any field of that interface would trigger
 * the directory walk, and the walk costs ~17s. Only icon decoding needs these, so `page` and
 * `audit` should not pay for it. A separate holder class defers the work to first use.
 */
class DdsFolders {

    /** Initialised on first access to this class, i.e. on the first icon lookup. */
    static final List<String> LIST = discover();

    private static List<String> discover () {
        long start = System.nanoTime();
        List<String> folders = new ArrayList<>();
        // Order matters: Tools.tryGetImage keeps the *last* folder holding a matching filename,
        // so scan base game first and the HD mod last -- a mod overrides the base game, and HD
        // overrides everything.
        for (String root : Arrays.asList(Constants.SDK_ASSETS, Constants.GAME_ASSETS,
                Constants.WORKSHOP, Constants.MODS_FOLDER, Constants.HD_MOD)) {
            add(new File(root), folders);
        }
        System.out.printf("[CONFIG] %d texture folders discovered in %.1fs%n",
                folders.size(), (System.nanoTime() - start) / 1e9);
        return folders;
    }

    /** Adds every directory under {@code root} that directly contains a .dds. */
    private static void add (File root, List<String> folders) {
        File[] children = root.listFiles();
        if (children == null) {
            return;
        }
        boolean hasDDS = false;
        for (File child : children) {
            if (child.isDirectory()) {
                add(child, folders);
            } else if (!hasDDS && child.getName().toLowerCase().endsWith(".dds")) {
                hasDDS = true;
            }
        }
        if (hasDDS) {
            folders.add(root.getAbsolutePath());
        }
    }
}
