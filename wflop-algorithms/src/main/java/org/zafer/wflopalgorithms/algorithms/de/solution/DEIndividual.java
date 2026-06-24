package org.zafer.wflopalgorithms.algorithms.de.solution;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.zafer.wflopmetaheuristic.Solution;

public class DEIndividual implements Solution {

    private double[] vector;   // continuous
    private double fitness;

    public DEIndividual(double[] vector) {
        this.vector = vector;
    }

    public double[] getVector() {
        return vector;
    }

    public void setVector(double[] vector) {
        this.vector = vector;
    }

    public double getFitness() {
        return fitness;
    }

    public void setFitness(double fitness) {
        this.fitness = BigDecimal.valueOf(fitness)
            .setScale(4, RoundingMode.HALF_UP)
            .doubleValue();
    }
}
