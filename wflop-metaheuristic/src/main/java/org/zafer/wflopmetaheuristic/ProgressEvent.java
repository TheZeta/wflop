package org.zafer.wflopmetaheuristic;

public class ProgressEvent {

    private final int iteration;
    private final double bestFitness;
    private final double averageFitness;

    public ProgressEvent(int iteration, double bestFitness, double averageFitness) {
        this.iteration = iteration;
        this.bestFitness = bestFitness;
        this.averageFitness = averageFitness;
    }

    public int getIteration() {
        return iteration;
    }

    public double getBestFitness() {
        return bestFitness;
    }

    public double getAverageFitness() {
        return averageFitness;
    }
}


