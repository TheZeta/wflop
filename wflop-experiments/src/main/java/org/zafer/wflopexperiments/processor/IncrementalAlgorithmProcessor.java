package org.zafer.wflopexperiments.processor;

import org.zafer.wflopexperiments.model.AlgorithmResult;
import org.zafer.wflopexperiments.model.ProblemResult;
import org.zafer.wflopexperiments.progress.ExperimentProgress;

/**
 * Processes algorithm results immediately after an algorithm completes all runs
 * for a given problem instance, rather than deferring processing until the end
 * of the entire experiment.
 *
 * <p><strong>Benefits:</strong>
 * <ul>
 *   <li>Prevents data loss if the experiment crashes during final processing</li>
 *   <li>Allows streaming/incremental output (e.g., append to CSV files)</li>
 *   <li>Enables real-time progress monitoring</li>
 * </ul>
 *
 * <p><strong>Invocation:</strong> An incremental processor is invoked after
 * an algorithm finishes all its runs for a single problem. The processor receives:
 * <ul>
 *   <li>The {@code ProblemResult} for the current problem (all algorithms for this problem so far)</li>
 *   <li>The {@code AlgorithmResult} that just completed (all runs)</li>
 *   <li>The {@code ExperimentProgress} indicating overall experiment state</li>
 * </ul>
 *
 * <p><strong>Error Handling:</strong> Implementations should throw exceptions on
 * fatal errors. The {@code ExperimentRunner} will catch and log these, then continue
 * processing (fail-safe approach to avoid losing already-collected data).
 */
public interface IncrementalAlgorithmProcessor {

    /**
     * Processes the algorithm results after an algorithm completes all runs
     * for a specific problem.
     *
     * @param problemResult    the current problem result (contains all algorithm results
     *                         for this problem processed so far)
     * @param algorithmResult  the algorithm result that just completed (all runs)
     * @param progress         overall experiment progress state
     * @throws Exception       if processing fails (will be caught and logged by runner)
     */
    void processAlgorithmResult(
            ProblemResult problemResult,
            AlgorithmResult algorithmResult,
            ExperimentProgress progress
    ) throws Exception;
}
