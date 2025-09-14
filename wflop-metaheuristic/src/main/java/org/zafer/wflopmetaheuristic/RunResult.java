package org.zafer.wflopmetaheuristic;

public class RunResult<S extends Solution> {

    private final S bestSolution;
    private final RunMetrics metrics;

    public RunResult(S bestSolution, RunMetrics metrics) {
        this.bestSolution = bestSolution;
        this.metrics = metrics;
    }

    public S getBestSolution() {
        return bestSolution;
    }

    public RunMetrics getMetrics() {
        return metrics;
    }
}


