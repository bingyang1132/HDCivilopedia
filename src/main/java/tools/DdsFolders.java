package tools;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Lazily built index of every .dds under the configured roots.
 *
 * Kept out of {@link Constants} on purpose: touching any field of that interface would trigger the
 * walk, and only icon decoding needs it — `page` and `audit` should not pay for it. A separate
 * holder class defers the work to first use.
 *
 * This replaced a hand-written list of 83 absolute paths, of which 31 were dead when checked
 * (mod folders get renamed), each dead entry silently losing a whole atlas of icons. Discovery
 * cannot go stale, which is why it is not cached to disk either.
 */
class DdsFolders {

    /**
     * Lower-cased file name to full path, ~14000 entries. Icon lookup used to ask every one of the
     * ~110 folders whether it held a given file, thousands of times per load; the walk already
     * sees every file, so indexing them costs nothing. Lower-cased because the {@code exists()}
     * check it replaces is case-insensitive on Windows.
     */
    static final Map<String, String> FILES = new HashMap<>();

    static {
        long start = System.nanoTime();
        // Order matters: a later entry overwrites an earlier one, so scan the base game first and
        // the HD mod last -- a mod overrides the base game, and HD overrides everything. This is
        // the precedence the old per-folder scan got from keeping its *last* match.
        for (String root : Arrays.asList(Constants.SDK_ASSETS, Constants.GAME_ASSETS,
                Constants.WORKSHOP, Constants.MODS_FOLDER, Constants.HD_MOD)) {
            add(root);
        }
        System.out.printf("[CONFIG] %d .dds files indexed in %.1fs%n",
                FILES.size(), (System.nanoTime() - start) / 1e9);
    }

    /**
     * Walks a root with {@link Files#walkFileTree}, which hands the file type over from the
     * directory listing instead of costing a stat per entry the way {@code File.isDirectory()} did.
     * Traversal order is the directory order either way, so precedence is unchanged.
     */
    private static void add (String root) {
        Path start = Paths.get(root);
        if (!Files.isDirectory(start)) {
            return;
        }
        try {
            Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile (Path file, BasicFileAttributes attributes) {
                    String name = file.getFileName().toString();
                    if (name.regionMatches(true, Math.max(0, name.length() - 4), ".dds", 0, 4)) {
                        FILES.put(name.toLowerCase(), file.toString());
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed (Path file, IOException e) {
                    return FileVisitResult.CONTINUE;   // unreadable folders were skipped before too
                }
            });
        } catch (IOException e) {
            System.out.println("[CONFIG] could not walk " + root + ": " + e.getMessage());
        }
    }
}
