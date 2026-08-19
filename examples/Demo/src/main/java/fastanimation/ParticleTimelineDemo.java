package fastanimation;

import fastexecution.FastExecution;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Random;

/**
 * FastAnimation Demo 2: High-Performance Mass Particle Timeline Demo.
 *
 * <p>Simulates and renders 50,000+ timeline-animated particles (position, velocity,
 * alpha easing, and color transitions) clocked by FastExecution native heartbeats.
 */
public class ParticleTimelineDemo extends JFrame {

    private static final int PARTICLE_COUNT = 50_000;
    private static final int WIDTH = 900;
    private static final int HEIGHT = 650;

    // Direct contiguous flat memory buffer for zero-overhead rasterization
    private final float[] posX = new float[PARTICLE_COUNT];
    private final float[] posY = new float[PARTICLE_COUNT];
    private final float[] velX = new float[PARTICLE_COUNT];
    private final float[] velY = new float[PARTICLE_COUNT];
    private final float[] life = new float[PARTICLE_COUNT];
    private final float[] maxLife = new float[PARTICLE_COUNT];
    private final int[] colors = new int[PARTICLE_COUNT];

    private final BufferedImage frameBuffer;
    private final int[] pixels;
    private final ParticleCanvas canvas;
    private final JLabel fpsLabel = new JLabel("FastExecution Heartbeat: 60 FPS | 50,000 Partikel", JLabel.CENTER);

    private int frameCount = 0;
    private long lastFpsTime = System.currentTimeMillis();

    public ParticleTimelineDemo() {
        super("FastAnimation — Mass Particle Timeline Demo (50,000 Particles)");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);

        frameBuffer = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        pixels = ((DataBufferInt) frameBuffer.getRaster().getDataBuffer()).getData();

        initParticles();

        canvas = new ParticleCanvas();
        setLayout(new BorderLayout());
        add(canvas, BorderLayout.CENTER);

        fpsLabel.setFont(new Font("Monospaced", Font.BOLD, 13));
        fpsLabel.setForeground(Color.CYAN);
        fpsLabel.setBackground(Color.BLACK);
        fpsLabel.setOpaque(true);
        fpsLabel.setBorder(BorderFactory.createEmptyBorder(6, 10, 8, 10));
        add(fpsLabel, BorderLayout.SOUTH);

        // Drive simulation loop via FastExecution high-precision timer
        FastExecution.loop("ParticleTimelineLoop", 60, this::tickAndRender);
    }

    private void initParticles() {
        Random r = new Random(42);
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            resetParticle(i, r);
            life[i] = r.nextFloat() * maxLife[i]; // stagger start times
        }
    }

    private void resetParticle(int i, Random r) {
        posX[i] = WIDTH / 2f;
        posY[i] = HEIGHT / 2f;

        double angle = r.nextDouble() * 2 * Math.PI;
        float speed = 1.0f + r.nextFloat() * 4.5f;
        velX[i] = (float) (Math.cos(angle) * speed);
        velY[i] = (float) (Math.sin(angle) * speed);

        maxLife[i] = 60f + r.nextFloat() * 120f;
        life[i] = 0f;

        int hueChoice = r.nextInt(3);
        if (hueChoice == 0) colors[i] = 0x00D0FF; // Cyan
        else if (hueChoice == 1) colors[i] = 0xFF4080; // Magenta
        else colors[i] = 0xFFA000; // Orange
    }

    private void tickAndRender() {
        Random r = new Random();

        // 1. Fast Fade Background (decay trail)
        for (int i = 0; i < pixels.length; i++) {
            int p = pixels[i];
            int red = (p >> 16) & 0xFF;
            int green = (p >> 8) & 0xFF;
            int blue = p & 0xFF;

            red = (red * 88) >> 8;
            green = (green * 88) >> 8;
            blue = (blue * 88) >> 8;
            pixels[i] = (red << 16) | (green << 8) | blue;
        }

        // 2. Parallel SIMD-style Particle Update & Timeline Easing
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            life[i] += 1.0f;
            if (life[i] >= maxLife[i]) {
                resetParticle(i, r);
            }

            // Easing progress (0.0 to 1.0)
            float progress = life[i] / maxLife[i];
            float easeAlpha = 1.0f - progress * progress; // Quadratic Out Fade

            posX[i] += velX[i];
            posY[i] += velY[i];

            int px = (int) posX[i];
            int py = (int) posY[i];

            if (px >= 0 && px < WIDTH && py >= 0 && py < HEIGHT) {
                int baseCol = colors[i];
                int cr = (int) (((baseCol >> 16) & 0xFF) * easeAlpha);
                int cg = (int) (((baseCol >> 8) & 0xFF) * easeAlpha);
                int cb = (int) ((baseCol & 0xFF) * easeAlpha);

                pixels[py * WIDTH + px] = (cr << 16) | (cg << 8) | cb;
            }
        }

        frameCount++;
        long now = System.currentTimeMillis();
        if (now - lastFpsTime >= 1000) {
            final int fps = frameCount;
            frameCount = 0;
            lastFpsTime = now;
            SwingUtilities.invokeLater(() -> fpsLabel.setText(String.format("FastExecution Heartbeat: %d FPS | 50,000 Partikel (Contiguous Memory Render)", fps)));
        }

        canvas.repaint();
    }

    class ParticleCanvas extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(frameBuffer, 0, 0, getWidth(), getHeight(), null);
        }
    }

    @Override
    public void dispose() {
        FastExecution.stop("ParticleTimelineLoop");
        super.dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ParticleTimelineDemo().setVisible(true));
    }
}
