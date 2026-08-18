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
        if (FastExecution.isActive(ENGINE_LOOP_NAME)) return;

        Runnable tickTask = AnimationEngine::tick;

        switch (mode) {
            case NATIVE_VSYNC:
                FastExecution.loopVSync(ENGINE_LOOP_NAME, 60, tickTask);
                break;
            case NATIVE_MM:
                FastExecution.loop(ENGINE_LOOP_NAME, 1000, tickTask);
                break;
            case JAVA:
            default:
                FastExecution.loop(ENGINE_LOOP_NAME, 200, tickTask);
                break;
        }
    }

    public static void stop() {
        FastExecution.stop(ENGINE_LOOP_NAME);
    }

    private static void restartEngine() {
        stop();
        try {
            Thread.sleep(20);
        } catch (InterruptedException ignored) {
        }
        startEngine();
    }

    private static void tick() {
        long now = System.nanoTime();
        float deltaMs = (now - lastTime) / 1_000_000.0f;
        lastTime = now;

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

    public static int getActiveAnimationCount() {
        return animations.size();
    }

    public static int getActiveTweenCount() {
        return animations.size();
    } // Simplified
}
