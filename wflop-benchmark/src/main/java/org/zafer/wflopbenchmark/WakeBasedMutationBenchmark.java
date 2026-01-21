package org.zafer.wflopbenchmark;

import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.core.type.TypeReference;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import org.zafer.wflopalgorithms.algorithms.wdga.strategy.WakeBasedMutationStrategy;
import org.zafer.wflopalgorithms.common.ga.solution.Individual;
import org.zafer.wflopbenchmark.helpers.RandomSolutionGenerator;
import org.zafer.wflopconfig.ConfigLoader;
import org.zafer.wflopcore.power.PowerCalculator;
import org.zafer.wflopcore.wake.DefaultWakeModelProvider;
import org.zafer.wflopcore.wake.WakeOptimization;
import org.zafer.wflopmodel.problem.WFLOP;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(value = 1)
@State(Scope.Benchmark)
public class WakeBasedMutationBenchmark {

    @Param({
        "NONE",
        "DISTANCE_MATRIX",
        "INTERSECTION_MATRIX",
        "BOTH"
    })
    public WakeOptimization optimization;

    private WakeBasedMutationStrategy wakeBasedMutationStrategy;
    private Individual originalIndividual;
    private WFLOP wflop;

    @Setup(Level.Trial)
    public void setup() {
        this.wflop = ConfigLoader.load(
                "wflop_problem.json",
                new TypeReference<WFLOP>() {}
        );

        PowerCalculator powerCalculator =
                new PowerCalculator(
                        wflop,
                        new DefaultWakeModelProvider(),
                        optimization
                );

        this.wakeBasedMutationStrategy =
                new WakeBasedMutationStrategy(0.1, 0.5, powerCalculator);

        originalIndividual =
                new Individual(RandomSolutionGenerator.populateUniqueRandomListShuffle(
                        wflop.getNumberOfTurbines(),
                        wflop.getCellCount()));
    }

    @Benchmark
    public void benchmarkMutation(Blackhole bh) {
        Individual copy = new Individual(originalIndividual.getGenes());

        wakeBasedMutationStrategy.mutate(copy, wflop);
        bh.consume(copy);
    }
}
