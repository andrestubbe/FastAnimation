package fastanimation;

import fastanimation.AnimationEngine.HeartbeatMode;
import fastdwm.FastDWM;
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
 *   <li>50,000 harmonic swirling 3D particles tracking the moving cube potential fields</li>
 *   <li>Phosphor trail decay for smooth motion blur</li>
 *   <li>Native VSync & FastExecution heartbeat at 120 FPS</li>
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
    // 50k Harmonic Particle Arrays (Contiguous Memory)
    // ---------------------------------------------------------
    private final float[] posX = new float[PARTICLE_COUNT];
    private final float[] posY = new float[PARTICLE_COUNT];
    private final float[] posZ = new float[PARTICLE_COUNT];
    private final float[] velX = new float[PARTICLE_COUNT];
    private final float[] velY = new float[PARTICLE_COUNT];
    private final float[] velZ = new float[PARTICLE_COUNT];
    private final float[] phase = new float[PARTICLE_COUNT];
    private final float[] freq = new float[PARTICLE_COUNT];
    private final float[] amp = new float[PARTICLE_COUNT];

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

        // 2. Initialize 50,000 Particles with harmonic motion dynamics
        Random r = new Random(42);
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            posX[i] = (r.nextFloat() * CUBE_SIZE * 2) - CUBE_SIZE;
            posY[i] = (r.nextFloat() * CUBE_SIZE * 2) - CUBE_SIZE;
            posZ[i] = (r.nextFloat() * CUBE_SIZE * 2) - CUBE_SIZE;

            velX[i] = (r.nextFloat() - 0.5f) * 1.5f;
            velY[i] = (r.nextFloat() - 0.5f) * 1.5f;
            velZ[i] = (r.nextFloat() - 0.5f) * 1.5f;

            phase[i] = r.nextFloat() * (float) (2 * Math.PI);
            freq[i] = 0.02f + r.nextFloat() * 0.04f;
            amp[i] = 2.0f + r.nextFloat() * 4.0f;
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
            long frameTimeTarget = 1_000_000_000L / 120;
            long lastRenderTime = System.nanoTime();
            float globalStep = 0f;

            while (true) {
                long nowLoop = System.nanoTime();
                if (nowLoop - lastRenderTime < frameTimeTarget) {
                    Thread.yield();
                    continue;
                }
                lastRenderTime = nowLoop;
                globalStep += 0.016f;

                // 1. Phosphor Trail Decay
                for (int i = 0; i < pixels.length; i++) {
                    int p = pixels[i];
                    int v = (p & 0xFF);
                    v = (v * 190) >> 8; // decay factor
                    pixels[i] = (v << 16) | (v << 8) | v;
                }

                // 2. Swarm Motion Update & Direct Pixel Splat for 50,000 Particles
                for (int i = 0; i < PARTICLE_COUNT; i++) {
                    phase[i] += freq[i];

                    // Quad-InOut harmonic wave motion
                    float waveX = (float) Math.sin(phase[i]) * amp[i];
                    float waveY = (float) Math.cos(phase[i] * 0.7f) * amp[i];
                    float waveZ = (float) Math.sin(phase[i] * 1.3f) * amp[i];

                    posX[i] += velX[i] + waveX;
                    posY[i] += velY[i] + waveY;
                    posZ[i] += velZ[i] + waveZ;

                    // Cube bounds wrap-around with smooth ease bounce
                    if (posX[i] > CUBE_SIZE) posX[i] = -CUBE_SIZE;
                    else if (posX[i] < -CUBE_SIZE) posX[i] = CUBE_SIZE;
                    if (posY[i] > CUBE_SIZE) posY[i] = -CUBE_SIZE;
                    else if (posY[i] < -CUBE_SIZE) posY[i] = CUBE_SIZE;
                    if (posZ[i] > CUBE_SIZE) posZ[i] = -CUBE_SIZE;
                    else if (posZ[i] < -CUBE_SIZE) posZ[i] = CUBE_SIZE;

                    // 3D -> 2D Perspective Projection
                    float zDepth = FOV + posZ[i] + CUBE_SIZE;
                    if (zDepth <= 1.0f) continue;

                    float scale = FOV / zDepth;
                    int sx = (int) (WIDTH / 2f + posX[i] * scale);
                    int sy = (int) (HEIGHT / 2f + posY[i] * scale);

                    if (sx >= 0 && sx < WIDTH && sy >= 0 && sy < HEIGHT) {
                        int intensity = (int) (Math.min(1.0f, scale * 1.5f) * 230);
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
                    float radius = 55f * scale * b.radiusScale;

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
                            parentFrame.setTitle("FastAnimation — 300 Tweened Spheres + 50,000 Particles | FPS: " + fps)
                    );
                    frames = 0;
                    lastFpsTime = now;
                }
            }
        }, "Render-Loop-Combined").start();
    }

    public static void main(String[] args) {
        System.setProperty("sun.java2d.opengl", "true");
        System.setProperty("sun.awt.noerasebackground", "true");

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("FastAnimation — 300 Spheres + 50,000 Particles");
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
