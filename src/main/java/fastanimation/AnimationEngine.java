package fastanimation;

import fastexecution.FastExecution;

import java.util.ArrayList;
import java.util.List;

/**
 * FastAnimation Engine - The high-precision heartbeat orchestrator powered by FastExecution.
 */
public final class AnimationEngine {

    public enum HeartbeatMode {JAVA, NATIVE_MM, NATIVE_VSYNC}

    private static final List<Animation> animations = new ArrayList<>();
    private static final List<Animation> toAdd = new ArrayList<>();
    private static final List<Animation> toRemove = new ArrayList<>();

    private static HeartbeatMode mode = HeartbeatMode.JAVA;
    private static final String ENGINE_LOOP_NAME = "FastAnimation-Heartbeat";
    private static Thread engineThread;
    private static boolean running = false;
    private static long lastTime = System.nanoTime();

    private AnimationEngine() {
    }

    public static void setHeartbeatMode(HeartbeatMode mode) {
        AnimationEngine.mode = mode;
        restartEngine();
    }

    public static void add(Animation animation) {
        synchronized (toAdd) {
            toAdd.add(animation);
        }
        startEngine();
    }

    public static void remove(Animation animation) {
        synchronized (toRemove) {
            toRemove.add(animation);
        }
    }

    private static void startEngine() {
        if (mode == HeartbeatMode.JAVA) {
            startJavaEngine();
        } else {
            startFastExecutionEngine();
        }
    }

    private static void startJavaEngine() {
        if (running) return;
        if (engineThread != null && engineThread.isAlive()) {
            return;
        }
        running = true;
        engineThread = new Thread(AnimationEngine::javaEngineLoop, "FastAnimation-Heartbeat");
        engineThread.setDaemon(true);
        engineThread.setPriority(Thread.MAX_PRIORITY);
        engineThread.start();
    }

    private static void startFastExecutionEngine() {
        if (FastExecution.isActive(ENGINE_LOOP_NAME)) return;

        lastTime = System.nanoTime();

        Runnable tickTask = () -> {
            long now = System.nanoTime();
            float deltaMs = (now - lastTime) / 1_000_000.0f;
            lastTime = now;
            tick(deltaMs);
        };

        switch (mode) {
            case NATIVE_VSYNC:
                FastExecution.loopVSync(ENGINE_LOOP_NAME, 60, tickTask);
                break;
            case NATIVE_MM:
                FastExecution.loop(ENGINE_LOOP_NAME, 1000, tickTask);
                break;
            default:
                break;
        }
    }

    public static void stop() {
        if (mode == HeartbeatMode.JAVA) {
            running = false;
            if (engineThread != null) engineThread.interrupt();
        } else {
            FastExecution.stop(ENGINE_LOOP_NAME);
        }
    }

    private static void restartEngine() {
        stop();
        try {
            Thread.sleep(20);
        } catch (InterruptedException ignored) {
        }
        startEngine();
    }

    private static void tick(float deltaMs) {
        // 1. Process pending changes (Fast Sync)
        synchronized (toAdd) {
            if (!toAdd.isEmpty()) {
                animations.addAll(toAdd);
                toAdd.clear();
            }
        }
        synchronized (toRemove) {
            if (!toRemove.isEmpty()) {
                if (toRemove.size() > 20) {
                    animations.removeAll(new java.util.HashSet<>(toRemove)); // O(N) removal
                } else {
                    animations.removeAll(toRemove);
                }
                toRemove.clear();
            }
        }

        // 2. High-speed Tick
        for (Animation anim : animations) {
            anim.update(deltaMs);
            if (anim.isComplete()) {
                synchronized (toRemove) {
                    toRemove.add(anim);
                }
            }
        }

        // 3. Auto-stop when empty
        if (animations.isEmpty() && toAdd.isEmpty()) {
            stop();
        }
    }

    private static void javaEngineLoop() {
        long loopLastTime = System.nanoTime();
        while (running) {
            long now = System.nanoTime();
            float deltaMs = (now - loopLastTime) / 1_000_000.0f;
            loopLastTime = now;

            tick(deltaMs);

            // Java timing
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    public static int getActiveAnimationCount() {
        return animations.size();
    }

    public static int getActiveTweenCount() {
        return animations.size();
    } // Simplified
}
