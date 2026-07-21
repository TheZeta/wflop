package org.zafer.wflopalgorithms.algorithms.de.solution;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;

import org.zafer.wflopalgorithms.common.Helper;
import org.zafer.wflopmetaheuristic.Solution;

public class DEIndividual implements Solution {

    private double[] vector;   // continuous
    private double fitness;

    private final int cellCount;

    public DEIndividual(double[] vector, int cellCount) {
        this.vector = vector;
        this.cellCount = cellCount;
    }

    @Override
    public double getFitness() {
        return fitness;
    }

    @Override
    public List<Integer> getList() {
        return Arrays.stream(Helper.discretize(vector, cellCount))
            .boxed()
            .toList();
    }

    public double[] getVector() {
        return vector;
    }

    public void setVector(double[] vector) {
        this.vector = vector;
    }

    public void setFitness(double fitness) {
        this.fitness = BigDecimal.valueOf(fitness)
            .setScale(4, RoundingMode.HALF_UP)
            .doubleValue();
    }
}
