package fastanimation;

import fastanimation.Animation;
import fastanimation.FastAnimation;
import fastanimation.AnimationEngine.HeartbeatMode;
import fasttween.FastTween;
import fastansi.FastANSI;
import fastansi.FastSGR;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Ultimate FastAnimation Benchmark - Java vs Native Heartbeat.
 */
public class Benchmark {
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("⚡ FastAnimation Benchmark\n");
        
        System.out.println("[JAVA MODE]");
        runSuite(HeartbeatMode.JAVA);
        
        System.out.println("\n[NATIVE_MM MODE (via FastDWM)]");
        runSuite(HeartbeatMode.NATIVE_MM);
        
        System.out.println("\n✅ Benchmark Complete.");
        FastAnimation.stopEngine();
    }
    
    private static void runSuite(HeartbeatMode mode) throws InterruptedException {
        FastAnimation.setHeartbeatMode(mode);
        
        // Ramp-Up Test
        runTest("     1k Tweens", 1000, false);
        runTest("    10k Tweens", 10000, false);
        runTest("   100k Tweens", 100000, false);
        runTest(" 1,000k Tweens", 1000000, false);
        
        // GC + Complex Timeline Stress
        runTest(" 10k GC Stress", 10000, true);
    }
    
    private static void runTest(String label, int count, boolean gcStress) throws InterruptedException {
        final int DURATION_SEC = 5; 
        
        AtomicLong totalJitter = new AtomicLong(0);
        AtomicLong maxJitter = new AtomicLong(0);
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
        
        printFinalStats(label, frames.get(), DURATION_SEC, totalJitter.get(), maxJitter.get());
        Thread.sleep(200); // Give engine time to clear
    }
    
    private static void printProgressBar(String label, float progress) {
        int barLength = 15;
        int filled = (int)(progress * barLength);
        StringBuilder sb = new StringBuilder();
        sb.append("\r").append(label).append(": ");
        
        String colorFilled = FastANSI.CSI + FastSGR.RESET + "m";
        String colorEmpty = FastANSI.CSI + "38;2;64;64;64m"; // 25% White (RGB 64,64,64)
        String colorReset = FastANSI.CSI + FastSGR.RESET + "m";

        for (int i = 0; i < barLength; i++) {
            if (i < filled) sb.append(colorFilled).append("\u2588"); 
            else sb.append(colorEmpty).append("\u2588"); 
        }
        sb.append(colorReset).append(String.format(" %3d%%", (int)(progress * 100)));
        System.out.print(sb.toString());
    }
    
    private static void printFinalStats(String label, int frames, int durationSec, long totalJitter, long maxJitter) {
        long avgJitter = frames > 0 ? totalJitter / frames : 0;
        int fps = frames / durationSec;
        String clear = "          ";
        
        String colorFilled = FastANSI.CSI + FastSGR.RESET + "m";
        String colorReset = FastANSI.CSI + FastSGR.RESET + "m";
        
        System.out.printf("\r%s: %s\u2588\u2588\u2588\u2588\u2588\u2588\u2588\u2588\u2588\u2588\u2588\u2588\u2588\u2588\u2588%s 100%% | FPS: %4d | Avg: %4d us | Max: %4d us %s%n", 
                label, colorFilled, colorReset, fps, avgJitter / 1000, maxJitter / 1000, clear);
    }
}
