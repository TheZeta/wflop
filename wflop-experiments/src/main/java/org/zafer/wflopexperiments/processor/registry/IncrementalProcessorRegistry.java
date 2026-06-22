package org.zafer.wflopexperiments.processor.registry;

import org.zafer.wflopexperiments.processor.IncrementalAlgorithmProcessor;
import org.zafer.wflopexperiments.processor.impl.IncrementalAverageBestFitnessProcessor;
import org.zafer.wflopexperiments.processor.impl.IncrementalConvergenceProcessor;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Registry for {@link IncrementalAlgorithmProcessor} implementations.
 *
 * <p>Follows the same pattern as {@link ProcessorRegistry}, using a static
 * factory function map to instantiate incremental processors by ID.
 *
 * <p>To register a new incremental processor, use:
 * <pre>{@code
 * IncrementalProcessorRegistry.register("my-processor-id", params ->
 *     new MyIncrementalProcessor(...)
 * );
 * }</pre>
 *
 * To instantiate a processor:
 * <pre>{@code
 * IncrementalAlgorithmProcessor proc =
 *     IncrementalProcessorRegistry.create("my-processor-id", params);
 * }</pre>
 */
public final class IncrementalProcessorRegistry {

    private static final Map<String, Function<Map<String, Object>, IncrementalAlgorithmProcessor>>
            REGISTRY = new HashMap<>();

    static {
        IncrementalProcessorRegistry.register(
                "convergence",
                p -> new IncrementalConvergenceProcessor(
                        IncrementalConvergenceProcessor.Mode.valueOf(
                                ((String) p.get("mode")).toUpperCase()
                        ),
                        IncrementalConvergenceProcessor.Aggregation.valueOf(
                                ((String) p.get("aggregation")).toUpperCase()
                        ),
                        ((Number) p.getOrDefault("timeStep", 1.0)).doubleValue(),
                        (String) p.get("outputPath")
                )
        );

        IncrementalProcessorRegistry.register(
                "average-best",
                p -> new IncrementalAverageBestFitnessProcessor(
                        (String) p.get("outputPath")
                )
        );
    }

    private IncrementalProcessorRegistry() {}

    /**
     * Registers an incremental processor factory with the given ID.
     *
     * @param id      unique processor identifier
     * @param factory function that creates an IncrementalAlgorithmProcessor from parameters
     */
    public static void register(
        String id,
        Function<Map<String, Object>, IncrementalAlgorithmProcessor> factory
    ) {
        REGISTRY.put(id, factory);
    }

    /**
     * Creates an incremental processor instance by ID and parameters.
     *
     * @param id         processor identifier (must be registered)
     * @param parameters processor configuration parameters
     * @return instantiated {@link IncrementalAlgorithmProcessor}
     * @throws IllegalArgumentException if processor ID is not registered
     */
    public static IncrementalAlgorithmProcessor create(
        String id,
        Map<String, Object> parameters
    ) {
        Function<Map<String, Object>, IncrementalAlgorithmProcessor> factory = REGISTRY.get(id);
        if (factory == null) {
            throw new IllegalArgumentException("Unknown incremental processor: " + id);
        }
        return factory.apply(parameters);
    }
}
