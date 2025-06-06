package org.zafer.wflopbenchmark.benchmarks;

import com.fasterxml.jackson.core.type.TypeReference;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.zafer.wflopbenchmark.helpers.RandomSolutionGenerator;
import org.zafer.wflopconfig.ConfigLoader;
import org.zafer.wflopcore.calculator.PowerOutputCalculator;
import org.zafer.wflopcore.calculator.WakeCalculatorJensen;
import org.zafer.wflopcore.model.GEOnePointFiveSLEPowerModel;
import org.zafer.wflopcore.model.PowerModel;
import org.zafer.wflopmodel.problem.WFLOP;
import org.zafer.wflopmodel.solution.Solution;
import org.zafer.wflopmodel.wind.WindProfile;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(5)
public class WFLOPBenchmark {

    @Param({"true", "false"})
    public boolean useDistanceMatrix;

    @Param({"true", "false"})
    public boolean useIntersectedAreaMatrix;

    private WFLOP wflop;
    private PowerOutputCalculator powerCalculator;
    private Solution iterationSolution;
    private WindProfile[] windProfiles;

    @Setup(Level.Trial)
    public void setup() {
        String wflopFile = String.format("wflop_distance_%s_area_%s.json",
                useDistanceMatrix ? "true" : "false",
                useIntersectedAreaMatrix ? "true" : "false");

        wflop = ConfigLoader.loadFromResource(wflopFile, new TypeReference<WFLOP>() {});

        PowerModel powerModel = new GEOnePointFiveSLEPowerModel();
        powerCalculator = new PowerOutputCalculator(wflop, powerModel);
    }

    @Setup(Level.Iteration)
    public void prepareIteration() {
        iterationSolution = new Solution(RandomSolutionGenerator.populateUniqueRandomListShuffle(
                wflop.getNumberOfTurbines(),
                wflop.getCellCount()));
    }

    @Benchmark
    public void benchmarkTotalPowerOutput(Blackhole bh) {
        bh.consume(powerCalculator.calculateTotalPowerOutput(iterationSolution));
    }
}
