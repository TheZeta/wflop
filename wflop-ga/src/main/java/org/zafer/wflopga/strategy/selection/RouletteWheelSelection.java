package org.zafer.wflopga.strategy.selection;

import org.zafer.wflopga.Individual;

import java.util.List;
import java.util.Random;

public class RouletteWheelSelection implements SelectionStrategy {

    private final Random random = new Random();

    @Override
    public Individual select(List<Individual> population) {
        double totalFitness = population.stream().mapToDouble(Individual::getFitness).sum();
        double r = random.nextDouble() * totalFitness;
        double cumulative = 0;

        for (Individual ind : population) {
            cumulative += ind.getFitness();
            if (cumulative >= r) {
                return ind;
            }
        }

        return population.get(population.size() - 1);  // fallback
    }
}
