package fastanimation;

import fastanimation.AnimationEngine.HeartbeatMode;
import fastgpu.DispatchSize;
import fastgpu.FastGPU;
import fastgpu.FastGPUBuffer;
import fastgpu.FastGPUKernel;
import fastgpu.KernelArgs;
import fastgpu.KernelLanguage;
import fasttheme.FastTheme;
import fasttween.Ease;
import fasttween.FastTween;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class FastGpuParticleTimelineDemo extends Canvas {

    private static final int WIDTH = 1173;
    private static final int HEIGHT = 610;

    private static final int BALL_COUNT = 300;
    private static final int PARTICLE_COUNT = 100_000;
    private static final float CUBE_SIZE = 600f;
    private static final float FOV = 450f;

    private static final float FOG_NEAR = 100f;
    private static final float FOG_FAR = 2400f;

    private static final float[] COLOR_MAGENTA = { 1.0f, 0.08f, 0.58f };
    private static final float[] COLOR_CYAN = { 0.0f, 0.94f, 1.0f };
    private static final float[] COLOR_AMBER = { 1.0f, 0.55f, 0.0f };

    private static final int TRIG_SIZE = 4096;
    private static final int TRIG_MASK = TRIG_SIZE - 1;
    private static final float RAD_TO_INDEX = (float) (TRIG_SIZE / (2.0 * Math.PI));
    private static final float[] SIN_TABLE = new float[TRIG_SIZE];
    private static final float[] COS_TABLE = new float[TRIG_SIZE];

    static {
        for (int i = 0; i < TRIG_SIZE; i++) {
            double angle = (i * 2.0 * Math.PI) / TRIG_SIZE;
            SIN_TABLE[i] = (float) Math.sin(angle);
            COS_TABLE[i] = (float) Math.cos(angle);
        }
    }

    private static float fastSin(float rad) {
        return SIN_TABLE[(int) (rad * RAD_TO_INDEX) & TRIG_MASK];
    }

    private static float fastCos(float rad) {
        return COS_TABLE[(int) (rad * RAD_TO_INDEX) & TRIG_MASK];
    }

    private static class Ball {
        float x, y, z;
        float boidOffsetX, boidOffsetY, boidOffsetZ;
        float radiusScale = 1.0f;
        float rotX, rotY, rotZ;
        float zDepth;
    }

    private final List<Ball> balls = new ArrayList<>();

    private final float[] particleParams = new float[PARTICLE_COUNT * 4];
    private final float[] particleState = new float[PARTICLE_COUNT * 8];
    private final float[] particleBaseSize = new float[PARTICLE_COUNT];
    private final float[] sphereBufferData = new float[BALL_COUNT * 4];
    private final float[] globalUniforms = new float[16];

    private BufferedImage screenBuffer;
    private int[] pixels;
    private final float[] zBuffer = new float[WIDTH * HEIGHT];
    private final JFrame parentFrame;

    private FastGPU gpu;
    private FastGPUBuffer gpuParamsBuffer;
    private FastGPUBuffer gpuStateBuffer;
    private FastGPUBuffer gpuSpheresBuffer;
    private FastGPUBuffer gpuUniformsBuffer;
    private FastGPUKernel particlePhysicsKernel;
    private boolean gpuActive = false;

    public FastGpuParticleTimelineDemo(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setIgnoreRepaint(true);

        initBuffers();
        init3DScene();
        initFastGpuPipeline();
    }

    private void initBuffers() {
        screenBuffer = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        pixels = ((DataBufferInt) screenBuffer.getRaster().getDataBuffer()).getData();
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

        Random r = new Random(42);
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            int bIdx = i % BALL_COUNT;
            Ball b = balls.get(bIdx);

            float orbitRad = 40.0f + r.nextFloat() * 200.0f;
            float orbitAng = r.nextFloat() * (float) (2 * Math.PI);
            float orbitSpd = (r.nextBoolean() ? 1 : -1) * (0.007f + r.nextFloat() * 0.016f);
            float orbitTlt = r.nextFloat() * (float) Math.PI;
            float orbitEcc = 0.5f + r.nextFloat() * 0.9f;
            float nPhase = r.nextFloat() * 100f;
            float nSpeed = 0.01f + r.nextFloat() * 0.03f;

            float pSize = 1.0f + r.nextFloat() * 0.6f;
            float sRoll = r.nextFloat();
            if (sRoll > 0.94f) pSize = 3.5f + r.nextFloat() * 2.0f;
            else if (sRoll > 0.70f) pSize = 2.0f + r.nextFloat() * 1.2f;
            particleBaseSize[i] = pSize;

            int pBase = i * 4;
            particleParams[pBase] = orbitRad;
            particleParams[pBase + 1] = orbitSpd;
            particleParams[pBase + 2] = orbitTlt;
            particleParams[pBase + 3] = orbitEcc;

            int sBase = i * 8;
            particleState[sBase] = b.x;
            particleState[sBase + 1] = b.y;
            particleState[sBase + 2] = b.z;
            particleState[sBase + 3] = 0;
            particleState[sBase + 4] = 0;
            particleState[sBase + 5] = 0;
            particleState[sBase + 6] = orbitAng;
            particleState[sBase + 7] = (float) bIdx;
        }
    }

    private void initFastGpuPipeline() {
        try {
            gpu = FastGPU.openDefault();
            System.out.println("⚡ FastGPU Engine Connected: Compiling GLSL Particle Physics Compute Kernel...");

            String glslKernel = """
                    #version 450
                    layout(local_size_x = 256) in;
                    
                    layout(std430, binding = 0) readonly buffer ParamsBuf {
                        vec4 params[]; // x: radius, y: speed, z: tilt, w: ecc
                    } bufParams;
                    
                    layout(std430, binding = 1) buffer StateBuf {
                        vec4 stateA[]; // stateA[2*i]: pos.xyz, vel.x | stateA[2*i+1]: vel.yz, angle, sphereIdx
                    } bufStateA;
                    
                    layout(std430, binding = 2) readonly buffer SphereBuf {
                        vec4 spheres[]; // x: sphereX, y: sphereY, z: sphereZ, w: scale
                    } bufSpheres;
                    
                    layout(std430, binding = 3) readonly buffer UniformBuf {
                        vec4 timeAndCam; // x: lightPhase, y: ambientPhase
                    } bufUniforms;
                    
                    void main() {
                        uint id = gl_GlobalInvocationID.x;
                        if (id >= 100000) return;
                        
                        vec4 p = bufParams.params[id];
                        vec4 pva = bufStateA.stateA[id * 2];
                        vec4 pvb = bufStateA.stateA[id * 2 + 1];
                        
                        float angle = pvb.z + p.y;
                        int sIdx = int(pvb.w);
                        vec4 sph = bufSpheres.spheres[sIdx];
                        
                        float rad = p.x * sph.w;
                        float tilt = p.z;
                        float ecc = p.w;
                        
                        float phase = float(id) * 0.05 + angle * 2.0;
                        float wobbleX = sin(phase) * 28.0;
                        float wobbleY = cos(phase * 1.3) * 28.0;
                        float wobbleZ = sin(phase * 0.7) * 28.0;
                        
                        float ox = (cos(angle) * rad * ecc) + wobbleX;
                        float oy = (sin(angle) * cos(tilt) * rad) + wobbleY;
                        float oz = (sin(angle) * sin(tilt) * rad) + wobbleZ;
                        
                        vec3 target = sph.xyz + vec3(ox, oy, oz);
                        vec3 pos = pva.xyz;
                        vec3 vel = vec3(pva.w, pvb.x, pvb.y);
                        
                        vel = (vel + (target - pos) * 0.025) * 0.92;
                        pos += vel;
                        
                        bufStateA.stateA[id * 2] = vec4(pos, vel.x);
                        bufStateA.stateA[id * 2 + 1] = vec4(vel.yz, angle, float(sIdx));
                    }
                    """;

            particlePhysicsKernel = gpu.compile("particle_step", glslKernel, KernelLanguage.GLSL_COMPUTE);
            gpuParamsBuffer = gpu.allocFloatBuffer(PARTICLE_COUNT * 4);
            gpuStateBuffer = gpu.allocFloatBuffer(PARTICLE_COUNT * 8);
            gpuSpheresBuffer = gpu.allocFloatBuffer(BALL_COUNT * 4);
            gpuUniformsBuffer = gpu.allocFloatBuffer(16);

            gpuParamsBuffer.upload(particleParams);
            gpuStateBuffer.upload(particleState);

            gpuActive = true;
            System.out.println("==================================================================");
            System.out.println("⚡ FastGPU HARDWARE COMPUTE PIPELINE INITIALIZED (100,000 Particles)");
            System.out.println("Vulkan Compute Pipeline: READY");
            System.out.println("==================================================================");
        } catch (Throwable t) {
            System.err.println("❌ FastGPU Hardware Error: " + t.getMessage());
            System.err.println("Note: FastGPU requires Vulkan SDK / glslc to compile GLSL shaders directly to SPIR-V.");
            gpuActive = false;
        }
    }

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

        int cr = (int) (26 + (Math.min(1.0f, r) * 255 - 26) * fog);
        int cg = (int) (27 + (Math.min(1.0f, g) * 255 - 27) * fog);
        int cb = (int) (38 + (Math.min(1.0f, b) * 255 - 38) * fog);

        cr = Math.min(255, Math.max(26, cr));
        cg = Math.min(255, Math.max(27, cg));
        cb = Math.min(255, Math.max(38, cb));

        return (cr << 16) | (cg << 8) | cb;
    }

    public void start() {
        createBufferStrategy(3);
        BufferStrategy bs = getBufferStrategy();

        new Thread(() -> {
            long lastFpsTime = System.nanoTime();
            int frames = 0;
            long frameTimeTarget = 1_000_000_000L / 60;
            long lastRenderTime = System.nanoTime();

            float camYaw = 0f;
            float camPitch = 0f;
            float lightPhase = 0f;
            float ambientPhase = 0f;

            while (true) {
                long nowLoop = System.nanoTime();
                if (nowLoop - lastRenderTime < frameTimeTarget) {
                    Thread.yield();
                    continue;
                }
                lastRenderTime = nowLoop;
                lightPhase += 0.018f;
                ambientPhase += 0.005f;

                updateGentleSeparation();

                float ambR = (float) (0.5f + 0.5f * fastSin(ambientPhase));
                float ambG = (float) (0.3f + 0.3f * fastSin(ambientPhase + 2.094f));
                float ambB = (float) (0.6f + 0.4f * fastSin(ambientPhase + 4.188f));

                float mEx = fastCos(lightPhase) * 560f;
                float mEy = fastSin(lightPhase * 0.7f) * 380f;
                float mEz = fastSin(lightPhase) * 560f;

                float cEx = fastCos(lightPhase + 2.094f) * 560f;
                float cEy = fastSin((lightPhase + 2.094f) * 0.7f) * 380f;
                float cEz = fastSin(lightPhase + 2.094f) * 560f;

                float aEx = fastCos(lightPhase + 4.188f) * 560f;
                float aEy = fastSin((lightPhase + 4.188f) * 0.7f) * 380f;
                float aEz = fastSin(lightPhase + 4.188f) * 560f;

                camYaw += 0.002f;
                camPitch = fastSin(camYaw * 0.5f) * 0.2f;

                float cosY = fastCos(camYaw);
                float sinY = fastSin(camYaw);
                float cosP = fastCos(camPitch);
                float sinP = fastSin(camPitch);

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

                for (int b = 0; b < BALL_COUNT; b++) {
                    Ball ball = balls.get(b);
                    float bx = ball.x + ball.boidOffsetX;
                    float by = ball.y + ball.boidOffsetY;
                    float bz = ball.z + ball.boidOffsetZ;

                    int sBase = b * 4;
                    sphereBufferData[sBase] = bx;
                    sphereBufferData[sBase + 1] = by;
                    sphereBufferData[sBase + 2] = bz;
                    sphereBufferData[sBase + 3] = ball.radiusScale;

                    ball.rotX = bx * cosY - bz * sinY;
                    float rz = bx * sinY + bz * cosY;
                    ball.rotY = by * cosP - rz * sinP;
                    ball.rotZ = by * sinP + rz * cosP;
                    ball.zDepth = FOV + ball.rotZ + CUBE_SIZE;

                    if (ball.zDepth <= 1.0f) continue;

                    float scale = FOV / ball.zDepth;
                    int sx = (int) (WIDTH / 2f + ball.rotX * scale);
                    int sy = (int) (HEIGHT / 2f + ball.rotY * scale);
                    int radius = (int) (48f * scale * ball.radiusScale);

                    if (radius <= 0) continue;

                    float fog = 1.0f - ((ball.zDepth - FOG_NEAR) / (FOG_FAR - FOG_NEAR));
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
                                float pixelZ = ball.zDepth - dz;

                                int idx = rowOffset + px;
                                if (pixelZ < zBuffer[idx]) {
                                    zBuffer[idx] = pixelZ;
                                    pixels[idx] = rgb;
                                }
                            }
                        }
                    }
                }

                int[] sphereBaseColors = new int[BALL_COUNT];
                for (int b = 0; b < BALL_COUNT; b++) {
                    Ball ball = balls.get(b);
                    sphereBaseColors[b] = computeRetroColor(ball.x + ball.boidOffsetX, ball.y + ball.boidOffsetY, ball.z + ball.boidOffsetZ, 1.0f, ambR, ambG, ambB, mEx, mEy, mEz, cEx, cEy, cEz, aEx, aEy, aEz);
                }

                if (gpuActive) {
                    globalUniforms[0] = cosY;
                    globalUniforms[1] = sinY;
                    globalUniforms[2] = cosP;
                    globalUniforms[3] = sinP;
                    gpuUniformsBuffer.upload(globalUniforms);
                    gpuSpheresBuffer.upload(sphereBufferData);

                    gpu.dispatch(
                            particlePhysicsKernel,
                            DispatchSize.of1D(PARTICLE_COUNT / 256 + 1),
                            KernelArgs.of(gpuParamsBuffer, gpuStateBuffer, gpuSpheresBuffer, gpuUniformsBuffer)
                    );
                    gpuStateBuffer.download(particleState);
                }

                for (int i = 0; i < PARTICLE_COUNT; i++) {
                    int sBase = i * 8;
                    float px, py, pz;
                    int bIdx;

                    if (gpuActive) {
                        px = particleState[sBase];
                        py = particleState[sBase + 1];
                        pz = particleState[sBase + 2];
                        bIdx = (int) particleState[sBase + 7];
                    } else {
                        bIdx = (int) particleState[sBase + 7];
                        Ball parent = balls.get(bIdx);
                        float angle = particleState[sBase + 6] + particleParams[i * 4 + 1];
                        particleState[sBase + 6] = angle;

                        float rad = particleParams[i * 4] * parent.radiusScale;
                        float tilt = particleParams[i * 4 + 2];
                        float ecc = particleParams[i * 4 + 3];

                        float ox = fastCos(angle) * rad * ecc;
                        float oy = fastSin(angle) * fastCos(tilt) * rad;
                        float oz = fastSin(angle) * fastSin(tilt) * rad;

                        float targetX = parent.x + parent.boidOffsetX + ox;
                        float targetY = parent.y + parent.boidOffsetY + oy;
                        float targetZ = parent.z + parent.boidOffsetZ + oz;

                        float vx = (particleState[sBase + 3] + (targetX - particleState[sBase]) * 0.025f) * 0.92f;
                        float vy = (particleState[sBase + 4] + (targetY - particleState[sBase + 1]) * 0.025f) * 0.92f;
                        float vz = (particleState[sBase + 5] + (targetZ - particleState[sBase + 2]) * 0.025f) * 0.92f;

                        particleState[sBase + 3] = vx;
                        particleState[sBase + 4] = vy;
                        particleState[sBase + 5] = vz;

                        px = particleState[sBase] + vx;
                        py = particleState[sBase + 1] + vy;
                        pz = particleState[sBase + 2] + vz;

                        particleState[sBase] = px;
                        particleState[sBase + 1] = py;
                        particleState[sBase + 2] = pz;
                    }

                    float rx = px * cosY - pz * sinY;
                    float rz = px * sinY + pz * cosY;
                    float ry = py * cosP - rz * sinP;
                    rz = py * sinP + rz * cosP;

                    float zDepth = FOV + rz + CUBE_SIZE;
                    if (zDepth <= 1.0f) continue;

                    float fog = 1.0f - ((zDepth - FOG_NEAR) / (FOG_FAR - FOG_NEAR));
                    fog = Math.max(0.35f, Math.min(1.0f, fog));

                    int baseColor = sphereBaseColors[bIdx % BALL_COUNT];
                    int sr = (baseColor >> 16) & 0xFF;
                    int sg = (baseColor >> 8) & 0xFF;
                    int sb = baseColor & 0xFF;
                    int cr = (int) (26 + (sr - 26) * fog);
                    int cg = (int) (27 + (sg - 27) * fog);
                    int cb = (int) (38 + (sb - 38) * fog);
                    int rgb = (cr << 16) | (cg << 8) | cb;

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

                        for (int rpy = minY; rpy <= maxY; rpy++) {
                            int dy = rpy - sy;
                            int dySq = dy * dy;
                            int rowOffset = rpy * WIDTH;

                            for (int rpx = minX; rpx <= maxX; rpx++) {
                                int dx = rpx - sx;
                                int distSq = dx * dx + dySq;
                                if (distSq <= radSq) {
                                    int idx = rowOffset + rpx;
                                    if (zDepth < zBuffer[idx]) {
                                        zBuffer[idx] = zDepth;
                                        pixels[idx] = rgb;
                                    }
                                }
                            }
                        }
                    }
                }

                Graphics g = bs.getDrawGraphics();
                g.drawImage(screenBuffer, 0, 0, null);
                g.dispose();
                bs.show();
                Toolkit.getDefaultToolkit().sync();

                frames++;
                long now = System.nanoTime();
                if (now - lastFpsTime >= 1_000_000_000L) {
                    int fps = frames;
                    String engineTag = gpuActive ? "FastGPU Vulkan/GLSL Compute" : "FastJava Host Pipeline";
                    SwingUtilities.invokeLater(() ->
                            parentFrame.setTitle("FastAnimation + FastTween + FastGPU — 100,000 Particles (" + engineTag + ") | FPS: " + fps)
                    );
                    frames = 0;
                    lastFpsTime = now;
                }
            }
        }, "Render-Loop-FastGPU").start();
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
            JFrame frame = new JFrame("FastAnimation + FastTween + FastGPU — 100,000 Particles");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);
            frame.setIgnoreRepaint(true);
            frame.setIconImage(createRoundIcon());

            FastGpuParticleTimelineDemo demo = new FastGpuParticleTimelineDemo(frame);
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
