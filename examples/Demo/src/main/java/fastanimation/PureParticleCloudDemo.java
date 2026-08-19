package fastanimation;

import fastanimation.AnimationEngine.HeartbeatMode;
import fastdwm.FastDWM;
import fasttheme.FastTheme;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Random;

/**
 * FastAnimation Demo 3: Pure 50,000 Mass Particle Cloud Explosion (Monochrome White).
 *
 * <p>Features:
 * <ul>
 *   <li>50,000 pure white 3D particles radiating from the core</li>
 *   <li>Phosphor trail decay with radial perspective projection</li>
 *   <li>Zero-allocation contiguous raster buffer rendering</li>
 *   <li>Native VSync & FastExecution heartbeat at 120 FPS</li>
 * </ul>
 */
public class PureParticleCloudDemo extends Canvas {

    private static final int WIDTH = 1173;
    private static final int HEIGHT = 610;

    private static final int PARTICLE_COUNT = 50_000;
    private static final float CUBE_SIZE = 500f;
    private static final float FOV = 400f;

    // Contiguous SIMD-friendly 3D Particle State Arrays
    private final float[] posX = new float[PARTICLE_COUNT];
    private final float[] posY = new float[PARTICLE_COUNT];
    private final float[] posZ = new float[PARTICLE_COUNT];
    private final float[] velX = new float[PARTICLE_COUNT];
    private final float[] velY = new float[PARTICLE_COUNT];
    private final float[] velZ = new float[PARTICLE_COUNT];
    private final float[] life = new float[PARTICLE_COUNT];
    private final float[] maxLife = new float[PARTICLE_COUNT];

    private BufferedImage screenBuffer;
    private int[] pixels;
    private final JFrame parentFrame;

    public PureParticleCloudDemo(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setIgnoreRepaint(true);

        initBuffers();
        init3DParticles();
    }

    private void initBuffers() {
        screenBuffer = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        pixels = ((DataBufferInt) screenBuffer.getRaster().getDataBuffer()).getData();
    }

    private void init3DParticles() {
        FastAnimation.setHeartbeatMode(HeartbeatMode.NATIVE_VSYNC);

        Random r = new Random(42);
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            resetParticle(i, r);
            life[i] = r.nextFloat() * maxLife[i];
        }
    }

    private void resetParticle(int i, Random r) {
        posX[i] = (r.nextFloat() - 0.5f) * 60f;
        posY[i] = (r.nextFloat() - 0.5f) * 60f;
        posZ[i] = (r.nextFloat() - 0.5f) * 60f;

        double theta = r.nextDouble() * 2 * Math.PI;
        double phi = Math.acos(2 * r.nextDouble() - 1);
        float speed = 0.8f + r.nextFloat() * 3.5f;

        velX[i] = (float) (Math.sin(phi) * Math.cos(theta) * speed);
        velY[i] = (float) (Math.sin(phi) * Math.sin(theta) * speed);
        velZ[i] = (float) (Math.cos(phi) * speed);

        maxLife[i] = 80f + r.nextFloat() * 140f;
        life[i] = 0f;
    }

    public void start() {
        createBufferStrategy(3);
        BufferStrategy bs = getBufferStrategy();

        new Thread(() -> {
            long lastFpsTime = System.nanoTime();
            int frames = 0;
            long frameTimeTarget = 1_000_000_000L / 60; // Locked 60 FPS target
            long lastRenderTime = System.nanoTime();
            Random r = new Random();

            while (true) {
                long nowLoop = System.nanoTime();
                if (nowLoop - lastRenderTime < frameTimeTarget) {
                    Thread.yield();
                    continue;
                }
                lastRenderTime = nowLoop;

                // 1. Fast Phosphor Trail Decay
                for (int i = 0; i < pixels.length; i++) {
                    int p = pixels[i];
                    int v = (p & 0xFF);
                    v = (v * 215) >> 8;
                    pixels[i] = (v << 16) | (v << 8) | v;
                }

                // 2. 3D Position Update & 3D->2D Projection
                for (int i = 0; i < PARTICLE_COUNT; i++) {
                    life[i] += 1.0f;
                    if (life[i] >= maxLife[i]) {
                        resetParticle(i, r);
                    }

                    float progress = life[i] / maxLife[i];
                    float alpha = 1.0f - progress * progress;

                    posX[i] += velX[i];
                    posY[i] += velY[i];
                    posZ[i] += velZ[i];

                    float zDepth = FOV + posZ[i] + CUBE_SIZE;
                    if (zDepth <= 1.0f) continue;

                    float scale = FOV / zDepth;
                    int sx = (int) (WIDTH / 2f + posX[i] * scale);
                    int sy = (int) (HEIGHT / 2f + posY[i] * scale);

                    if (sx >= 0 && sx < WIDTH && sy >= 0 && sy < HEIGHT) {
                        int intensity = (int) (Math.min(1.0f, scale * 1.8f) * alpha * 255);
                        intensity = Math.min(255, Math.max(0, intensity));

                        int current = pixels[sy * WIDTH + sx] & 0xFF;
                        int blended = Math.min(255, current + intensity);
                        pixels[sy * WIDTH + sx] = (blended << 16) | (blended << 8) | blended;
                    }
                }

                // 3. Present Frame
                Graphics g = bs.getDrawGraphics();
                g.drawImage(screenBuffer, 0, 0, null);
                g.dispose();
                bs.show();
                Toolkit.getDefaultToolkit().sync();

                // 4. FPS Counter in Window Title
                frames++;
                long now = System.nanoTime();
                if (now - lastFpsTime >= 1_000_000_000L) {
                    int fps = frames;
                    SwingUtilities.invokeLater(() ->
                            parentFrame.setTitle("FastAnimation — Pure 50,000 Particle Cloud | FPS: " + fps)
                    );
                    frames = 0;
                    lastFpsTime = now;
                }
            }
        }, "Render-Loop-Pure50k").start();
    }

    private static BufferedImage createRoundIcon() {
        BufferedImage icon = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = icon.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillOval(4, 4, 56, 56);
        g.dispose();
        return icon;
    }

    public static void main(String[] args) {
        System.setProperty("sun.java2d.opengl", "true");
        System.setProperty("sun.awt.noerasebackground", "true");

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("FastAnimation — Pure 50,000 Particle Cloud (Monochrome)");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);
            frame.setIgnoreRepaint(true);
            frame.setIconImage(createRoundIcon());

            PureParticleCloudDemo demo = new PureParticleCloudDemo(frame);
            frame.add(demo);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.addNotify();

            try {
                long hwnd = FastTheme.getWindowHandle(frame);
                FastTheme.setTitleBarDarkMode(hwnd, true);
                FastTheme.setTitleBarColor(hwnd, 0, 0, 0);
                FastTheme.setTitleBarTextColor(hwnd, 255, 255, 255);
            } catch (Exception ignored) {}

            frame.setVisible(true);
            demo.start();
        });
    }
}
