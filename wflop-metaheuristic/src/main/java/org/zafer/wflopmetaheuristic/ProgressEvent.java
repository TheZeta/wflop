package org.zafer.wflopmetaheuristic;

public class ProgressEvent {

    private final int iteration;
    private final int generations;
    private final double bestFitness;
    private final double averageFitness;

    public ProgressEvent(int iteration, int generations, double bestFitness, double averageFitness) {
        this.iteration = iteration;
        this.generations = generations;
        this.bestFitness = bestFitness;
        this.averageFitness = averageFitness;
    }

    public int getIteration() {
        return iteration;
    }

    public int getGenerations() {
        return generations;
    }

    public double getBestFitness() {
        return bestFitness;
    }

    public double getAverageFitness() {
        return averageFitness;
    }
}


