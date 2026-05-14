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

public class DefaultAlgorithmRegistry implements AlgorithmRegistry {

    private final Map<String, Class<? extends Metaheuristic>> registry = new HashMap<>();

    public DefaultAlgorithmRegistry() {
        register("GA", GA.class);
        register("WDGA", WDGA.class);
        register("PSO", PSO.class);
        register("SA", SA.class);
        register("DE", DE.class);
        register("LSHADE", LSHADE.class);
        register("FODE", FODE.class);
    }

    private void register(String algorithmName, Class<? extends Metaheuristic> algorithmClass) {
        registry.put(algorithmName, algorithmClass);
    }

    @Override
    public Class<? extends Metaheuristic> getAlgorithmClass(String algorithmName)
        throws AlgorithmLoadException {

        Class<? extends Metaheuristic> algorithmClass = registry.get(algorithmName);

        if (algorithmClass == null) {
            throw new AlgorithmLoadException(
                "Algorithm '" + algorithmName + "' is not registered. " +
                "Available algorithms: " + registry.keySet()
            );
        }

        return algorithmClass;
    }
}
