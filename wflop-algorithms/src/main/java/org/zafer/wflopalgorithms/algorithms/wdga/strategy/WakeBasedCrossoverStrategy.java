package org.zafer.wflopalgorithms.algorithms.wdga.strategy;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.zafer.wflopalgorithms.common.ga.solution.Individual;
import org.zafer.wflopalgorithms.common.ga.strategy.CrossoverStrategy;
import org.zafer.wflopmodel.problem.WFLOP;

/**
 * Zone-based crossover strategy for WDGA.
 * Divides solutions by zones (e.g., left/right halves) and combines them.
 * If offspring has incorrect number of turbines, applies repair operations.
 */
public class WakeBasedCrossoverStrategy implements CrossoverStrategy {

    private final Random random;
    private final double crossoverRate;

    public WakeBasedCrossoverStrategy(double crossoverRate) {
        this.crossoverRate = crossoverRate;
        this.random = new Random();
    }

    public WakeBasedCrossoverStrategy(double crossoverRate, long seed) {
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
        
        // Determine crossover zone (left/right split)
        int dimension = problem.getDimension();
        int crossoverZone = dimension / 2; // Split in the middle
        
        Set<Integer> childGenes = new LinkedHashSet<>();
        
        // Take left zone from parent1 and right zone from parent2
        for (int gene : genes1) {
            int x = gene % dimension;
            if (x < crossoverZone) {
                childGenes.add(gene);
            }
        }
        
        for (int gene : genes2) {
            int x = gene % dimension;
            if (x >= crossoverZone) {
                childGenes.add(gene);
            }
        }
        
        // Repair if necessary
        int targetTurbines = problem.getNumberOfTurbines();
        if (childGenes.size() < targetTurbines) {
            // Add turbines to reach target count
            addRandomTurbines(childGenes, targetTurbines, problem);
        } else if (childGenes.size() > targetTurbines) {
            // Remove excess turbines randomly
            removeRandomTurbines(childGenes, targetTurbines);
        }
        
        return new Individual(new ArrayList<>(childGenes));
    }

    private void addRandomTurbines(Set<Integer> genes, int targetCount, WFLOP problem) {
        int layoutSize = problem.getCellCount();
        while (genes.size() < targetCount) {
            int randomGene = random.nextInt(layoutSize);
            genes.add(randomGene);
        }
    }

    private void removeRandomTurbines(Set<Integer> genes, int targetCount) {
        List<Integer> geneList = new ArrayList<>(genes);
        while (genes.size() > targetCount) {
            int randomIndex = random.nextInt(geneList.size());
            genes.remove(geneList.get(randomIndex));
            geneList.remove(randomIndex);
        }
    }
}
