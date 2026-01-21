package org.zafer.wflopexperiments.processor.impl;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.zafer.wflopexperiments.model.AlgorithmResult;
import org.zafer.wflopexperiments.model.ExperimentResult;
import org.zafer.wflopexperiments.model.ProblemResult;
import org.zafer.wflopexperiments.model.RunResult;
import org.zafer.wflopmetaheuristic.listener.ConvergenceListener;
import org.zafer.wflopexperiments.processor.ExperimentProcessor;

public class ConvergenceProcessor implements ExperimentProcessor {

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

    public ConvergenceProcessor(
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
    public void process(ExperimentResult result) {
        for (ProblemResult problem : result.getProblemResults()) {
            for (AlgorithmResult algorithm : problem.getAlgorithmResults()) {

                List<List<ConvergenceListener.DataPoint>> runs =
                    algorithm.getRuns().stream()
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

                exportCsv(problem.getProblemId(), algorithm.getAlgorithmId(), data);
            }
        }
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
    ) {
        Path path = Paths.get(outputPath + "_" + problemId + "_" + algorithmId + ".csv");

        try {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }

            try (FileWriter w = new FileWriter(path.toFile())) {
                w.write("x,best_fitness\n");
                for (Point p : data) {
                    w.write(p.x + "," + p.y + "\n");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private record Point(double x, double y) {}
}
