package org.zafer.wflopmetaheuristic;

import org.zafer.wflopmetaheuristic.termination.TerminationProgress;

public class ProgressEvent {

    private final double bestFitness;
    private final double totalPowerWithoutWake;

    private final TerminationProgress terminationProgress;

    public ProgressEvent(
        double bestFitness,
        double totalPowerWithoutWake,
        TerminationProgress terminationProgress
    ) {
        this.bestFitness = bestFitness;
        this.totalPowerWithoutWake = totalPowerWithoutWake;
        this.terminationProgress = terminationProgress;
    }

    public TerminationProgress getTerminationProgress() {
        return terminationProgress;
    }

    public double getBestFitness() {
        return bestFitness;
    }

    public double getTotalPowerWithoutWake() {
        return totalPowerWithoutWake;
    }
}