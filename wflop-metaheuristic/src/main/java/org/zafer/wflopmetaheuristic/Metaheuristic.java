package org.zafer.wflopmetaheuristic;

public interface Metaheuristic {

    Solution run();

    default Solution runWithListeners(java.util.List<ProgressListener> listeners) {
        return run();
    }
}
