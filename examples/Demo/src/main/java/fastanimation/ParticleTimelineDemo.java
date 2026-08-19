package fastanimation;

import fastanimation.AnimationEngine.HeartbeatMode;
import fasttheme.FastTheme;
import fasttween.Ease;
import fasttween.FastTween;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * FastAnimation Demo 2: Mass 3D Particle Cloud + Tweened Geometry Balls.
 *
 * <p>Features:
 * <ul>
 *   <li>300 large, smoothly tweened monochrome spheres (FastTween Quad-InOut interpolation)</li>
 *   <li>50,000 harmonic swirling 3D particles tracking the spheres in dynamic multi-orbit trails</li>
 *   <li>Phosphor trail decay for smooth motion blur</li>
 *   <li>Native VSync & FastExecution heartbeat at locked 60 FPS</li>
 * </ul>
 */
public class ParticleTimelineDemo extends Canvas {

    private static final int WIDTH = 1173;
    private static final int HEIGHT = 610;

    private static final int BALL_COUNT = 300;
    private static final int PARTICLE_COUNT = 50_000;
    private static final float CUBE_SIZE = 600f;
    private static final float FOV = 400f;

    private static final Ellipse2D ellipse2D = new Ellipse2D.Float();

    // ---------------------------------------------------------
    // 3D Ball Model
    // ---------------------------------------------------------
    private static class Ball {
        float x, y, z;
        float radiusScale = 1.0f;
    }

    private final List<Ball> balls = new ArrayList<>();

    // ---------------------------------------------------------
    // 50k Particle State Arrays (Tied to Spheres)
    // ---------------------------------------------------------
    private final float[] posX = new float[PARTICLE_COUNT];
    private final float[] posY = new float[PARTICLE_COUNT];
    private final float[] posZ = new float[PARTICLE_COUNT];
    private final int[] targetBallIndex = new int[PARTICLE_COUNT];
    private final float[] orbitRadius = new float[PARTICLE_COUNT];
    private final float[] orbitAngle = new float[PARTICLE_COUNT];
    private final float[] orbitSpeed = new float[PARTICLE_COUNT];
    private final float[] orbitTilt = new float[PARTICLE_COUNT];

    private BufferedImage screenBuffer;
    private int[] pixels;
    private final JFrame parentFrame;

    public ParticleTimelineDemo(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setIgnoreRepaint(true);

        initBuffers();
        init3DScene();
    }

    private void initBuffers() {
        screenBuffer = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        pixels = ((DataBufferInt) screenBuffer.getRaster().getDataBuffer()).getData();
    }

    private void init3DScene() {
        FastAnimation.setHeartbeatMode(HeartbeatMode.NATIVE_VSYNC);

        // 1. Initialize 300 Tweened Balls
        for (int i = 0; i < BALL_COUNT; i++) {
            Ball b = new Ball();
            b.x = (float) ((Math.random() * CUBE_SIZE * 2) - CUBE_SIZE);
            b.y = (float) ((Math.random() * CUBE_SIZE * 2) - CUBE_SIZE);
            b.z = (float) ((Math.random() * CUBE_SIZE * 2) - CUBE_SIZE);
            balls.add(b);

            animateAxisX(b);
            animateAxisY(b);
            animateAxisZ(b);
            animateScale(b);
        }

        // 2. Initialize 50,000 Particles tied to the 300 tweened spheres
        Random r = new Random(42);
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            int bIdx = i % BALL_COUNT;
            targetBallIndex[i] = bIdx;

            orbitRadius[i] = 12.0f + r.nextFloat() * 65.0f;
            orbitAngle[i] = r.nextFloat() * (float) (2 * Math.PI);
            orbitSpeed[i] = (r.nextBoolean() ? 1 : -1) * (0.03f + r.nextFloat() * 0.08f);
            orbitTilt[i] = r.nextFloat() * (float) Math.PI;

            Ball b = balls.get(bIdx);
            posX[i] = b.x;
            posY[i] = b.y;
            posZ[i] = b.z;
        }
    }

    // ---------------------------------------------------------
    // Ball Tween Loops (Infinite Quad-InOut)
    // ---------------------------------------------------------
    private void animateAxisX(Ball b) {
        float current = b.x;
        float target = (float) ((Math.random() * CUBE_SIZE * 2) - CUBE_SIZE);
        long duration = (long) (Math.abs(target - current) / CUBE_SIZE * 2000 + 1000 + Math.random() * 1000);
        FastAnimation.parallel(
                FastTween.to(current, target, duration)
                        .ease(Ease.QUAD_IN_OUT)
                        .onUpdate(v -> b.x = v)
                        .onComplete(() -> animateAxisX(b))
        ).start();
    }

    private void animateAxisY(Ball b) {
        float current = b.y;
        float target = (float) ((Math.random() * CUBE_SIZE * 2) - CUBE_SIZE);
        long duration = (long) (Math.abs(target - current) / CUBE_SIZE * 2000 + 1000 + Math.random() * 1000);
        FastAnimation.parallel(
                FastTween.to(current, target, duration)
                        .ease(Ease.QUAD_IN_OUT)
                        .onUpdate(v -> b.y = v)
                        .onComplete(() -> animateAxisY(b))
        ).start();
    }

    private void animateAxisZ(Ball b) {
        float current = b.z;
        float target = (float) ((Math.random() * CUBE_SIZE * 2) - CUBE_SIZE);
        long duration = (long) (Math.abs(target - current) / CUBE_SIZE * 2000 + 1000 + Math.random() * 1000);
        FastAnimation.parallel(
                FastTween.to(current, target, duration)
                        .ease(Ease.QUAD_IN_OUT)
                        .onUpdate(v -> b.z = v)
                        .onComplete(() -> animateAxisZ(b))
        ).start();
    }

    private void animateScale(Ball b) {
        float current = b.radiusScale;
        float target = 0.2f + (float) (Math.random() * 0.8f);
        long duration = (long) (1000 + Math.random() * 2000);
        FastAnimation.parallel(
                FastTween.to(current, target, duration)
                        .ease(Ease.QUAD_IN_OUT)
                        .onUpdate(v -> b.radiusScale = v)
                        .onComplete(() -> animateScale(b))
        ).start();
    }

    // ---------------------------------------------------------
    // Combined High-Speed Render Loop
    // ---------------------------------------------------------
    public void start() {
        createBufferStrategy(3);
        BufferStrategy bs = getBufferStrategy();

        new Thread(() -> {
            long lastFpsTime = System.nanoTime();
            int frames = 0;
            long frameTimeTarget = 1_000_000_000L / 60; // Locked 60 FPS target
            long lastRenderTime = System.nanoTime();

            while (true) {
                long nowLoop = System.nanoTime();
                if (nowLoop - lastRenderTime < frameTimeTarget) {
                    Thread.yield();
                    continue;
                }
                lastRenderTime = nowLoop;

                // 1. Phosphor Trail Decay
                for (int i = 0; i < pixels.length; i++) {
                    int p = pixels[i];
                    int v = (p & 0xFF);
                    v = (v * 195) >> 8; // decay factor
                    pixels[i] = (v << 16) | (v << 8) | v;
                }

                // 2. Swarm Motion Update: Particles follow and orbit their parent sphere
                for (int i = 0; i < PARTICLE_COUNT; i++) {
                    int bIdx = targetBallIndex[i];
                    Ball parent = balls.get(bIdx);

                    orbitAngle[i] += orbitSpeed[i];
                    float r = orbitRadius[i] * parent.radiusScale;
                    float tilt = orbitTilt[i];

                    float ox = (float) (Math.cos(orbitAngle[i]) * r);
                    float oy = (float) (Math.sin(orbitAngle[i]) * Math.cos(tilt) * r);
                    float oz = (float) (Math.sin(orbitAngle[i]) * Math.sin(tilt) * r);

                    // Smooth attraction to sphere center + orbit offset
                    posX[i] += (parent.x + ox - posX[i]) * 0.22f;
                    posY[i] += (parent.y + oy - posY[i]) * 0.22f;
                    posZ[i] += (parent.z + oz - posZ[i]) * 0.22f;

                    // 3D -> 2D Perspective Projection
                    float zDepth = FOV + posZ[i] + CUBE_SIZE;
                    if (zDepth <= 1.0f) continue;

                    float scale = FOV / zDepth;
                    int sx = (int) (WIDTH / 2f + posX[i] * scale);
                    int sy = (int) (HEIGHT / 2f + posY[i] * scale);

                    if (sx >= 0 && sx < WIDTH && sy >= 0 && sy < HEIGHT) {
                        int intensity = (int) (Math.min(1.0f, scale * 1.6f) * 240);
                        int current = pixels[sy * WIDTH + sx] & 0xFF;
                        int blended = Math.min(255, current + intensity);
                        pixels[sy * WIDTH + sx] = (blended << 16) | (blended << 8) | blended;
                    }
                }

                // 3. Render Large Tweened Spheres on Top
                Graphics2D g2d = screenBuffer.createGraphics();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.WHITE);

                for (Ball b : balls) {
                    float zDepth = FOV + b.z + CUBE_SIZE;
                    if (zDepth <= 0.1f) continue;

                    float scale = FOV / zDepth;
                    float screenX = WIDTH / 2f + b.x * scale;
                    float screenY = HEIGHT / 2f + b.y * scale;
                    float radius = 48f * scale * b.radiusScale;

                    if (radius > 0) {
                        ellipse2D.setFrame(screenX - radius, screenY - radius, radius * 2, radius * 2);
                        g2d.fill(ellipse2D);
                    }
                }
                g2d.dispose();

                // 4. Present Frame
                Graphics g = bs.getDrawGraphics();
                g.drawImage(screenBuffer, 0, 0, null);
                g.dispose();
                bs.show();
                Toolkit.getDefaultToolkit().sync();

                // 5. FPS Counter in Window Title
                frames++;
                long now = System.nanoTime();
                if (now - lastFpsTime >= 1_000_000_000L) {
                    int fps = frames;
                    SwingUtilities.invokeLater(() ->
                            parentFrame.setTitle("FastAnimation — 300 Spheres with 50,000 Orbit Particles | FPS: " + fps)
                    );
                    frames = 0;
                    lastFpsTime = now;
                }
            }
        }, "Render-Loop-Coupled").start();
    }

    public static void main(String[] args) {
        System.setProperty("sun.java2d.opengl", "true");
        System.setProperty("sun.awt.noerasebackground", "true");

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("FastAnimation — 300 Spheres with 50,000 Orbit Particles");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);
            frame.setIgnoreRepaint(true);

            ParticleTimelineDemo demo = new ParticleTimelineDemo(frame);
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
