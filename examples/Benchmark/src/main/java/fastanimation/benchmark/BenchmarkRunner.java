package fastanimation.benchmark;

import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Collection;

public class BenchmarkRunner {

    public static void main(String[] args) throws Exception {
        System.out.println("===========================================");
        System.out.println("FastAnimation Benchmark (v0.1.0)");
        System.out.println("===========================================\n");

        System.out.println("--------------------------------------------");
        System.out.println("|  FastAnimation Orchestration Test        |");
        System.out.println("--------------------------------------------");
        
        System.out.println("\n[INFO] Initializing JMH Runner...");
        System.out.println("This will take a few minutes to complete as JMH isolates,");
        System.out.println("warms up, and measures throughput rigorously.\n");

        Options opt = new OptionsBuilder()
                .include(EngineBenchmark.class.getSimpleName())
                .jvmArgs("--sun-misc-unsafe-memory-access=allow")
                .forks(1)
                .build();

        Collection<RunResult> results = new Runner(opt).run();

        System.out.println("\n===========================================");
        System.out.println("FastAnimation Benchmark Results Summary:");
        System.out.println("===========================================\n");
        
        System.out.printf("%-30s | %-15s | %-10s%n", "Benchmark", "Score (ops/ms)", "Error");
        System.out.println("------------------------------------------------------------------");
        
        for (RunResult result : results) {
            String name = result.getPrimaryResult().getLabel();
            double score = result.getPrimaryResult().getScore();
            double error = result.getPrimaryResult().getScoreError();
            
            System.out.printf("%-30s | %10.3f      |  %7.3f%n", name, score, error);
        }
        
        System.out.println("------------------------------------------------------------------");
        System.out.println("\n[DONE] Benchmark Complete.");
    }
}
