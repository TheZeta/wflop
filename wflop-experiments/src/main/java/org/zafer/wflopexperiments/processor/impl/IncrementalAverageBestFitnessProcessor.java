package org.zafer.wflopexperiments.processor.impl;

import org.zafer.wflopexperiments.model.AlgorithmResult;
import org.zafer.wflopexperiments.model.ProblemResult;
import org.zafer.wflopexperiments.model.RunResult;
import org.zafer.wflopexperiments.processor.IncrementalAlgorithmProcessor;
import org.zafer.wflopexperiments.progress.ExperimentProgress;
import org.zafer.wflopmetaheuristic.listener.ConvergenceListener;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Incremental version of AverageBestFitnessProcessor.
 *
 * <p>Processes best fitness statistics for a single algorithm after it completes
 * all runs for a problem. Appends the result to an output CSV file (or creates it
 * on first invocation).
 *
 * <p>Configuration parameters:
 * <ul>
 *   <li>{@code outputPath}: output CSV file path (default extension: ".csv")</li>
 * </ul>
 */
public class IncrementalAverageBestFitnessProcessor implements IncrementalAlgorithmProcessor {

    private final String outputPath;
    private volatile boolean headerWritten = false;

    public IncrementalAverageBestFitnessProcessor(String outputPath) {
        this.outputPath = outputPath;
    }

    @Override
    public void processAlgorithmResult(
            ProblemResult problemResult,
            AlgorithmResult algorithmResult,
            ExperimentProgress progress
    ) throws Exception {
        double bestFitnessMean = algorithmResult.getRuns().stream()
                .mapToDouble(this::finalBestFitness)
                .average()
                .orElse(Double.NaN);

        double bestFitnessAchievedAtMean = algorithmResult.getRuns().stream()
                .mapToDouble(this::finalBestFitnessAchievedAt)
                .average()
                .orElse(Double.NaN);

        double conversionEfficiency = bestFitnessMean / totalPowerWithoutWake(
                algorithmResult.getRuns().getLast()
        );

        writeOrAppendCsv(
                problemResult.getProblemId(),
                algorithmResult.getAlgorithmId(),
                bestFitnessMean,
                bestFitnessAchievedAtMean,
                conversionEfficiency
        );

        // Console feedback
        System.out.printf(
                "[AVG-INCREMENTAL] %s | %s => %.6f%n",
                problemResult.getProblemId(),
                algorithmResult.getAlgorithmId(),
                bestFitnessMean
        );
    }

    private void writeOrAppendCsv(
            String problemId,
            String algorithmId,
            double bestFitnessMean,
            double bestFitnessAchievedAtMean,
            double conversionEfficiency
    ) throws IOException {
        String resolvedPath = outputPath.endsWith(".csv") ? outputPath : outputPath + ".csv";
        Path path = Path.of(resolvedPath);

        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }

        boolean fileExists = Files.exists(path);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path.toFile(), true))) {
            // Write header only on first write (when file does not exist)
            if (!fileExists) {
                writer.write(
                        "problem,algorithm,mean_best_fitness,mean_best_found_at,conversion_efficiency\n"
                );
            }

            // Append data row
            writer.write(
                    problemId + "," +
                            algorithmId + "," +
                            bestFitnessMean + "," +
                            bestFitnessAchievedAtMean + "," +
                            conversionEfficiency + "\n"
            );
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

    private double finalBestFitnessAchievedAt(RunResult run) {
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
        return data.getLast().getBestFitnessAchievedAt();
    }

    private double totalPowerWithoutWake(RunResult run) {
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
        return data.getLast().getTotalPowerWithoutWake();
    }
}
