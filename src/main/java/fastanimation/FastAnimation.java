package fastanimation;

import fasttween.Ease;
import fasttween.FastTween;
import fasttween.Tween;

/**
 * FastAnimation - Lightweight Java timeline engine for orchestrating tweens.
 * 
 * <p>FastAnimation is the "Director" that controls sequences of tweens,
 * parallel execution, loops, keyframes, and events. Built on FastTween
 * for zero overhead.
 * 
 * <p><b>Basic usage:</b>
 * <pre>
 * // Sequence of tweens
 * Animation anim = FastAnimation.sequence(
 *     FastTween.to(0f, 100f, 200).ease(Ease.QUAD_OUT),
 *     FastTween.to(100f, 20f, 400).ease(Ease.CUBIC_IN_OUT)
 * ).loop(3).start();
 * 
 * // Parallel execution
 * Animation parallel = FastAnimation.parallel(
 *     FastTween.to(x0, x1, 300).onUpdate(x -> setX(x)),
 *     FastTween.to(y0, y1, 300).onUpdate(y -> setY(y))
 * ).start();
 * </pre>
 * 
 * <p><b>Features:</b>
 * <ul>
 *   <li>Sequences - Chain tweens one after another</li>
 *   <li>Parallel - Run tweens simultaneously</li>
 *   <li>Loops - Repeat with count or infinite</li>
 *   <li>Keyframes - Define progress points (0% → 30% → 100%)</li>
 *   <li>Events - onStart, onUpdate, onComplete hooks</li>
 *   <li>Built-in Ticker - Automatic time management</li>
 * </ul>
 * 
 * @author FastJava Team
 * @version 1.0.0
 */
public final class FastAnimation {
    
    public static final String VERSION = "1.0.0";
    
    private FastAnimation() {
        // Utility class
    }
    
    /**
     * Creates a sequence animation where tweens run one after another.
     * 
     * @param tweens Tweens to sequence
     * @return Animation instance (call .start() to begin)
     */
    public static Animation sequence(Tween... tweens) {
        Animation anim = new Animation(Animation.Mode.SEQUENCE);
        for (Tween tween : tweens) {
            anim.add(tween);
        }
        return anim;
    }
    
    /**
     * Creates a parallel animation where all tweens run simultaneously.
     * 
     * @param tweens Tweens to run in parallel
     * @return Animation instance (call .start() to begin)
     */
    public static Animation parallel(Tween... tweens) {
        Animation anim = new Animation(Animation.Mode.PARALLEL);
        for (Tween tween : tweens) {
            anim.add(tween);
        }
        return anim;
    }
    
    /**
     * Creates an empty sequence animation.
     * 
     * @return Animation instance
     */
    public static Animation sequence() {
        return new Animation(Animation.Mode.SEQUENCE);
    }
    
    /**
     * Creates an empty parallel animation.
     * 
     * @return Animation instance
     */
    public static Animation parallel() {
        return new Animation(Animation.Mode.PARALLEL);
    }
    
    /**
     * Creates a track for animating a specific property.
     * 
     * @param name Property name (for debugging)
     * @param setter Consumer receiving the animated value
     * @return New track
     */
    public static Track track(String name, java.util.function.Consumer<Float> setter) {
        return new Track(name, setter);
    }
    
    /**
     * Creates a keyframe at a specific progress point.
     * 
     * @param progress Progress point [0.0, 1.0]
     * @param tween Tween to execute
     * @return New keyframe
     */
    public static Keyframe keyframe(float progress, Tween tween) {
        return new Keyframe(progress, tween);
    }
    
    /**
     * Stops the global animation engine entirely.
     * Use with caution - this stops ALL animations.
     */
    public static void stopEngine() {
        AnimationEngine.stop();
    }
    
    /**
     * Returns the number of active tweens being managed.
     * 
     * @return Active tween count
     */
    public static int getActiveTweenCount() {
        return AnimationEngine.getActiveTweenCount();
    }
    
    /**
     * Returns the number of active animations being managed.
     * 
     * @return Active animation count
     */
    public static int getActiveAnimationCount() {
        return AnimationEngine.getActiveAnimationCount();
    }
    
    /**
     * Main entry point for demo/testing.
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        System.out.println("FastAnimation v" + VERSION);
        System.out.println("Use examples/00-basic-usage/ for runnable demos");
    }
}
