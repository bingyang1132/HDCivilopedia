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

    public static void saveImage (BufferedImage image, String path) throws Exception {
        if (image == null) return;
        File folder = new File("output/icons");
        folder.mkdirs();
        File file = new File(folder, path);
        file.getParentFile().mkdirs();
        if (file.exists()) {
            file.delete();
        }
        ImageIO.write(image, "png", file);
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
        FileInputStream in = new FileInputStream(file);
        byte[] buffer = new byte[in.available()];
        in.read(buffer);
        in.close();
        int[] pixels = DDSReader.read(buffer, DDSReader.ARGB, 0);
        int width = DDSReader.getWidth(buffer);
        int height = DDSReader.getHeight(buffer);
        int w = width / perRow;
        int h = height / perColumn;
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        int x = (id % perRow) * w;
        int y = (id / perRow) * h;
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                image.setRGB(i, j, pixels[(y + j) * width + x + i]);
            }
        }
        return image;
    }
}
