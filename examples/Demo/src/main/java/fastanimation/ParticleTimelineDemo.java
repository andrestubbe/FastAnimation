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
 * FastAnimation Demo 2: Volumetric 3D FastTween Realm + 50,000 Multi-Scale Star Dust Particles.
 *
 * <p>Features:
 * <ul>
 *   <li>300 Independent spheres moving via FastTween Quad-InOut axis keyframes with doubled separation</li>
 *   <li>50,000 Volumetric depth-scaled star particles (up to 5x5 soft luminous disks + core)</li>
 *   <li>Fluid gravitational follow physics with natural momentum drag & non-linear orbit spirals</li>
 *   <li>Gentle 3D orbital camera rotation (Yaw + Pitch matrix)</li>
 *   <li>Locked 60 FPS high-precision FastExecution heartbeat</li>
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
    // 3D Sphere Model
    // ---------------------------------------------------------
    private static class Ball {
        float x, y, z;
        float boidOffsetX, boidOffsetY, boidOffsetZ;
        float radiusScale = 1.0f;
    }

    private final List<Ball> balls = new ArrayList<>();

    // ---------------------------------------------------------
    // 50k Particle State Arrays (Tied to Spheres)
    // ---------------------------------------------------------
    private final float[] posX = new float[PARTICLE_COUNT];
    private final float[] posY = new float[PARTICLE_COUNT];
    private final float[] posZ = new float[PARTICLE_COUNT];
    private final float[] velX = new float[PARTICLE_COUNT];
    private final float[] velY = new float[PARTICLE_COUNT];
    private final float[] velZ = new float[PARTICLE_COUNT];
    private final int[] targetBallIndex = new int[PARTICLE_COUNT];
    private final float[] orbitRadius = new float[PARTICLE_COUNT];
    private final float[] orbitAngle = new float[PARTICLE_COUNT];
    private final float[] orbitSpeed = new float[PARTICLE_COUNT];
    private final float[] orbitTilt = new float[PARTICLE_COUNT];
    private final float[] particleBaseSize = new float[PARTICLE_COUNT];

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

        // 1. Initialize 300 Spheres with FastTween Axis Loops
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

        // 2. Initialize 50,000 Particles with varying physical base sizes
        Random r = new Random(42);
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            int bIdx = i % BALL_COUNT;
            targetBallIndex[i] = bIdx;

            orbitRadius[i] = 20.0f + r.nextFloat() * 110.0f;
            orbitAngle[i] = r.nextFloat() * (float) (2 * Math.PI);
            orbitSpeed[i] = (r.nextBoolean() ? 1 : -1) * (0.005f + r.nextFloat() * 0.012f);
            orbitTilt[i] = r.nextFloat() * (float) Math.PI;

            // Varied particle sizes: mostly dust (1.0), some bright star sparks (2.5 - 4.5)
            float sRoll = r.nextFloat();
            if (sRoll > 0.94f) {
                particleBaseSize[i] = 3.5f + r.nextFloat() * 2.0f; // Big glowing star cores
            } else if (sRoll > 0.70f) {
                particleBaseSize[i] = 2.0f + r.nextFloat() * 1.2f; // Medium nodes
            } else {
                particleBaseSize[i] = 1.0f + r.nextFloat() * 0.6f; // Fine dust
            }

            Ball b = balls.get(bIdx);
            posX[i] = b.x;
            posY[i] = b.y;
            posZ[i] = b.z;
        }
    }

    // ---------------------------------------------------------
    // FastTween Axis Animations
    // ---------------------------------------------------------
    private void animateAxisX(Ball b) {
        float current = b.x;
        float target = (float) ((Math.random() * CUBE_SIZE * 2) - CUBE_SIZE);
        long duration = (long) (Math.abs(target - current) / CUBE_SIZE * 2200 + 1200 + Math.random() * 1200);
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
        long duration = (long) (Math.abs(target - current) / CUBE_SIZE * 2200 + 1200 + Math.random() * 1200);
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
        long duration = (long) (Math.abs(target - current) / CUBE_SIZE * 2200 + 1200 + Math.random() * 1200);
        FastAnimation.parallel(
                FastTween.to(current, target, duration)
                        .ease(Ease.QUAD_IN_OUT)
                        .onUpdate(v -> b.z = v)
                        .onComplete(() -> animateAxisZ(b))
        ).start();
    }

    private void animateScale(Ball b) {
        float current = b.radiusScale;
        float target = 0.3f + (float) (Math.random() * 0.7f);
        long duration = (long) (1200 + Math.random() * 2000);
        FastAnimation.parallel(
                FastTween.to(current, target, duration)
                        .ease(Ease.QUAD_IN_OUT)
                        .onUpdate(v -> b.radiusScale = v)
                        .onComplete(() -> animateScale(b))
        ).start();
    }

    // ---------------------------------------------------------
    // Gentle Local Repulsion Offset (Keeps spheres separated smoothly)
    // ---------------------------------------------------------
    private void updateGentleSeparation() {
        float sepDist = 180.0f;
        for (int i = 0; i < BALL_COUNT; i++) {
            Ball b = balls.get(i);
            float sx = 0, sy = 0, sz = 0;

            for (int j = 0; j < BALL_COUNT; j++) {
                if (i == j) continue;
                Ball other = balls.get(j);

                float dx = (b.x + b.boidOffsetX) - (other.x + other.boidOffsetX);
                float dy = (b.y + b.boidOffsetY) - (other.y + other.boidOffsetY);
                float dz = (b.z + b.boidOffsetZ) - (other.z + other.boidOffsetZ);
                float distSq = dx * dx + dy * dy + dz * dz;

                if (distSq > 0 && distSq < sepDist * sepDist) {
                    float d = (float) Math.sqrt(distSq);
                    float force = (sepDist - d) / sepDist;
                    sx += (dx / d) * force * 2.4f;
                    sy += (dy / d) * force * 2.4f;
                    sz += (dz / d) * force * 2.4f;
                }
            }

            b.boidOffsetX += (sx - b.boidOffsetX) * 0.08f;
            b.boidOffsetY += (sy - b.boidOffsetY) * 0.08f;
            b.boidOffsetZ += (sz - b.boidOffsetZ) * 0.08f;
        }
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

                // 1. Gentle Sphere Separation
                updateGentleSeparation();

                // 2. Slow, graceful 3D Camera Orbit
                camYaw += 0.002f;
                camPitch = (float) Math.sin(camYaw * 0.5f) * 0.2f;

                float cosY = (float) Math.cos(camYaw);
                float sinY = (float) Math.sin(camYaw);
                float cosP = (float) Math.cos(camPitch);
                float sinP = (float) Math.sin(camPitch);

                // 3. Crisp Black Screen Clear
                java.util.Arrays.fill(pixels, 0);

                // 4. Volumetric Orbit Particles Update & Multi-Pixel Glow Splatting
                for (int i = 0; i < PARTICLE_COUNT; i++) {
                    int bIdx = targetBallIndex[i];
                    Ball parent = balls.get(bIdx);

                    orbitAngle[i] += orbitSpeed[i];

                    // Probabilistic migration (0.12% chance to drift to another sphere)
                    if (r.nextInt(800) == 0) {
                        targetBallIndex[i] = r.nextInt(BALL_COUNT);
                        orbitRadius[i] = 20.0f + r.nextFloat() * 110.0f;
                        orbitSpeed[i] = (r.nextBoolean() ? 1 : -1) * (0.005f + r.nextFloat() * 0.012f);
                    }

                    float radius = orbitRadius[i] * parent.radiusScale;
                    float tilt = orbitTilt[i];

                    float ox = (float) (Math.cos(orbitAngle[i]) * radius);
                    float oy = (float) (Math.sin(orbitAngle[i]) * Math.cos(tilt) * radius);
                    float oz = (float) (Math.sin(orbitAngle[i]) * Math.sin(tilt) * radius);

                    float targetX = parent.x + parent.boidOffsetX + ox;
                    float targetY = parent.y + parent.boidOffsetY + oy;
                    float targetZ = parent.z + parent.boidOffsetZ + oz;

                    // Physics momentum with smooth damping (eliminates artificial linear snapping)
                    velX[i] = (velX[i] + (targetX - posX[i]) * 0.035f) * 0.90f;
                    velY[i] = (velY[i] + (targetY - posY[i]) * 0.035f) * 0.90f;
                    velZ[i] = (velZ[i] + (targetZ - posZ[i]) * 0.035f) * 0.90f;

                    posX[i] += velX[i];
                    posY[i] += velY[i];
                    posZ[i] += velZ[i];

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

                    float pSize = particleBaseSize[i] * scale;

                    if (sx >= 2 && sx < WIDTH - 2 && sy >= 2 && sy < HEIGHT - 2) {
                        int coreIntensity = (int) (Math.min(1.0f, scale * 1.7f) * 255);
                        int centerIdx = sy * WIDTH + sx;

                        // Center core
                        blendPixel(centerIdx, coreIntensity);

                        // If particle is larger or closer to camera, splat volumetric 3x3 or 5x5 disk
                        if (pSize > 1.8f) {
                            int halo1 = coreIntensity >> 1; // 50% brightness inner halo
                            int halo2 = coreIntensity >> 3; // 12% outer flare

                            // 3x3 inner diamond
                            blendPixel(centerIdx - 1, halo1);
                            blendPixel(centerIdx + 1, halo1);
                            blendPixel(centerIdx - WIDTH, halo1);
                            blendPixel(centerIdx + WIDTH, halo1);

                            if (pSize > 3.0f) {
                                // 5x5 outer cross for massive star nodes
                                blendPixel(centerIdx - 2, halo2);
                                blendPixel(centerIdx + 2, halo2);
                                blendPixel(centerIdx - WIDTH * 2, halo2);
                                blendPixel(centerIdx + WIDTH * 2, halo2);
                                blendPixel(centerIdx - WIDTH - 1, halo2);
                                blendPixel(centerIdx - WIDTH + 1, halo2);
                                blendPixel(centerIdx + WIDTH - 1, halo2);
                                blendPixel(centerIdx + WIDTH + 1, halo2);
                            }
                        } else {
                            // Sub-pixel 3x3 faint flare for small dust
                            int glow = coreIntensity >> 3;
                            blendPixel(centerIdx - 1, glow);
                            blendPixel(centerIdx + 1, glow);
                            blendPixel(centerIdx - WIDTH, glow);
                            blendPixel(centerIdx + WIDTH, glow);
                        }
                    }
                }

                // 5. Render Large FastTween Spheres
                Graphics2D g2d = screenBuffer.createGraphics();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.WHITE);

                for (Ball b : balls) {
                    float bx = b.x + b.boidOffsetX;
                    float by = b.y + b.boidOffsetY;
                    float bz = b.z + b.boidOffsetZ;

                    float rx = bx * cosY - bz * sinY;
                    float rz = bx * sinY + bz * cosY;
                    float ry = by * cosP - rz * sinP;
                    rz = by * sinP + rz * cosP;

                    float zDepth = FOV + rz + CUBE_SIZE;
                    if (zDepth <= 0.1f) continue;

                    float scale = FOV / zDepth;
                    float screenX = WIDTH / 2f + rx * scale;
                    float screenY = HEIGHT / 2f + ry * scale;
                    float radius = 48f * scale * b.radiusScale;

                    if (radius > 0) {
                        ellipse2D.setFrame(screenX - radius, screenY - radius, radius * 2, radius * 2);
                        g2d.fill(ellipse2D);
                    }
                }
                g2d.dispose();

                // 6. Present Frame
                Graphics g = bs.getDrawGraphics();
                g.drawImage(screenBuffer, 0, 0, null);
                g.dispose();
                bs.show();
                Toolkit.getDefaultToolkit().sync();

                // 7. FPS Counter in Window Title
                frames++;
                long now = System.nanoTime();
                if (now - lastFpsTime >= 1_000_000_000L) {
                    int fps = frames;
                    SwingUtilities.invokeLater(() ->
                            parentFrame.setTitle("FastAnimation — 300 Spheres + 50,000 Volumetric Stars | FPS: " + fps)
                    );
                    frames = 0;
                    lastFpsTime = now;
                }
            }
        }, "Render-Loop-Volumetric").start();
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
            JFrame frame = new JFrame("FastAnimation — 300 Spheres + 50,000 Volumetric Stars");
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
