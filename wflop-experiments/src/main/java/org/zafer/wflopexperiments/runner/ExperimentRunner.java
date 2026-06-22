package org.zafer.wflopexperiments.runner;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.zafer.wflopalgorithms.factory.AlgorithmFactory;
import org.zafer.wflopalgorithms.factory.AlgorithmLoadException;
import org.zafer.wflopconfig.ConfigLoader;
import org.zafer.wflopexperiments.config.*;
import org.zafer.wflopexperiments.model.*;
import org.zafer.wflopexperiments.processor.ExperimentProcessor;
import org.zafer.wflopexperiments.processor.IncrementalAlgorithmProcessor;
import org.zafer.wflopexperiments.processor.registry.IncrementalProcessorRegistry;
import org.zafer.wflopexperiments.processor.registry.ProcessorRegistry;
import org.zafer.wflopexperiments.progress.ExperimentProgress;
import org.zafer.wflopmetaheuristic.listener.ProgressListener;
import org.zafer.wflopmetaheuristic.listener.registry.ListenerRegistry;
import org.zafer.wflopmetaheuristic.Metaheuristic;
import org.zafer.wflopmodel.problem.WFLOP;

public class ExperimentRunner {

    private static final Logger logger = LoggerFactory.getLogger(ExperimentRunner.class);

    private final ExperimentConfig config;
    private final AlgorithmFactory algorithmFactory;

    public ExperimentRunner(ExperimentConfig config, AlgorithmFactory algorithmFactory) {
        this.config = config;
        this.algorithmFactory = algorithmFactory;
    }

    public void run() {
        ExperimentResult experimentResult = new ExperimentResult(config.getExperimentName());

        // Initialize progress tracker with totals
        ExperimentProgress progress = new ExperimentProgress(
                config.getProblems().size(),
                config.getAlgorithms().size(),
                config.getRuns()
        );

        logger.info("Starting experiment: {} ({}×{}×{} = {} total work items)",
                config.getExperimentName(),
                config.getProblems().size(),
                config.getAlgorithms().size(),
                config.getRuns(),
                config.getProblems().size() * config.getAlgorithms().size() * config.getRuns()
        );

        // 1. Loop over problems
        for (ProblemConfig problemConfig : config.getProblems()) {
            ProblemResult problemResult = new ProblemResult(problemConfig.getId());

            // 2. Loop over algorithms
            for (AlgorithmConfig algorithmConfig : config.getAlgorithms()) {
                AlgorithmResult algorithmResult = new AlgorithmResult(algorithmConfig.getId());

                // 🔥 WARM-UP PHASE
                for (int w = 1; w <= config.getWarmupRuns(); w++) {
                    WFLOP wflop =
                        ConfigLoader.load(problemConfig.getPath(), new TypeReference<WFLOP>() {});

                    Metaheuristic warmupAlgorithm;
                    try {
                        warmupAlgorithm = algorithmFactory.load(algorithmConfig.getPath());
                    } catch (AlgorithmLoadException e) {
                        throw new RuntimeException(e);
                    }

                    // No listeners, no result collection
                    warmupAlgorithm.run(wflop);
                }

                // 3. Loop over runs
                for (int run = 1; run <= config.getRuns(); run++) {
                    // 3.1 Load WFLOP instance (per run)
                    WFLOP wflop =
                        ConfigLoader.load(problemConfig.getPath(), new TypeReference<WFLOP>() {});

                    // 3.2 Load algorithm
                    Metaheuristic algorithm;
                    try {
                        algorithm = algorithmFactory.load(algorithmConfig.getPath());
                    } catch (AlgorithmLoadException e) {
                        throw new RuntimeException(e);
                    }

                    // 3.3 Attach listeners
                    List<ListenerData> collectedData = new ArrayList<>();
                    List<ProgressListener> listeners = new ArrayList<>();
                    for (String listenerId : config.getListeners()) {
                        ProgressListener listener = ListenerRegistry.create(listenerId);
                        listeners.add(listener);
                        collectedData.add(new ListenerData(listenerId, listener));
                    }

                    // 3.4 Run algorithm
                    algorithm.runWithListeners(wflop, listeners);

                    // 3.5 Store run result
                    algorithmResult.addRun(new RunResult(run, collectedData));

                    // Update progress
                    progress.nextRun();
                    logger.info("Completed run {}/{}: {} progress={}%",
                            run,
                            config.getRuns(),
                            progress.getLabel(),
                            String.format("%.1f", progress.getProgress() * 100)
                    );
                }

                // 4. Store algorithm result for this problem
                problemResult.addAlgorithmResult(algorithmResult);

                // ⭐ INCREMENTAL PROCESSING: Process data immediately after algorithm finishes
                processIncrementally(problemResult, algorithmResult, progress);

                progress.nextAlgorithm();
                logger.info("Completed algorithm {}/{} for {}: {} progress={}%",
                        progress.getAlgorithmIndex(),
                        config.getAlgorithms().size(),
                        problemResult.getProblemId(),
                        progress.getLabel(),
                        String.format("%.1f", progress.getProgress() * 100)
                );
            }

            // 5. Store problem result
            experimentResult.addProblemResult(problemResult);

            progress.nextProblem();
            logger.info("Completed problem {}/{}: progress={}%",
                    progress.getProblemIndex(),
                    config.getProblems().size(),
                    String.format("%.1f", progress.getProgress() * 100)
            );
        }

        logger.info("Experiment data collection complete. Processing final results.");

        // 6. Post-process experiment results (final/legacy processors)
        processFinally(experimentResult);

        logger.info("Experiment finished successfully.");
    }

    /**
     * Invokes incremental processors after an algorithm completes all runs
     * for a given problem. These processors write/append results immediately.
     */
    private void processIncrementally(
            ProblemResult problemResult,
            AlgorithmResult algorithmResult,
            ExperimentProgress progress
    ) {
        for (ProcessorConfig processorConfig : config.getProcessors()) {
            if (!processorConfig.isIncremental()) {
                continue; // Skip non-incremental processors
            }

            try {
                IncrementalAlgorithmProcessor processor = IncrementalProcessorRegistry.create(
                        processorConfig.getId(),
                        processorConfig.getParams()
                );

                processor.processAlgorithmResult(problemResult, algorithmResult, progress);

                logger.debug("Incremental processor '{}' completed for {}/{}",
                        processorConfig.getId(),
                        problemResult.getProblemId(),
                        algorithmResult.getAlgorithmId()
                );
            } catch (Exception e) {
                logger.error(
                        "Incremental processor '{}' failed for {}/{}: {}",
                        processorConfig.getId(),
                        problemResult.getProblemId(),
                        algorithmResult.getAlgorithmId(),
                        e.getMessage(),
                        e
                );
                // Continue processing (fail-safe: don't let one processor failure halt the experiment)
            }
        }
    }

    /**
     * Invokes final processors after the entire experiment completes.
     * These processors operate on the complete ExperimentResult tree.
     */
    private void processFinally(ExperimentResult experimentResult) {
        for (ProcessorConfig processorConfig : config.getProcessors()) {
            if (processorConfig.isIncremental()) {
                continue; // Skip incremental processors
            }

            try {
                ExperimentProcessor processor = ProcessorRegistry.create(
                        processorConfig.getId(),
                        processorConfig.getParams()
                );

                processor.process(experimentResult);

                logger.debug("Final processor '{}' completed", processorConfig.getId());
            } catch (Exception e) {
                logger.error(
                        "Final processor '{}' failed: {}",
                        processorConfig.getId(),
                        e.getMessage(),
                        e
                );
                // Continue processing other final processors
            }
        }
    }
}
