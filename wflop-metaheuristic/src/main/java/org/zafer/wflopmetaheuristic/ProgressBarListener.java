package org.zafer.wflopmetaheuristic;

public class ProgressBarListener implements ProgressListener {

    private static final char PROGRESS_CHAR = '█';
    private static final char REMAINING_CHAR = '░';
    private static final int BAR_LENGTH = 50;

    @Override
    public void onIteration(ProgressEvent event) {
        int current = event.getIteration();
        int generations = event.getGenerations();

        int clampedCurrent = Math.min(current, generations);

        double progress = (double) clampedCurrent / generations;
        int percent = (int) (progress * 100);

        int progressChars = (int) (progress * BAR_LENGTH);
        int remainingChars = BAR_LENGTH - progressChars;

        StringBuilder bar = new StringBuilder("Iteration: [");

        for (int i = 0; i < progressChars; i++) {
            bar.append(PROGRESS_CHAR);
        }

        for (int i = 0; i < remainingChars; i++) {
            bar.append(REMAINING_CHAR);
        }

        bar.append("] ")
                .append(String.format("%3d", percent))
                .append("% (")
                .append(clampedCurrent).append("/").append(generations)
                .append(")");

        System.out.print("\r" + bar.toString());

        System.out.flush();

        if (clampedCurrent >= generations) {
            System.out.println();
        }
    }
}