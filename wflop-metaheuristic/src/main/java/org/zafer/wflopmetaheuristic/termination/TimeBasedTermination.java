package org.zafer.wflopmetaheuristic.termination;

public class TimeBasedTermination implements TerminationCondition {

    private final long maxMillis;
    private long startTime;

    public TimeBasedTermination(long maxMillis) {
        this.maxMillis = maxMillis;
    }

    @Override
    public void onStart() {
        this.startTime = System.currentTimeMillis();
    }

    @Override
    public void onGeneration(int generation) {
        // No-op
    }

    @Override
    public boolean shouldTerminate() {
        return System.currentTimeMillis() - startTime >= maxMillis;
    }

    @Override
    public TerminationProgress getProgress() {
        long elapsed = System.currentTimeMillis() - startTime;
        double progress = Math.min(1.0, (double) elapsed / maxMillis);

        return new TerminationProgress(
                progress,
                "Time",
                elapsed,
                maxMillis
        );
    }
}
