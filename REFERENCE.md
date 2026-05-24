# FastAnimation Reference & Vocabulary

## 1. Core Vocabulary

*   **Engine (`AnimationEngine`)**: The central background daemon thread that orchestrates all active animations and timelines. It continuously loops, pushing time forward based on the selected Heartbeat Mode.
*   **Heartbeat Mode**: The underlying native or managed timing source driving the Engine.
    *   `JAVA`: Relies on standard Java `Thread.sleep()`. Susceptible to JVM jitter and OS scheduling inaccuracies.
    *   `NATIVE_MM`: Uses `FastDWM` to hook into Windows Multimedia Timers for strict, sub-millisecond precision.
    *   `NATIVE_VSYNC`: Uses `FastDWM` to lock the animation engine perfectly to the monitor's hardware refresh rate (e.g., 60Hz, 144Hz) for tear-free UI rendering.
*   **Animation**: The core timeline container. Can operate in modes like `SEQUENCE` (play sequentially), `PARALLEL` (play simultaneously), or `TIMELINE` (play based on specific progress percentages).
*   **Tween (`FastTween`)**: Provided by the sibling library. Handles the mathematical interpolation of a value from point A to point B over a specific duration.
*   **Keyframe**: A specific `Tween` action anchored to a specific percentage point within a `TIMELINE` animation.
*   **Jitter**: The deviation (measured in microseconds) between when an animation frame *should* have fired and when it *actually* fired. Lower jitter equals smoother animations.

## 2. API Quick Reference

### `FastAnimation` (Main Facade)
*   `setHeartbeatMode(HeartbeatMode mode)`: Hot-swaps the engine's timing source. Automatically restarts the engine if it is running.
*   `sequence(Tween... tweens)`: Creates an `Animation` where each tween waits for the previous one to finish before starting.
*   `parallel(Tween... tweens)`: Creates an `Animation` where all provided tweens start immediately at the same time.
*   `timeline(Keyframe... keyframes)`: Creates an `Animation` structured around percentage milestones.
*   `keyframe(float progress, Tween tween)`: Binds a tween to a specific progress marker (`0.0f` to `1.0f`).
*   `stopEngine()`: Forcefully interrupts and halts the background engine thread.

### `AnimationEngine`
*   `add(Animation animation)`: Queues an animation for execution. Automatically wakes up the engine thread if it was sleeping.
*   `remove(Animation animation)`: Queues an active animation for removal.
*   `getActiveAnimationCount()`: Returns the number of animations currently being processed in the loop.

## 3. Engine Guarantees & Contracts

*   **Zero-Allocation Tick Phase**: The core `engineLoop()` calculates `deltaMs` and updates tracks without instantiating new objects, completely preventing Garbage Collection (GC) stutter during motion.
*   **Deferred Synchronization**: Animations are added to `toAdd` and `toRemove` staging lists. The main engine loop cleanly flushes these lists at the start of each frame, ensuring thread-safe access without locking up the high-speed tick process.
*   **Auto-Sleep Architecture**: When both the active animation list and pending queues are empty, the daemon thread intentionally breaks its loop and dies to completely eliminate background CPU usage. It will instantly respawn the next time an animation is added.

## 4. Platform Requirements

| Platform | Heartbeat Support |
|----------|-------------------|
| Windows 10/11 (x64) | `JAVA`, `NATIVE_MM`, `NATIVE_VSYNC` ✅ |
| Linux / macOS | `JAVA` (Native modes require FastDWM platform ports) 🚧 |

---
**Part of the FastJava Ecosystem** — *Making the JVM faster.*
