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
 * FastAnimation Demo 2: Cinematic 3D Particle Realm.
 *
 * <p>Features:
 * <ul>
 *   <li>300 large, smoothly tweened monochrome spheres (FastTween Quad-InOut interpolation)</li>
 *   <li>50,000 harmonic swirling 3D particles tracking the spheres with dynamic gravitational migration</li>
 *   <li>Continuous cinematic 3D orbital camera rotation (Yaw + Pitch matrix)</li>
 *   <li>Sub-pixel Gaussian bloom & glow flare kernel (3x3 luminous cross-splat)</li>
 *   <li>Phosphor trail decay for smooth optical motion blur at locked 60 FPS</li>
 * </ul>
 */
public class ParticleTimelineDemo extends Canvas {

    private static final int WIDTH = 1173;
    private static final int HEIGHT = 610;

    private static final int BALL_COUNT = 300;
    private static final int PARTICLE_COUNT = 50_000;
    private static final float CUBE_SIZE = 600f;
    private static final float FOV = 450f;

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

            orbitRadius[i] = 40.0f + r.nextFloat() * 220.0f;
            orbitAngle[i] = r.nextFloat() * (float) (2 * Math.PI);
            orbitSpeed[i] = (r.nextBoolean() ? 1 : -1) * (0.02f + r.nextFloat() * 0.07f);
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
            Random r = new Random();

            float camYaw = 0f;
            float camPitch = 0f;

            while (true) {
                long nowLoop = System.nanoTime();
                if (nowLoop - lastRenderTime < frameTimeTarget) {
                    Thread.yield();
                    continue;
                }
                lastRenderTime = nowLoop;

                // 1. Slow, majestic 3D Camera Orbit
                camYaw += 0.0035f;
                camPitch = (float) Math.sin(camYaw * 0.5f) * 0.25f;

                float cosY = (float) Math.cos(camYaw);
                float sinY = (float) Math.sin(camYaw);
                float cosP = (float) Math.cos(camPitch);
                float sinP = (float) Math.sin(camPitch);

                // 2. Phosphor Trail Decay with Soft Contrast Retention
                for (int i = 0; i < pixels.length; i++) {
                    int p = pixels[i];
                    int v = (p & 0xFF);
                    v = (v * 198) >> 8; // gentle optical fade
                    pixels[i] = (v << 16) | (v << 8) | v;
                }

                // 3. Swarm Physics & Bloom Splatting for 50,000 Particles
                for (int i = 0; i < PARTICLE_COUNT; i++) {
                    int bIdx = targetBallIndex[i];
                    Ball parent = balls.get(bIdx);

                    orbitAngle[i] += orbitSpeed[i];

                    // Probabilistic sphere migration (0.4% chance to leap to another sphere)
                    if (r.nextInt(250) == 0) {
                        targetBallIndex[i] = r.nextInt(BALL_COUNT);
                        orbitRadius[i] = 40.0f + r.nextFloat() * 220.0f;
                        orbitSpeed[i] = (r.nextBoolean() ? 1 : -1) * (0.02f + r.nextFloat() * 0.07f);
                    }

                    float radius = orbitRadius[i] * parent.radiusScale;
                    float tilt = orbitTilt[i];

                    float ox = (float) (Math.cos(orbitAngle[i]) * radius);
                    float oy = (float) (Math.sin(orbitAngle[i]) * Math.cos(tilt) * radius);
                    float oz = (float) (Math.sin(orbitAngle[i]) * Math.sin(tilt) * radius);

                    // Fluid gravitational pull toward parent sphere
                    posX[i] += (parent.x + ox - posX[i]) * 0.09f;
                    posY[i] += (parent.y + oy - posY[i]) * 0.09f;
                    posZ[i] += (parent.z + oz - posZ[i]) * 0.09f;

                    // Camera 3D Rotation Transform (Yaw + Pitch)
                    float rx = posX[i] * cosY - posZ[i] * sinY;
                    float rz = posX[i] * sinY + posZ[i] * cosY;
                    float ry = posY[i] * cosP - rz * sinP;
                    rz = posY[i] * sinP + rz * cosP;

                    // Perspective Projection
                    float zDepth = FOV + rz + CUBE_SIZE;
                    if (zDepth <= 1.0f) continue;

                    float scale = FOV / zDepth;
                    int sx = (int) (WIDTH / 2f + rx * scale);
                    int sy = (int) (HEIGHT / 2f + ry * scale);

                    if (sx >= 1 && sx < WIDTH - 1 && sy >= 1 && sy < HEIGHT - 1) {
                        int coreIntensity = (int) (Math.min(1.0f, scale * 1.6f) * 255);
                        int glowIntensity = coreIntensity >> 2; // Sub-pixel flare halo

                        int centerIdx = sy * WIDTH + sx;

                        // Center core
                        int cur = pixels[centerIdx] & 0xFF;
                        int bld = Math.min(255, cur + coreIntensity);
                        pixels[centerIdx] = (bld << 16) | (bld << 8) | bld;

                        // 3x3 Luminous Glow Flare
                        blendPixel(centerIdx - 1, glowIntensity);
                        blendPixel(centerIdx + 1, glowIntensity);
                        blendPixel(centerIdx - WIDTH, glowIntensity);
                        blendPixel(centerIdx + WIDTH, glowIntensity);
                    }
                }

                // 4. Render Large Tweened Spheres with Depth Scaling
                Graphics2D g2d = screenBuffer.createGraphics();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.WHITE);

                for (Ball b : balls) {
                    float rx = b.x * cosY - b.z * sinY;
                    float rz = b.x * sinY + b.z * cosY;
                    float ry = b.y * cosP - rz * sinP;
                    rz = b.y * sinP + rz * cosP;

                    float zDepth = FOV + rz + CUBE_SIZE;
                    if (zDepth <= 0.1f) continue;

                    float scale = FOV / zDepth;
                    float screenX = WIDTH / 2f + rx * scale;
                    float screenY = HEIGHT / 2f + ry * scale;
                    float radius = 50f * scale * b.radiusScale;

                    if (radius > 0) {
                        ellipse2D.setFrame(screenX - radius, screenY - radius, radius * 2, radius * 2);
                        g2d.fill(ellipse2D);
                    }
                }
                g2d.dispose();

                // 5. Present Frame
                Graphics g = bs.getDrawGraphics();
                g.drawImage(screenBuffer, 0, 0, null);
                g.dispose();
                bs.show();
                Toolkit.getDefaultToolkit().sync();

                // 6. FPS Counter in Window Title
                frames++;
                long now = System.nanoTime();
                if (now - lastFpsTime >= 1_000_000_000L) {
                    int fps = frames;
                    SwingUtilities.invokeLater(() ->
                            parentFrame.setTitle("FastAnimation — Cinematic 3D Orbit Swarm (50,000 Particles + Glow) | FPS: " + fps)
                    );
                    frames = 0;
                    lastFpsTime = now;
                }
            }
        }, "Render-Loop-Cinematic").start();
    }

    private void blendPixel(int index, int add) {
        int cur = pixels[index] & 0xFF;
        int res = Math.min(255, cur + add);
        pixels[index] = (res << 16) | (res << 8) | res;
    }

    public static void main(String[] args) {
        System.setProperty("sun.java2d.opengl", "true");
        System.setProperty("sun.awt.noerasebackground", "true");

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("FastAnimation — Cinematic 3D Orbit Swarm (50,000 Particles)");
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
