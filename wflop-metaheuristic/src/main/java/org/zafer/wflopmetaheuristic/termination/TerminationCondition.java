package org.zafer.wflopmetaheuristic.termination;

public interface TerminationCondition {

    void onStart();
    void onGeneration();
    boolean shouldTerminate();

    TerminationProgress getTerminationProgress();
    default double getProgress() {
        return getTerminationProgress().getProgress();
    }
}
