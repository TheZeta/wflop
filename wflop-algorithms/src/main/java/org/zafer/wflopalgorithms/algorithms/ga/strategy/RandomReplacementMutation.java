package org.zafer.wflopalgorithms.algorithms.ga.strategy;

import org.zafer.wflopalgorithms.algorithms.ga.Individual;
import org.zafer.wflopmodel.problem.WFLOP;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Random replacement mutation strategy.
 * Replaces a random gene with a new random value that doesn't already exist in the individual.
 */
public class RandomReplacementMutation implements MutationStrategy {

    private final Random random;
    private final double mutationRate;

    public RandomReplacementMutation(double mutationRate) {
        this.mutationRate = mutationRate;
        this.random = new Random();
    }

    public RandomReplacementMutation(double mutationRate, long seed) {
        this.mutationRate = mutationRate;
        this.random = new Random(seed);
    }

    @Override
    public Individual mutate(Individual individual, WFLOP problem) {
        if (random.nextDouble() > mutationRate) {
            // No mutation
            return individual;
        }

        List<Integer> genes = individual.getGenes();
        Set<Integer> existingGenes = new HashSet<>(genes);
        
        // Select a random position to mutate
        int mutationPos = random.nextInt(genes.size());
        
        // Find a new gene value that doesn't exist
        int layoutSize = problem.getCellCount();
        int newGene;
        int attempts = 0;
        do {
            newGene = random.nextInt(layoutSize);
            attempts++;
        } while (existingGenes.contains(newGene) && attempts < layoutSize * 2);
        
        // Replace the gene
        genes.set(mutationPos, newGene);

        return new Individual(genes);
    }
}
