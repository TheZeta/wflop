package org.zafer.wflopmetaheuristic;

import java.util.ArrayList;
import java.util.List;

public class MetaheuristicRunner {

    private final Metaheuristic algorithm;
    private final List<ProgressListener> listeners = new ArrayList<>();

    public MetaheuristicRunner(Metaheuristic algorithm) {
        this.algorithm = algorithm;
    }

    public void addListener(ProgressListener listener) {
        listeners.add(listener);
    }

    public RunResult run() {
        long start = System.currentTimeMillis();
        final int[] iterationCounter = new int[] { 0 };
        ProgressListener countingListener = evt -> iterationCounter[0] = evt.getIteration();

        List<ProgressListener> effective = new ArrayList<>(listeners.size() + 1);
        effective.add(countingListener);
        effective.addAll(listeners);

        Solution best = algorithm.runWithListeners(effective);
        long end = System.currentTimeMillis();
        RunMetrics metrics = new RunMetrics(start, end, iterationCounter[0], best.getFitness());
        return new RunResult(best, metrics);
    }
}


