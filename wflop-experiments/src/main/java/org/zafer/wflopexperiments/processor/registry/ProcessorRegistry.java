package org.zafer.wflopexperiments.processor.registry;

import org.zafer.wflopexperiments.processor.ExperimentProcessor;
import org.zafer.wflopexperiments.processor.impl.AverageBestFitnessProcessor;
import org.zafer.wflopexperiments.processor.impl.ConvergenceProcessor;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class ProcessorRegistry {

    private static final Map<String, Function<Map<String, Object>, ExperimentProcessor>>
            REGISTRY = new HashMap<>();

    static {
        ProcessorRegistry.register(
            "convergence",
            p -> new ConvergenceProcessor(
                ConvergenceProcessor.Mode.valueOf(
                    ((String) p.get("mode")).toUpperCase()
                ),
                ConvergenceProcessor.Aggregation.valueOf(
                    ((String) p.get("aggregation")).toUpperCase()
                ),
                ((Number) p.getOrDefault("timeStep", 1.0)).doubleValue(),
                (String) p.get("outputPath")
            )
        );

        ProcessorRegistry.register(
            "average-best",
            p -> new AverageBestFitnessProcessor(
                (String) p.get("outputPath")
            )
        );
    }

    private ProcessorRegistry() {}

    public static void register(
        String id,
        Function<Map<String, Object>, ExperimentProcessor> factory
    ) {
        REGISTRY.put(id, factory);
    }

    public static ExperimentProcessor create(
        String id,
        Map<String, Object> parameters
    ) {
        Function<Map<String, Object>, ExperimentProcessor> factory = REGISTRY.get(id);
        if (factory == null) {
            throw new IllegalArgumentException("Unknown processor: " + id);
        }
        return factory.apply(parameters);
    }
}
