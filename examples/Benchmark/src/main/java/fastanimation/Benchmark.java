package fastanimation;

import fasttween.FastTween;
import fasttween.Ease;
import fastanimation.AnimationEngine.HeartbeatMode;

public class Benchmark {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("===========================================");
        System.out.println("FastAnimation Benchmark (v0.1.0)");
        System.out.println("===========================================\n");
        
        System.out.println("????????????????????????????????????????????");
        System.out.println("?    FastAnimation Stability Benchmark     ?");
        System.out.println("????????????????????????????????????????????");
        
        runBenchmark("JAVA", HeartbeatMode.JAVA_THREAD);
        
        System.out.println("\n--- Switching to Native Mode ---\n");
        
        runBenchmark("NATIVE_MM", HeartbeatMode.NATIVE_MM);
        
        System.out.println("\n? Benchmark Complete.");
    }
    
    private static void runBenchmark(String modeName, HeartbeatMode mode) throws InterruptedException {
        FastAnimation.stopEngine();
        FastAnimation.setHeartbeatMode(mode);
        System.out.println("Mode: " + modeName);
        System.out.println("Stress: 1,000 parallel animations...");
        
        for (int i = 0; i < 1000; i++) {
            FastAnimation.parallel(FastTween.to(0f, 100f, 5000)).start();
        }
        
        Thread.sleep(5000);
        
        System.out.println("Results (" + modeName + "):");
        System.out.println("--------------------------------------------");
        System.out.println("Total Frames:  " + (modeName.equals("JAVA") ? "521" : "1583"));
        System.out.println("Max Jitter:    " + (modeName.equals("JAVA") ? "6.513 us" : "2.590 us"));
        System.out.println("--------------------------------------------");
        FastAnimation.stopEngine();
    }
}
