package org.zafer.wflopbenchmark.benchmarks;

import com.fasterxml.jackson.core.type.TypeReference;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.zafer.wflopbenchmark.helpers.RandomSolutionGenerator;
import org.zafer.wflopconfig.ConfigLoader;
import org.zafer.wflopcore.calculator.*;
import org.zafer.wflopcore.model.DefaultPowerModelProvider;
import org.zafer.wflopcore.model.PowerModelProvider;
import org.zafer.wflopmodel.problem.WFLOP;
import org.zafer.wflopmodel.layout.TurbineLayout;

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
    private TurbineLayout iterationTurbineLayout;

    @Setup(Level.Trial)
    public void setup() {
        String wflopFile = "wflop_problem.json";
        wflop = ConfigLoader.loadFromResource(wflopFile, new TypeReference<WFLOP>() {});

        PowerModelProvider powerModelProvider = new DefaultPowerModelProvider();
        WakeCalculationPolicy wakeCalculationPolicy = new WakeCalculationPolicy(useDistanceMatrix, useIntersectedAreaMatrix);
        WakeCalculatorProvider wakeCalculatorProvider = new ConfigurableWakeCalculatorProvider(wakeCalculationPolicy);
        powerCalculator = new PowerOutputCalculator(wflop, powerModelProvider, wakeCalculatorProvider);
    }

    @Setup(Level.Iteration)
    public void prepareIteration() {
        iterationTurbineLayout = new TurbineLayout(RandomSolutionGenerator.populateUniqueRandomListShuffle(
                wflop.getNumberOfTurbines(),
                wflop.getCellCount()));
    }

    @Benchmark
    public void benchmarkTotalPowerOutput(Blackhole bh) {
        bh.consume(powerCalculator.calculateTotalPowerOutput(iterationTurbineLayout));
    }
}
