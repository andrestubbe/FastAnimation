package fastanimation;

import fasttween.Tween;

/**
 * FastAnimation - Lightweight Java timeline engine for orchestrating tweens.
 */
public final class FastAnimation {
    
    public static final String VERSION = "0.1.0";
    
    private FastAnimation() {
        // Utility class
    }
    
    /** Sets the engine's heartbeat strategy. */
    public static void setHeartbeatMode(AnimationEngine.HeartbeatMode mode) {
        AnimationEngine.setHeartbeatMode(mode);
    }
    
    /** Creates a sequence of tweens. */
    public static Animation sequence(Tween... tweens) {
        Animation anim = new Animation(Animation.Mode.SEQUENCE);
        for (Tween t : tweens) anim.add(t);
        return anim;
    }

    /** Creates a sequence of animations. */
    public static Animation sequence(Animation... animations) {
        Animation anim = new Animation(Animation.Mode.SEQUENCE);
        for (Animation a : animations) anim.add(a);
        return anim;
    }
    
    /** Creates a parallel group of tweens. */
    public static Animation parallel(Tween... tweens) {
        Animation anim = new Animation(Animation.Mode.PARALLEL);
        for (Tween t : tweens) anim.add(t);
        return anim;
    }

    /** Creates a parallel group of animations. */
    public static Animation parallel(Animation... animations) {
        Animation anim = new Animation(Animation.Mode.PARALLEL);
        for (Animation a : animations) anim.add(a);
        return anim;
    }

    /** Creates a percentage-based timeline. */
    public static Animation timeline(Keyframe... keyframes) {
        Animation anim = new Animation(Animation.Mode.TIMELINE);
        for (Keyframe k : keyframes) anim.add(k);
        return anim;
    }

    /** Creates a keyframe helper. */
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
