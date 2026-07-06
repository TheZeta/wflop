package org.zafer.wflopmetaheuristic.termination;

public class  GenerationBasedTermination implements TerminationCondition {

    private final int maxGenerations;
    private int currentGeneration = 0;

    public GenerationBasedTermination(int maxGenerations) {
        this.maxGenerations = maxGenerations;
    }

    @Override
    public void onStart() {
        // No-op
    }

    @Override
    public void onGeneration() {
        this.currentGeneration++;
    }

    @Override
    public boolean shouldTerminate() {
        return currentGeneration >= maxGenerations;
    }

    @Override
    public TerminationProgress getTerminationProgress() {
        return new TerminationProgress(
                (double) currentGeneration / maxGenerations,
                "Generations",
                currentGeneration,
                maxGenerations
        );
    }

    @Override
    public boolean equals(Object obj) {
        GenerationBasedTermination getter = (GenerationBasedTermination)obj;
        return this.maxGenerations == getter.maxGenerations;
    }
}