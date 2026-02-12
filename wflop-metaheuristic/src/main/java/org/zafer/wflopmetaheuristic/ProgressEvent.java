package org.zafer.wflopmetaheuristic;

import org.zafer.wflopmetaheuristic.termination.TerminationProgress;

public class ProgressEvent {

    private final int iteration;
    private final double bestFitness;
    private final double averageFitness;
    private final double totalPowerWithoutWake;

    private final TerminationProgress terminationProgress;

    public ProgressEvent(
        int iteration,
        double bestFitness,
        double averageFitness,
        double totalPowerWithoutWake,
        TerminationProgress terminationProgress
    ) {
        this.iteration = iteration;
        this.bestFitness = bestFitness;
        this.averageFitness = averageFitness;
        this.totalPowerWithoutWake = totalPowerWithoutWake;
        this.terminationProgress = terminationProgress;
    }

    public TerminationProgress getTerminationProgress() {
        return terminationProgress;
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

    public double getTotalPowerWithoutWake() {
        return totalPowerWithoutWake;
    }
}