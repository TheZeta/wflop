package org.zafer.wflopalgorithms.algorithms.ga.strategy;

import org.zafer.wflopalgorithms.algorithms.ga.Individual;
import org.zafer.wflopmodel.problem.WFLOP;

/**
 * Strategy interface for performing mutation operations on individuals.
 */
@FunctionalInterface
public interface MutationStrategy {
    
    /**
     * Performs mutation on an individual based on the strategy's logic.
     * 
     * @param individual The individual to mutate
     * @param problem The WFLOP problem instance
     * @return The mutated individual
     */
    Individual mutate(Individual individual, WFLOP problem);
}
