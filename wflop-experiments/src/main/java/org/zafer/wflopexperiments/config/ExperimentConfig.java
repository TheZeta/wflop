package org.zafer.wflopexperiments.config;

import java.util.List;

public class ExperimentConfig {

    private String experimentName;
    private int runs;

    private List<ProblemConfig> problems;
    private List<AlgorithmConfig> algorithms;
    private List<String> listeners;
    private List<ProcessorConfig> processors;

    // Required by Jackson
    public ExperimentConfig() {
    }

    public String getExperimentName() {
        return experimentName;
    }

    public int getRuns() {
        return runs;
    }

    public List<ProblemConfig> getProblems() {
        return problems;
    }

    public List<AlgorithmConfig> getAlgorithms() {
        return algorithms;
    }

    public List<String> getListeners() {
        return listeners;
    }

    public List<ProcessorConfig> getProcessors() {
        return processors;
    }
}
