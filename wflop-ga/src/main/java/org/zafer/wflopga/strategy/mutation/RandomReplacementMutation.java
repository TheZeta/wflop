package org.zafer.wflopga.strategy.mutation;

import org.zafer.wflopga.Individual;
import org.zafer.wflopmodel.problem.WFLOP;

import java.util.*;

public class RandomReplacementMutation implements MutationStrategy {
    private final double mutationRate;
    private final Random random = new Random();

    public RandomReplacementMutation(double mutationRate) {
        this.mutationRate = mutationRate;
    }

    @Override
    public Individual mutate(Individual individual, WFLOP problem) {
        List<Integer> genes = new ArrayList<>(individual.getSolution().getTurbineIndices());
        int layoutSize = problem.getCellCount();

        if (random.nextDouble() < mutationRate) {
            int i = random.nextInt(genes.size());
            int newIdx;
            do {
                newIdx = random.nextInt(layoutSize);
            } while (genes.contains(newIdx));
            genes.set(i, newIdx);
        }

        return new Individual(genes);
    }
}
