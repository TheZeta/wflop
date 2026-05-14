package org.zafer.wflopexperiments.cli;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.zafer.wflopalgorithms.factory.AlgorithmFactory;
import org.zafer.wflopalgorithms.factory.AlgorithmRegistry;
import org.zafer.wflopalgorithms.factory.DefaultAlgorithmRegistry;
import org.zafer.wflopexperiments.config.ExperimentConfig;
import org.zafer.wflopexperiments.runner.ExperimentRunner;

public final class ExperimentMain {

    public static void main(String[] args) throws Exception {
        Path configPath = Paths.get(args[0]);

        ExperimentConfig config =
            new ObjectMapper().readValue(
                configPath.toFile(),
                ExperimentConfig.class
            );
        AlgorithmRegistry registry = new DefaultAlgorithmRegistry();
        AlgorithmFactory factory = new AlgorithmFactory(registry);

        new ExperimentRunner(config, factory).run();
    }
}
