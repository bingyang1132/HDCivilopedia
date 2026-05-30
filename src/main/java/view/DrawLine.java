package view;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class DrawLine {

    public static final File folder = new File("manual/output/css");

    public static final int WIDTH = 620;
    public static final int B1 = 20;

    public static final int W = 220;
    public static final int H = 70;
    public static final int B2 = 25;

    // color of the line
    public static final Color c = new Color(191, 162, 113);

    // space between rows
    public static final int D = 50;

    // width of a connection line
    public static final int A = 6;

    // radius of inner circle
    public static final int R = 16;

    // percentage of horizontal line position
    public static final double PER = 0.8;
    
    public static final int left () {
        return B1 + W / 2;
    }

    public static final int middle () {
        return WIDTH / 2;
    }

    public static final int right () {
        return WIDTH - B1 - W / 2;
    }

    public static BufferedImage empty () {
        int w = WIDTH - 2 * W - 4 * B2;
        int h = H;
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        return image;
    }

    public static BufferedImage horizontal () {
        int w = WIDTH - 2 * W - 4 * B2;
        int h = H;
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) image.getGraphics();
        g.setColor(c);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.fillRect(0, (int) (h * PER) - A / 2, w, A / 2);
        g.dispose();
        return image;
    }

    public static BufferedImage lr_m () {
        int w = WIDTH;
        int h = D;
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) image.getGraphics();
        g.setColor(c);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        drawLine(left(), middle(), g);
        drawLine(right(), middle(), g);
        g.dispose();
        return image;
    }
    
    public static BufferedImage m_lr () {
        int w = WIDTH;
        int h = D;
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) image.getGraphics();
        g.setColor(c);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        drawLine(middle(), left(), g);
        drawLine(middle(), right(), g);
        g.dispose();
        return image;
    }

    public static BufferedImage lr_lr () {
        int w = WIDTH;
        int h = D;
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) image.getGraphics();
        g.setColor(c);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        drawLine(left(), right(), g);
        drawLine(right(), left(), g);
        g.fillRect(left() - A / 2, 0, A, D);
        g.fillRect(right() - A / 2, 0, A, D);
        g.dispose();
        return image;
    }

    public static BufferedImage singleVerticle () {
        int w = WIDTH;
        int h = D;
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) image.getGraphics();
        g.setColor(c);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.fillRect(middle() - A / 2, 0, A, D);
        g.dispose();
        return image;
    }
    
    public static BufferedImage doubleVerticle () {
        int w = WIDTH;
        int h = D;
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) image.getGraphics();
        g.setColor(c);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.fillRect(left() - A / 2, 0, A, D);
        g.fillRect(right() - A / 2, 0, A, D);
        g.dispose();
        return image;
    }

    public static void drawLine (int t, int b, Graphics2D g) {
        g.fillRect(t - A / 2, 0, A, (D - A) / 2 - R);
        if (t < b) {
            Shape inner1 = new Arc2D.Double(t + A / 2, (D - A) / 2 - 2 * R, 2 * R, 2 * R, 0, 360, Arc2D.PIE);
            Shape outer1 = new Arc2D.Double(t - A / 2, (D - A) / 2 - 2 * R - A, 2 * (A + R), 2 * (A + R), 180, 90, Arc2D.PIE);
            Area area1 = new Area(new Area(outer1));
            area1.subtract(new Area(inner1));
            g.fill(area1);

            g.fillRect(t + A / 2 + R, (D - A) / 2, b - t - A - 2 * R, A);

            Shape inner2 = new Arc2D.Double(b - A / 2 - 2 * R, (D + A) / 2, 2 * R, 2 * R, 0, 360, Arc2D.PIE);
            Shape outer2 = new Arc2D.Double(b - A / 2 - 2 * R - A, (D - A) / 2, 2 * (A + R), 2 * (A + R), 0, 90, Arc2D.PIE);
            Area area2 = new Area(new Area(outer2));
            area2.subtract(new Area(inner2));
            g.fill(area2);
        } else {
            Shape inner1 = new Arc2D.Double(t - A / 2 - 2 * R, (D - A) / 2 - 2 * R, 2 * R, 2 * R, 0, 360, Arc2D.PIE);
            Shape outer1 = new Arc2D.Double(t - A / 2 - 2 * R - A, (D - A) / 2 - 2 * R - A, 2 * (A + R), 2 * (A + R), 270, 90, Arc2D.PIE);
            Area area1 = new Area(new Area(outer1));
            area1.subtract(new Area(inner1));
            g.fill(area1);

            g.fillRect(b + A / 2 + R, (D - A) / 2, t - b - A - 2 * R, A);

            Shape inner2 = new Arc2D.Double(b + A / 2, (D + A) / 2, 2 * R, 2 * R, 0, 360, Arc2D.PIE);
            Shape outer2 = new Arc2D.Double(b - A / 2, (D - A) / 2, 2 * (A + R), 2 * (A + R), 90, 90, Arc2D.PIE);
            Area area2 = new Area(new Area(outer2));
            area2.subtract(new Area(inner2));
            g.fill(area2);
        }
        g.fillRect(b - A / 2, (D + A) / 2 + R, A, (D - A) / 2 - R);
    }

    public static void drawLines () throws IOException {
        File horizontal = new File(folder, "horizontal.png");
        if (horizontal.exists()) {
            horizontal.delete();
        }
        ImageIO.write(horizontal(), "png", horizontal);

        File m_lr = new File(folder, "m_lr.png");
        if (m_lr.exists()) {
            m_lr.delete();
        }
        ImageIO.write(m_lr(), "png", m_lr);

        File lr_m = new File(folder, "lr_m.png");
        if (lr_m.exists()) {
            lr_m.delete();
        }
        ImageIO.write(lr_m(), "png", lr_m);
        
        File lr_lr = new File(folder, "lr_lr.png");
        if (lr_lr.exists()) {
            lr_lr.delete();
        }
        ImageIO.write(lr_lr(), "png", lr_lr);

        File verticle1 = new File(folder, "verticle1.png");
        if (verticle1.exists()) {
            verticle1.delete();
        }
        ImageIO.write(singleVerticle(), "png", verticle1);

        File verticle2 = new File(folder, "verticle2.png");
        if (verticle2.exists()) {
            verticle2.delete();
        }
        ImageIO.write(doubleVerticle(), "png", verticle2);

        File empty = new File(folder, "empty.png");
        if (empty.exists()) {
            empty.delete();
        }
        ImageIO.write(empty(), "png", empty);
    }

    public static void main(String[] args) throws IOException {
        drawLines();
    }
}
