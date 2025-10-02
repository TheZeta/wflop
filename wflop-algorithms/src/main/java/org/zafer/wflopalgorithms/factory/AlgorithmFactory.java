package org.zafer.wflopalgorithms.factory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.zafer.wflopmetaheuristic.Metaheuristic;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Factory class for loading algorithm instances from JSON files using reflection.
 * Implements a caching mechanism to avoid repeated reflection lookups.
 */
public class AlgorithmFactory {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Map<String, Class<? extends Metaheuristic>> algorithmCache = new HashMap<>();
    
    private static final String ALGORITHM_TYPE_KEY = "algorithmType";
    private static final String BASE_PACKAGE = "org.zafer.wflopalgorithms";

    /**
     * Loads an algorithm instance from a JSON file using reflection.
     * The JSON file must contain an "algorithmType" property that maps to the class name.
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

            String algorithmType = rootNode.get(ALGORITHM_TYPE_KEY).asText();
            
            // Get the algorithm class (from cache or via reflection)
            Class<? extends Metaheuristic> algorithmClass = resolveAlgorithmClass(algorithmType);
            
            // Re-open the stream for Jackson deserialization
            try (InputStream deserStream = AlgorithmFactory.class.getClassLoader()
                    .getResourceAsStream(jsonResourcePath)) {
                return objectMapper.readValue(deserStream, algorithmClass);
            }

        } catch (IOException e) {
            throw new AlgorithmLoadException("Failed to load algorithm from JSON: " + e.getMessage(), e);
        }
    }

    /**
     * Resolves the algorithm class from the algorithm type string.
     * Uses caching to avoid repeated reflection lookups.
     * 
     * @param algorithmType The algorithm type identifier (e.g., "GA", "PSO")
     * @return The algorithm class
     * @throws AlgorithmLoadException If the class cannot be resolved
     */
    @SuppressWarnings("unchecked")
    private static Class<? extends Metaheuristic> resolveAlgorithmClass(String algorithmType) 
            throws AlgorithmLoadException {
        
        // Check cache first
        if (algorithmCache.containsKey(algorithmType)) {
            return algorithmCache.get(algorithmType);
        }

        // Use reflection to load the class
        String className = buildClassName(algorithmType);
        
        try {
            Class<?> clazz = Class.forName(className);
            
            if (!Metaheuristic.class.isAssignableFrom(clazz)) {
                throw new AlgorithmLoadException(
                    "Class " + className + " does not implement Metaheuristic interface"
                );
            }
            
            Class<? extends Metaheuristic> algorithmClass = (Class<? extends Metaheuristic>) clazz;
            
            // Cache the resolved class
            algorithmCache.put(algorithmType, algorithmClass);
            
            return algorithmClass;
            
        } catch (ClassNotFoundException e) {
            throw new AlgorithmLoadException(
                "Algorithm class not found: " + className, e
            );
        }
    }

    /**
     * Builds the fully qualified class name from the algorithm type.
     * Assumes the class name matches the algorithm type and follows the package structure.
     * 
     * @param algorithmType The algorithm type identifier
     * @return The fully qualified class name
     */
    private static String buildClassName(String algorithmType) {
        // Convert algorithm type to lowercase for package name
        String packageSuffix = algorithmType.toLowerCase();
        // Class name is the same as algorithm type
        return BASE_PACKAGE + ".algorithms." + packageSuffix + "." + algorithmType;
    }

    /**
     * Clears the algorithm cache. Useful for testing.
     */
    static void clearCache() {
        algorithmCache.clear();
    }

    /**
     * Gets the current cache size. Useful for testing.
     */
    static int getCacheSize() {
        return algorithmCache.size();
    }
}


