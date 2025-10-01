package org.zafer.wflopga.strategy.crossover;

import org.zafer.wflopga.Individual;
import org.zafer.wflopmodel.problem.WFLOP;

import java.util.*;

public class SinglePointCrossover implements CrossoverStrategy {

    private final Random random = new Random();
    private final double crossoverRate;

    public SinglePointCrossover(double crossoverRate) {
        this.crossoverRate = crossoverRate;
    }

    @Override
    public Individual crossover(Individual p1, Individual p2, WFLOP problem) {
        List<Integer> t1 = p1.getTurbineIndices();
        List<Integer> t2 = p2.getTurbineIndices();
        int numberOfTurbines = problem.getNumberOfTurbines();

        if (random.nextDouble() < crossoverRate) {
            // Ensure we have a valid cut point that allows genes from both parents
            int cut = random.nextInt(numberOfTurbines - 1) + 1; // Cut between 1 and numberOfTurbines-1
            
            Set<Integer> childGenes = new LinkedHashSet<>();
            
            // Add genes from parent1 up to cut point
            for (int i = 0; i < cut && childGenes.size() < numberOfTurbines; i++) {
                childGenes.add(t1.get(i));
            }
            
            // Add remaining genes from parent2
            for (Integer idx : t2) {
                if (childGenes.size() >= numberOfTurbines) break;
                childGenes.add(idx);
            }
            
            // If we still don't have enough genes, fill with remaining genes from parent1
            for (int i = cut; i < t1.size() && childGenes.size() < numberOfTurbines; i++) {
                childGenes.add(t1.get(i));
            }
            
            return new Individual(new ArrayList<>(childGenes));
        } else {
            return new Individual(new ArrayList<>(t1));
        }
    }
}
