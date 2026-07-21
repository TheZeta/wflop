package org.zafer.wflopalgorithms.algorithms.fode.solution;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;

import org.zafer.wflopalgorithms.common.Helper;
import org.zafer.wflopmetaheuristic.Solution;

public class FODEIndividual implements Solution {

    private double[] vector;
    private double fitness;
    private double F;
    private double CR;

    private final int cellCount;

    public FODEIndividual(double[] vector, int cellCount) {
        this.vector = vector;
        this.cellCount = cellCount;
    }

    @Override
    public double getFitness() { return fitness; }

    @Override
    public List<Integer> getList() {
        return Arrays.stream(Helper.discretize(vector, cellCount))
            .boxed()
            .toList();
    }

    public double[] getVector() { return vector; }
    public void setVector(double[] vector) { this.vector = vector; }

    public void setFitness(double fitness) {
        this.fitness = BigDecimal.valueOf(fitness)
            .setScale(4, RoundingMode.HALF_UP)
            .doubleValue();
    }

    public double getF() { return F; }
    public void setF(double f) { F = f; }

    public double getCR() { return CR; }
    public void setCR(double cr) { CR = cr; }
}
