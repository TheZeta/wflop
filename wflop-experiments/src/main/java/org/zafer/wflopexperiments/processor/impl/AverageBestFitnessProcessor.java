package org.zafer.wflopexperiments.processor.impl;

import org.zafer.wflopmetaheuristic.listener.ConvergenceListener;
import org.zafer.wflopexperiments.model.*;
import org.zafer.wflopexperiments.processor.ExperimentProcessor;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class AverageBestFitnessProcessor implements ExperimentProcessor {

    private final String outputPath;

    public AverageBestFitnessProcessor(String outputPath) {
        this.outputPath = outputPath;
    }

    @Override
    public void process(ExperimentResult result) {
        try {
            Path path = Path.of(outputPath);
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }

            try (FileWriter writer = new FileWriter(path.toFile())) {
                writer.write("algorithm,mean_best_fitness\n");

                for (AlgorithmResult algorithm : result.getAlgorithmResults()) {
                    double mean =
                            algorithm.getRuns().stream()
                                    .mapToDouble(this::finalBestFitness)
                                    .average()
                                    .orElse(Double.NaN);

                    // Persist
                    writer.write(
                            algorithm.getAlgorithmId() + "," + mean + "\n"
                    );

                    // Also print (nice for quick feedback)
                    System.out.printf(
                            "[AVG] %s → %.6f%n",
                            algorithm.getAlgorithmId(),
                            mean
                    );
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private double finalBestFitness(RunResult run) {
        ConvergenceListener listener =
            run.getListenerData().stream()
                .filter(d -> d.getPayload() instanceof ConvergenceListener)
                .map(d -> (ConvergenceListener) d.getPayload())
                .findFirst()
                .orElseThrow(() ->
                    new IllegalStateException(
                        "ConvergenceListener missing in run " + run.getRunIndex()
                    )
                );

        List<ConvergenceListener.DataPoint> data = listener.getData();

        return data.get(data.size() - 1).getBestFitness();
    }
}
