package org.zafer.wflopmetaheuristic;

public class RunResult {

    private final Solution bestSolution;
    private final RunMetrics metrics;

    public RunResult(Solution bestSolution, RunMetrics metrics) {
        this.bestSolution = bestSolution;
        this.metrics = metrics;
    }

    public Solution getBestSolution() {
        return bestSolution;
    }

    public RunMetrics getMetrics() {
        return metrics;
    }
}


