package org.zafer.wflopexperiments.model;

import java.util.ArrayList;
import java.util.List;

public class ProblemResult {

    private final String problemId;
    private final List<AlgorithmResult> algorithmResults = new ArrayList<>();

    public ProblemResult(String problemId) {
        this.problemId = problemId;
    }

    public void addAlgorithmResult(AlgorithmResult result) {
        algorithmResults.add(result);
    }

    public String getProblemId() {
        return problemId;
    }

    public List<AlgorithmResult> getAlgorithmResults() {
        return List.copyOf(algorithmResults);
    }
}
