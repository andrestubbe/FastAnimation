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
        System.out.println("⚡ FastAnimation Ultimate Benchmark Suite");
        System.out.println("=========================================\n");
        
        System.out.println("[JAVA MODE]");
        runSuite(HeartbeatMode.JAVA);
        
        System.out.println("\n[NATIVE_MM MODE]");
        runSuite(HeartbeatMode.NATIVE_MM);
        
        System.out.println("\n✅ Benchmark Complete.");
        FastAnimation.stopEngine();
    }
    
    private static void runSuite(HeartbeatMode mode) throws InterruptedException {
        FastAnimation.setHeartbeatMode(mode);
        
        // Ramp-Up Test
        runTest("  1,000 Tweens", 1000, false);
        runTest(" 10,000 Tweens", 10000, false);
        runTest("100,000 Tweens", 100000, false);
        
        // GC + Complex Timeline Stress
        runTest(" 10k GC Stress", 10000, true);
    }
    
    private static void runTest(String label, int count, boolean gcStress) throws InterruptedException {
        final int DURATION_SEC = 2; 
        
        AtomicLong totalJitter = new AtomicLong(0);
        AtomicLong maxJitter = new AtomicLong(0);
        AtomicInteger spikes = new AtomicInteger(0);
        AtomicInteger frames = new AtomicInteger(0);
        
        final long[] lastTime = { System.nanoTime() };
        final long[] lastDelta = { 0 };
        
        // Prepare Tweens
        if (gcStress) {
            for (int i = 0; i < count; i++) {
                FastAnimation.sequence(
                    FastTween.to(0f, 50f, DURATION_SEC * 500).onUpdate(v -> { byte[] junk = new byte[64]; }),
                    FastTween.to(50f, 100f, DURATION_SEC * 500)
                ).start();
            }
        } else {
            for (int i = 0; i < count; i++) {
                FastTween.to(0f, 100f, DURATION_SEC * 1000).start();
            }
        }

        // Measure true jitter (tick-to-tick variation)
        FastAnimation.parallel(
            FastTween.to(0f, 1f, DURATION_SEC * 1000)
                .onUpdate(v -> {
                    long now = System.nanoTime();
                    if (frames.get() > 5) {
                        long currentDelta = now - lastTime[0];
                        if (lastDelta[0] > 0) {
                            long diff = Math.abs(currentDelta - lastDelta[0]);
                            totalJitter.addAndGet(diff);
                            if (diff > maxJitter.get()) maxJitter.set(diff);
                            if (diff > 5_000_000) spikes.incrementAndGet(); // >5ms variation
                        }
                        lastDelta[0] = currentDelta;
                    }
                    lastTime[0] = now;
                    frames.incrementAndGet();
                })
        ).start();

        long startTest = System.currentTimeMillis();
        long endTest = startTest + (DURATION_SEC * 1000);
        
        while (System.currentTimeMillis() < endTest) {
            long now = System.currentTimeMillis();
            float progress = (float)(now - startTest) / (DURATION_SEC * 1000f);
            if (progress > 1f) progress = 1f;
            printProgressBar(label, progress);
            Thread.sleep(50);
        }
        
        printFinalStats(label, frames.get(), totalJitter.get(), maxJitter.get(), spikes.get());
        Thread.sleep(200); // Give engine time to clear
    }
    
    private static void printProgressBar(String label, float progress) {
        int barLength = 15;
        int filled = (int)(progress * barLength);
        StringBuilder sb = new StringBuilder();
        sb.append("\r").append(label).append(": ");
        for (int i = 0; i < barLength; i++) {
            if (i < filled) sb.append("\u2588"); 
            else sb.append("\u2591"); 
        }
        sb.append(String.format(" %3d%%", (int)(progress * 100)));
        System.out.print(sb.toString());
    }
    
    private static void printFinalStats(String label, int frames, long totalJitter, long maxJitter, int spikes) {
        long avgJitter = frames > 0 ? totalJitter / frames : 0;
        String clear = "          ";
        System.out.printf("\r%s: \u2588\u2588\u2588\u2588\u2588\u2588\u2588\u2588\u2588\u2588\u2588\u2588\u2588\u2588\u2588 100%% | Avg: %4d us | Max: %4d us | Spikes(>5ms): %2d %s%n", 
                label, avgJitter / 1000, maxJitter / 1000, spikes, clear);
    }
}
