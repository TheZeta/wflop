package org.zafer.wflopalgorithms.factory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.zafer.wflopmetaheuristic.Metaheuristic;

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
     * @param jsonPath The path to the JSON resource file
     * @return An instance of Metaheuristic loaded from the JSON
     * @throws AlgorithmLoadException If the algorithm cannot be loaded
     */
    public static Metaheuristic loadFromJson(String jsonPath) throws AlgorithmLoadException {
        try (InputStream inputStream = openJson(jsonPath)) {

            // Parse JSON
            JsonNode rootNode = objectMapper.readTree(inputStream);

            if (!rootNode.has(ALGORITHM_TYPE_KEY)) {
                throw new AlgorithmLoadException(
                        "JSON file must contain '" + ALGORITHM_TYPE_KEY + "' property"
                );
            }

            String algorithm = rootNode.get(ALGORITHM_TYPE_KEY).asText();
            Class<? extends Metaheuristic> algorithmClass =
                    AlgorithmRegistry.getAlgorithmClass(algorithm);

            // Re-open for deserialization
            try (InputStream deserStream = openJson(jsonPath)) {
                return objectMapper.readValue(deserStream, algorithmClass);
            }

        } catch (IOException e) {
            throw new AlgorithmLoadException("Failed to load algorithm from JSON: " + jsonPath, e);
        }
    }

    private static InputStream openJson(String path) throws IOException {
        // 1. Check filesystem first
        Path filePath = Path.of(path);
        if (Files.exists(filePath)) {
            return Files.newInputStream(filePath);
        }

        // 2. Fallback to classpath
        InputStream classpathStream = AlgorithmFactory.class
                .getClassLoader()
                .getResourceAsStream(path);

        if (classpathStream != null) {
            return classpathStream;
        }

        throw new IOException("JSON config not found in filesystem or classpath: " + path);
    }
}
