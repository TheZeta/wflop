package org.zafer.wflopexperiments.runner;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import com.fasterxml.jackson.core.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.zafer.wflopalgorithms.factory.AlgorithmFactory;
import org.zafer.wflopalgorithms.factory.AlgorithmLoadException;
import org.zafer.wflopconfig.ConfigLoader;
import org.zafer.wflopexperiments.config.*;
import org.zafer.wflopexperiments.model.*;
import org.zafer.wflopexperiments.processor.IncrementalAlgorithmProcessor;
import org.zafer.wflopexperiments.processor.registry.IncrementalProcessorRegistry;
import org.zafer.wflopexperiments.progress.ExperimentProgress;
import org.zafer.wflopmetaheuristic.listener.ProgressListener;
import org.zafer.wflopmetaheuristic.listener.registry.ListenerRegistry;
import org.zafer.wflopmetaheuristic.Metaheuristic;
import org.zafer.wflopmodel.problem.WFLOP;

public class ExperimentRunner {

    private static final Logger logger = LoggerFactory.getLogger(ExperimentRunner.class);

    private final ExperimentConfig config;
    private final AlgorithmFactory algorithmFactory;
    private final int threadCount;

    public ExperimentRunner(ExperimentConfig config, AlgorithmFactory algorithmFactory, int threadCount) {
        this.config = config;
        this.algorithmFactory = algorithmFactory;
        this.threadCount = Math.max(1, threadCount);
    }

    public void run() {
        if (threadCount <= 1 || config.getAlgorithms().size() <= 1) {
            runSequential();
            return;
        }

        runParallel();
    }

    private void runSequential() {
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

        for (ProblemConfig problemConfig : config.getProblems()) {
            ProblemResult problemResult = new ProblemResult(problemConfig.getId());
            runSequentialProblem(problemConfig, problemResult, progress);
            experimentResult.addProblemResult(problemResult);

            progress.nextProblem();
            String progressPercent = formatPercent(progress.getProgress());
            logger.info("Completed problem {}/{}: progress={}%",
                progress.getProblemIndex(),
                config.getProblems().size(),
                progressPercent
            );
        }

        logger.info("Experiment data collection complete. Processing final results.");
    }

    private void runSequentialProblem(
        ProblemConfig problemConfig,
        ProblemResult problemResult,
        ExperimentProgress progress
    ) {
        for (AlgorithmConfig algorithmConfig : config.getAlgorithms()) {
            AlgorithmResult algorithmResult = executeSequentialAlgorithm(problemConfig, algorithmConfig, progress);

            problemResult.addAlgorithmResult(algorithmResult);
            processIncrementally(problemResult, algorithmResult, progress);

            progress.nextAlgorithm();
            String progressPercent = formatPercent(progress.getProgress());
            logger.info("Completed algorithm {}/{} for {}: {} progress={}%",
                progress.getAlgorithmIndex(),
                config.getAlgorithms().size(),
                problemResult.getProblemId(),
                progress.getLabel(),
                progressPercent
            );
        }
    }

    private AlgorithmResult executeSequentialAlgorithm(
        ProblemConfig problemConfig,
        AlgorithmConfig algorithmConfig,
        ExperimentProgress progress
    ) {
        AlgorithmResult algorithmResult = new AlgorithmResult(algorithmConfig.getId());

        for (int w = 1; w <= config.getWarmupRuns(); w++) {
            WFLOP wflop = ConfigLoader.load(problemConfig.getPath(), new TypeReference<WFLOP>() {});

            Metaheuristic warmupAlgorithm;
            try {
                warmupAlgorithm = algorithmFactory.load(algorithmConfig.getPath());
            } catch (AlgorithmLoadException e) {
                throw new RuntimeException(e);
            }

            warmupAlgorithm.run(wflop);
        }

        for (int run = 1; run <= config.getRuns(); run++) {
            WFLOP wflop = ConfigLoader.load(problemConfig.getPath(), new TypeReference<WFLOP>() {});

            Metaheuristic algorithm;
            try {
                algorithm = algorithmFactory.load(algorithmConfig.getPath());
            } catch (AlgorithmLoadException e) {
                throw new RuntimeException(e);
            }

            List<ListenerData> collectedData = new ArrayList<>();
            List<ProgressListener> listeners = new ArrayList<>();
            for (String listenerId : config.getListeners()) {
                ProgressListener listener = ListenerRegistry.create(listenerId);
                listeners.add(listener);
                collectedData.add(new ListenerData(listenerId, listener));
            }

            algorithm.runWithListeners(wflop, listeners);
            algorithmResult.addRun(new RunResult(run, collectedData));

            progress.nextRun();
            String progressPercent = formatPercent(progress.getProgress());
            logger.info("Completed run {}/{}: {} progress={}%",
                run,
                config.getRuns(),
                progress.getLabel(),
                progressPercent
            );
        }

        return algorithmResult;
    }

    private void runParallel() {
        ExperimentResult experimentResult = new ExperimentResult(config.getExperimentName());
        AtomicInteger completedPairs = new AtomicInteger();
        int totalPairs = config.getProblems().size() * config.getAlgorithms().size();

        logger.info("Starting experiment: {} ({}×{}×{} = {} total work items, {} worker threads)",
            config.getExperimentName(),
            config.getProblems().size(),
            config.getAlgorithms().size(),
            config.getRuns(),
            config.getProblems().size() * config.getAlgorithms().size() * config.getRuns(),
            threadCount
        );

        try (ExecutorService executor = Executors.newFixedThreadPool(threadCount)) {
            for (int problemIndex = 0; problemIndex < config.getProblems().size(); problemIndex++) {
                ProblemConfig problemConfig = config.getProblems().get(problemIndex);
                ProblemResult problemResult;
                try {
                    problemResult = runParallelProblem(
                        executor,
                        problemConfig,
                        problemIndex,
                        completedPairs,
                        totalPairs
                    );
                } catch (RuntimeException e) {
                    executor.shutdownNow();
                    throw e;
                }

                experimentResult.addProblemResult(problemResult);

                String progressPercent = formatPercent((problemIndex + 1) * 1.0 / config.getProblems().size());
                logger.info("Completed problem {}/{}: progress={}%",
                    problemIndex + 1,
                    config.getProblems().size(),
                    progressPercent
                );
            }
        }

        logger.info("Experiment data collection complete. Processing final results.");
    }

    private ProblemResult runParallelProblem(
        ExecutorService executor,
        ProblemConfig problemConfig,
        int problemIndex,
        AtomicInteger completedPairs,
        int totalPairs
    ) {
        CompletionService<AlgorithmResult> completionService = new ExecutorCompletionService<>(executor);
        Map<String, AlgorithmResult> completedAlgorithms = new LinkedHashMap<>();

        for (AlgorithmConfig algorithmConfig : config.getAlgorithms()) {
            completionService.submit(() -> executeAlgorithmPair(problemConfig, algorithmConfig));
        }

        collectParallelAlgorithmResults(problemConfig, completionService, completedAlgorithms, completedPairs, totalPairs);

        ProblemResult problemResult = new ProblemResult(problemConfig.getId());
        for (AlgorithmConfig algorithmConfig : config.getAlgorithms()) {
            AlgorithmResult algorithmResult = completedAlgorithms.get(algorithmConfig.getId());

            if (algorithmResult == null) {
                throw new IllegalStateException(
                    "Missing algorithm result for " + problemConfig.getId() + " / " + algorithmConfig.getId()
                );
            }

            problemResult.addAlgorithmResult(algorithmResult);
        }

        processParallelIncrementalResults(problemResult, problemIndex);
        return problemResult;
    }

    private void collectParallelAlgorithmResults(
        ProblemConfig problemConfig,
        CompletionService<AlgorithmResult> completionService,
        Map<String, AlgorithmResult> completedAlgorithms,
        AtomicInteger completedPairs,
        int totalPairs
    ) {
        for (int algorithmIndex = 0; algorithmIndex < config.getAlgorithms().size(); algorithmIndex++) {
            try {
                AlgorithmResult algorithmResult = completionService.take().get();
                completedAlgorithms.put(algorithmResult.getAlgorithmId(), algorithmResult);

                int completed = completedPairs.incrementAndGet();
                String progressPercent = formatPercent(completed * 1.0 / totalPairs);
                logger.info("Completed pair {}/{}: {} / {} progress={}%",
                    completed,
                    totalPairs,
                    problemConfig.getId(),
                    algorithmResult.getAlgorithmId(),
                    progressPercent
                );
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for parallel experiment results", e);
            } catch (ExecutionException e) {
                throw new RuntimeException("Parallel experiment execution failed", e.getCause());
            }
        }
    }

    private void processParallelIncrementalResults(ProblemResult problemResult, int problemIndex) {
        List<AlgorithmResult> algorithmResults = problemResult.getAlgorithmResults();

        for (int algorithmIndex = 0; algorithmIndex < algorithmResults.size(); algorithmIndex++) {
            AlgorithmResult algorithmResult = algorithmResults.get(algorithmIndex);
            processIncrementally(
                problemResult,
                algorithmResult,
                new ExperimentProgress(
                    config.getProblems().size(),
                    config.getAlgorithms().size(),
                    config.getRuns(),
                    problemIndex,
                    algorithmIndex,
                    config.getRuns()
                )
            );
        }
    }

    private AlgorithmResult executeAlgorithmPair(ProblemConfig problemConfig, AlgorithmConfig algorithmConfig) {
        // Warm-up runs are intentionally isolated per task so the parallel path
        // matches the sequential execution semantics.
        for (int w = 1; w <= config.getWarmupRuns(); w++) {
            WFLOP wflop = ConfigLoader.load(problemConfig.getPath(), new TypeReference<WFLOP>() {});

            Metaheuristic warmupAlgorithm;
            try {
                warmupAlgorithm = algorithmFactory.load(algorithmConfig.getPath());
            } catch (AlgorithmLoadException e) {
                throw new RuntimeException(e);
            }

            warmupAlgorithm.run(wflop);
        }

        AlgorithmResult algorithmResult = new AlgorithmResult(algorithmConfig.getId());

        for (int run = 1; run <= config.getRuns(); run++) {
            WFLOP wflop = ConfigLoader.load(problemConfig.getPath(), new TypeReference<WFLOP>() {});

            Metaheuristic algorithm;
            try {
                algorithm = algorithmFactory.load(algorithmConfig.getPath());
            } catch (AlgorithmLoadException e) {
                throw new RuntimeException(e);
            }

            List<ListenerData> collectedData = new ArrayList<>();
            List<ProgressListener> listeners = new ArrayList<>();
            for (String listenerId : config.getListeners()) {
                ProgressListener listener = ListenerRegistry.create(listenerId);
                listeners.add(listener);
                collectedData.add(new ListenerData(listenerId, listener));
            }

            algorithm.runWithListeners(wflop, listeners);
            algorithmResult.addRun(new RunResult(run, collectedData));
        }

        return algorithmResult;
    }

    private String formatPercent(double fraction) {
        return String.format("%.1f", fraction * 100);
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
}
