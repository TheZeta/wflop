package org.zafer.wflopalgorithms.algorithms.sa;

import java.util.List;
import org.zafer.wflopmetaheuristic.Solution;

public class AnnealingState implements Solution {

    private final List<Integer> layout;
    private double fitness;

    public AnnealingState(List<Integer> layout) {
        this.layout = layout;
    }

    public List<Integer> getLayout() {
        return layout;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    @Override
    public double getFitness() {
        return fitness;
    }
}
