package org.zafer.wflopmetaheuristic.termination;

public interface TerminationCondition {

    void onStart();
    void onGeneration(int generation);
    boolean shouldTerminate();

    TerminationProgress getProgress();
}
