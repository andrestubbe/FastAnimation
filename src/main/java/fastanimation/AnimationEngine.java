package fastanimation;

import fasttween.Tween;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Central time-based ticker for all animations.
 * 
 * <p>The AnimationEngine is the "motor" that drives all animations. It:
 * <ul>
 *   <li>Maintains a list of all active tweens</li>
 *   <li>Calls update(delta) every frame</li>
 *   <li>Removes finished tweens</li>
 *   <li>Provides precise delta time calculation</li>
 * </ul>
 * 
 * <p>Starts automatically when the first tween is added. Runs in a daemon
 * thread so it won't prevent JVM shutdown.
 * 
 * @author FastJava Team
 * @version 1.0.0
 */
public class AnimationEngine {
    
    private static final List<Tween> activeTweens = new ArrayList<>();
    private static final List<Animation> activeAnimations = new ArrayList<>();
    private static volatile boolean running = false;
    private static Thread engineThread;
    
    /**
     * Adds a tween to the engine. Starts the engine if not running.
     * 
     * @param tween Tween to animate
     */
    public static void add(Tween tween) {
        synchronized (activeTweens) {
            activeTweens.add(tween);
        }
        ensureRunning();
    }
    
    /**
     * Adds an animation to the engine. Starts the engine if not running.
     * 
     * @param animation Animation to process
     */
    public static void add(Animation animation) {
        synchronized (activeAnimations) {
            activeAnimations.add(animation);
        }
        ensureRunning();
    }
    
    /**
     * Removes a tween from the engine.
     * 
     * @param tween Tween to remove
     */
    public static void remove(Tween tween) {
        synchronized (activeTweens) {
            activeTweens.remove(tween);
        }
    }
    
    /**
     * Removes an animation from the engine.
     * 
     * @param animation Animation to remove
     */
    public static void remove(Animation animation) {
        synchronized (activeAnimations) {
            activeAnimations.remove(animation);
        }
    }
    
    /**
     * Stops the engine entirely.
     */
    public static void stop() {
        running = false;
        if (engineThread != null) {
            engineThread.interrupt();
        }
    }
    
    /**
     * Returns the number of active tweens.
     * 
     * @return Active tween count
     */
    public static int getActiveTweenCount() {
        synchronized (activeTweens) {
            return activeTweens.size();
        }
    }
    
    /**
     * Returns the number of active animations.
     * 
     * @return Active animation count
     */
    public static int getActiveAnimationCount() {
        synchronized (activeAnimations) {
            return activeAnimations.size();
        }
    }
    
    private static void ensureRunning() {
        if (!running) {
            synchronized (AnimationEngine.class) {
                if (!running) {
                    startEngine();
                }
            }
        }
    }
    
    private static void startEngine() {
        running = true;
        engineThread = new Thread(() -> {
            long lastTime = System.nanoTime();
            
            while (running) {
                long currentTime = System.nanoTime();
                float deltaMs = (currentTime - lastTime) / 1_000_000f;
                lastTime = currentTime;
                
                update(deltaMs);
                
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "FastAnimation-Engine");
        
        engineThread.setDaemon(true);
        engineThread.start();
    }
    
    private static void update(float deltaMs) {
        synchronized (activeTweens) {
            Iterator<Tween> it = activeTweens.iterator();
            while (it.hasNext()) {
                Tween tween = it.next();
                tween.update();
                if (!tween.isRunning()) {
                    it.remove();
                }
            }
        }
        
        synchronized (activeAnimations) {
            Iterator<Animation> it = activeAnimations.iterator();
            while (it.hasNext()) {
                Animation anim = it.next();
                anim.update(deltaMs);
                if (anim.isComplete()) {
                    it.remove();
                }
            }
        }
    }
}
