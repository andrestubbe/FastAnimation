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
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * FastAnimation Demo 2: Volumetric 3D FastTween Realm + 3 Dynamic RGB Color Emitters + Z-Buffer.
 *
 * <p>Features:
 * <ul>
 *   <li>3 Dynamic 3D Color Emitters (Red, Green, Blue) orbiting the cube perimeter and lighting spheres/particles based on 3D distance</li>
 *   <li>Full per-pixel Float Z-Buffer (Depth Buffer) for 100% correct 3D occlusion</li>
 *   <li>50,000 Particles & 300 Spheres colored dynamically by RGB light fields + depth fog</li>
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

    // Atmospheric Fog Range
    private static final float FOG_NEAR = 150f;
    private static final float FOG_FAR = 1550f;

    // ---------------------------------------------------------
    // 3D Sphere Model with Projected Z-Depth
    // ---------------------------------------------------------
    private static class Ball {
        float x, y, z;
        float boidOffsetX, boidOffsetY, boidOffsetZ;
        float radiusScale = 1.0f;
        float rotX, rotY, rotZ;
        float zDepth;
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
    private final float[] zBuffer = new float[WIDTH * HEIGHT];
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

        // 2. Initialize 50,000 Particles
        Random r = new Random(42);
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            int bIdx = i % BALL_COUNT;
            targetBallIndex[i] = bIdx;

            orbitRadius[i] = 20.0f + r.nextFloat() * 110.0f;
            orbitAngle[i] = r.nextFloat() * (float) (2 * Math.PI);
            orbitSpeed[i] = (r.nextBoolean() ? 1 : -1) * (0.005f + r.nextFloat() * 0.012f);
            orbitTilt[i] = r.nextFloat() * (float) Math.PI;

            float sRoll = r.nextFloat();
            if (sRoll > 0.94f) {
                particleBaseSize[i] = 3.5f + r.nextFloat() * 2.0f;
            } else if (sRoll > 0.70f) {
                particleBaseSize[i] = 2.0f + r.nextFloat() * 1.2f;
            } else {
                particleBaseSize[i] = 1.0f + r.nextFloat() * 0.6f;
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
    // Gentle Local Repulsion Offset
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
    // Color Emitter Light Field Calculator (RGB based on 3D distance)
    // ---------------------------------------------------------
    private static int computeRgbColor(float px, float py, float pz, float fog,
                                       float rEx, float rEy, float rEz,
                                       float gEx, float gEy, float gEz,
                                       float bEx, float bEy, float bEz) {
        float lightRadius = 750f;

        // Red emitter distance
        float rDx = px - rEx, rDy = py - rEy, rDz = pz - rEz;
        float rDist = (float) Math.sqrt(rDx * rDx + rDy * rDy + rDz * rDz);
        float rWeight = Math.max(0.15f, 1.0f - (rDist / lightRadius));

        // Green emitter distance
        float gDx = px - gEx, gDy = py - gEy, gDz = pz - gEz;
        float gDist = (float) Math.sqrt(gDx * gDx + gDy * gDy + gDz * gDz);
        float gWeight = Math.max(0.15f, 1.0f - (gDist / lightRadius));

        // Blue emitter distance
        float bDx = px - bEx, bDy = py - bEy, bDz = pz - bEz;
        float bDist = (float) Math.sqrt(bDx * bDx + bDy * bDy + bDz * bDz);
        float bWeight = Math.max(0.15f, 1.0f - (bDist / lightRadius));

        // Combine with depth fog
        int cr = (int) (Math.min(1.0f, rWeight * 1.3f) * fog * 255);
        int cg = (int) (Math.min(1.0f, gWeight * 1.3f) * fog * 255);
        int cb = (int) (Math.min(1.0f, bWeight * 1.3f) * fog * 255);

        cr = Math.min(255, Math.max(25, cr));
        cg = Math.min(255, Math.max(25, cg));
        cb = Math.min(255, Math.max(25, cb));

        return (cr << 16) | (cg << 8) | cb;
    }

    // ---------------------------------------------------------
    // Combined High-Speed Render Loop with RGB Light Emitters & Z-Buffer
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
            float lightPhase = 0f;

            while (true) {
                long nowLoop = System.nanoTime();
                if (nowLoop - lastRenderTime < frameTimeTarget) {
                    Thread.yield();
                    continue;
                }
                lastRenderTime = nowLoop;
                lightPhase += 0.018f;

                // 1. Gentle Sphere Separation
                updateGentleSeparation();

                // 2. 3 Dynamic RGB Light Emitter Positions orbiting in 3D
                float rEx = (float) Math.cos(lightPhase) * 550f;
                float rEy = (float) Math.sin(lightPhase * 0.7f) * 400f;
                float rEz = (float) Math.sin(lightPhase) * 550f;

                float gEx = (float) Math.cos(lightPhase + 2.094f) * 550f;
                float gEy = (float) Math.sin((lightPhase + 2.094f) * 0.7f) * 400f;
                float gEz = (float) Math.sin(lightPhase + 2.094f) * 550f;

                float bEx = (float) Math.cos(lightPhase + 4.188f) * 550f;
                float bEy = (float) Math.sin((lightPhase + 4.188f) * 0.7f) * 400f;
                float bEz = (float) Math.sin(lightPhase + 4.188f) * 550f;

                // 3. Slow, graceful 3D Camera Orbit
                camYaw += 0.002f;
                camPitch = (float) Math.sin(camYaw * 0.5f) * 0.2f;

                float cosY = (float) Math.cos(camYaw);
                float sinY = (float) Math.sin(camYaw);
                float cosP = (float) Math.cos(camPitch);
                float sinP = (float) Math.sin(camPitch);

                // 4. Crisp Screen & Z-Buffer Reset
                Arrays.fill(pixels, 0);
                Arrays.fill(zBuffer, Float.MAX_VALUE);

                // 5. Rasterize 300 Spheres into Z-Buffer & Pixel Buffer with Dynamic RGB Lighting
                for (Ball b : balls) {
                    float bx = b.x + b.boidOffsetX;
                    float by = b.y + b.boidOffsetY;
                    float bz = b.z + b.boidOffsetZ;

                    b.rotX = bx * cosY - bz * sinY;
                    float rz = bx * sinY + bz * cosY;
                    b.rotY = by * cosP - rz * sinP;
                    b.rotZ = by * sinP + rz * cosP;
                    b.zDepth = FOV + b.rotZ + CUBE_SIZE;

                    if (b.zDepth <= 1.0f) continue;

                    float scale = FOV / b.zDepth;
                    int sx = (int) (WIDTH / 2f + b.rotX * scale);
                    int sy = (int) (HEIGHT / 2f + b.rotY * scale);
                    int radius = (int) (48f * scale * b.radiusScale);

                    if (radius <= 0) continue;

                    float fog = 1.0f - ((b.zDepth - FOG_NEAR) / (FOG_FAR - FOG_NEAR));
                    fog = Math.max(0.08f, Math.min(1.0f, fog));

                    int rgb = computeRgbColor(bx, by, bz, fog, rEx, rEy, rEz, gEx, gEy, gEz, bEx, bEy, bEz);

                    int radSq = radius * radius;
                    int minX = Math.max(0, sx - radius);
                    int maxX = Math.min(WIDTH - 1, sx + radius);
                    int minY = Math.max(0, sy - radius);
                    int maxY = Math.min(HEIGHT - 1, sy + radius);

                    for (int py = minY; py <= maxY; py++) {
                        int dy = py - sy;
                        int dySq = dy * dy;
                        int rowOffset = py * WIDTH;

                        for (int px = minX; px <= maxX; px++) {
                            int dx = px - sx;
                            int distSq = dx * dx + dySq;
                            if (distSq <= radSq) {
                                float dz = (float) Math.sqrt(radSq - distSq) / scale;
                                float pixelZ = b.zDepth - dz;

                                int idx = rowOffset + px;
                                if (pixelZ < zBuffer[idx]) {
                                    zBuffer[idx] = pixelZ;
                                    pixels[idx] = rgb;
                                }
                            }
                        }
                    }
                }

                // 6. Volumetric Orbit Particles with Dynamic RGB Lighting & Z-Buffer Occlusion
                for (int i = 0; i < PARTICLE_COUNT; i++) {
                    int bIdx = targetBallIndex[i];
                    Ball parent = balls.get(bIdx);

                    orbitAngle[i] += orbitSpeed[i];

                    // Probabilistic migration
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

                    float fog = 1.0f - ((zDepth - FOG_NEAR) / (FOG_FAR - FOG_NEAR));
                    fog = Math.max(0.08f, Math.min(1.0f, fog));

                    int rgb = computeRgbColor(posX[i], posY[i], posZ[i], fog, rEx, rEy, rEz, gEx, gEy, gEz, bEx, bEy, bEz);

                    float scale = FOV / zDepth;
                    int sx = (int) (WIDTH / 2f + rx * scale);
                    int sy = (int) (HEIGHT / 2f + ry * scale);

                    float pSize = particleBaseSize[i] * scale;
                    int rad = (int) Math.max(1, pSize);
                    int radSq = rad * rad;

                    int minX = Math.max(0, sx - rad);
                    int maxX = Math.min(WIDTH - 1, sx + rad);
                    int minY = Math.max(0, sy - rad);
                    int maxY = Math.min(HEIGHT - 1, sy + rad);

                    for (int py = minY; py <= maxY; py++) {
                        int dy = py - sy;
                        int dySq = dy * dy;
                        int rowOffset = py * WIDTH;

                        for (int px = minX; px <= maxX; px++) {
                            int dx = px - sx;
                            int distSq = dx * dx + dySq;
                            if (distSq <= radSq) {
                                int idx = rowOffset + px;
                                if (zDepth < zBuffer[idx]) {
                                    zBuffer[idx] = zDepth;
                                    pixels[idx] = rgb;
                                }
                            }
                        }
                    }
                }

                // 7. Present Frame
                Graphics g = bs.getDrawGraphics();
                g.drawImage(screenBuffer, 0, 0, null);
                g.dispose();
                bs.show();
                Toolkit.getDefaultToolkit().sync();

                // 8. FPS Counter in Window Title
                frames++;
                long now = System.nanoTime();
                if (now - lastFpsTime >= 1_000_000_000L) {
                    int fps = frames;
                    SwingUtilities.invokeLater(() ->
                            parentFrame.setTitle("FastAnimation — 300 Spheres + 50,000 Particles (3x RGB Emitters + Z-Buffer) | FPS: " + fps)
                    );
                    frames = 0;
                    lastFpsTime = now;
                }
            }
        }, "Render-Loop-RGB-Emitters").start();
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
            JFrame frame = new JFrame("FastAnimation — 300 Spheres + 50,000 Particles (RGB Emitters)");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);
            frame.setIgnoreRepaint(true);
            frame.setIconImage(createRoundIcon());

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
