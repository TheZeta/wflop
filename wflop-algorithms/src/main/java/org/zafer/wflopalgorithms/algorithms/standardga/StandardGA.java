package org.zafer.wflopalgorithms.algorithms.standardga;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.zafer.wflopalgorithms.common.ga.GA;

public class StandardGA extends GA {

    @JsonCreator
    public StandardGA(
            @JsonProperty("algorithm") String algorithm,
            @JsonProperty("populationSize") int populationSize,
            @JsonProperty("generations") int generations,
            @JsonProperty("crossoverRate") double crossoverRate,
            @JsonProperty("mutationRate") double mutationRate,
            @JsonProperty("selectionStrategy") String selectionStrategy,
            @JsonProperty("crossoverStrategy") String crossoverStrategy,
            @JsonProperty("mutationStrategy") String mutationStrategy
    ) {

        super(
            algorithm,
            populationSize,
            generations,
            crossoverRate,
            mutationRate,
            selectionStrategy,
            crossoverStrategy,
            mutationStrategy
        );
    }
}
