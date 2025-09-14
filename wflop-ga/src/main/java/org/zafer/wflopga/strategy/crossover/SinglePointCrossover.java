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
            int cut = random.nextInt(numberOfTurbines);
            Set<Integer> childGenes = new LinkedHashSet<>(t1.subList(0, cut));
            for (Integer idx : t2) {
                if (childGenes.size() >= numberOfTurbines) break;
                childGenes.add(idx);
            }
            return new Individual(new ArrayList<>(childGenes));
        } else {
            return new Individual(new ArrayList<>(t1));
        }


    }
}
