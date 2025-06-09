package org.zafer.wflopga;

import org.zafer.wflopmetaheuristic.Solution;
import org.zafer.wflopmodel.layout.TurbineLayout;

import java.util.List;

public class Individual implements Solution {

    private final TurbineLayout turbineLayout;
    private double fitness;

    public Individual(TurbineLayout turbineLayout, double fitness) {
        this.turbineLayout = turbineLayout;
        this.fitness = fitness;
    }

    public Individual(TurbineLayout turbineLayout) {
        this.turbineLayout = turbineLayout;
    }

    public Individual(List<Integer> genes) {
        turbineLayout = new TurbineLayout(genes);
    }

    public TurbineLayout getSolution() {
        return turbineLayout;
    }

    public List<Integer> getTurbineIndices() {
        return turbineLayout.getTurbineIndices();
    }

    @Override
    public double getFitness() {
        return fitness;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }
}
