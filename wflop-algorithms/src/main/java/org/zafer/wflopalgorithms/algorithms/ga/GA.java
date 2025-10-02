package org.zafer.wflopalgorithms.algorithms.ga;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.zafer.wflopmetaheuristic.Metaheuristic;
import org.zafer.wflopmetaheuristic.Solution;

/**
 * Genetic Algorithm implementation that can be loaded from JSON.
 * This is a sample implementation demonstrating the factory pattern.
 */
public class GA implements Metaheuristic {

    private final String algorithmType;
    private final int populationSize;
    private final int generations;
    private final double crossoverRate;
    private final double mutationRate;
    private final String selectionStrategy;

    @JsonCreator
    public GA(
            @JsonProperty("algorithmType") String algorithmType,
            @JsonProperty("populationSize") int populationSize,
            @JsonProperty("generations") int generations,
            @JsonProperty("crossoverRate") double crossoverRate,
            @JsonProperty("mutationRate") double mutationRate,
            @JsonProperty("selectionStrategy") String selectionStrategy
    ) {
        this.algorithmType = algorithmType;
        this.populationSize = populationSize;
        this.generations = generations;
        this.crossoverRate = crossoverRate;
        this.mutationRate = mutationRate;
        this.selectionStrategy = selectionStrategy;
    }

    @Override
    public Solution run() {
        // Sample implementation - returns a mock solution
        return new GASolution(populationSize * generations * crossoverRate);
    }

    // Getters for testing and validation
    public String getAlgorithmType() {
        return algorithmType;
    }

    public int getPopulationSize() {
        return populationSize;
    }

    public int getGenerations() {
        return generations;
    }

    public double getCrossoverRate() {
        return crossoverRate;
    }

    public double getMutationRate() {
        return mutationRate;
    }

    public String getSelectionStrategy() {
        return selectionStrategy;
    }

    /**
     * Simple Solution implementation for GA.
     */
    private static class GASolution implements Solution {
        private final double fitness;

        public GASolution(double fitness) {
            this.fitness = fitness;
        }

        @Override
        public double getFitness() {
            return fitness;
        }
    }
}

