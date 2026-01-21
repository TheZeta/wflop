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
        ExperimentResult experimentResult =
                new ExperimentResult(config.getExperimentName());

        for (AlgorithmConfig algoConfig : config.getAlgorithms()) {

            AlgorithmResult algorithmResult =
                    new AlgorithmResult(algoConfig.getId());

            for (int run = 1; run <= config.getRuns(); run++) {

                // 1. Load WFLOP instance (per run)
                WFLOP wflop = ConfigLoader.load(
                    config.getProblemPath(),
                    new TypeReference<WFLOP>() {}
                );

                // 2. Load algorithm
                Metaheuristic algorithm;
                try {
                    algorithm = AlgorithmFactory.loadFromJson(algoConfig.getPath());
                } catch (AlgorithmLoadException e) {
                    throw new RuntimeException(e);
                }

                // 3. Attach listeners
                List<ListenerData> collectedData = new ArrayList<>();
                List<ProgressListener> listeners = new ArrayList<>();

                for (String listenerId : config.getListeners()) {
                    ProgressListener listener =
                            ListenerRegistry.create(listenerId);

                    listeners.add(listener);
                    collectedData.add(new ListenerData(listenerId, listener));
                }

                // 4. Run algorithm with WFLOP instance
                algorithm.runWithListeners(wflop, listeners);

                // 5. Store run result
                algorithmResult.addRun(
                        new RunResult(run, collectedData)
                );
            }

            experimentResult.addAlgorithmResult(algorithmResult);
        }

        // 6. Post-process experiment results
        for (ProcessorConfig processorConfig : config.getProcessors()) {
            ExperimentProcessor processor =
                ProcessorRegistry.create(
                    processorConfig.getId(),
                    processorConfig.getParams()
                );

            processor.process(experimentResult);
        }
    }
}
