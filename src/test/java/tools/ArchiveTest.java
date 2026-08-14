package tools;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * The snapshots feed the changelog diff, so what matters is that a snapshot is complete (nothing
 * dropped from the copy) and small (icons and images stay out — 122 MB of binaries that never
 * change), and that delete only ever removes what it is pointed at.
 *
 * {@code Archive.run()} itself is not covered: it reads the working directory and the config, so
 * exercising it would mean building a whole fake pedia output. The pieces below are where the
 * behaviour lives.
 */
public class ArchiveTest {

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    @Test
    public void zipKeepsPagesAndCssButNotIconsOrImages () throws Exception {
        File from = tmp.newFolder("output");
        write(new File(from, "zh_Hans_CN/buildings/a.html"), "page");
        write(new File(from, "css/pedia.css"), "css");
        write(new File(from, "icons/ICON_A.png"), "binary");
        write(new File(from, "images/big.png"), "binary");

        File zip = new File(tmp.getRoot(), "out.zip");
        assertEquals(2, Archive.zip(from, zip));

        Set<String> names = entries(zip);
        assertTrue(names.contains("zh_Hans_CN/buildings/a.html"));
        assertTrue(names.contains("css/pedia.css"));
        assertFalse(names.contains("icons/ICON_A.png"));
        assertFalse(names.contains("images/big.png"));
    }

    @Test
    public void zipOfAMissingFolderIsEmptyRatherThanAnError () throws Exception {
        File zip = new File(tmp.getRoot(), "none.zip");
        assertEquals(0, Archive.zip(new File(tmp.getRoot(), "nothing-here"), zip));
    }

    @Test
    public void copyReproducesTheTreeAndCountsEveryFile () throws Exception {
        File from = tmp.newFolder("json");
        write(new File(from, "zh_Hans_CN/units/contents.json"), "{\"a\":1}");
        write(new File(from, "en_US/units/contents.json"), "{\"b\":2}");

        File to = new File(tmp.getRoot(), "copy");
        assertEquals(2, Archive.copy(from, to));
        // byte for byte: the snapshot is what a later diff is run against, so a copy that
        // normalises anything at all would show up as a phantom change
        assertArrayEquals("{\"a\":1}".getBytes("UTF-8"),
                read(new File(to, "zh_Hans_CN/units/contents.json")));
        assertArrayEquals("{\"b\":2}".getBytes("UTF-8"),
                read(new File(to, "en_US/units/contents.json")));
    }

    /**
     * Snapshots share their naming with the archives made by hand, so the day's folder can already
     * hold a release zip and an apk. delete() is only ever pointed at the four things Archive
     * writes, and has to leave the rest of the folder alone.
     */
    @Test
    public void deleteTakesItsTargetAndNothingElse () throws Exception {
        File dir = tmp.newFolder("HDCivilopedia_20260814");
        write(new File(dir, "json/a.json"), "ours");
        write(new File(dir, "HDCivilopedia_20260814.zip"), "release");

        Archive.delete(new File(dir, "json"));
        assertFalse(new File(dir, "json").exists());
        assertTrue(new File(dir, "HDCivilopedia_20260814.zip").exists());

        Archive.delete(new File(dir, "manifest.json"));   // never written, must not throw
    }

    private static byte[] read (File file) throws Exception {
        byte[] bytes = new byte[(int) file.length()];
        try (java.io.InputStream in = new java.io.FileInputStream(file)) {
            assertEquals(bytes.length, in.read(bytes));
        }
        return bytes;
    }

    private static void write (File file, String text) throws Exception {
        file.getParentFile().mkdirs();
        try (OutputStream out = new FileOutputStream(file)) {
            out.write(text.getBytes("UTF-8"));
        }
    }

    private static Set<String> entries (File zip) throws Exception {
        Set<String> names = new HashSet<>();
        try (ZipFile file = new ZipFile(zip)) {
            Enumeration<? extends ZipEntry> all = file.entries();
            while (all.hasMoreElements()) {
                names.add(all.nextElement().getName());
            }
        }
        return names;
    }
}
