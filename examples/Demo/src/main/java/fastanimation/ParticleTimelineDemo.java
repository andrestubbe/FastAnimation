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
 * FastAnimation Demo 2: Tokyo Night Synthwave + 'Enter the Void' Kinetic Typography Cuts (15 Font Cuts) + Z-Buffer.
 *
 * <p>Features:
 * <ul>
 *   <li>15 Rapid typographic font cuts (Impact, Serif, Sans, Monospace, Sci-Fi vector, Heavy Outline, Condensed, Blackletter, etc.)</li>
 *   <li>'Enter the Void' style kinetic rhythm (fast stroboscopic font switches every ~7-10 frames)</li>
 *   <li>Tightened 2-line vertical gap ('RETRO' and 'SYNTH' stacked tightly)</li>
 *   <li>Pure additive white typography overlay directly atop 3D particle realm</li>
 *   <li>Full per-pixel Float Z-Buffer for 100% correct 3D occlusion between spheres and particles</li>
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
    private static final float FOG_NEAR = 100f;
    private static final float FOG_FAR = 2400f;

    // Retro Neon Palette Constants (RGB Float Weights)
    private static final float[] COLOR_MAGENTA = { 1.0f, 0.08f, 0.58f };
    private static final float[] COLOR_CYAN = { 0.0f, 0.94f, 1.0f };
    private static final float[] COLOR_AMBER = { 1.0f, 0.55f, 0.0f };

    private static final int FONT_MASK_COUNT = 15;

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
    // 50k Particle State Arrays (Tied to Spheres with Turbulence)
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
    private final float[] orbitEccentricity = new float[PARTICLE_COUNT];
    private final float[] noisePhase = new float[PARTICLE_COUNT];
    private final float[] noiseSpeed = new float[PARTICLE_COUNT];
    private final float[] particleBaseSize = new float[PARTICLE_COUNT];

    private BufferedImage screenBuffer;
    private int[] pixels;
    private final float[] zBuffer = new float[WIDTH * HEIGHT];
    private final byte[][] textAlphaMasks = new byte[FONT_MASK_COUNT][WIDTH * HEIGHT];
    private final JFrame parentFrame;

    public ParticleTimelineDemo(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setIgnoreRepaint(true);

        initBuffers();
        initTextMasks();
        init3DScene();
    }

    private void initBuffers() {
        screenBuffer = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        pixels = ((DataBufferInt) screenBuffer.getRaster().getDataBuffer()).getData();
    }

    private void initTextMasks() {
        // 1. Cut 0: Exact custom vector sci-fi image (Tightly stacked)
        try {
            java.io.InputStream is = getClass().getResourceAsStream("/retro_synth_clean.jpg");
            if (is != null) {
                BufferedImage raw = javax.imageio.ImageIO.read(is);
                int rw = raw.getWidth();
                int rh = raw.getHeight();

                int cropY = (int) (rh * 0.38);
                int cropH = (int) (rh * 0.24);
                int cropRetroX = (int) (rw * 0.045);
                int cropRetroW = (int) (rw * 0.435);
                int cropSynthX = (int) (rw * 0.515);
                int cropSynthW = (int) (rw * 0.440);

                BufferedImage imgRetro = raw.getSubimage(cropRetroX, cropY, cropRetroW, cropH);
                BufferedImage imgSynth = raw.getSubimage(cropSynthX, cropY, cropSynthW, cropH);

                int targetW = 240;
                int targetH = (int) (cropH * ((float) targetW / cropRetroW));

                BufferedImage canvas = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = canvas.createGraphics();
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                int cx = WIDTH / 2;
                int cy = HEIGHT / 2;

                // Tightly stacked: only 2px gap
                g.drawImage(imgRetro, cx - targetW / 2, cy - targetH - 1, targetW, targetH, null);
                g.drawImage(imgSynth, cx - targetW / 2, cy + 1, targetW, targetH, null);
                g.dispose();

                int[] maskPixels = ((DataBufferInt) canvas.getRaster().getDataBuffer()).getData();
                for (int i = 0; i < maskPixels.length; i++) {
                    int p = maskPixels[i];
                    int r = (p >> 16) & 0xFF;
                    int gr = (p >> 8) & 0xFF;
                    int b = p & 0xFF;
                    int lum = Math.max(r, Math.max(gr, b));
                    textAlphaMasks[0][i] = (byte) (lum > 40 ? lum : 0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 14 Distinct Font Profiles for 'Enter the Void' kinetic cuts
        String[] fontNames = {
                "Impact", "Arial Black", "Trebuchet MS", "Courier New", "Georgia",
                "Times New Roman", "Verdana", "Tahoma", "Lucida Console", "Century Gothic",
                "Franklin Gothic Heavy", "Consolas", "Palatino Linotype", "Comic Sans MS"
        };
        int[] fontStyles = {
                Font.BOLD, Font.BOLD, Font.BOLD, Font.BOLD, Font.ITALIC | Font.BOLD,
                Font.BOLD, Font.BOLD, Font.BOLD, Font.BOLD, Font.BOLD,
                Font.BOLD, Font.BOLD, Font.ITALIC | Font.BOLD, Font.BOLD
        };
        int[] fontSizes = {
                68, 62, 60, 58, 64,
                64, 58, 60, 56, 62,
                64, 58, 62, 60
        };

        for (int cut = 1; cut < FONT_MASK_COUNT; cut++) {
            int idx = cut - 1;
            BufferedImage canvas = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = canvas.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Font font = new Font(fontNames[idx], fontStyles[idx], fontSizes[idx]);
            g.setFont(font);
            FontMetrics fm = g.getFontMetrics();

            String line1 = "RETRO";
            String line2 = "SYNTH";

            int w1 = fm.stringWidth(line1);
            int w2 = fm.stringWidth(line2);
            int cx = WIDTH / 2;
            int cy = HEIGHT / 2;

            int ascent = fm.getAscent();
            int h = ascent - fm.getDescent();

            // Very tight vertical stacking (2px gap)
            int y1 = cy - 2;
            int y2 = cy + ascent + 2;

            g.setColor(Color.WHITE);
            g.drawString(line1, cx - w1 / 2, y1);
            g.drawString(line2, cx - w2 / 2, y2);
            g.dispose();

            int[] textPixels = ((DataBufferInt) canvas.getRaster().getDataBuffer()).getData();
            for (int i = 0; i < textPixels.length; i++) {
                int alpha = (textPixels[i] >>> 24) & 0xFF;
                textAlphaMasks[cut][i] = (byte) alpha;
            }
        }
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

        // 2. Initialize 50,000 Particles with wide chaotic orbits
        Random r = new Random(42);
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            int bIdx = i % BALL_COUNT;
            targetBallIndex[i] = bIdx;

            orbitRadius[i] = 40.0f + r.nextFloat() * 200.0f;
            orbitAngle[i] = r.nextFloat() * (float) (2 * Math.PI);
            orbitSpeed[i] = (r.nextBoolean() ? 1 : -1) * (0.007f + r.nextFloat() * 0.016f);
            orbitTilt[i] = r.nextFloat() * (float) Math.PI;
            orbitEccentricity[i] = 0.5f + r.nextFloat() * 0.9f;
            noisePhase[i] = r.nextFloat() * 100f;
            noiseSpeed[i] = 0.01f + r.nextFloat() * 0.03f;

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
    // Retro Synthwave Color Shader
    // ---------------------------------------------------------
    private static int computeRetroColor(float px, float py, float pz, float fog,
                                         float ambR, float ambG, float ambB,
                                         float mEx, float mEy, float mEz,
                                         float cEx, float cEy, float cEz,
                                         float aEx, float aEy, float aEz) {
        float lightRadius = 700f;

        float mDx = px - mEx, mDy = py - mEy, mDz = pz - mEz;
        float mDist = (float) Math.sqrt(mDx * mDx + mDy * mDy + mDz * mDz);
        float mWeight = Math.max(0f, 1.0f - (mDist / lightRadius));
        mWeight = mWeight * mWeight;

        float cDx = px - cEx, cDy = py - cEy, cDz = pz - cEz;
        float cDist = (float) Math.sqrt(cDx * cDx + cDy * cDy + cDz * cDz);
        float cWeight = Math.max(0f, 1.0f - (cDist / lightRadius));
        cWeight = cWeight * cWeight;

        float aDx = px - aEx, aDy = py - aEy, aDz = pz - aEz;
        float aDist = (float) Math.sqrt(aDx * aDx + aDy * aDy + aDz * aDz);
        float aWeight = Math.max(0f, 1.0f - (aDist / lightRadius));
        aWeight = aWeight * aWeight;

        float r = ambR * 0.35f + COLOR_MAGENTA[0] * mWeight * 1.4f + COLOR_CYAN[0] * cWeight * 1.2f + COLOR_AMBER[0] * aWeight * 1.3f;
        float g = ambG * 0.35f + COLOR_MAGENTA[1] * mWeight * 1.4f + COLOR_CYAN[1] * cWeight * 1.2f + COLOR_AMBER[1] * aWeight * 1.3f;
        float b = ambB * 0.35f + COLOR_MAGENTA[2] * mWeight * 1.4f + COLOR_CYAN[2] * cWeight * 1.2f + COLOR_AMBER[2] * aWeight * 1.3f;

        // Blend into TokyoNight background color (#1a1b26 -> 26, 27, 38)
        int cr = (int) (26 + (Math.min(1.0f, r) * 255 - 26) * fog);
        int cg = (int) (27 + (Math.min(1.0f, g) * 255 - 27) * fog);
        int cb = (int) (38 + (Math.min(1.0f, b) * 255 - 38) * fog);

        cr = Math.min(255, Math.max(26, cr));
        cg = Math.min(255, Math.max(27, cg));
        cb = Math.min(255, Math.max(38, cb));

        return (cr << 16) | (cg << 8) | cb;
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
            long frameTimeTarget = 1_000_000_000L / 60;
            long lastRenderTime = System.nanoTime();
            Random r = new Random();

            float camYaw = 0f;
            float camPitch = 0f;
            float lightPhase = 0f;
            float ambientPhase = 0f;
            int fontCutIndex = 0;
            int fontFrameCounter = 0;

            while (true) {
                long nowLoop = System.nanoTime();
                if (nowLoop - lastRenderTime < frameTimeTarget) {
                    Thread.yield();
                    continue;
                }
                lastRenderTime = nowLoop;
                lightPhase += 0.018f;
                ambientPhase += 0.005f;

                // Enter the Void style rapid kinetic typography cuts (every 8 frames)
                fontFrameCounter++;
                if (fontFrameCounter >= 8) {
                    fontCutIndex = (fontCutIndex + 1) % FONT_MASK_COUNT;
                    fontFrameCounter = 0;
                }

                // 1. Gentle Sphere Separation
                updateGentleSeparation();

                // 2. Ambient Color Field
                float ambR = (float) (0.5f + 0.5f * Math.sin(ambientPhase));
                float ambG = (float) (0.3f + 0.3f * Math.sin(ambientPhase + 2.094f));
                float ambB = (float) (0.6f + 0.4f * Math.sin(ambientPhase + 4.188f));

                // 3. Emitters
                float mEx = (float) Math.cos(lightPhase) * 560f;
                float mEy = (float) Math.sin(lightPhase * 0.7f) * 380f;
                float mEz = (float) Math.sin(lightPhase) * 560f;

                float cEx = (float) Math.cos(lightPhase + 2.094f) * 560f;
                float cEy = (float) Math.sin((lightPhase + 2.094f) * 0.7f) * 380f;
                float cEz = (float) Math.sin(lightPhase + 2.094f) * 560f;

                float aEx = (float) Math.cos(lightPhase + 4.188f) * 560f;
                float aEy = (float) Math.sin((lightPhase + 4.188f) * 0.7f) * 380f;
                float aEz = (float) Math.sin(lightPhase + 4.188f) * 560f;

                // 4. 3D Camera Orbit
                camYaw += 0.002f;
                camPitch = (float) Math.sin(camYaw * 0.5f) * 0.2f;

                float cosY = (float) Math.cos(camYaw);
                float sinY = (float) Math.sin(camYaw);
                float cosP = (float) Math.cos(camPitch);
                float sinP = (float) Math.sin(camPitch);

                // 5. Cinematic Motion Blur Decay
                int bgR = 26, bgG = 27, bgB = 38;
                for (int i = 0; i < pixels.length; i++) {
                    int p = pixels[i];
                    int pr = (p >> 16) & 0xFF;
                    int pg = (p >> 8) & 0xFF;
                    int pb = p & 0xFF;

                    pr = bgR + (((pr - bgR) * 170) >> 8);
                    pg = bgG + (((pg - bgG) * 170) >> 8);
                    pb = bgB + (((pb - bgB) * 170) >> 8);

                    pixels[i] = (pr << 16) | (pg << 8) | pb;
                }
                Arrays.fill(zBuffer, Float.MAX_VALUE);

                // 6. Rasterize 300 Spheres into Z-Buffer & Pixel Buffer
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
                    fog = Math.max(0.35f, Math.min(1.0f, fog));

                    int rgb = computeRetroColor(bx, by, bz, fog, ambR, ambG, ambB, mEx, mEy, mEz, cEx, cEy, cEz, aEx, aEy, aEz);

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

                // 7. Volumetric Orbit Particles
                for (int i = 0; i < PARTICLE_COUNT; i++) {
                    int bIdx = targetBallIndex[i];
                    Ball parent = balls.get(bIdx);

                    orbitAngle[i] += orbitSpeed[i];
                    noisePhase[i] += noiseSpeed[i];

                    if (r.nextInt(280) == 0) {
                        targetBallIndex[i] = r.nextInt(BALL_COUNT);
                        orbitRadius[i] = 40.0f + r.nextFloat() * 200.0f;
                        orbitSpeed[i] = (r.nextBoolean() ? 1 : -1) * (0.007f + r.nextFloat() * 0.016f);
                        orbitEccentricity[i] = 0.5f + r.nextFloat() * 0.9f;
                    }

                    float radius = orbitRadius[i] * parent.radiusScale;
                    float tilt = orbitTilt[i];
                    float ecc = orbitEccentricity[i];

                    float wobbleX = (float) Math.sin(noisePhase[i]) * 28.0f;
                    float wobbleY = (float) Math.cos(noisePhase[i] * 1.3f) * 28.0f;
                    float wobbleZ = (float) Math.sin(noisePhase[i] * 0.7f) * 28.0f;

                    float ox = (float) (Math.cos(orbitAngle[i]) * radius * ecc) + wobbleX;
                    float oy = (float) (Math.sin(orbitAngle[i]) * Math.cos(tilt) * radius) + wobbleY;
                    float oz = (float) (Math.sin(orbitAngle[i]) * Math.sin(tilt) * radius) + wobbleZ;

                    float targetX = parent.x + parent.boidOffsetX + ox;
                    float targetY = parent.y + parent.boidOffsetY + oy;
                    float targetZ = parent.z + parent.boidOffsetZ + oz;

                    velX[i] = (velX[i] + (targetX - posX[i]) * 0.025f) * 0.92f;
                    velY[i] = (velY[i] + (targetY - posY[i]) * 0.025f) * 0.92f;
                    velZ[i] = (velZ[i] + (targetZ - posZ[i]) * 0.025f) * 0.92f;

                    posX[i] += velX[i];
                    posY[i] += velY[i];
                    posZ[i] += velZ[i];

                    // Camera 3D Transform
                    float rx = posX[i] * cosY - posZ[i] * sinY;
                    float rz = posX[i] * sinY + posZ[i] * cosY;
                    float ry = posY[i] * cosP - rz * sinP;
                    rz = posY[i] * sinP + rz * cosP;

                    float zDepth = FOV + rz + CUBE_SIZE;
                    if (zDepth <= 1.0f) continue;

                    float fog = 1.0f - ((zDepth - FOG_NEAR) / (FOG_FAR - FOG_NEAR));
                    fog = Math.max(0.35f, Math.min(1.0f, fog));

                    int rgb = computeRetroColor(posX[i], posY[i], posZ[i], fog, ambR, ambG, ambB, mEx, mEy, mEz, cEx, cEy, cEz, aEx, aEy, aEz);

                    float scale = FOV / zDepth;
                    int sx = (int) (WIDTH / 2f + rx * scale);
                    int sy = (int) (HEIGHT / 2f + ry * scale);

                    float pSize = particleBaseSize[i] * scale;
                    int rad = (int) Math.max(1, pSize);
                    if (rad <= 1) {
                        if (sx >= 0 && sx < WIDTH && sy >= 0 && sy < HEIGHT) {
                            int idx = sy * WIDTH + sx;
                            if (zDepth < zBuffer[idx]) {
                                zBuffer[idx] = zDepth;
                                pixels[idx] = rgb;
                            }
                        }
                    } else {
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
                }

                // 8. Additive White Blend for Current 'Enter the Void' Kinetic Typography Cut
                byte[] currentMask = textAlphaMasks[fontCutIndex];
                for (int i = 0; i < pixels.length; i++) {
                    int alpha = currentMask[i] & 0xFF;
                    if (alpha > 0) {
                        int p = pixels[i];
                        int pr = (p >> 16) & 0xFF;
                        int pg = (p >> 8) & 0xFF;
                        int pb = p & 0xFF;

                        pr = Math.min(255, pr + alpha);
                        pg = Math.min(255, pg + alpha);
                        pb = Math.min(255, pb + alpha);

                        pixels[i] = (pr << 16) | (pg << 8) | pb;
                    }
                }

                // 9. Present Frame
                Graphics g = bs.getDrawGraphics();
                g.drawImage(screenBuffer, 0, 0, null);
                g.dispose();
                bs.show();
                Toolkit.getDefaultToolkit().sync();

                // 10. FPS Counter in Window Title
                frames++;
                long now = System.nanoTime();
                if (now - lastFpsTime >= 1_000_000_000L) {
                    int fps = frames;
                    SwingUtilities.invokeLater(() ->
                            parentFrame.setTitle("FastAnimation — 300 Spheres + 50,000 Particles (Enter the Void Typography) | FPS: " + fps)
                    );
                    frames = 0;
                    lastFpsTime = now;
                }
            }
        }, "Render-Loop-Retro").start();
    }

    private static BufferedImage createRoundIcon() {
        BufferedImage icon = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = icon.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(122, 162, 247));
        g.fillOval(4, 4, 56, 56);
        g.dispose();
        return icon;
    }

    public static void main(String[] args) {
        System.setProperty("sun.java2d.opengl", "true");
        System.setProperty("sun.awt.noerasebackground", "true");

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("FastAnimation — 300 Spheres + 50,000 Particles (Enter the Void)");
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
                FastTheme.setTitleBarColor(hwnd, 26, 27, 38);
                FastTheme.setTitleBarTextColor(hwnd, 169, 177, 214);
            } catch (Exception ignored) {}

            frame.setVisible(true);
            demo.start();
        });
    }
}
