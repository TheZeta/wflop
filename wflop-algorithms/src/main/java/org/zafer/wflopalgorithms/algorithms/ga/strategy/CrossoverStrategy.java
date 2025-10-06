package org.zafer.wflopalgorithms.algorithms.ga.strategy;

import org.zafer.wflopalgorithms.algorithms.ga.Individual;
import org.zafer.wflopmodel.problem.WFLOP;

/**
 * Strategy interface for performing crossover operations between two individuals.
 */
@FunctionalInterface
public interface CrossoverStrategy {
    
    /**
     * Performs crossover between two parent individuals to create an offspring.
     * 
     * @param parent1 The first parent
     * @param parent2 The second parent
     * @param problem The WFLOP problem instance
     * @return The offspring individual
     */
    Individual crossover(Individual parent1, Individual parent2, WFLOP problem);
}
