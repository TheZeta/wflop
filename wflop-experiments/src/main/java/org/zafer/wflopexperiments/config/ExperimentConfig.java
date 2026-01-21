package org.zafer.wflopexperiments.config;

import java.util.List;

public class ExperimentConfig {

    private String experimentName;
    private int runs;
    private String problemPath;
    private List<String> listeners;
    private List<AlgorithmConfig> algorithms;
    private List<ProcessorConfig> processors;

    public String getExperimentName() {
        return experimentName;
    }

    public int getRuns() {
        return runs;
    }

    public String getProblemPath() {
        return problemPath;
    }

    public List<String> getListeners() {
        return listeners;
    }

    public List<AlgorithmConfig> getAlgorithms() {
        return algorithms;
    }

    public List<ProcessorConfig> getProcessors() {
        return processors;
    }
}
