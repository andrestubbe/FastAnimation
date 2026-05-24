package fastanimation;

import fastdwm.FastDWM;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

/**
 * FastAnimation Ground Zero - DEBUGGING FLICKER
 * PHASE 4.4: Triple-Buffered Stability
 */
public class Demo1 extends Canvas {
    
    static {
        System.setProperty("sun.java2d.opengl", "true");
        System.setProperty("sun.awt.noerasebackground", "true");
    }

    private static final int WIDTH = 1184;
    private static final int HEIGHT = 621;
    private static final int BRAND_TEAL_RGB = 0x15545e;
    private static final int BRAND_AQUA_RGB = 0x53b6b1;
    private static final int AQUA_RB = (BRAND_AQUA_RGB & 0xff00ff);
    private static final int AQUA_G = (BRAND_AQUA_RGB & 0x00ff00);
    
    private BufferedImage screenBuffer;
    private int[] pixels;
    private int[] backgroundPixels;

    public Demo1() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setIgnoreRepaint(true);
        initBuffers();
    }

    private void initBuffers() {
        screenBuffer = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        pixels = ((DataBufferInt) screenBuffer.getRaster().getDataBuffer()).getData();
        backgroundPixels = new int[WIDTH * HEIGHT];
        for (int i = 0; i < backgroundPixels.length; i++) backgroundPixels[i] = BRAND_TEAL_RGB;
    }

    public void start() {
        // Triple buffering provides the necessary back-pressure for 120Hz
        createBufferStrategy(3);
        BufferStrategy bs = getBufferStrategy();
        
        new Thread(() -> {
            float cx = WIDTH / 2f, cy = HEIGHT / 2f;
            float speedX = 500f, speedY = 350f;
            long lastTime = System.nanoTime();
            int frameCount = 0;
            
            while (true) {
                FastDWM.waitForVSync();

                long now = System.nanoTime();
                float dt = (now - lastTime) / 1_000_000_000f;
                lastTime = now;
                
                if (frameCount % 60 == 0) {
                    System.out.printf("[SYNC-STATS] dt:%.4f (Target: 0.0083)\n", dt);
                }
                frameCount++;

                cx += speedX * dt;
                cy += speedY * dt;
                if (cx < 50 || cx > WIDTH - 50) speedX = -speedX;
                if (cy < 50 || cy > HEIGHT - 50) speedY = -speedY;

                System.arraycopy(backgroundPixels, 0, pixels, 0, backgroundPixels.length);
                drawCircle((int)cx, (int)cy, 70, 150);

                Graphics g = bs.getDrawGraphics();
                g.drawImage(screenBuffer, 0, 0, null);
                g.dispose();
                bs.show();
                Toolkit.getDefaultToolkit().sync();
            }
        }, "Debug-Loop").start();
    }

    private void drawCircle(int px, int py, int r, int a) {
        int r2 = r * r;
        for (int dy = -r; dy < r; dy++) {
            int sy = py + dy;
            if (sy < 0 || sy >= HEIGHT) continue;
            int offset = sy * WIDTH;
            int dy2 = dy * dy;
            for (int dx = -r; dx < r; dx++) {
                if (dx * dx + dy2 > r2) continue;
                int sx = px + dx;
                if (sx < 0 || sx >= WIDTH) continue;
                int idx = offset + sx;
                int bg = pixels[idx];
                int rb = (bg & 0xff00ff);
                int g = (bg & 0x00ff00);
                pixels[idx] = (rb + (((AQUA_RB - rb) * a) >> 8) & 0xff00ff) |
                             (g + (((AQUA_G - g) * a) >> 8) & 0x00ff00);
            }
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("DEBUG: Phase 4.4 (Triple-Buffer Sync)");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setIgnoreRepaint(true);
        Demo1 demo = new Demo1();
        frame.add(demo);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        demo.start();
    }
}

