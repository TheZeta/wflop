package org.zafer.wflopalgorithms.algorithms.novelga;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.zafer.wflopalgorithms.algorithms.novelga.strategy.WakeBasedCrossoverStrategy;
import org.zafer.wflopalgorithms.algorithms.novelga.strategy.WakeBasedMutationStrategy;
import org.zafer.wflopalgorithms.common.ga.GA;
import org.zafer.wflopalgorithms.common.ga.strategy.CrossoverStrategy;
import org.zafer.wflopalgorithms.common.ga.strategy.MutationStrategy;
import org.zafer.wflopcore.power.PowerCalculator;
import org.zafer.wflopmetaheuristic.termination.TerminationConditionConfig;

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
            @JsonProperty("mutationSelectionPercentage") Double mutationSelectionPercentage,
            @JsonProperty("termination") TerminationConditionConfig terminationConfig
    ) {
        super(
            algorithm,
            populationSize,
            crossoverRate,
            mutationRate,
            selectionStrategy,
            "wakeBasedCrossover",
            "wakeBasedMutation",
            terminationConfig
        );
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
        return new WakeBasedCrossoverStrategy(seed);
    }

    @Override
    protected MutationStrategy createMutationStrategy(
        PowerCalculator powerCalculator
    ) {

        long seed = getRandom().nextLong();
        return new WakeBasedMutationStrategy(
            wakeAnalysisPercentage,
            mutationSelectionPercentage,
            seed,
                powerCalculator);
    }

    // Getters for testing and validation
    public double getWakeAnalysisPercentage() {
        return wakeAnalysisPercentage;
    }

    public double getMutationSelectionPercentage() {
        return mutationSelectionPercentage;
    }
}

