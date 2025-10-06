package org.zafer.wflopapi.service;

import org.zafer.wflopalgorithms.algorithms.ga.GA;
import org.zafer.wflopmetaheuristic.Metaheuristic;
import org.springframework.stereotype.Component;

@Component
public class WFLOPAlgorithmFactory {

    /**
     * Creates a default GA instance with hardcoded parameters.
     * For production, consider loading from JSON config.
     */
    public Metaheuristic createDefaultGA() {
        // Create GA with default parameters (matches the old wflop-ga config)
        return new GA(
                "GA",
                100,        // populationSize
                200,        // generations
                0.7,        // crossoverRate
                0.1,        // mutationRate
                "tournament",        // selectionStrategy
                "singlepoint",       // crossoverStrategy
                "randomreplacement"  // mutationStrategy
        );
    }
}
