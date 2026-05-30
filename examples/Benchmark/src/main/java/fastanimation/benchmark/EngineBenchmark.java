package fastanimation.benchmark;

import fastanimation.Animation;
import fastanimation.FastAnimation;
import fasttween.FastTween;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
@State(Scope.Thread)
public class EngineBenchmark {

    @Benchmark
    public Animation orchestrateParallel() {
        // Measures the overhead of wrapping tweens in FastAnimation orchestration (Track/Parallel)
        return FastAnimation.parallel(
                FastTween.to(0f, 100f, 1000),
                FastTween.to(50f, 200f, 1000),
                FastTween.to(0f, 1f, 1000)
        );
    }

    @Benchmark
    public Animation orchestrateSequence() {
        // Measures the overhead of sequence chaining
        return FastAnimation.sequence(
                FastTween.to(0f, 100f, 1000),
                FastTween.to(100f, 50f, 500),
                FastTween.to(50f, 0f, 1000)
        );
    }
}
