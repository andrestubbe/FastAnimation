package fastanimation;

import fasttween.Tween;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

/**
 * A track binds a property to one or more tweens with keyframes.
 * 
 * <p>Tracks allow complex property animations where the value changes
 * based on multiple keyframes over time. Think of it as a timeline track
 * in video editing software.
 * 
 * @author FastJava Team
 * @version 1.0.0
 */
public class Track {
    
    private final String name;
    private final List<Keyframe> keyframes;
    private final Consumer<Float> setter;
    
    private float currentValue = 0;
    private boolean isRunning = false;
    
    /**
     * Creates a track for a named property.
     * 
     * @param name Property name (for debugging)
     * @param setter Consumer that receives the animated value
     */
    public Track(String name, Consumer<Float> setter) {
        this.name = name;
        this.setter = setter;
        this.keyframes = new ArrayList<>();
    }
    
    /**
     * Adds a keyframe to this track.
     * 
     * @param progress Progress point [0.0, 1.0]
     * @param tween Tween to execute
     * @return This track for chaining
     */
    public Track keyframe(float progress, Tween tween) {
        keyframes.add(new Keyframe(progress, tween));
        // Sort by progress to ensure correct order
        keyframes.sort(Comparator.comparing(Keyframe::getProgress));
        return this;
    }
    
    /**
     * Updates the track based on animation progress.
     * 
     * @param progress Animation progress [0.0, 1.0]
     */
    public void update(float progress) {
        isRunning = true;
        
        // Start keyframes that should begin at this progress
        for (Keyframe keyframe : keyframes) {
            keyframe.startIfNeeded(progress);
        }
        
        // Find the currently active keyframe and update value
        Keyframe activeKeyframe = findActiveKeyframe();
        if (activeKeyframe != null) {
            currentValue = activeKeyframe.getTween().currentValue();
            if (setter != null) {
                setter.accept(currentValue);
            }
        }
    }
    
    /**
     * Starts all keyframes in this track.
     */
    public void start() {
        isRunning = true;
        for (Keyframe keyframe : keyframes) {
            keyframe.reset();
        }
    }
    
    /**
     * Stops this track.
     */
    public void stop() {
        isRunning = false;
    }
    
    /**
     * Resets all keyframes in this track.
     */
    public void reset() {
        for (Keyframe keyframe : keyframes) {
            keyframe.reset();
        }
    }
    
    /**
     * Checks if all keyframes in this track are complete.
     * 
     * @return true if all complete
     */
    public boolean isComplete() {
        for (Keyframe keyframe : keyframes) {
            if (!keyframe.isComplete()) {
                return false;
            }
        }
        return true;
    }
    
    private Keyframe findActiveKeyframe() {
        // Find the most recent started keyframe that hasn't completed
        Keyframe active = null;
        for (Keyframe keyframe : keyframes) {
            if (keyframe.hasStarted()) {
                active = keyframe;
            }
        }
        return active;
    }
    
    /**
     * Returns the track name.
     * 
     * @return Name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Returns the current value.
     * 
     * @return Current value
     */
    public float getCurrentValue() {
        return currentValue;
    }
    
    /**
     * Returns the list of keyframes (unmodifiable).
     * 
     * @return Keyframes
     */
    public List<Keyframe> getKeyframes() {
        return Collections.unmodifiableList(keyframes);
    }
    
    /**
     * Returns whether this track is running.
     * 
     * @return true if running
     */
    public boolean isRunning() {
        return isRunning;
    }
    
    @Override
    public String toString() {
        return String.format("Track[%s: %d keyframes]", name, keyframes.size());
    }
}
