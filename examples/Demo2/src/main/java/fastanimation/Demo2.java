package fastanimation;

import fastanimation.AnimationEngine.HeartbeatMode;
import fasttheme.FastTheme;
import fastdwm.FastDWM;
import fasttween.Ease;
import fasttween.FastTween;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Random;

/**
 * FastAnimation v0.1.0 - "The Pixel-Perfect 1-to-1 Engine"
 */
public class Demo2 extends Canvas {
    
    static {
        System.setProperty("sun.awt.noerasebackground", "true");
        System.setProperty("sun.java2d.noddraw", "true");
    }

    private final int COUNT = 250;
    private final float[] x = new float[COUNT];
    private final float[] y = new float[COUNT];
    private final float[] vx = new float[COUNT];
    private final float[] vy = new float[COUNT];
    private final float[] size = new float[COUNT];
    private final float[] alpha = new float[COUNT];
    private final long[] durations = new long[COUNT];
    private final long[] startTimes = new long[COUNT];
    
    private static final int WIDTH = 1184;
    private static final int HEIGHT = 621;
    private static final int BRAND_TEAL_RGB = 0x15545e;
    private static final int BRAND_AQUA_RGB = 0x53b6b1;
    private static final int AQUA_RB = (BRAND_AQUA_RGB & 0xff00ff);
    private static final int AQUA_G = (BRAND_AQUA_RGB & 0x00ff00);
    
    private static final Color BRAND_TEAL = new Color(0x15, 0x54, 0x5e);
    private static final Color BRAND_AQUA = new Color(0x53, 0xb6, 0xb1);
    
    private BufferedImage screenBuffer;
    private int[] pixels;
    private int[] backgroundPixels;
    private final int[][] spanTable = new int[81][81];

    private int frames = 0;
    private long lastFPSCheck = System.currentTimeMillis();

    public Demo2() {
        // CRITICAL: Ensure the canvas is EXACTLY the size of the pixel buffer
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setIgnoreRepaint(true);
        initSpanTable();
        initParticles();
        initBuffers();
    }

    private void initSpanTable() {
        for (int r = 1; r <= 40; r++) {
            int r2 = r * r;
            int d = r * 2;
            for (int dy = -r; dy < r; dy++) {
                spanTable[d][dy + r] = (int) Math.sqrt(r2 - dy * dy);
            }
        }
    }

    private void initBuffers() {
        screenBuffer = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        pixels = ((DataBufferInt) screenBuffer.getRaster().getDataBuffer()).getData();
        backgroundPixels = new int[WIDTH * HEIGHT];
        for (int i = 0; i < backgroundPixels.length; i++) backgroundPixels[i] = BRAND_TEAL_RGB;
    }

    private void initParticles() {
        Random r = new Random();
        for (int i = 0; i < COUNT; i++) resetParticle(i, r);
    }

    private void resetParticle(int i, Random r) {
        x[i] = r.nextFloat() * WIDTH;
        y[i] = r.nextFloat() * HEIGHT;
        vx[i] = (r.nextFloat() - 0.5f) * 0.3f;
        vy[i] = (r.nextFloat() - 0.5f) * 0.3f;
        durations[i] = 1500 + r.nextInt(2500);
        startTimes[i] = System.currentTimeMillis();
    }

    private void updatePhysics() {
        long now = System.currentTimeMillis();
        Random r = new Random();
        for (int i = 0; i < COUNT; i++) {
            x[i] += vx[i];
            y[i] += vy[i];
            if (x[i] < 0) x[i] = WIDTH; else if (x[i] > WIDTH) x[i] = 0;
            if (y[i] < 0) y[i] = HEIGHT; else if (y[i] > HEIGHT) y[i] = 0;
            
            long elapsed = now - startTimes[i];
            float p = Math.min(1.0f, (float)elapsed / durations[i]);
            float eased = Ease.CUBIC_OUT.apply(p);
            size[i] = eased * 70;
            alpha[i] = 1.0f - eased;
            if (p >= 1.0f) resetParticle(i, r);
        }
    }

    public void start() {
        Thread renderThread = new Thread(() -> {
            while (true) {
                FastDWM.waitForVSync();
                updatePhysics();
                renderPixels();
                
                Graphics g = getGraphics();
                if (g != null) {
                    // Draw at 0,0 to ensure 1-to-1 pixel mapping
                    g.drawImage(screenBuffer, 0, 0, null);
                    g.dispose();
                    Toolkit.getDefaultToolkit().sync();
                }
                updateFPS();
            }
        }, "Render-Loop");
        renderThread.setPriority(Thread.MAX_PRIORITY);
        renderThread.start();
    }

    private void renderPixels() {
        System.arraycopy(backgroundPixels, 0, pixels, 0, backgroundPixels.length);
        for (int i = 0; i < COUNT; i++) {
            int py = (int)y[i];
            int d = (int)size[i];
            int r = d / 2;
            int a = (int)(alpha[i] * 255);
            if (a < 5 || r < 2 || d > 80) continue;

            int px = (int)x[i];
            int[] spans = spanTable[d];
            for (int dy = -r; dy < r; dy++) {
                int sy = py + dy;
                if (sy < 0 || sy >= HEIGHT) continue;
                int span = spans[dy + r];
                int offset = sy * WIDTH;
                int start = Math.max(0, px - span);
                int limit = offset + Math.min(WIDTH, px + span);
                for (int idx = offset + start; idx < limit; idx++) {
                    int bg = pixels[idx];
                    int rb = (bg & 0xff00ff);
                    int g = (bg & 0x00ff00);
                    pixels[idx] = (rb + (((AQUA_RB - rb) * a) >> 8) & 0xff00ff) |
                                 (g + (((AQUA_G - g) * a) >> 8) & 0x00ff00);
                }
            }
        }
    }

    private void updateFPS() {
        frames++;
        long now = System.currentTimeMillis();
        if (now - lastFPSCheck >= 1000) {
            int currentFPS = frames;
            System.out.println("[FPS] FastAnimation: " + currentFPS);
            SwingUtilities.invokeLater(() -> {
                Window win = SwingUtilities.getWindowAncestor(this);
                if (win instanceof JFrame) ((JFrame) win).setTitle("FastAnimation (1-to-1 Pixel Lock) FPS: " + currentFPS);
            });
            frames = 0;
            lastFPSCheck = now;
        }
    }

    private static Image createCircleIcon() {
        BufferedImage img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(BRAND_AQUA);
        g2.fillOval(4, 4, 24, 24);
        g2.dispose();
        return img;
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("FastAnimation Demo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setIconImage(createCircleIcon());
        frame.setIgnoreRepaint(true);
        
        Demo2 demo = new Demo2();
        frame.add(demo);
        frame.pack(); // Size the frame around the PreferredSize of the Demo
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        Timer styleTimer = new Timer(50, e -> {
            long hwnd = FastTheme.getWindowHandle(frame);
            if (hwnd != 0) {
                FastTheme.setTitleBarDarkMode(hwnd, true);
                FastTheme.setTitleBarColor(hwnd, BRAND_TEAL.getRed(), BRAND_TEAL.getGreen(), BRAND_TEAL.getBlue());
            }
        });
        styleTimer.setRepeats(false);
        styleTimer.start();

        demo.start();
    }
}

