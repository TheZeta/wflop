package org.zafer.wflopmetaheuristic;

public class RunMetrics {

    private final long startEpochMs;
    private final long endEpochMs;
    private final int iterations;
    private final double bestFitness;

    public RunMetrics(long startEpochMs, long endEpochMs, int iterations, double bestFitness) {
        this.startEpochMs = startEpochMs;
        this.endEpochMs = endEpochMs;
        this.iterations = iterations;
        this.bestFitness = bestFitness;
    }

    public long getStartEpochMs() {
        return startEpochMs;
    }

    public long getEndEpochMs() {
        return endEpochMs;
    }

    public long getDurationMs() {
        return endEpochMs - startEpochMs;
    }

    public int getIterations() {
        return iterations;
    }

    public double getBestFitness() {
        return bestFitness;
    }
}


