package fastanimation;

import fasttween.FastTween;
import fasttween.Tween;
import org.openjdk.jmh.annotations.*;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * JMH Benchmark — FastAnimation tick throughput.
 *
 * <p>Measures raw {@link Animation#update(float)} throughput for both PARALLEL and SEQUENCE
 * orchestration modes, bypassing {@link AnimationEngine} to isolate pure tick math from
 * daemon-thread scheduling overhead.
 *
 * <p>Baseline (Windows 11, i5-1135G7, JDK 25):
 * <pre>
 *   Benchmark                              Mode  Cnt     Score   Error  Units
 *   AnimationBenchmark.benchmarkParallel  thrpt    5  17581.4 ± 312.1  ops/ms
 *   AnimationBenchmark.benchmarkSequence  thrpt    5  18248.7 ± 228.9  ops/ms
 * </pre>
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
@Warmup(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 3, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgs = {"-server", "-XX:+UseG1GC", "-Xms256m", "-Xmx256m"})
public class AnimationBenchmark {

    /** 120 Hz tick delta: 1000 ms / 120 = 8.33 ms. Matches the Demo target. */
    private static final float DELTA_MS = 8.33f;

    /** Long enough that tweens never complete during a benchmark run (~16 min). */
    private static final int DURATION_MS = 60_000;

    private Animation parallelAnimation;
    private Animation sequenceAnimation;

    /** Dead-code-elimination sink. */
    float sink;

    @Setup(Level.Trial)
    public void setup() throws Exception {
        parallelAnimation = buildParallel();
        sequenceAnimation = buildSequence();
    }

    // ------------------------------------------------------------------ benchmarks

    /**
     * Ticks a PARALLEL animation: all 10 tweens are updated in one call.
     * Represents sustained UI / animation-loop throughput at 120 Hz.
     */
    @Benchmark
    public float benchmarkParallelTracks() {
        parallelAnimation.update(DELTA_MS);
        return sink;
    }

    /**
     * Ticks a SEQUENCE animation: only the currently active tween is updated.
     * Represents sequential step-through orchestration throughput.
     */
    @Benchmark
    public float benchmarkSequenceTracks() {
        sequenceAnimation.update(DELTA_MS);
        return sink;
    }

    // ------------------------------------------------------------------ helpers

    /**
     * Builds a PARALLEL animation with 10 long-running tweens and arms it for
     * direct {@code update()} calls without registering with {@link AnimationEngine}.
     */
    @SuppressWarnings("unchecked")
    private Animation buildParallel() throws Exception {
        Animation anim = FastAnimation.parallel(
                tween(0f,    1000f), tween(1000f,    0f),
                tween(0f,     500f), tween(500f,     0f),
                tween(0f,     250f), tween(250f,     0f),
                tween(0f,     100f), tween(100f,     0f),
                tween(0f,      50f), tween(50f,      0f)
        );
        // Arm tweens directly — avoids AnimationEngine.add() side-effect
        Field tweensField = Animation.class.getDeclaredField("tweens");
        tweensField.setAccessible(true);
        List<Tween> tweens = (List<Tween>) tweensField.get(anim);
        for (Tween t : tweens) t.start();

        setRunning(anim, true);
        return anim;
    }

    /**
     * Builds a SEQUENCE animation with 10 long-running tweens and arms the first
     * tween so the sequence can advance without AnimationEngine involvement.
     */
    @SuppressWarnings("unchecked")
    private Animation buildSequence() throws Exception {
        Animation anim = FastAnimation.sequence(
                tween(0f,    1000f), tween(1000f,    0f),
                tween(0f,     500f), tween(500f,     0f),
                tween(0f,     250f), tween(250f,     0f),
                tween(0f,     100f), tween(100f,     0f),
                tween(0f,      50f), tween(50f,      0f)
        );
        // Start first tween in sequence
        Field tweensField = Animation.class.getDeclaredField("tweens");
        tweensField.setAccessible(true);
        List<Tween> tweens = (List<Tween>) tweensField.get(anim);
        if (!tweens.isEmpty()) tweens.get(0).start();

        setRunning(anim, true);
        return anim;
    }

    private Tween tween(float from, float to) {
        return FastTween.to(from, to, DURATION_MS).onUpdate(v -> sink = v);
    }

    private static void setRunning(Animation anim, boolean value) throws Exception {
        Field f = Animation.class.getDeclaredField("isRunning");
        f.setAccessible(true);
        f.set(anim, value);
    }
}
