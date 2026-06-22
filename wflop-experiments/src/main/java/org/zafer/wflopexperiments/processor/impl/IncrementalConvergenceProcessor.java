package org.zafer.wflopexperiments.processor.impl;

import org.zafer.wflopexperiments.model.AlgorithmResult;
import org.zafer.wflopexperiments.model.ProblemResult;
import org.zafer.wflopexperiments.model.RunResult;
import org.zafer.wflopexperiments.processor.IncrementalAlgorithmProcessor;
import org.zafer.wflopexperiments.progress.ExperimentProgress;
import org.zafer.wflopmetaheuristic.listener.ConvergenceListener;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Incremental version of ConvergenceProcessor.
 * 
 * <p>Processes convergence data for a single algorithm after it completes all runs
 * for a problem. Supports the same aggregation modes as ConvergenceProcessor but
 * writes/overwrites the output file after each algorithm completes.
 *
 * <p>Configuration parameters:
 * <ul>
 *   <li>{@code mode}: "ITERATION" or "TIME" (default: "ITERATION")</li>
 *   <li>{@code aggregation}: "MEAN", "MEDIAN", or "NONE" (default: "MEAN")</li>
 *   <li>{@code timeStep}: time step for TIME mode (default: 1.0)</li>
 *   <li>{@code outputPath}: base output path (without extension)</li>
 * </ul>
 */
public class IncrementalConvergenceProcessor implements IncrementalAlgorithmProcessor {

    public enum Mode {
        ITERATION,
        TIME
    }

    public enum Aggregation {
        MEAN,
        MEDIAN,
        NONE
    }

    private final Mode mode;
    private final Aggregation aggregation;
    private final double timeStep;
    private final String outputPath;

    public IncrementalConvergenceProcessor(
            Mode mode,
            Aggregation aggregation,
            double timeStep,
            String outputPath
    ) {
        this.mode = mode;
        this.aggregation = aggregation;
        this.timeStep = timeStep;
        this.outputPath = outputPath;
    }

    @Override
    public void processAlgorithmResult(
            ProblemResult problemResult,
            AlgorithmResult algorithmResult,
            ExperimentProgress progress
    ) throws Exception {
        List<List<ConvergenceListener.DataPoint>> runs =
                algorithmResult.getRuns().stream()
                        .map(this::extract)
                        .toList();

        List<Point> data;

        if (aggregation == Aggregation.NONE) {
            data = singleRun(runs);
        } else if (mode == Mode.ITERATION) {
            data = aggregateByIteration(runs);
        } else {
            data = aggregateByTime(runs);
        }

        exportCsv(problemResult.getProblemId(), algorithmResult.getAlgorithmId(), data);
    }

    private List<ConvergenceListener.DataPoint> extract(RunResult run) {
        return run.getListenerData().stream()
                .filter(d -> d.getPayload() instanceof ConvergenceListener)
                .map(d -> (ConvergenceListener) d.getPayload())
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "ConvergenceListener missing in run " + run.getRunIndex()
                        )
                )
                .getData();
    }

    // Aggregation.NONE → first run only
    private List<Point> singleRun(
            List<List<ConvergenceListener.DataPoint>> runs
    ) {
        if (runs.isEmpty()) {
            return List.of();
        }

        List<ConvergenceListener.DataPoint> run = runs.get(0);
        List<Point> out = new ArrayList<>();

        for (ConvergenceListener.DataPoint p : run) {
            double x = (mode == Mode.TIME) ? p.getElapsedTimeSeconds() : p.getIteration();
            out.add(new Point(x, p.getBestFitness()));
        }

        return out;
    }

    private List<Point> aggregateByIteration(
            List<List<ConvergenceListener.DataPoint>> runs
    ) {
        int length = runs.stream()
                .mapToInt(List::size)
                .min()
                .orElse(0);

        List<Point> out = new ArrayList<>();

        for (int i = 0; i < length; i++) {
            int idx = i;
            List<Double> values = runs.stream()
                    .map(r -> r.get(idx).getBestFitness())
                    .toList();

            out.add(new Point(i + 1, aggregate(values)));
        }

        return out;
    }

    private List<Point> aggregateByTime(
            List<List<ConvergenceListener.DataPoint>> runs
    ) {
        double maxTime = runs.stream()
                .flatMap(List::stream)
                .mapToDouble(ConvergenceListener.DataPoint::getElapsedTimeSeconds)
                .max()
                .orElse(0);

        List<Point> out = new ArrayList<>();

        for (double t = 0; t <= maxTime; t += timeStep) {
            List<Double> values = new ArrayList<>();
            for (List<ConvergenceListener.DataPoint> run : runs) {
                values.add(bestSoFar(run, t));
            }
            out.add(new Point(t, aggregate(values)));
        }

        return out;
    }

    private double bestSoFar(
            List<ConvergenceListener.DataPoint> run,
            double time
    ) {
        return run.stream()
                .filter(p -> p.getElapsedTimeSeconds() <= time)
                .mapToDouble(ConvergenceListener.DataPoint::getBestFitness)
                .max()
                .orElse(Double.NEGATIVE_INFINITY);
    }

    private double aggregate(List<Double> values) {
        return switch (aggregation) {
            case MEAN ->
                    values.stream().mapToDouble(d -> d).average().orElse(0);
            case MEDIAN -> {
                List<Double> sorted = values.stream().sorted().toList();
                int n = sorted.size();
                yield (n % 2 == 0)
                        ? (sorted.get(n / 2 - 1) + sorted.get(n / 2)) / 2
                        : sorted.get(n / 2);
            }
            case NONE ->
                    throw new IllegalStateException("Aggregation.NONE should not reach here");
        };
    }

    private void exportCsv(
            String problemId,
            String algorithmId,
            List<Point> data
    ) throws IOException {
        Path path = Paths.get(outputPath + "_" + problemId + "_" + algorithmId + ".csv");

        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }

        try (FileWriter w = new FileWriter(path.toFile())) {
            w.write("x,best_fitness\n");
            for (Point p : data) {
                w.write(p.x + "," + p.y + "\n");
            }
        }
    }

    private record Point(double x, double y) {}
}
