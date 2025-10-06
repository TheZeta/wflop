package org.zafer.wflopalgorithms.algorithms.pso;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.zafer.wflopmetaheuristic.Metaheuristic;
import org.zafer.wflopmetaheuristic.Solution;
import org.zafer.wflopmodel.problem.WFLOP;

/**
 * Particle Swarm Optimization implementation for WFLOP that can be loaded from JSON.
 * This is a stateless, reusable algorithm instance.
 * 
 * Usage:
 *   Metaheuristic pso = AlgorithmFactory.loadFromJson("path/to/pso_config.json");
 *   Solution solution = pso.run(wflopInstance);
 */
public class PSO implements Metaheuristic {

    private final String algorithm;
    private final int swarmSize;
    private final int maxIterations;
    private final double inertiaWeight;
    private final double cognitiveComponent;
    private final double socialComponent;

    @JsonCreator
    public PSO(
            @JsonProperty("algorithm") String algorithm,
            @JsonProperty("swarmSize") int swarmSize,
            @JsonProperty("maxIterations") int maxIterations,
            @JsonProperty("inertiaWeight") double inertiaWeight,
            @JsonProperty("cognitiveComponent") double cognitiveComponent,
            @JsonProperty("socialComponent") double socialComponent
    ) {
        this.algorithm = algorithm;
        this.swarmSize = swarmSize;
        this.maxIterations = maxIterations;
        this.inertiaWeight = inertiaWeight;
        this.cognitiveComponent = cognitiveComponent;
        this.socialComponent = socialComponent;
    }

    @Override
    public Solution run(WFLOP problem) {
        // TODO: Implement actual PSO algorithm
        // For now, returns a placeholder solution
        return new PSOSolution(swarmSize * maxIterations * inertiaWeight);
    }

    // Getters for testing and validation
    public String getAlgorithm() {
        return algorithm;
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
