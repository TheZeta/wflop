package org.zafer.wflopexperiments.runner;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;

import org.zafer.wflopalgorithms.factory.AlgorithmFactory;
import org.zafer.wflopalgorithms.factory.AlgorithmLoadException;
import org.zafer.wflopconfig.ConfigLoader;
import org.zafer.wflopexperiments.config.*;
import org.zafer.wflopexperiments.model.*;
import org.zafer.wflopexperiments.processor.ExperimentProcessor;
import org.zafer.wflopexperiments.processor.registry.ProcessorRegistry;
import org.zafer.wflopmetaheuristic.listener.ProgressListener;
import org.zafer.wflopmetaheuristic.listener.registry.ListenerRegistry;
import org.zafer.wflopmetaheuristic.Metaheuristic;
import org.zafer.wflopmodel.problem.WFLOP;

public class ExperimentRunner {

    private final ExperimentConfig config;

    public ExperimentRunner(ExperimentConfig config) {
        this.config = config;
    }

    public void run() {
        ExperimentResult experimentResult = new ExperimentResult(config.getExperimentName());

        // 1. Loop over problems
        for (ProblemConfig problemConfig : config.getProblems()) {
            ProblemResult problemResult = new ProblemResult(problemConfig.getId());

            // 2. Loop over algorithms
            for (AlgorithmConfig algoConfig : config.getAlgorithms()) {
                AlgorithmResult algorithmResult = new AlgorithmResult(algoConfig.getId());

                // 🔥 WARM-UP PHASE
                for (int w = 1; w <= config.getWarmupRuns(); w++) {
                    WFLOP wflop = ConfigLoader.load(
                        problemConfig.getPath(),
                        new TypeReference<WFLOP>() {
                        }
                    );

                    Metaheuristic warmupAlgorithm;
                    try {
                        warmupAlgorithm = AlgorithmFactory.loadFromJson(
                            algoConfig.getPath()
                        );
                    } catch (AlgorithmLoadException e) {
                        throw new RuntimeException(e);
                    }

                    // No listeners, no result collection
                    warmupAlgorithm.run(wflop);
                }

                // 3. Loop over runs
                for (int run = 1; run <= config.getRuns(); run++) {
                    // 3.1 Load WFLOP instance (per run)
                    WFLOP wflop = ConfigLoader.load(
                        problemConfig.getPath(),
                        new TypeReference<WFLOP>() {}
                    );

                    // 3.2 Load algorithm
                    Metaheuristic algorithm;
                    try {
                        algorithm = AlgorithmFactory.loadFromJson(
                                algoConfig.getPath()
                        );
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
                }
                // 4. Store algorithm result for this problem
                problemResult.addAlgorithmResult(algorithmResult);
            }
            // 5. Store problem result
            experimentResult.addProblemResult(problemResult);
        }

        // 6. Post-process experiment results
        for (ProcessorConfig processorConfig : config.getProcessors()) {
            ExperimentProcessor processor = ProcessorRegistry.create(
                processorConfig.getId(),
                processorConfig.getParams()
            );

            processor.process(experimentResult);
        }
    }
}
