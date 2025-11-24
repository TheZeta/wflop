package org.zafer.wflopalgorithms.algorithms.novelga;

import java.util.Random;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.zafer.wflopalgorithms.algorithms.novelga.strategy.WakeBasedCrossoverStrategy;
import org.zafer.wflopalgorithms.algorithms.novelga.strategy.WakeBasedMutationStrategy;
import org.zafer.wflopalgorithms.common.ga.GA;
import org.zafer.wflopalgorithms.common.ga.strategy.CrossoverStrategy;
import org.zafer.wflopalgorithms.common.ga.strategy.MutationStrategy;
import org.zafer.wflopcore.calculator.PowerOutputCalculator;
import org.zafer.wflopmetaheuristic.Metaheuristic;
import org.zafer.wflopmetaheuristic.ProgressEvent;
import org.zafer.wflopmetaheuristic.ProgressListener;
import org.zafer.wflopmetaheuristic.Solution;
import org.zafer.wflopmodel.layout.TurbineLayout;
import org.zafer.wflopmodel.problem.WFLOP;

public class NovelGA extends GA {

    private final double wakeAnalysisPercentage; // Percentage of turbines to analyze
    private final double mutationSelectionPercentage; // Percentage of analyzed turbines to mutate

    @JsonCreator
    public NovelGA(
            @JsonProperty("algorithm") String algorithm,
            @JsonProperty("populationSize") int populationSize,
            @JsonProperty("generations") int generations,
            @JsonProperty("crossoverRate") double crossoverRate,
            @JsonProperty("mutationRate") double mutationRate,
            @JsonProperty("selectionStrategy") String selectionStrategy,
            @JsonProperty("wakeAnalysisPercentage") Double wakeAnalysisPercentage,
            @JsonProperty("mutationSelectionPercentage") Double mutationSelectionPercentage
    ) {
        super(algorithm, populationSize, generations, crossoverRate, mutationRate,
            selectionStrategy, "wakeBasedCrossover", "wakeBasedMutation");
        this.wakeAnalysisPercentage = wakeAnalysisPercentage != null 
            ? wakeAnalysisPercentage
            : 0.1;
        this.mutationSelectionPercentage = mutationSelectionPercentage != null
            ? mutationSelectionPercentage
            : 0.5;
    }

    @Override
    protected CrossoverStrategy createCrossoverStrategy() {
        long seed = getRandom().nextLong();
        return new WakeBasedCrossoverStrategy(getCrossoverRate(), seed);
    }

    @Override
    protected MutationStrategy createMutationStrategy() {
        long seed = getRandom().nextLong();
        return new WakeBasedMutationStrategy(getMutationRate(), wakeAnalysisPercentage,
		mutationSelectionPercentage, seed); 
    }

    // Getters for testing and validation
    public double getWakeAnalysisPercentage() {
        return wakeAnalysisPercentage;
    }

    public double getMutationSelectionPercentage() {
        return mutationSelectionPercentage;
    }
}

