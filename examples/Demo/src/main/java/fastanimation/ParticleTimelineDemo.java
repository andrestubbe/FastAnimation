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
 * FastAnimation Demo 2: 3D BOIDS Flocking Realm + 50,000 Particle Cloud.
 *
 * <p>Features:
 * <ul>
 *   <li>300 Spheres driven by real-time 3D BOIDS flocking physics (Separation, Alignment, Cohesion)</li>
 *   <li>50,000 Swarm particles with smooth gravitational orbits tracking the boid flock</li>
 *   <li>Continuous cinematic 3D orbital camera rotation (Yaw + Pitch matrix)</li>
 *   <li>Sub-pixel Gaussian bloom & glow flare kernel (3x3 luminous cross-splat)</li>
 *   <li>Locked 60 FPS high-precision FastExecution heartbeat</li>
 * </ul>
 */
public class ParticleTimelineDemo extends Canvas {

    private static final int WIDTH = 1173;
    private static final int HEIGHT = 610;

    private static final int BALL_COUNT = 300;
    private static final int PARTICLE_COUNT = 50_000;
    private static final float CUBE_SIZE = 550f;
    private static final float FOV = 450f;

    // BOIDS Physics Parameters
    private static final float MAX_SPEED = 5.5f;
    private static final float MAX_FORCE = 0.18f;
    private static final float DESIRED_SEPARATION = 45.0f;
    private static final float NEIGHBOR_DIST = 130.0f;

    private static final Ellipse2D ellipse2D = new Ellipse2D.Float();

    // ---------------------------------------------------------
    // 3D Boid Ball Model
    // ---------------------------------------------------------
    private static class Boid {
        float x, y, z;
        float vx, vy, vz;
        float radiusScale = 1.0f;
    }

    private final List<Boid> boids = new ArrayList<>();

    // ---------------------------------------------------------
    // 50k Particle State Arrays (Tied to Boids)
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

        // 1. Initialize 300 3D Boids
        Random r = new Random(42);
        for (int i = 0; i < BALL_COUNT; i++) {
            Boid b = new Boid();
            b.x = (r.nextFloat() * CUBE_SIZE * 2) - CUBE_SIZE;
            b.y = (r.nextFloat() * CUBE_SIZE * 2) - CUBE_SIZE;
            b.z = (r.nextFloat() * CUBE_SIZE * 2) - CUBE_SIZE;

            double theta = r.nextDouble() * 2 * Math.PI;
            double phi = Math.acos(2 * r.nextDouble() - 1);
            float speed = 2.0f + r.nextFloat() * 2.0f;

            b.vx = (float) (Math.sin(phi) * Math.cos(theta) * speed);
            b.vy = (float) (Math.sin(phi) * Math.sin(theta) * speed);
            b.vz = (float) (Math.cos(phi) * speed);

            boids.add(b);
            animateScale(b);
        }

        // 2. Initialize 50,000 Particles tied to the 300 boids
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            int bIdx = i % BALL_COUNT;
            targetBallIndex[i] = bIdx;

            orbitRadius[i] = 18.0f + r.nextFloat() * 105.0f;
            orbitAngle[i] = r.nextFloat() * (float) (2 * Math.PI);
            orbitSpeed[i] = (r.nextBoolean() ? 1 : -1) * (0.02f + r.nextFloat() * 0.07f);
            orbitTilt[i] = r.nextFloat() * (float) Math.PI;

            Boid b = boids.get(bIdx);
            posX[i] = b.x;
            posY[i] = b.y;
            posZ[i] = b.z;
        }
    }

    private void animateScale(Boid b) {
        float current = b.radiusScale;
        float target = 0.3f + (float) (Math.random() * 0.7f);
        long duration = (long) (1000 + Math.random() * 2000);
        FastAnimation.parallel(
                FastTween.to(current, target, duration)
                        .ease(Ease.QUAD_IN_OUT)
                        .onUpdate(v -> b.radiusScale = v)
                        .onComplete(() -> animateScale(b))
        ).start();
    }

    // ---------------------------------------------------------
    // 3D BOIDS Flocking Simulation Step
    // ---------------------------------------------------------
    private void updateBoids() {
        for (int i = 0; i < BALL_COUNT; i++) {
            Boid b = boids.get(i);

            float sepX = 0, sepY = 0, sepZ = 0;
            float aliX = 0, aliY = 0, aliZ = 0;
            float cohX = 0, cohY = 0, cohZ = 0;
            int count = 0;
            int sepCount = 0;

            for (int j = 0; j < BALL_COUNT; j++) {
                if (i == j) continue;
                Boid other = boids.get(j);

                float dx = b.x - other.x;
                float dy = b.y - other.y;
                float dz = b.z - other.z;
                float distSq = dx * dx + dy * dy + dz * dz;

                if (distSq > 0 && distSq < NEIGHBOR_DIST * NEIGHBOR_DIST) {
                    float d = (float) Math.sqrt(distSq);
                    aliX += other.vx;
                    aliY += other.vy;
                    aliZ += other.vz;

                    cohX += other.x;
                    cohY += other.y;
                    cohZ += other.z;
                    count++;

                    if (d < DESIRED_SEPARATION) {
                        sepX += dx / (d * d);
                        sepY += dy / (d * d);
                        sepZ += dz / (d * d);
                        sepCount++;
                    }
                }
            }

            // Apply Flocking Forces
            float ax = 0, ay = 0, az = 0;

            if (sepCount > 0) {
                ax += sepX * 1.5f;
                ay += sepY * 1.5f;
                az += sepZ * 1.5f;
            }

            if (count > 0) {
                // Alignment
                aliX /= count;
                aliY /= count;
                aliZ /= count;
                ax += (aliX - b.vx) * 0.05f;
                ay += (aliY - b.vy) * 0.05f;
                az += (aliZ - b.vz) * 0.05f;

                // Cohesion
                cohX /= count;
                cohY /= count;
                cohZ /= count;
                ax += (cohX - b.x) * 0.003f;
                ay += (cohY - b.y) * 0.003f;
                az += (cohZ - b.z) * 0.003f;
            }

            // Boundary containment box
            float boundForce = 0.08f;
            if (b.x > CUBE_SIZE) ax -= boundForce * (b.x - CUBE_SIZE);
            else if (b.x < -CUBE_SIZE) ax -= boundForce * (b.x + CUBE_SIZE);
            if (b.y > CUBE_SIZE) ay -= boundForce * (b.y - CUBE_SIZE);
            else if (b.y < -CUBE_SIZE) ay -= boundForce * (b.y + CUBE_SIZE);
            if (b.z > CUBE_SIZE) az -= boundForce * (b.z - CUBE_SIZE);
            else if (b.z < -CUBE_SIZE) az -= boundForce * (b.z + CUBE_SIZE);

            // Limit acceleration & update velocity
            b.vx += Math.max(-MAX_FORCE, Math.min(MAX_FORCE, ax));
            b.vy += Math.max(-MAX_FORCE, Math.min(MAX_FORCE, ay));
            b.vz += Math.max(-MAX_FORCE, Math.min(MAX_FORCE, az));

            // Limit Max Speed
            float speed = (float) Math.sqrt(b.vx * b.vx + b.vy * b.vy + b.vz * b.vz);
            if (speed > MAX_SPEED) {
                b.vx = (b.vx / speed) * MAX_SPEED;
                b.vy = (b.vy / speed) * MAX_SPEED;
                b.vz = (b.vz / speed) * MAX_SPEED;
            }

            b.x += b.vx;
            b.y += b.vy;
            b.z += b.vz;
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

                // 1. Update 300 BOIDS flocking simulation
                updateBoids();

                // 2. Slow, majestic 3D Camera Orbit
                camYaw += 0.0035f;
                camPitch = (float) Math.sin(camYaw * 0.5f) * 0.25f;

                float cosY = (float) Math.cos(camYaw);
                float sinY = (float) Math.sin(camYaw);
                float cosP = (float) Math.cos(camPitch);
                float sinP = (float) Math.sin(camPitch);

                // 3. Crisp Black Screen Clear
                java.util.Arrays.fill(pixels, 0);

                // 4. Swarm Physics & Bloom Splatting for 50,000 Particles
                for (int i = 0; i < PARTICLE_COUNT; i++) {
                    int bIdx = targetBallIndex[i];
                    Boid parent = boids.get(bIdx);

                    orbitAngle[i] += orbitSpeed[i];

                    // Probabilistic boid hopping (0.3% chance to migrate)
                    if (r.nextInt(330) == 0) {
                        targetBallIndex[i] = r.nextInt(BALL_COUNT);
                        orbitRadius[i] = 18.0f + r.nextFloat() * 105.0f;
                        orbitSpeed[i] = (r.nextBoolean() ? 1 : -1) * (0.02f + r.nextFloat() * 0.07f);
                    }

                    float radius = orbitRadius[i] * parent.radiusScale;
                    float tilt = orbitTilt[i];

                    float ox = (float) (Math.cos(orbitAngle[i]) * radius);
                    float oy = (float) (Math.sin(orbitAngle[i]) * Math.cos(tilt) * radius);
                    float oz = (float) (Math.sin(orbitAngle[i]) * Math.sin(tilt) * radius);

                    // Fluid gravitational pull toward parent boid
                    posX[i] += (parent.x + ox - posX[i]) * 0.12f;
                    posY[i] += (parent.y + oy - posY[i]) * 0.12f;
                    posZ[i] += (parent.z + oz - posZ[i]) * 0.12f;

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
                        int glowIntensity = coreIntensity >> 2;

                        int centerIdx = sy * WIDTH + sx;

                        int cur = pixels[centerIdx] & 0xFF;
                        int bld = Math.min(255, cur + coreIntensity);
                        pixels[centerIdx] = (bld << 16) | (bld << 8) | bld;

                        blendPixel(centerIdx - 1, glowIntensity);
                        blendPixel(centerIdx + 1, glowIntensity);
                        blendPixel(centerIdx - WIDTH, glowIntensity);
                        blendPixel(centerIdx + WIDTH, glowIntensity);
                    }
                }

                // 5. Render Large BOID Spheres
                Graphics2D g2d = screenBuffer.createGraphics();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.WHITE);

                for (Boid b : boids) {
                    float rx = b.x * cosY - b.z * sinY;
                    float rz = b.x * sinY + b.z * cosY;
                    float ry = b.y * cosP - rz * sinP;
                    rz = b.y * sinP + rz * cosP;

                    float zDepth = FOV + rz + CUBE_SIZE;
                    if (zDepth <= 0.1f) continue;

                    float scale = FOV / zDepth;
                    float screenX = WIDTH / 2f + rx * scale;
                    float screenY = HEIGHT / 2f + ry * scale;
                    float radius = 46f * scale * b.radiusScale;

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
                            parentFrame.setTitle("FastAnimation — 300 BOIDS Flocking + 50,000 Particles | FPS: " + fps)
                    );
                    frames = 0;
                    lastFpsTime = now;
                }
            }
        }, "Render-Loop-Boids").start();
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
            JFrame frame = new JFrame("FastAnimation — 300 BOIDS Flocking + 50,000 Particles");
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
