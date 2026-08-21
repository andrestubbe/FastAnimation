package fastanimation;

import fasttween.Tween;

/**
 * FastAnimation - Lightweight Java timeline engine for orchestrating tweens.
 */
public final class FastAnimation {

    public static final String VERSION = "0.1.2";

    private FastAnimation() {
        // Utility class
    }

    /**
     * Sets the engine's heartbeat strategy.
     */
    public static void setHeartbeatMode(AnimationEngine.HeartbeatMode mode) {
        AnimationEngine.setHeartbeatMode(mode);
    }

    /**
     * Advances the animation timeline by deltaMs (for offline recording or manual stepping).
     */
    public static void step(float deltaMs) {
        AnimationEngine.step(deltaMs);
    }

    /**
     * Creates a sequence of tweens.
     */
    public static Animation sequence(Tween... tweens) {
        Animation animation = new Animation(Animation.Mode.SEQUENCE);
        for (Tween tween : tweens) {
            animation.add(tween);
        }
        return animation;
    }

    /**
     * Creates a sequence of animations.
     */
    public static Animation sequence(Animation... animations) {
        Animation animation = new Animation(Animation.Mode.SEQUENCE);
        for (Animation animation1 : animations) {
            animation.add(animation1);
        }
        return animation;
    }

    /**
     * Creates a parallel group of tweens.
     */
    public static Animation parallel(Tween... tweens) {
        Animation animation = new Animation(Animation.Mode.PARALLEL);
        for (Tween tween : tweens) {
            animation.add(tween);
        }
        return animation;
    }

    /**
     * Creates a parallel group of animations.
     */
    public static Animation parallel(Animation... animations) {
        Animation animation = new Animation(Animation.Mode.PARALLEL);
        for (Animation animation1 : animations) {
            animation.add(animation1);
        }
        return animation;
    }

    /**
     * Creates a percentage-based timeline.
     */
    public static Animation timeline(Keyframe... keyframes) {
        Animation animation = new Animation(Animation.Mode.TIMELINE);
        for (Keyframe k : keyframes) {
            animation.add(k);
        }
        return animation;
    }

    /**
     * Creates a keyframe helper.
     */
    public static Keyframe keyframe(float progress, Tween tween) {
        return new Keyframe(progress, tween);
    }

    public static void stopEngine() {
        AnimationEngine.stop();
    }

    public static int getActiveTweenCount() {
        return AnimationEngine.getActiveTweenCount();
    }

    public static int getActiveAnimationCount() {
        return AnimationEngine.getActiveAnimationCount();
    }
}
