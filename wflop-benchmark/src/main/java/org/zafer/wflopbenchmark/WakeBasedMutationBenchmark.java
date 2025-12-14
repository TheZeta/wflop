package org.zafer.wflopbenchmark;

import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.core.type.TypeReference;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import org.zafer.wflopalgorithms.algorithms.novelga.strategy.WakeBasedMutationStrategy;
import org.zafer.wflopalgorithms.common.ga.solution.Individual;
import org.zafer.wflopbenchmark.helpers.RandomSolutionGenerator;
import org.zafer.wflopconfig.ConfigLoader;
import org.zafer.wflopcore.power.PowerOutputCalculator;
import org.zafer.wflopcore.wake.DefaultWakeCalculatorProvider;
import org.zafer.wflopcore.wake.WakeCalculationPolicy;
import org.zafer.wflopmodel.problem.WFLOP;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(value = 1)
@State(Scope.Benchmark)
public class WakeBasedMutationBenchmark {

    @Param({"true,true", "true, false", "false, true", "false,false"})
    public String policy;

    private WakeBasedMutationStrategy wakeBasedMutationStrategy;
    private Individual originalIndividual;
    private WFLOP wflop;

    @Setup(Level.Trial)
    public void setup() {
        boolean useDistanceMatrix;
        boolean useIntersectedAreaMatrix;

        String[] parts = policy.split(",");
        useDistanceMatrix = Boolean.parseBoolean(parts[0]);
        useIntersectedAreaMatrix = Boolean.parseBoolean(parts[1]);

        this.wflop = ConfigLoader.loadFromResource(
                "wflop_problem.json",
                new TypeReference<WFLOP>() {}
        );

        PowerOutputCalculator powerOutputCalculator =
                new PowerOutputCalculator(
                        wflop,
                        new DefaultWakeCalculatorProvider(),
                        new WakeCalculationPolicy(useDistanceMatrix, useIntersectedAreaMatrix)
                );

        this.wakeBasedMutationStrategy =
                new WakeBasedMutationStrategy(0.1, 0.5, powerOutputCalculator);

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
