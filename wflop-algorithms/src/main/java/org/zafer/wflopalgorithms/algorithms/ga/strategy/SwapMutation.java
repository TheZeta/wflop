package org.zafer.wflopalgorithms.algorithms.ga.strategy;

import org.zafer.wflopalgorithms.algorithms.ga.Individual;
import org.zafer.wflopmodel.problem.WFLOP;

import java.util.List;
import java.util.Random;

/**
 * Swap mutation strategy.
 * Randomly swaps two genes in the individual with a given probability.
 */
public class SwapMutation implements MutationStrategy {

    private final Random random;
    private final double mutationRate;

    public SwapMutation(double mutationRate) {
        this.mutationRate = mutationRate;
        this.random = new Random();
    }

    public SwapMutation(double mutationRate, long seed) {
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
        
        if (genes.size() < 2) {
            return individual;
        }

        // Swap two random positions
        int pos1 = random.nextInt(genes.size());
        int pos2 = random.nextInt(genes.size());
        
        while (pos2 == pos1) {
            pos2 = random.nextInt(genes.size());
        }

        Integer temp = genes.get(pos1);
        genes.set(pos1, genes.get(pos2));
        genes.set(pos2, temp);

        return new Individual(genes);
    }
}
