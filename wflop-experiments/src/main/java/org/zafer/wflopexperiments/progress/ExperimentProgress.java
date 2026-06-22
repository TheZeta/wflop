package org.zafer.wflopexperiments.progress;

/**
 * Tracks the overall progress of an experiment run, representing what portion
 * of the total work (all problem × algorithm × run combinations) has been completed.
 *
 * <p>This class is used to:
 * <ul>
 *   <li>Calculate overall completion percentage (0..1)</li>
 *   <li>Provide human-readable progress labels for logging/UI</li>
 *   <li>Facilitate incremental processing after each algorithm-problem combo</li>
 * </ul>
 *
 * <p>Progress is calculated as a linear fraction of total work items:
 * <pre>
 * totalWork = totalProblems × totalAlgorithms × totalRunsPerAlgorithm
 * completedWork = (problemIndex × totalAlgorithms × totalRunsPerAlgorithm)
 *               + (algorithmIndex × totalRunsPerAlgorithm)
 *               + currentRun
 * progress = completedWork / totalWork
 * </pre>
 */
public class ExperimentProgress {

    private final int totalProblems;
    private final int totalAlgorithms;
    private final int totalRunsPerAlgorithm;

    private int problemIndex;       // 0-based
    private int algorithmIndex;     // 0-based
    private int currentRun;         // 1-based (1 to totalRunsPerAlgorithm)

    /**
     * Constructs an ExperimentProgress tracker.
     *
     * @param totalProblems             number of problems in the experiment
     * @param totalAlgorithms           number of algorithms per problem
     * @param totalRunsPerAlgorithm     number of runs per algorithm-problem combo
     */
    public ExperimentProgress(int totalProblems, int totalAlgorithms, int totalRunsPerAlgorithm) {
        this.totalProblems = totalProblems;
        this.totalAlgorithms = totalAlgorithms;
        this.totalRunsPerAlgorithm = totalRunsPerAlgorithm;
        this.problemIndex = 0;
        this.algorithmIndex = 0;
        this.currentRun = 1;
    }

    /**
     * Returns the overall progress as a fraction in [0, 1], where 0 means no work done
     * and 1 means all work completed.
     */
    public double getProgress() {
        long totalWork = (long) totalProblems * totalAlgorithms * totalRunsPerAlgorithm;
        long completedWork =
                (long) problemIndex * totalAlgorithms * totalRunsPerAlgorithm
                        + (long) algorithmIndex * totalRunsPerAlgorithm
                        + currentRun;
        return completedWork / (double) totalWork;
    }

    /**
     * Returns a human-readable label describing the current position in the experiment.
     * Format: "Problem X/Y, Algorithm A/B, Run R/S"
     */
    public String getLabel() {
        return String.format(
                "Problem %d/%d, Algorithm %d/%d, Run %d/%d",
                problemIndex + 1,
                totalProblems,
                algorithmIndex + 1,
                totalAlgorithms,
                currentRun,
                totalRunsPerAlgorithm
        );
    }

    /**
     * Moves to the next problem, resetting algorithm and run indices.
     */
    public void nextProblem() {
        problemIndex++;
        algorithmIndex = 0;
        currentRun = 1;
    }

    /**
     * Moves to the next algorithm within the current problem, resetting run index.
     */
    public void nextAlgorithm() {
        algorithmIndex++;
        currentRun = 1;
    }

    /**
     * Advances the current run by one.
     */
    public void nextRun() {
        currentRun++;
    }

    // Getters for inspection
    public int getProblemIndex() {
        return problemIndex;
    }

    public int getAlgorithmIndex() {
        return algorithmIndex;
    }

    public int getCurrentRun() {
        return currentRun;
    }

    public int getTotalProblems() {
        return totalProblems;
    }

    public int getTotalAlgorithms() {
        return totalAlgorithms;
    }

    public int getTotalRunsPerAlgorithm() {
        return totalRunsPerAlgorithm;
    }
}
