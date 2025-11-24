package org.zafer.wflopalgorithms.common.ga.strategy;

import org.zafer.wflopalgorithms.common.ga.solution.Individual;
import org.zafer.wflopmodel.problem.WFLOP;

import java.util.*;

/**
 * Single-point crossover strategy.
 * Creates an offspring by combining genes from both parents at a random crossover point.
 * Ensures unique turbine placements by using a set.
 */
public class SinglePointCrossover implements CrossoverStrategy {

    private final Random random;
    private final double crossoverRate;

    public SinglePointCrossover(double crossoverRate) {
        this.crossoverRate = crossoverRate;
        this.random = new Random();
    }

    public SinglePointCrossover(double crossoverRate, long seed) {
        this.crossoverRate = crossoverRate;
        this.random = new Random(seed);
    }

    @Override
    public Individual crossover(Individual parent1, Individual parent2, WFLOP problem) {
        // Decide whether to perform crossover
        if (random.nextDouble() > crossoverRate) {
            // No crossover, return copy of parent1
            return new Individual(parent1.getGenes());
        }

        List<Integer> genes1 = parent1.getGenes();
        List<Integer> genes2 = parent2.getGenes();
        
        // Perform single-point crossover
        int crossoverPoint = random.nextInt(genes1.size());
        Set<Integer> childGenes = new LinkedHashSet<>();
        
        // Take genes from first parent up to crossover point
        for (int i = 0; i < crossoverPoint; i++) {
            childGenes.add(genes1.get(i));
        }
        
        // Fill remaining positions with genes from second parent
        for (int gene : genes2) {
            if (childGenes.size() >= genes1.size()) break;
            childGenes.add(gene);
        }
        
        // If still not enough genes, take remaining from first parent
        for (int gene : genes1) {
            if (childGenes.size() >= genes1.size()) break;
            childGenes.add(gene);
        }
        
        // If still not enough (edge case), fill with random valid positions
        while (childGenes.size() < genes1.size()) {
            childGenes.add(random.nextInt(problem.getCellCount()));
        }
        
        return new Individual(new ArrayList<>(childGenes));
    }
}
