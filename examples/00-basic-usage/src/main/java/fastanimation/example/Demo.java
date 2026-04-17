package fastanimation.example;

import fastanimation.Animation;
import fastanimation.FastAnimation;
import fasttween.Ease;
import fasttween.FastTween;

/**
 * Demo showcasing FastAnimation functionality.
 * 
 * Run with: mvn compile exec:java
 */
public class Demo {
    
    public static void main(String[] args) throws InterruptedException {
        printBanner();
        
        demoSequence();
        demoParallel();
        demoLoop();
        
        System.out.println("\n✅ Demo complete!");
        FastAnimation.stopEngine();
    }
    
    private static void demoSequence() throws InterruptedException {
        System.out.println("\n--- Sequence Demo (0 -> 50 -> 100) ---");
        
        Animation anim = FastAnimation.sequence(
            FastTween.to(0f, 50f, 400).ease(Ease.QUAD_OUT)
                .onUpdate(v -> System.out.printf("Step 1: %.1f%n", v)),
            FastTween.to(50f, 100f, 400).ease(Ease.CUBIC_OUT)
                .onUpdate(v -> System.out.printf("Step 2: %.1f%n", v))
        ).onComplete(() -> System.out.println("Sequence complete!"))
         .start();
        
        while (anim.isRunning()) {
            Thread.sleep(16);
        }
    }
    
    private static void demoParallel() throws InterruptedException {
        System.out.println("\n--- Parallel Demo (X and Y simultaneously) ---");
        
        final float[] x = {0};
        final float[] y = {0};
        
        Animation anim = FastAnimation.parallel(
            FastTween.to(0f, 100f, 600).ease(Ease.QUAD_OUT)
                .onUpdate(v -> {
                    x[0] = v;
                    System.out.printf("X: %.1f, Y: %.1f%n", x[0], y[0]);
                }),
            FastTween.to(0f, 50f, 600).ease(Ease.CUBIC_OUT)
                .onUpdate(v -> y[0] = v)
        ).onComplete(() -> System.out.println("Parallel complete!"))
         .start();
        
        while (anim.isRunning()) {
            Thread.sleep(16);
        }
    }
    
    private static void demoLoop() throws InterruptedException {
        System.out.println("\n--- Loop Demo (3x bounce) ---");
        
        int[] count = {0};
        
        Animation anim = FastAnimation.sequence(
            FastTween.to(0f, 100f, 200).ease(Ease.BOUNCE_OUT)
                .onUpdate(v -> System.out.printf("Loop %d: %.1f%n", count[0] + 1, v))
        ).loop(3)
         .onComplete(() -> System.out.println("All loops complete!"))
         .start();
        
        while (anim.isRunning()) {
            Thread.sleep(16);
        }
    }
    
    private static void printBanner() {
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║        FastAnimation Demo v1.0.0         ║");
        System.out.println("╚══════════════════════════════════════════╝");
    }
}
