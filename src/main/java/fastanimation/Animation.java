package fastanimation;

import fasttween.Ease;
import fasttween.Tween;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A timeline animation that orchestrates multiple tweens.
 * 
 * <p>FastAnimation is the "Director" that controls sequences of tweens,
 * loops, keyframes, and events. It provides:
 * <ul>
 *   <li><b>Sequences</b> - Chain tweens one after another</li>
 *   <li><b>Parallel</b> - Run tweens simultaneously</li>
 *   <li><b>Loops</b> - Repeat animations with count or infinite</li>
 *   <li><b>Events</b> - onStart, onUpdate, onComplete hooks</li>
 * </ul>
 * 
 * @author FastJava Team
 * @version 1.0.0
 */
public class Animation {
    
    public enum Mode {
        SEQUENCE, PARALLEL
    }
    
    private final Mode mode;
    private final List<Tween> tweens;
    private final List<Keyframe> keyframes;
    
    private int loopCount = 1;
    private int currentLoop = 0;
    private int currentTweenIndex = 0;
    private float elapsedTime = 0;
    private float totalDuration = 0;
    
    private boolean isRunning = false;
    private boolean isComplete = false;
    private boolean isPaused = false;
    
    private Runnable onStart;
    private Consumer<Float> onUpdate;
    private Runnable onComplete;
    
    Animation(Mode mode) {
        this.mode = mode;
        this.tweens = new ArrayList<>();
        this.keyframes = new ArrayList<>();
    }
    
    /**
     * Adds a tween to this animation.
     * 
     * @param tween Tween to add
     * @return This animation for chaining
     */
    public Animation add(Tween tween) {
        tweens.add(tween);
        calculateDuration();
        return this;
    }
    
    /**
     * Adds multiple tweens to this animation.
     * 
     * @param tweens Tweens to add
     * @return This animation for chaining
     */
    public Animation add(Tween... tweens) {
        for (Tween tween : tweens) {
            this.tweens.add(tween);
        }
        calculateDuration();
        return this;
    }
    
    /**
     * Sets the number of loops (1 = no loop, -1 = infinite).
     * 
     * @param count Loop count, -1 for infinite
     * @return This animation for chaining
     */
    public Animation loop(int count) {
        this.loopCount = count;
        return this;
    }
    
    /**
     * Sets a callback for when the animation starts.
     * 
     * @param callback Runnable to invoke
     * @return This animation for chaining
     */
    public Animation onStart(Runnable callback) {
        this.onStart = callback;
        return this;
    }
    
    /**
     * Sets a callback for each update frame.
     * 
     * @param callback Consumer receiving progress [0.0, 1.0]
     * @return This animation for chaining
     */
    public Animation onUpdate(Consumer<Float> callback) {
        this.onUpdate = callback;
        return this;
    }
    
    /**
     * Sets a callback for when the animation completes.
     * 
     * @param callback Runnable to invoke
     * @return This animation for chaining
     */
    public Animation onComplete(Runnable callback) {
        this.onComplete = callback;
        return this;
    }
    
    /**
     * Starts this animation. Registers with the AnimationEngine.
     * 
     * @return This animation for chaining
     */
    public Animation start() {
        if (isRunning) {
            return this;
        }
        
        isRunning = true;
        isComplete = false;
        isPaused = false;
        currentLoop = 0;
        currentTweenIndex = 0;
        elapsedTime = 0;
        
        if (onStart != null) {
            onStart.run();
        }
        
        AnimationEngine.add(this);
        
        if (mode == Mode.PARALLEL) {
            for (Tween tween : tweens) {
                tween.start();
            }
        } else {
            if (!tweens.isEmpty()) {
                tweens.get(0).start();
            }
        }
        
        return this;
    }
    
    /**
     * Pauses this animation.
     * 
     * @return This animation for chaining
     */
    public Animation pause() {
        isPaused = true;
        for (Tween tween : tweens) {
            // Note: Tween doesn't have pause(), would need to add
        }
        return this;
    }
    
    /**
     * Resumes this animation.
     * 
     * @return This animation for chaining
     */
    public Animation resume() {
        isPaused = false;
        return this;
    }
    
    /**
     * Stops this animation immediately.
     * 
     * @return This animation for chaining
     */
    public Animation stop() {
        isRunning = false;
        AnimationEngine.remove(this);
        return this;
    }
    
    /**
     * Restarts this animation from the beginning.
     * 
     * @return This animation for chaining
     */
    public Animation restart() {
        stop();
        return start();
    }
    
    void update(float deltaMs) {
        if (!isRunning || isPaused) {
            return;
        }
        
        elapsedTime += deltaMs;
        
        if (mode == Mode.SEQUENCE) {
            updateSequence(deltaMs);
        } else {
            updateParallel(deltaMs);
        }
        
        float progress = totalDuration > 0 ? Math.min(1.0f, elapsedTime / totalDuration) : 1.0f;
        if (onUpdate != null) {
            onUpdate.accept(progress);
        }
    }
    
    private void updateSequence(float deltaMs) {
        if (currentTweenIndex >= tweens.size()) {
            handleLoopOrComplete();
            return;
        }
        
        Tween current = tweens.get(currentTweenIndex);
        if (!current.isRunning() && !current.isComplete()) {
            current.start();
        }
        
        if (current.isComplete()) {
            currentTweenIndex++;
            if (currentTweenIndex < tweens.size()) {
                tweens.get(currentTweenIndex).start();
            } else {
                handleLoopOrComplete();
            }
        }
    }
    
    private void updateParallel(float deltaMs) {
        boolean allComplete = true;
        for (Tween tween : tweens) {
            if (!tween.isComplete()) {
                allComplete = false;
                break;
            }
        }
        
        if (allComplete && !tweens.isEmpty()) {
            handleLoopOrComplete();
        }
    }
    
    private void handleLoopOrComplete() {
        currentLoop++;
        
        if (loopCount == -1 || currentLoop < loopCount) {
            currentTweenIndex = 0;
            elapsedTime = 0;
            
            for (Tween tween : tweens) {
                tween.reset();
            }
            
            if (mode == Mode.SEQUENCE && !tweens.isEmpty()) {
                tweens.get(0).start();
            } else {
                for (Tween tween : tweens) {
                    tween.start();
                }
            }
        } else {
            isComplete = true;
            isRunning = false;
            AnimationEngine.remove(this);
            
            if (onComplete != null) {
                onComplete.run();
            }
        }
    }
    
    private void calculateDuration() {
        if (mode == Mode.PARALLEL) {
            totalDuration = 0;
            for (Tween tween : tweens) {
                totalDuration = Math.max(totalDuration, tween.getDuration());
            }
        } else {
            totalDuration = 0;
            for (Tween tween : tweens) {
                totalDuration += tween.getDuration();
            }
        }
        
        if (loopCount > 1) {
            totalDuration *= loopCount;
        }
    }
    
    /**
     * Checks if this animation is currently running.
     * 
     * @return true if running
     */
    public boolean isRunning() {
        return isRunning;
    }
    
    /**
     * Checks if this animation has completed.
     * 
     * @return true if complete
     */
    public boolean isComplete() {
        return isComplete;
    }
    
    /**
     * Checks if this animation is paused.
     * 
     * @return true if paused
     */
    public boolean isPaused() {
        return isPaused;
    }
    
    /**
     * Returns the mode of this animation.
     * 
     * @return Mode (SEQUENCE or PARALLEL)
     */
    public Mode getMode() {
        return mode;
    }
    
    /**
     * Returns the current progress [0.0, 1.0].
     * 
     * @return Progress value
     */
    public float getProgress() {
        if (totalDuration <= 0) return 1.0f;
        return Math.min(1.0f, elapsedTime / totalDuration);
    }
}
