package org.zafer.wflopalgorithms.factory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.zafer.wflopmetaheuristic.Metaheuristic;

import java.io.IOException;
import java.io.InputStream;

/**
 * Factory class for loading algorithm instances from JSON files using a registry.
 * The registry maps algorithm names to their corresponding classes.
 */
public class AlgorithmFactory {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    private static final String ALGORITHM_TYPE_KEY = "algorithm";

    /**
     * Loads an algorithm instance from a JSON file using the algorithm registry.
     * The JSON file must contain an "algorithm" property that maps to a registered algorithm name.
     * 
     * @param jsonResourcePath The path to the JSON resource file
     * @return An instance of Metaheuristic loaded from the JSON
     * @throws AlgorithmLoadException If the algorithm cannot be loaded
     */
    public static Metaheuristic loadFromJson(String jsonResourcePath) throws AlgorithmLoadException {
        try (InputStream inputStream = AlgorithmFactory.class.getClassLoader()
                .getResourceAsStream(jsonResourcePath)) {
            
            if (inputStream == null) {
                throw new AlgorithmLoadException("JSON resource not found: " + jsonResourcePath);
            }

            // Parse JSON to get the algorithm type
            JsonNode rootNode = objectMapper.readTree(inputStream);
            
            if (!rootNode.has(ALGORITHM_TYPE_KEY)) {
                throw new AlgorithmLoadException(
                    "JSON file must contain '" + ALGORITHM_TYPE_KEY + "' property"
                );
            }

            String algorithm = rootNode.get(ALGORITHM_TYPE_KEY).asText();
            
            // Get the algorithm class from registry
            Class<? extends Metaheuristic> algorithmClass = AlgorithmRegistry.getAlgorithmClass(algorithm);
            
            // Re-open the stream for Jackson deserialization
            try (InputStream deserStream = AlgorithmFactory.class.getClassLoader()
                    .getResourceAsStream(jsonResourcePath)) {
                return objectMapper.readValue(deserStream, algorithmClass);
            }

        } catch (IOException e) {
            throw new AlgorithmLoadException("Failed to load algorithm from JSON: " + e.getMessage(), e);
        }
    }

}


