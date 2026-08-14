package tools;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageEditor {

    /**
     * Queues an icon; {@link #flushImages} does the encoding.
     *
     * PNG encoding was 13s of a 40s {@code load()} — 1898 icons, a few ms each, one at a time on
     * whichever model happened to be loading. Nothing reads output/icons back while loading
     * ({@code decodeIcon} only ever goes to the .dds), so they can all be written at the end, in
     * parallel. Keyed by path so a second save of the same icon wins, as it did before.
     */
    public static void saveImage (BufferedImage image, String path) throws Exception {
        if (image == null) return;
        synchronized (PENDING) {
            PENDING.put(path, image);
        }
    }

    private static final java.util.LinkedHashMap<String, BufferedImage> PENDING =
            new java.util.LinkedHashMap<>();

    /**
     * Writes every queued icon. Called at the end of {@code initIcons()} and of {@code load()},
     * which are the points anything else starts reading the folder — the changelog resource lists
     * it, and initIcons itself reads a hand-placed png back when decoding fails.
     */
    public static void flushImages () {
        java.util.Map<String, BufferedImage> pending;
        synchronized (PENDING) {
            if (PENDING.isEmpty()) {
                return;
            }
            pending = new java.util.LinkedHashMap<>(PENDING);
            PENDING.clear();
        }
        long start = System.nanoTime();
        new File("output/icons").mkdirs();
        pending.entrySet().parallelStream().forEach(entry -> {
            File file = new File("output/icons", entry.getKey());
            try {
                file.getParentFile().mkdirs();
                if (file.exists()) {
                    file.delete();
                }
                ImageIO.write(entry.getValue(), "png", file);
            } catch (Exception e) {
                // loud, because a missing icon is otherwise invisible until someone counts them
                System.out.println("[ICONS] could not write " + entry.getKey() + ": " + e.getMessage());
            }
        });
        System.out.printf("[TIME] %-22s %8.2fs  (%d icons)%n", "write icons",
                (System.nanoTime() - start) / 1e9, pending.size());
    }

    public static BufferedImage renderImage(BufferedImage origin, int front, int back) {
        int width = origin.getWidth();
        int height = origin.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) image.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(back));
        g.fillOval(0, 0, width, height);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int c = origin.getRGB(i, j);
                int ftc = (c & 0xff000000) + (front & 0x00ffffff);
                origin.setRGB(i, j, ftc);
            }
        }
        g.drawImage(origin, 0, 0, width, height, null);
        g.dispose();
        return image;
    }

    public static BufferedImage addBackground(BufferedImage origin) {
        int width = origin.getWidth();
        int height = origin.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) image.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.BLACK);
        g.fillOval(0, 0, width, height);
        g.drawImage(origin, 0, 0, width, height, null);
        g.dispose();
        return image;
    }

    public static BufferedImage cutImage(File file, int perRow, int perColumn, int id) throws IOException {
        Atlas atlas = atlas(file);
        int w = atlas.width / perRow;
        int h = atlas.height / perColumn;
        int x = (id % perRow) * w;
        int y = (id / perRow) * h;
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        // one bulk copy of the tile instead of w*h setRGB calls; the offset/scansize pair addresses
        // the tile in place, so this is the same pixels as the loop it replaces
        image.setRGB(0, 0, w, h, atlas.pixels, y * atlas.width + x, atlas.width);
        return image;
    }

    /** A decoded atlas: ARGB pixels for the whole sheet, which every icon on it is cut from. */
    private static final class Atlas {
        final int[] pixels;
        final int width;
        final int height;

        Atlas (int[] pixels, int width, int height) {
            this.pixels = pixels;
            this.width = width;
            this.height = height;
        }
    }

    /**
     * Decoded atlases, most-recently-used last. Cutting one icon out used to read and decode the
     * entire .dds every time, and hundreds of icons share a single sheet — that decoding was most
     * of what {@code load()} spent its time on.
     *
     * Bounded by pixels rather than entries because the sheets are wildly uneven: the median is
     * 0.1 MB on disk but the largest is 33 MB, and ARGB decoding turns each pixel into 4 bytes.
     */
    private static final int BUDGET = 48 << 20;                  // pixels, so ~192 MB of int[]
    private static final java.util.LinkedHashMap<String, Atlas> ATLASES =
            new java.util.LinkedHashMap<>(16, 0.75f, true);
    private static long held = 0;

    private static synchronized Atlas atlas (File file) throws IOException {
        String key = file.getPath();
        Atlas hit = ATLASES.get(key);
        if (hit != null) {
            return hit;
        }
        byte[] buffer = readFully(file);
        Atlas atlas = new Atlas(DDSReader.read(buffer, DDSReader.ARGB, 0),
                DDSReader.getWidth(buffer), DDSReader.getHeight(buffer));
        ATLASES.put(key, atlas);
        held += atlas.pixels.length;
        java.util.Iterator<java.util.Map.Entry<String, Atlas>> eldest = ATLASES.entrySet().iterator();
        while (held > BUDGET && ATLASES.size() > 1 && eldest.hasNext()) {
            held -= eldest.next().getValue().pixels.length;
            eldest.remove();
        }
        return atlas;
    }

    /** available() is not a length; a short read here would silently produce a corrupt icon. */
    private static byte[] readFully (File file) throws IOException {
        byte[] buffer = new byte[(int) file.length()];
        try (FileInputStream in = new FileInputStream(file)) {
            int at = 0;
            while (at < buffer.length) {
                int read = in.read(buffer, at, buffer.length - at);
                if (read < 0) {
                    throw new IOException("short read on " + file.getPath());
                }
                at += read;
            }
        }
        return buffer;
    }
}
