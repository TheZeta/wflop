package org.zafer.wflopapi.service;

import org.zafer.wflopalgorithms.algorithms.standardga.StandardGA;
import org.zafer.wflopalgorithms.factory.AlgorithmFactory;
import org.zafer.wflopalgorithms.factory.AlgorithmLoadException;
import org.zafer.wflopmetaheuristic.Metaheuristic;
import org.springframework.stereotype.Component;
import org.zafer.wflopmetaheuristic.termination.TerminationConditionConfig;

@Component
public class WFLOPAlgorithmFactory {

    /**
     * Creates a default GA instance with hardcoded parameters.
     * For production, consider loading from JSON config.
     */
    public Metaheuristic createDefaultGA() {
        String jsonPath = "org/zafer/wflopalgorithms/algorithms/novelga/algorithm_instance.json";
        Metaheuristic algorithm = null;

        try {
            algorithm = AlgorithmFactory.loadFromJson(jsonPath);
        } catch (AlgorithmLoadException e) {
            throw new RuntimeException(e);
        }

        return algorithm;
    }
}
