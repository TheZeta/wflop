package org.zafer.wflopalgorithms.algorithms.pso;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.zafer.wflopmetaheuristic.Metaheuristic;
import org.zafer.wflopmetaheuristic.Solution;

/**
 * Particle Swarm Optimization implementation that can be loaded from JSON.
 * This is a sample implementation demonstrating the factory pattern.
 */
public class PSO implements Metaheuristic {

    private final String algorithmType;
    private final int swarmSize;
    private final int maxIterations;
    private final double inertiaWeight;
    private final double cognitiveComponent;
    private final double socialComponent;

    @JsonCreator
    public PSO(
            @JsonProperty("algorithmType") String algorithmType,
            @JsonProperty("swarmSize") int swarmSize,
            @JsonProperty("maxIterations") int maxIterations,
            @JsonProperty("inertiaWeight") double inertiaWeight,
            @JsonProperty("cognitiveComponent") double cognitiveComponent,
            @JsonProperty("socialComponent") double socialComponent
    ) {
        this.algorithmType = algorithmType;
        this.swarmSize = swarmSize;
        this.maxIterations = maxIterations;
        this.inertiaWeight = inertiaWeight;
        this.cognitiveComponent = cognitiveComponent;
        this.socialComponent = socialComponent;
    }

    @Override
    public Solution run() {
        // Sample implementation - returns a mock solution
        return new PSOSolution(swarmSize * maxIterations * inertiaWeight);
    }

    // Getters for testing and validation
    public String getAlgorithmType() {
        return algorithmType;
    }

    public int getSwarmSize() {
        return swarmSize;
    }

    public int getMaxIterations() {
        return maxIterations;
    }

    public double getInertiaWeight() {
        return inertiaWeight;
    }

    public double getCognitiveComponent() {
        return cognitiveComponent;
    }

    public double getSocialComponent() {
        return socialComponent;
    }

    /**
     * Simple Solution implementation for PSO.
     */
    private static class PSOSolution implements Solution {
        private final double fitness;

        public PSOSolution(double fitness) {
            this.fitness = fitness;
        }

        @Override
        public double getFitness() {
            return fitness;
        }
    }
}

