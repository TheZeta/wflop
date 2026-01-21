package org.zafer.wflopalgorithms.algorithms.fode.solution;

import org.zafer.wflopmetaheuristic.Solution;

public class FODEIndividual implements Solution {

    private double[] vector;
    private double fitness;
    private double F;
    private double CR;

    public FODEIndividual(double[] vector) {
        this.vector = vector;
    }

    public double[] getVector() { return vector; }
    public void setVector(double[] vector) { this.vector = vector; }

    public double getFitness() { return fitness; }
    public void setFitness(double fitness) { this.fitness = fitness; }

    public double getF() { return F; }
    public void setF(double f) { F = f; }

    public double getCR() { return CR; }
    public void setCR(double cr) { CR = cr; }
}
