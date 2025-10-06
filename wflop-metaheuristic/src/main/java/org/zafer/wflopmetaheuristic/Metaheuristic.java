package org.zafer.wflopmetaheuristic;

import org.zafer.wflopmodel.problem.WFLOP;

public interface Metaheuristic {

    /**
     * Runs the algorithm on the given problem.
     * 
     * @param problem The WFLOP problem instance
     * @return The solution found by the algorithm
     */
    Solution run(WFLOP problem);

    /**
     * Runs the algorithm on the given problem with progress listeners.
     * 
     * @param problem The WFLOP problem instance
     * @param listeners Progress listeners to monitor algorithm execution
     * @return The solution found by the algorithm
     */
    default Solution runWithListeners(WFLOP problem, java.util.List<ProgressListener> listeners) {
        return run(problem);
    }
}
