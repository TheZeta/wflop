package org.zafer.wflopalgorithms.common.ga.solution;

import org.zafer.wflopmetaheuristic.Solution;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an individual in the genetic algorithm population.
 * Stores a solution as a list of genes (integer indices) and its fitness value.
 */
public class Individual implements Solution {

    private final List<Integer> genes;
    private double fitness;

    public Individual(List<Integer> genes) {
        this.genes = new ArrayList<>(genes);
        this.fitness = 0.0;
    }

    public Individual(List<Integer> genes, double fitness) {
        this.genes = new ArrayList<>(genes);
        this.fitness = fitness;
    }

    public List<Integer> getGenes() {
        return new ArrayList<>(genes);
    }

    @Override
    public double getFitness() {
        return fitness;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    /**
     * Creates a copy of this individual with new genes.
     */
    public Individual withGenes(List<Integer> newGenes) {
        return new Individual(newGenes, 0.0);
    }
}
