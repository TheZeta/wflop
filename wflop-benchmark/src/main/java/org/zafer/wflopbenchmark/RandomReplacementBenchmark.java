package org.zafer.wflopbenchmark;

import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.core.type.TypeReference;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import org.openjdk.jmh.infra.Blackhole;
import org.zafer.wflopalgorithms.common.ga.solution.Individual;
import org.zafer.wflopalgorithms.common.ga.strategy.RandomReplacementMutation;
import org.zafer.wflopbenchmark.helpers.RandomSolutionGenerator;
import org.zafer.wflopconfig.ConfigLoader;
import org.zafer.wflopmodel.problem.WFLOP;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(value = 1)
@State(Scope.Benchmark)
public class RandomReplacementBenchmark {

    private RandomReplacementMutation randomReplacementMutation;
    private Individual originalIndividual;
    private WFLOP wflop;

    @Setup(Level.Trial)
    public void setup() {
        this.wflop = ConfigLoader.load(
                "wflop_problem.json",
                new TypeReference<WFLOP>() {});
        this.randomReplacementMutation = new RandomReplacementMutation();

        originalIndividual = new Individual(RandomSolutionGenerator.populateUniqueRandomListShuffle(
                wflop.getNumberOfTurbines(),
                wflop.getCellCount()));
    }

    @Benchmark
    public void benchmarkMutation(Blackhole bh) {
        Individual copy = new Individual(originalIndividual.getGenes());

        randomReplacementMutation.mutate(copy, wflop);
        bh.consume(copy);
    }
}
