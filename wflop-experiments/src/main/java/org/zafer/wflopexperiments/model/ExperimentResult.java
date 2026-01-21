package org.zafer.wflopexperiments.model;

import java.util.ArrayList;
import java.util.List;

public class ExperimentResult {

    private final String experimentName;
    private final List<ProblemResult> problemResults = new ArrayList<>();

    public ExperimentResult(String experimentName) {
        this.experimentName = experimentName;
    }

    public void addProblemResult(ProblemResult result) {
        problemResults.add(result);
    }

    public String getExperimentName() {
        return experimentName;
    }

    public List<ProblemResult> getProblemResults() {
        return List.copyOf(problemResults);
    }
}
