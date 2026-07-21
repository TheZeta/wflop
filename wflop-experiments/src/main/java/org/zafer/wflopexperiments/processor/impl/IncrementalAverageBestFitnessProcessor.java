package org.zafer.wflopexperiments.processor.impl;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.zafer.wflopexperiments.model.AlgorithmResult;
import org.zafer.wflopexperiments.model.ProblemResult;
import org.zafer.wflopexperiments.model.RunResult;
import org.zafer.wflopexperiments.processor.IncrementalAlgorithmProcessor;
import org.zafer.wflopexperiments.progress.ExperimentProgress;
import org.zafer.wflopmetaheuristic.listener.ConvergenceListener;

public class IncrementalAverageBestFitnessProcessor implements IncrementalAlgorithmProcessor {

    private final String outputPath;

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

        double bestFitnessAchievedAtIterationMean = algorithmResult.getRuns().stream()
            .mapToDouble(this::finalBestFitnessAchievedAtIteration)
            .average()
            .orElse(Double.NaN);

        double bestFitnessAchievedAtTimeMean = algorithmResult.getRuns().stream()
            .mapToDouble(this::finalBestFitnessAchievedAtTime)
            .average()
            .orElse(Double.NaN);

        int totalIterationCount = (int) algorithmResult.getRuns().stream()
            .mapToInt(this::totalIterationCount)
            .average()
            .orElse(-1);

        double conversionEfficiency = bestFitnessMean / totalPowerWithoutWake(
            algorithmResult.getRuns().getLast()
        );

        double matrixInitTime = algorithmResult.getRuns().stream()
            .mapToDouble(this::matrixInitTime)
            .average()
            .orElse(-1);

        List<List<Integer>> layouts = new ArrayList<>();

        for (RunResult r : algorithmResult.getRuns()) {
            List<Integer> sorted = r.getLayout().stream()
                .sorted()
                .toList();

            layouts.add(sorted);
        }

        writeOrAppendCsv(
            problemResult.getProblemId(),
            algorithmResult.getAlgorithmId(),
            bestFitnessMean,
            bestFitnessAchievedAtIterationMean,
            bestFitnessAchievedAtTimeMean,
            totalIterationCount,
            conversionEfficiency,
            matrixInitTime,
            layouts
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
        double bestFitnessAchievedAtIterationMean,
        double bestFitnessAchievedAtTimeMean,
        int totalIterationCount,
        double conversionEfficiency,
        double matrixInitTime,
        List<List<Integer>> layouts
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
                    "problem,algorithm,mean_best_fitness," +
                    "mean_best_found_at_iteration,mean_best_found_at_time," +
                    "total_iteration_count,conversion_efficiency,layouts\n"
                );
            }

            // Append data row
            writer.write(
                problemId + "," +
                algorithmId + "," +
                bestFitnessMean + "," +
                bestFitnessAchievedAtIterationMean + "," +
                bestFitnessAchievedAtTimeMean + "," +
                totalIterationCount + "," +
                conversionEfficiency + "," +
                matrixInitTime + "," +
                "\"" + layouts + "\"" + "\n"
            );
        }
    }

    private double finalBestFitness(RunResult run) {
        return extractLastData(run).bestFitness();
    }

    private double finalBestFitnessAchievedAtIteration(RunResult run) {
        return extractLastData(run).bestFitnessAchievedAtIteration();
    }

    private double finalBestFitnessAchievedAtTime(RunResult run) {
        return extractLastData(run).bestFitnessAchievedAtTime();
    }

    private int totalIterationCount(RunResult run) {
        return extractLastData(run).iteration();
    }

    private double totalPowerWithoutWake(RunResult run) {
        return extractLastData(run).totalPowerWithoutWake();
    }

    private double matrixInitTime(RunResult run) {
        return extractLastData(run).matrixInitTime();
    }

    private ConvergenceListener.DataPoint extractLastData(RunResult run) {
        ConvergenceListener listener = run.getListenerData().stream()
            .filter(d -> d.getPayload() instanceof ConvergenceListener)
            .map(d -> (ConvergenceListener) d.getPayload())
            .findFirst()
            .orElseThrow(() ->
                new IllegalStateException(
                    "ConvergenceListener missing in run " + run.getRunIndex()
                )
            );

        return listener.getData().getLast();
    }
}
