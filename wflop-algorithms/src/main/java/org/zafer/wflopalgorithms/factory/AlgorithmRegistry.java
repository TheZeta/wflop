package org.zafer.wflopalgorithms.factory;

import java.util.HashMap;
import java.util.Map;

import org.zafer.wflopalgorithms.algorithms.de.DE;
import org.zafer.wflopalgorithms.algorithms.fode.FODE;
import org.zafer.wflopalgorithms.algorithms.ga.GA;
import org.zafer.wflopalgorithms.algorithms.lshade.LSHADE;
import org.zafer.wflopalgorithms.algorithms.sa.SA;
import org.zafer.wflopalgorithms.algorithms.wdga.WDGA;
import org.zafer.wflopalgorithms.algorithms.pso.PSO;
import org.zafer.wflopmetaheuristic.Metaheuristic;

/**
 * Registry class that maps algorithm names to their corresponding classes.
 * This provides a centralized mapping of algorithm identifiers to implementation classes.
 */
public class AlgorithmRegistry {

    private static final Map<String, Class<? extends Metaheuristic>> REGISTRY = new HashMap<>();

    static {
        register("GA", GA.class);
        register("WDGA", WDGA.class);
        register("PSO", PSO.class);
        register("SA", SA.class);
        register("DE", DE.class);
        register("LSHADE", LSHADE.class);
        register("FODE", FODE.class);
    }

    /**
     * Registers an algorithm name with its corresponding class.
     * 
     * @param algorithmName The algorithm identifier (e.g., "WDGA", "PSO")
     * @param algorithmClass The class that implements Metaheuristic
     */
    public static void register(String algorithmName, Class<? extends Metaheuristic> algorithmClass) {
        REGISTRY.put(algorithmName, algorithmClass);
    }

    /**
     * Retrieves the algorithm class for the given algorithm name.
     * 
     * @param algorithmName The algorithm identifier
     * @return The algorithm class
     * @throws AlgorithmLoadException If the algorithm is not registered
     */
    public static Class<? extends Metaheuristic> getAlgorithmClass(String algorithmName) 
            throws AlgorithmLoadException {
        Class<? extends Metaheuristic> algorithmClass = REGISTRY.get(algorithmName);
        
        if (algorithmClass == null) {
            throw new AlgorithmLoadException(
                "Algorithm '" + algorithmName + "' is not registered. " +
                "Available algorithms: " + REGISTRY.keySet()
            );
        }
        
        return algorithmClass;
    }

    /**
     * Checks if an algorithm is registered.
     * 
     * @param algorithmName The algorithm identifier
     * @return true if the algorithm is registered, false otherwise
     */
    public static boolean isRegistered(String algorithmName) {
        return REGISTRY.containsKey(algorithmName);
    }

    /**
     * Gets all registered algorithm names.
     * 
     * @return A set of all registered algorithm names
     */
    public static java.util.Set<String> getRegisteredAlgorithms() {
        return REGISTRY.keySet();
    }
}
