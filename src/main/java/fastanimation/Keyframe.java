package fastanimation;

import fasttween.Tween;

/**
 * A keyframe defines a tween at a specific progress point in an animation.
 * 
 * <p>Keyframes allow complex multi-stage animations where different tweens
 * start at different points in the overall timeline (0% → 30% → 100%).
 * 
 * @author FastJava Team
 * @version 1.0.0
 */
public class Keyframe {
    
    private final float progress;  // 0.0 to 1.0
    private final Tween tween;
    private boolean hasStarted = false;
    
    /**
     * Creates a keyframe at a specific progress point.
     * 
     * @param progress Progress point [0.0, 1.0] where 0.0 = start, 1.0 = end
     * @param tween Tween to execute at this point
     */
    public Keyframe(float progress, Tween tween) {
        this.progress = Math.max(0.0f, Math.min(1.0f, progress));
        this.tween = tween;
    }
    
    /**
     * Checks if this keyframe should start at the given animation progress.
     * 
     * @param animationProgress Current animation progress [0.0, 1.0]
     * @return true if the keyframe should start
     */
    public boolean shouldStart(float animationProgress) {
        return !hasStarted && animationProgress >= progress;
    }
    
    /**
     * Starts the keyframe's tween if not already started.
     * 
     * @param animationProgress Current animation progress
     */
    public void startIfNeeded(float animationProgress) {
        if (shouldStart(animationProgress)) {
            hasStarted = true;
            tween.start();
        }
    }
    
    /**
     * Resets the keyframe so it can be triggered again.
     */
    public void reset() {
        hasStarted = false;
        tween.reset();
    }
    
    /**
     * Returns the progress point of this keyframe.
     * 
     * @return Progress [0.0, 1.0]
     */
    public float getProgress() {
        return progress;
    }
    
    /**
     * Returns the tween associated with this keyframe.
     * 
     * @return The tween
     */
    public Tween getTween() {
        return tween;
    }
    
    /**
     * Checks if this keyframe has already started.
     * 
     * @return true if started
     */
    public boolean hasStarted() {
        return hasStarted;
    }
    
    /**
     * Checks if the keyframe's tween is complete.
     * 
     * @return true if complete
     */
    public boolean isComplete() {
        return tween.isComplete();
    }
    
    @Override
    public String toString() {
        return String.format("Keyframe[%.1f%% -> %s]", progress * 100, tween);
    }
}
