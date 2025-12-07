package org.zafer.wflopmetaheuristic.termination;

public class GenerationBasedTermination implements TerminationCondition {

    private final int maxGenerations;
    private int currentGeneration;

    public GenerationBasedTermination(int maxGenerations) {
        this.maxGenerations = maxGenerations;
    }

    @Override
    public void onStart() {
        // No-op
    }

    @Override
    public void onGeneration(int generation) {
        this.currentGeneration = generation;
    }

    @Override
    public boolean shouldTerminate() {
        return currentGeneration >= maxGenerations;
    }

    @Override
    public TerminationProgress getProgress() {
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