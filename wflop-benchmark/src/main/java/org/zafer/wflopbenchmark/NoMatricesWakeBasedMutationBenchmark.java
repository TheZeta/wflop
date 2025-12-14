package org.zafer.wflopbenchmark;

import com.fasterxml.jackson.core.type.TypeReference;
import org.openjdk.jmh.annotations.*;
import org.zafer.wflopalgorithms.algorithms.novelga.strategy.WakeBasedMutationStrategy;
import org.zafer.wflopalgorithms.common.ga.solution.Individual;
import org.zafer.wflopbenchmark.helpers.RandomSolutionGenerator;
import org.zafer.wflopconfig.ConfigLoader;
import org.zafer.wflopcore.wake.NoMatricesWakeCalculatorProvider;
import org.zafer.wflopcore.power.PowerOutputCalculator;
import org.zafer.wflopmodel.problem.WFLOP;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(value = 1)
@State(Scope.Benchmark)
public class NoMatricesWakeBasedMutationBenchmark {

    private WakeBasedMutationStrategy wakeBasedMutationStrategy;
    private Individual originalIndividual;
    private WFLOP wflop;
    private PowerOutputCalculator powerOutputCalculator;
    private double wakeAnalysisPercentage = 0.1;
    private double mutationSelectionPercentage = 0.5;

    @Setup(Level.Trial)
    public void setup() {
        this.wflop = ConfigLoader.loadFromResource(
                "wflop_problem.json",
                new TypeReference<WFLOP>() {});
        this.powerOutputCalculator = new PowerOutputCalculator(wflop, new NoMatricesWakeCalculatorProvider());
        this.wakeBasedMutationStrategy = new WakeBasedMutationStrategy(
                wakeAnalysisPercentage,
                mutationSelectionPercentage,
                powerOutputCalculator);

        originalIndividual = new Individual(RandomSolutionGenerator.populateUniqueRandomListShuffle(
                wflop.getNumberOfTurbines(),
                wflop.getCellCount()));
    }

    @Benchmark
    public Individual benchmarkMutation() {
        Individual copy = new Individual(originalIndividual.getGenes());

        wakeBasedMutationStrategy.mutate(copy, wflop);

        return copy;
    }
}
