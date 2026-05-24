package fastanimation;

import fastanimation.Animation;
import fastanimation.FastAnimation;
import fasttween.FastTween;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Benchmark comparing Animation Engine stability.
 */
public class Demo4 {
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║    FastAnimation Stability Benchmark     ║");
        System.out.println("╚══════════════════════════════════════════╝");
        
        System.out.println("\nMode: Standard Java (Thread.sleep)");
        System.out.println("Running 1,000 parallel animations...");
        
        final int DURATION_SEC = 3;
        AtomicLong maxJitter = new AtomicLong(0);
        AtomicInteger frames = new AtomicInteger(0);
        final long[] lastTime = { System.nanoTime() };
        
        // Spawn 1,000 tweens to stress the engine
        for (int i = 0; i < 1000; i++) {
            FastTween.to(0f, 100f, DURATION_SEC * 1000).start();
        }

        // Create a dedicated animation to track engine timing
        FastAnimation.parallel(
            FastTween.to(0f, 1f, DURATION_SEC * 1000)
                .onUpdate(v -> {
                    long now = System.nanoTime();
                    if (frames.get() > 10) {
                        // Calculate deviation from perfect 1ms tick
                        long diff = Math.abs((now - lastTime[0]) - 1_000_000);
                        if (diff > maxJitter.get()) maxJitter.set(diff);
                    }
                    lastTime[0] = now;
                    frames.incrementAndGet();
                })
        ).start();

        Thread.sleep(DURATION_SEC * 1000);
        
        System.out.println("\nResults (Java Mode):");
        System.out.println("--------------------------------------------");
        System.out.printf("Total Frames:  %,d%n", frames.get());
        System.out.printf("Average FPS:   %.1f%n", (float)frames.get() / DURATION_SEC);
        System.out.printf("Max Jitter:    %,d us%n", maxJitter.get() / 1000);
        System.out.println("--------------------------------------------");
        
        System.out.println("\n✅ Test Complete.");
        FastAnimation.stopEngine();
    }
}

