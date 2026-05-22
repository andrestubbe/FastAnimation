package fastanimation;

import fasttween.Tween;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A timeline animation that orchestrates multiple tweens, child animations, and keyframes.
 */
public class Animation {
    
    public enum Mode { SEQUENCE, PARALLEL, TIMELINE }
    
    private final Mode mode;
    private final List<Tween> tweens = new ArrayList<>();
    private final List<Animation> children = new ArrayList<>();
    private final List<Keyframe> keyframes = new ArrayList<>();
    
    private int loopCount = 1;
    private int currentLoop = 0;
    private int currentIndex = 0;
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
    }
    
    public Animation add(Tween tween) { tweens.add(tween); calculateDuration(); return this; }
    public Animation add(Animation child) { children.add(child); calculateDuration(); return this; }
    public Animation add(Keyframe keyframe) { keyframes.add(keyframe); calculateDuration(); return this; }
    
    public Animation loop(int count) { this.loopCount = count; return this; }
    public Animation onStart(Runnable callback) { this.onStart = callback; return this; }
    public Animation onUpdate(Consumer<Float> callback) { this.onUpdate = callback; return this; }
    public Animation onComplete(Runnable callback) { this.onComplete = callback; return this; }
    
    public Animation start() {
        if (isRunning) return this;
        isRunning = true;
        isComplete = false;
        isPaused = false;
        currentLoop = 0;
        currentIndex = 0;
        elapsedTime = 0;
        
        if (onStart != null) onStart.run();
        AnimationEngine.add(this);
        
        if (mode == Mode.PARALLEL) {
            for (Tween t : tweens) t.start();
            for (Animation a : children) a.start();
        } else if (mode == Mode.SEQUENCE) {
            startNextInSequence();
        }
        return this;
    }
    
    private void startNextInSequence() {
        if (currentIndex < tweens.size()) tweens.get(currentIndex).start();
        else if (currentIndex < tweens.size() + children.size()) children.get(currentIndex - tweens.size()).start();
    }

    public Animation stop() { isRunning = false; return this; }
    
    void update(float deltaMs) {
        if (!isRunning || isPaused) return;
        elapsedTime += deltaMs;
        
        float progress = totalDuration > 0 ? Math.min(1.0f, elapsedTime / totalDuration) : 1.0f;

        if (mode == Mode.SEQUENCE) updateSequence();
        else if (mode == Mode.PARALLEL) updateParallel();
        else if (mode == Mode.TIMELINE) updateTimeline(progress);
        
        if (onUpdate != null) onUpdate.accept(progress);
    }
    
    private void updateSequence() {
        int totalItems = tweens.size() + children.size();
        if (currentIndex >= totalItems) { handleLoopOrComplete(); return; }

        boolean currentDone = false;
        if (currentIndex < tweens.size()) {
            Tween t = tweens.get(currentIndex);
            t.update();
            currentDone = t.isComplete();
        } else {
            Animation a = children.get(currentIndex - tweens.size());
            currentDone = a.isComplete();
        }

        if (currentDone) {
            currentIndex++;
            if (currentIndex < totalItems) startNextInSequence();
            else handleLoopOrComplete();
        }
    }
    
    private void updateParallel() {
        boolean allDone = true;
        for (Tween t : tweens) { t.update(); if (!t.isComplete()) allDone = false; }
        for (Animation a : children) { if (!a.isComplete()) allDone = false; }
        if (allDone && (!tweens.isEmpty() || !children.isEmpty())) handleLoopOrComplete();
    }

    private void updateTimeline(float progress) {
        boolean allDone = true;
        for (Keyframe k : keyframes) {
            if (progress >= k.getProgress()) {
                if (!k.getTween().isRunning() && !k.getTween().isComplete()) k.getTween().start();
                k.getTween().update();
            }
            if (!k.getTween().isComplete()) allDone = false;
        }
        if (allDone && progress >= 1.0f) handleLoopOrComplete();
    }
    
    private void handleLoopOrComplete() {
        currentLoop++;
        if (loopCount == -1 || currentLoop < loopCount) {
            currentIndex = 0;
            elapsedTime = 0;
            for (Tween t : tweens) t.reset();
            for (Animation a : children) { a.stop(); a.isComplete = false; }
            for (Keyframe k : keyframes) k.getTween().reset();
            startNextInSequence();
        } else {
            isComplete = true;
            isRunning = false;
            if (onComplete != null) onComplete.run();
        }
    }
    
    private void calculateDuration() {
        totalDuration = 0;
        if (mode == Mode.PARALLEL) {
            for (Tween t : tweens) totalDuration = Math.max(totalDuration, t.getDuration());
            for (Animation a : children) totalDuration = Math.max(totalDuration, a.totalDuration);
        } else if (mode == Mode.SEQUENCE) {
            for (Tween t : tweens) totalDuration += t.getDuration();
            for (Animation a : children) totalDuration += a.totalDuration;
        } else if (mode == Mode.TIMELINE) {
            for (Keyframe k : keyframes) {
                // Approximate duration based on last keyframe + its tween duration
                float pointDuration = (1.0f / k.getProgress()) * k.getTween().getDuration(); // This is tricky
                // Better: Explicitly set timeline duration or use 1000ms default
                totalDuration = 2000; // Default for now
            }
        }
    }
    
    public Animation duration(long ms) { this.totalDuration = ms; return this; }
    public boolean isRunning() { return isRunning; }
    public boolean isComplete() { return isComplete; }
}
