package org.zafer.wflopexperiments.model;

import java.util.ArrayList;
import java.util.List;

public class ExperimentResult {

    private final String experimentName;
    private final List<AlgorithmResult> algorithmResults = new ArrayList<>();

    public ExperimentResult(String experimentName) {
        this.experimentName = experimentName;
    }

    public void addAlgorithmResult(AlgorithmResult result) {
        algorithmResults.add(result);
    }

    public String getExperimentName() {
        return experimentName;
    }

    public List<AlgorithmResult> getAlgorithmResults() {
        return List.copyOf(algorithmResults);
    }
}
