package org.zafer.wflopexperiments.processor.impl;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.zafer.wflopexperiments.model.AlgorithmResult;
import org.zafer.wflopexperiments.model.ExperimentResult;
import org.zafer.wflopexperiments.model.ProblemResult;
import org.zafer.wflopexperiments.model.RunResult;
import org.zafer.wflopmetaheuristic.listener.ConvergenceListener;
import org.zafer.wflopexperiments.processor.ExperimentProcessor;

public class AverageBestFitnessProcessor implements ExperimentProcessor {

    private final String outputPath;

    public AverageBestFitnessProcessor(String outputPath) {
        this.outputPath = outputPath;
    }

    @Override
    public void process(ExperimentResult result) {
        try {
            String resolvedPath = outputPath.endsWith(".csv") ? outputPath : outputPath + ".csv";

            Path path = Path.of(resolvedPath);

            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }

            try (FileWriter writer = new FileWriter(path.toFile())) {
                writer.write("problem,algorithm,mean_best_fitness\n");

                for (ProblemResult problem : result.getProblemResults()) {
                    for (AlgorithmResult algorithm : problem.getAlgorithmResults()) {
                        double mean = algorithm.getRuns().stream()
                            .mapToDouble(this::finalBestFitness)
                            .average()
                            .orElse(Double.NaN);

                        // Persist
                        writer.write(
                            problem.getProblemId() + "," +
                            algorithm.getAlgorithmId() + "," +
                            mean + "\n"
                        );

                        // Console feedback
                        System.out.printf(
                            "[AVG] %s | %s → %.6f%n",
                            problem.getProblemId(),
                            algorithm.getAlgorithmId(),
                            mean
                        );
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private double finalBestFitness(RunResult run) {
        ConvergenceListener listener = run.getListenerData().stream()
            .filter(d -> d.getPayload() instanceof ConvergenceListener)
            .map(d -> (ConvergenceListener) d.getPayload())
            .findFirst()
            .orElseThrow(() ->
                new IllegalStateException(
                    "ConvergenceListener missing in run " + run.getRunIndex()
                )
            );

        List<ConvergenceListener.DataPoint> data = listener.getData();
        return data.getLast().getBestFitness();
    }
}
