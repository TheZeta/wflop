package org.zafer.wflopalgorithms.algorithms.ga.strategy;

import org.zafer.wflopalgorithms.algorithms.ga.Individual;

import java.util.List;

/**
 * Strategy interface for selecting individuals from a population.
 */
@FunctionalInterface
public interface SelectionStrategy {
    
    /**
     * Selects an individual from the population based on the strategy's logic.
     * 
     * @param population The population to select from
     * @return The selected individual
     */
    Individual select(List<Individual> population);
}

