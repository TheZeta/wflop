package org.zafer.wflopmetaheuristic;

public interface Metaheuristic<S extends Solution> {

    S run();

    default S runWithListeners(java.util.List<ProgressListener> listeners) {
        return run();
    }
}
