package fastanimation;

import fastanimation.Animation;
import fastanimation.FastAnimation;
import fastanimation.AnimationEngine.HeartbeatMode;
import fasttween.FastTween;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Ultimate FastAnimation Benchmark - Java vs Native Heartbeat.
 */
public class Benchmark {
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║    FastAnimation Stability Benchmark     ║");
        System.out.println("╚══════════════════════════════════════════╝");
        
        runTest(HeartbeatMode.JAVA);
        System.out.println("\n--- Switching to Native Mode ---\n");
        runTest(HeartbeatMode.NATIVE_MM);
        
        System.out.println("\n✅ Benchmark Complete.");
        FastAnimation.stopEngine();
    }
    
    private static void runTest(HeartbeatMode mode) throws InterruptedException {
        System.out.println("Mode: " + mode);
        System.out.println("Stress: 1,000 parallel animations...");
        
        FastAnimation.setHeartbeatMode(mode);
        
        final int DURATION_SEC = 3;
        AtomicLong maxJitter = new AtomicLong(0);
        AtomicInteger frames = new AtomicInteger(0);
        final long[] lastTime = { System.nanoTime() };
        
        // Spawn 1,000 tweens to stress the engine
        for (int i = 0; i < 1000; i++) {
            FastTween.to(0f, 100f, DURATION_SEC * 1000).start();
        }

        // Measure jitter on a probe animation
        FastAnimation.parallel(
            FastTween.to(0f, 1f, DURATION_SEC * 1000)
                .onUpdate(v -> {
                    long now = System.nanoTime();
                    if (frames.get() > 10) {
                        long diff = Math.abs((now - lastTime[0]) - 1_000_000);
                        if (diff > maxJitter.get()) maxJitter.set(diff);
                    }
                    lastTime[0] = now;
                    frames.incrementAndGet();
                })
        ).start();

        Thread.sleep(DURATION_SEC * 1000);
        
        System.out.printf("Results (%s):%n", mode);
        System.out.println("--------------------------------------------");
        System.out.printf("Total Frames:  %,d%n", frames.get());
        System.out.printf("Max Jitter:    %,d us%n", maxJitter.get() / 1000);
        System.out.println("--------------------------------------------");
    }
}
