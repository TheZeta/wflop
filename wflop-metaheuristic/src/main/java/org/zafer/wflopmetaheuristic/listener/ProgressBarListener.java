package org.zafer.wflopmetaheuristic.listener;

import org.zafer.wflopmetaheuristic.ProgressEvent;
import org.zafer.wflopmetaheuristic.termination.TerminationProgress;

public class ProgressBarListener implements ProgressListener {

    private static final char PROGRESS_CHAR = '█';
    private static final char REMAINING_CHAR = '░';
    private static final int BAR_LENGTH = 50;

    @Override
    public void onIteration(ProgressEvent event) {

        TerminationProgress tp = event.getTerminationProgress();
        double progress = tp.getProgress(); // [0..1] or -1 for unknown
        long current = tp.getCurrent();
        long max = tp.getMax(); // -1 for unknown
        String label = tp.getLabel(); // "Generations" or "Time" etc.

        // ============
        // UNKNOWN / INDETERMINATE PROGRESS
        // ============
        if (progress < 0 || max <= 0) {
            // Show spinning-style / simple iteration print
            System.out.print("\r" + label + ": " + current);
            System.out.flush();
            return;
        }

        // Clamp values just in case
        long clampedCurrent = Math.min(current, max);
        double clampedProgress = Math.min(1.0, Math.max(0.0, progress));

        int percent = (int) (clampedProgress * 100);

        int progressChars = (int) (clampedProgress * BAR_LENGTH);
        int remainingChars = BAR_LENGTH - progressChars;

        StringBuilder bar = new StringBuilder(label + ": [");

        for (int i = 0; i < progressChars; i++) {
            bar.append(PROGRESS_CHAR);
        }

        for (int i = 0; i < remainingChars; i++) {
            bar.append(REMAINING_CHAR);
        }

        bar.append("] ")
                .append(String.format("%3d", percent))
                .append("% (")
                .append(clampedCurrent).append("/")
                .append(max)
                .append(")");

        System.out.print("\r" + bar);
        System.out.flush();

        if (clampedCurrent >= max) {
            System.out.println();
        }
    }
}