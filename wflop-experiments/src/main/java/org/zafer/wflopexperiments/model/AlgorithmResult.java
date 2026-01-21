package org.zafer.wflopexperiments.model;

import java.util.ArrayList;
import java.util.List;

public class AlgorithmResult {

    private final String algorithmId;
    private final List<RunResult> runs = new ArrayList<>();

    public AlgorithmResult(String algorithmId) {
        this.algorithmId = algorithmId;
    }

    public void addRun(RunResult run) {
        runs.add(run);
    }

    public String getAlgorithmId() {
        return algorithmId;
    }

    public List<RunResult> getRuns() {
        return List.copyOf(runs);
    }
}
