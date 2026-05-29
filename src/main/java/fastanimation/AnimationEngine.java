package fastanimation;

import java.util.ArrayList;
import java.util.List;

/**
 * FastAnimation Engine - The high-precision heartbeat orchestrator.
 */
public final class AnimationEngine {
    
    public enum HeartbeatMode { JAVA, NATIVE_MM, NATIVE_VSYNC }
    
    private static final List<Animation> animations = new ArrayList<>();
    private static final List<Animation> toAdd = new ArrayList<>();
    private static final List<Animation> toRemove = new ArrayList<>();
    
    private static HeartbeatMode mode = HeartbeatMode.JAVA;
    private static Thread engineThread;
    private static boolean running = false;
    
    private AnimationEngine() {}
    
    public static void setHeartbeatMode(HeartbeatMode mode) {
        AnimationEngine.mode = mode;
        restartEngine();
    }
    
    public static void add(Animation animation) {
        synchronized(toAdd) {
            toAdd.add(animation);
        }
        startEngine();
    }
    
    public static void remove(Animation animation) {
        synchronized(toRemove) {
            toRemove.add(animation);
        }
    }
    
    private static void startEngine() {
        if (running) return;
        if (engineThread != null && engineThread.isAlive()) {
            // Old thread is still dying (e.g. caught in a long operation).
            // It will exit naturally because running=false. We shouldn't spawn a second one!
            return;
        }
        running = true;
        engineThread = new Thread(AnimationEngine::engineLoop, "FastAnimation-Heartbeat");
        engineThread.setDaemon(true);
        engineThread.setPriority(Thread.MAX_PRIORITY);
        engineThread.start();
    }
    
    public static void stop() {
        running = false;
        if (engineThread != null) engineThread.interrupt();
    }
    
    private static void restartEngine() {
        stop();
        try { Thread.sleep(20); } catch (InterruptedException ignored) {}
        startEngine();
    }
    
    private static void engineLoop() {
        long lastTime = System.nanoTime();
        
        while (running) {
            long now = System.nanoTime();
            float deltaMs = (now - lastTime) / 1_000_000.0f;
            lastTime = now;
            
            // 1. Process pending changes (Fast Sync)
            synchronized(toAdd) {
                if (!toAdd.isEmpty()) {
                    animations.addAll(toAdd);
                    toAdd.clear();
                }
            }
            synchronized(toRemove) {
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
                    synchronized(toRemove) { toRemove.add(anim); }
                }
            }
            
            // 3. Precision Timing
            if (mode == HeartbeatMode.NATIVE_MM) {
                fastdwm.FastDWM.createPeriodicTimer(1, () -> {}); // Ensure MM timer active
                try { Thread.sleep(1); } catch (InterruptedException e) { break; }
            } else if (mode == HeartbeatMode.NATIVE_VSYNC) {
                fastdwm.FastDWM.waitForVSync();
            } else {
                try { Thread.sleep(5); } catch (InterruptedException e) { break; }
            }

            if (animations.isEmpty() && toAdd.isEmpty()) {
                running = false;
                break;
            }
        }
    }
    
    public static int getActiveAnimationCount() { return animations.size(); }
    public static int getActiveTweenCount() { return animations.size(); } // Simplified
}
