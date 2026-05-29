package fastanimation;

import fastdwm.FastDWM;
import fasttheme.FastTheme;
import fastanimation.AnimationEngine.HeartbeatMode;
import fasttween.FastTween;
import fasttween.Ease;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;
import java.util.List;

/**
 * FastAnimation Demo - Pseudo 3D Realm (Pure Black + White)
 */
public class Demo extends Canvas {
    
    static {
        System.setProperty("sun.java2d.opengl", "true");
        System.setProperty("sun.awt.noerasebackground", "true");
    }

    private static final int WIDTH = 1173;
    private static final int HEIGHT = 610;
    
    private BufferedImage screenBuffer;
    private int[] pixels;
    private int[] backgroundPixels;
    
    private static final int CUBE_SIZE = 400;
    private static final int BALL_COUNT = 300;
    private static final float FOV = 600f;

    private static class Ball {
        float x, y, z;
        float radiusScale = 1.0f;
    }
    
    private List<Ball> balls = new ArrayList<>();
    private JFrame parentFrame;

    public Demo(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setIgnoreRepaint(true);
        initBuffers();
        init3DScene();
    }

    private void initBuffers() {
        screenBuffer = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        pixels = ((DataBufferInt) screenBuffer.getRaster().getDataBuffer()).getData();
        backgroundPixels = new int[WIDTH * HEIGHT];
        // Pure black background
        for (int i = 0; i < backgroundPixels.length; i++) backgroundPixels[i] = 0x000000;
    }

    private void init3DScene() {
        FastAnimation.setHeartbeatMode(HeartbeatMode.NATIVE_VSYNC);
        
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
    }
    
    private void animateAxisX(Ball b) {
        float current = b.x;
        float target = (float) ((Math.random() * CUBE_SIZE * 2) - CUBE_SIZE);
        long duration = (long) (Math.abs(target - current) / CUBE_SIZE * 2000 + 1000 + Math.random() * 1000);
        FastAnimation.parallel(FastTween.to(current, target, duration).ease(Ease.QUAD_IN_OUT).onUpdate(v -> b.x = v).onComplete(() -> animateAxisX(b))).start();
    }
    
    private void animateAxisY(Ball b) {
        float current = b.y;
        float target = (float) ((Math.random() * CUBE_SIZE * 2) - CUBE_SIZE);
        long duration = (long) (Math.abs(target - current) / CUBE_SIZE * 2000 + 1000 + Math.random() * 1000);
        FastAnimation.parallel(FastTween.to(current, target, duration).ease(Ease.QUAD_IN_OUT).onUpdate(v -> b.y = v).onComplete(() -> animateAxisY(b))).start();
    }
    
    private void animateAxisZ(Ball b) {
        float current = b.z;
        float target = (float) ((Math.random() * CUBE_SIZE * 2) - CUBE_SIZE);
        long duration = (long) (Math.abs(target - current) / CUBE_SIZE * 2000 + 1000 + Math.random() * 1000);
        FastAnimation.parallel(FastTween.to(current, target, duration).ease(Ease.QUAD_IN_OUT).onUpdate(v -> b.z = v).onComplete(() -> animateAxisZ(b))).start();
    }
    
    private void animateScale(Ball b) {
        float current = b.radiusScale;
        float target = 0.2f + (float) (Math.random() * 0.8f); // Random target between 0.2 and 1.0
        long duration = (long) (1000 + Math.random() * 2000); // 1-3 seconds
        FastAnimation.parallel(FastTween.to(current, target, duration).ease(Ease.QUAD_IN_OUT).onUpdate(v -> b.radiusScale = v).onComplete(() -> animateScale(b))).start();
    }

    public void start() {
        createBufferStrategy(3);
        BufferStrategy bs = getBufferStrategy();
        
        new Thread(() -> {
            long lastFpsTime = System.nanoTime();
            int frames = 0;
            
            long frameTimeTarget = 1_000_000_000L / 120; // 120 FPS
            long lastRenderTime = System.nanoTime();
            
            while (true) {
                // Throttle to exactly 120 FPS (Bypass monitor VSync)
                long nowLoop = System.nanoTime();
                if (nowLoop - lastRenderTime < frameTimeTarget) {
                    Thread.yield();
                    continue;
                }
                lastRenderTime = nowLoop;

                System.arraycopy(backgroundPixels, 0, pixels, 0, backgroundPixels.length);
                
                Graphics2D g2d = screenBuffer.createGraphics();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.WHITE);
                
                // Sort Balls by Depth
                balls.sort((b1, b2) -> Float.compare(b2.z, b1.z));
                
                // Draw Balls with Anti-Aliasing (AA)
                for (Ball b : balls) {
                    float zDepth = FOV + b.z + CUBE_SIZE;
                    if (zDepth <= 0.1f) continue;
                    
                    float scale = FOV / zDepth;
                    int screenX = (int) (WIDTH / 2f + b.x * scale);
                    int screenY = (int) (HEIGHT / 2f + b.y * scale);
                    int radius = (int) (40f * scale * b.radiusScale);
                    
                    if (radius > 0) {
                        g2d.fillOval(screenX - radius, screenY - radius, radius * 2, radius * 2);
                    }
                }
                
                g2d.dispose();

                Graphics g = bs.getDrawGraphics();
                g.drawImage(screenBuffer, 0, 0, null);
                g.dispose();
                bs.show();
                Toolkit.getDefaultToolkit().sync();
                
                // FPS Calculation
                frames++;
                long now = System.nanoTime();
                if (now - lastFpsTime >= 1_000_000_000L) {
                    int fps = frames;
                    SwingUtilities.invokeLater(() -> {
                        parentFrame.setTitle("FastAnimation Demo - Fps:" + fps);
                    });
                    frames = 0;
                    lastFpsTime = now;
                }
            }
        }, "Render-Loop").start();
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
        JFrame frame = new JFrame("FastAnimation Demo - Fps:...");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setIgnoreRepaint(true);
        frame.setIconImage(createRoundIcon());
        
        Demo demo = new Demo(frame);
        frame.add(demo);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        
        // Use FastTheme to force the native Windows titlebar to pitch black
        try {
            long hwnd = FastTheme.getWindowHandle(frame);
            FastTheme.setTitleBarDarkMode(hwnd, true); // base dark mode
            FastTheme.setTitleBarColor(hwnd, 0, 0, 0); // pitch black
            FastTheme.setTitleBarTextColor(hwnd, 255, 255, 255); // white text
        } catch (Exception e) {
            System.err.println("FastTheme dark mode failed: " + e.getMessage());
        }
        
        demo.start();
    }
}
