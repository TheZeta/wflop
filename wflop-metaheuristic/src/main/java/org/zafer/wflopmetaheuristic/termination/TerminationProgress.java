package org.zafer.wflopmetaheuristic.termination;

public class TerminationProgress {

    // Value in [0,1], or -1 if unknown
    private final double progress;

    // A human-friendly label (e.g., “Generations”, “Elapsed Time”)
    private final String label;

    // Best-effort numeric values the listener *may* use
    private final long current;
    private final long max; // -1 if infinite/unknown

    public TerminationProgress(double progress, String label, long current, long max) {
        this.progress = progress;
        this.label = label;
        this.current = current;
        this.max = max;
    }

    public double getProgress() { return progress; }
    public String getLabel() { return label; }
    public long getCurrent() { return current; }
    public long getMax() { return max; }
}