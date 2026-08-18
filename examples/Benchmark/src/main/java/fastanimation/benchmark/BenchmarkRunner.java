package fastanimation.benchmark;

import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * JMH entry point for FastAnimation benchmarks.
 *
 * <p>Run via the shaded fat-JAR produced by {@code mvn clean package}:
 * <pre>
 *   java -jar target/benchmarks.jar
 * </pre>
 *
 * <p>Or directly from Maven:
 * <pre>
 *   mvn clean package -DskipTests &amp;&amp; java -jar target/benchmarks.jar
 * </pre>
 */
public class BenchmarkRunner {

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include("fastanimation\\.AnimationBenchmark")
                .build();
        new Runner(opt).run();
    }
}
