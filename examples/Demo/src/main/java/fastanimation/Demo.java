package fastanimation;

import fastdwm.FastDWM;
import fasttheme.FastTheme;
import fastanimation.AnimationEngine.HeartbeatMode;
import fasttween.FastTween;
import fasttween.Ease;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;
import java.util.List;

/**
 * FastAnimation Demo — Pseudo‑3D Particle Realm (Black & White)
 * <p>
 * Demonstrates:
 * - Pure Java software rendering
 * - 3D → 2D projection
 * - 300 independently tweened objects
 * - Native VSync heartbeat via FastAnimation
 * - Clean 120 FPS render loop
 */
public class Demo extends Canvas {


    // --- Window / Render Target ---
    private static final int WIDTH = 1173;
    private static final int HEIGHT = 610;

    // --- 3D Scene Settings ---
    private static final int CUBE_SIZE = 600;
    private static final int BALL_COUNT = 300;
    private static final float FOV = 300f;
    private static final Ellipse2D ellipse2D = new Ellipse2D.Float();

    // --- Rendering Buffers ---
    private BufferedImage screenBuffer;
    private int[] pixels;
    private int[] backgroundPixels;

    // --- Scene Objects ---
    private final List<Ball> balls = new ArrayList<>();
    private final JFrame parentFrame;

    /**
     * Simple particle object in 3D space.
     */
    private static class Ball {
        float x, y, z;
        float radiusScale = 1.0f;
    }

    // ---------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------
    public Demo(JFrame parentFrame) {
        this.parentFrame = parentFrame;

        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setIgnoreRepaint(true);

        initBuffers();
        init3DScene();
    }

    // ---------------------------------------------------------
    // Buffer Initialization
    // ---------------------------------------------------------
    private void initBuffers() {
        screenBuffer = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        pixels = ((DataBufferInt) screenBuffer.getRaster().getDataBuffer()).getData();

        // Pre‑allocated black background
        backgroundPixels = new int[WIDTH * HEIGHT];
        for (int i = 0; i < backgroundPixels.length; i++) {
            backgroundPixels[i] = 0x000000;
        }
    }

    // ---------------------------------------------------------
    // Scene Initialization
    // ---------------------------------------------------------
    private void init3DScene() {
        // Use native VSync as heartbeat for all tweens
        FastAnimation.setHeartbeatMode(HeartbeatMode.NATIVE_VSYNC);

        for (int i = 0; i < BALL_COUNT; i++) {
            Ball b = new Ball();

            // Random initial position inside cube
            b.x = (float) ((Math.random() * CUBE_SIZE * 2) - CUBE_SIZE);
            b.y = (float) ((Math.random() * CUBE_SIZE * 2) - CUBE_SIZE);
            b.z = (float) ((Math.random() * CUBE_SIZE * 2) - CUBE_SIZE);

            balls.add(b);

            // Start infinite tween loops
            animateAxisX(b);
            animateAxisY(b);
            animateAxisZ(b);
            animateScale(b);
        }
    }

    // ---------------------------------------------------------
    // Tween Animations (infinite loops)
    // ---------------------------------------------------------

    private void animateAxisX(Ball b) {
        float current = b.x;
        float target = (float) ((Math.random() * CUBE_SIZE * 2) - CUBE_SIZE);
        long duration = (long) (
                Math.abs(target - current) / CUBE_SIZE * 2000
                        + 1000
                        + Math.random() * 1000
        );
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
        long duration = (long) (
                Math.abs(target - current) / CUBE_SIZE * 2000
                        + 1000
                        + Math.random() * 1000
        );
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
        long duration = (long) (
                Math.abs(target - current) / CUBE_SIZE * 2000
                        + 1000
                        + Math.random() * 1000
        );
        FastAnimation.parallel(
                FastTween.to(current, target, duration)
                        .ease(Ease.QUAD_IN_OUT)
                        .onUpdate(v -> b.z = v)
                        .onComplete(() -> animateAxisZ(b))
        ).start();
    }

    private void animateScale(Ball b) {
        float current = b.radiusScale;
        float target = 0.2f + (float) (Math.random() * 0.8f); // 0.2 → 1.0
        long duration = (long) (1000 + Math.random() * 2000); // 1–3s
        FastAnimation.parallel(
                FastTween.to(current, target, duration)
                        .ease(Ease.QUAD_IN_OUT)
                        .onUpdate(v -> b.radiusScale = v)
                        .onComplete(() -> animateScale(b))
        ).start();
    }

    // ---------------------------------------------------------
    // Render Loop (120 FPS)
    // ---------------------------------------------------------
    public void start() {
        createBufferStrategy(3);
        BufferStrategy bs = getBufferStrategy();

        new Thread(() -> {

            long lastFpsTime = System.nanoTime();
            int frames = 0;

            long frameTimeTarget = 1_000_000_000L / 120; // 120 FPS
            long lastRenderTime = System.nanoTime();

            while (true) {

                // Manual FPS throttle (bypasses monitor VSync)
                long nowLoop = System.nanoTime();
                if (nowLoop - lastRenderTime < frameTimeTarget) {
                    Thread.yield();
                    continue;
                }
                lastRenderTime = nowLoop;

                // Clear screen
                System.arraycopy(backgroundPixels, 0, pixels, 0, backgroundPixels.length);

                // --- Software Rendering ---
                Graphics2D g2d = screenBuffer.createGraphics();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.WHITE);

                // Draw all balls
                for (Ball b : balls) {

                    // Simple perspective projection
                    float zDepth = FOV + b.z + CUBE_SIZE;
                    if (zDepth <= 0.1f) continue;

                    float scale = FOV / zDepth;

                    float screenX = WIDTH / 2f + b.x * scale;
                    float screenY = HEIGHT / 2f + b.y * scale;
                    float radius = 60f * scale * b.radiusScale;
                    if (radius > 0) {
                        ellipse2D.setFrame(screenX - radius, screenY - radius, radius * 2, radius * 2);
                        g2d.fill(ellipse2D);
                    }
                }

                g2d.dispose();

                // --- Present Frame ---
                Graphics g = bs.getDrawGraphics();
                g.drawImage(screenBuffer, 0, 0, null);
                g.dispose();
                bs.show();
                Toolkit.getDefaultToolkit().sync();

                // --- FPS Counter ---
                frames++;
                long now = System.nanoTime();
                if (now - lastFpsTime >= 1_000_000_000L) {
                    int fps = frames;
                    SwingUtilities.invokeLater(() ->
                            parentFrame.setTitle("FastAnimation Demo - FPS: " + fps)
                    );
                    frames = 0;
                    lastFpsTime = now;
                }
            }

        }, "Render-Loop").start();
    }

    // ---------------------------------------------------------
    // Utility: Round Window Icon
    // ---------------------------------------------------------
    private static BufferedImage createRoundIcon() {
        BufferedImage icon = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = icon.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillOval(4, 4, 56, 56);
        g.dispose();
        return icon;
    }

    // ---------------------------------------------------------
    // Main Entry Point
    // ---------------------------------------------------------
    public static void main(String[] args) {

        System.setProperty("sun.java2d.opengl", "true");
        System.setProperty("sun.awt.noerasebackground", "true");

        JFrame frame = new JFrame("FastAnimation Demo - FPS: ...");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setIgnoreRepaint(true);
        frame.setIconImage(createRoundIcon());

        Demo demo = new Demo(frame);
        frame.add(demo);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.addNotify();

        // Apply native Windows styling via FastTheme
        try {
            long hwnd = FastTheme.getWindowHandle(frame);
            FastTheme.setTitleBarDarkMode(hwnd, true);
            FastTheme.setTitleBarColor(hwnd, 0, 0, 0);
            FastTheme.setTitleBarTextColor(hwnd, 255, 255, 255);
            FastTheme.setWindowTransparency(hwnd, 224);
        } catch (Exception e) {
            System.err.println("FastTheme dark mode failed: " + e.getMessage());
        }

        frame.setVisible(true);
        demo.start();
    }
}
