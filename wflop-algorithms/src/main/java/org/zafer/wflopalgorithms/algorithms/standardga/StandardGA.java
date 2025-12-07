package org.zafer.wflopalgorithms.algorithms.standardga;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.zafer.wflopalgorithms.common.ga.GA;
import org.zafer.wflopmetaheuristic.termination.TerminationConditionConfig;

public class StandardGA extends GA {

    @JsonCreator
    public StandardGA(
            @JsonProperty("algorithm") String algorithm,
            @JsonProperty("populationSize") int populationSize,
            @JsonProperty("crossoverRate") double crossoverRate,
            @JsonProperty("mutationRate") double mutationRate,
            @JsonProperty("selectionStrategy") String selectionStrategy,
            @JsonProperty("crossoverStrategy") String crossoverStrategy,
            @JsonProperty("mutationStrategy") String mutationStrategy,
            @JsonProperty("termination") TerminationConditionConfig terminationConfig
    ) {

        super(
            algorithm,
            populationSize,
            crossoverRate,
            mutationRate,
            selectionStrategy,
            crossoverStrategy,
            mutationStrategy,
            terminationConfig
        );
    }
}
