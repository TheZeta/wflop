package org.zafer.wflopexperiments.processor.impl;

import org.zafer.wflopexperiments.model.*;
import org.zafer.wflopexperiments.processor.ExperimentProcessor;
import org.zafer.wflopexperiments.util.RunResultUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class LatexTableProcessor implements ExperimentProcessor {

    private final String outputPath;

    public LatexTableProcessor(String outputPath) {
        this.outputPath = outputPath;
    }

    @Override
    public void process(ExperimentResult result) {

        try (FileWriter w = new FileWriter(outputPath)) {

            w.write("\\begin{tabular}{l c}\n");
            w.write("\\hline\n");
            w.write("Algorithm & Best Fitness (mean $\\pm$ std)\\\\\n");
            w.write("\\hline\n");

            for (AlgorithmResult algo : result.getAlgorithmResults()) {

                List<Double> values =
                        algo.getRuns().stream()
                                .map(RunResultUtils::finalBestFitness)
                                .toList();

                double mean = mean(values);
                double std = std(values, mean);

                w.write(
                        algo.getAlgorithmId() + " & " +
                                String.format("%.4f $\\pm$ %.4f", mean, std) +
                                "\\\\\n"
                );
            }

            w.write("\\hline\n");
            w.write("\\end{tabular}\n");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private double mean(List<Double> v) {
        return v.stream().mapToDouble(d -> d).average().orElse(0);
    }

    private double std(List<Double> v, double mean) {
        double sum =
                v.stream()
                        .mapToDouble(d -> (d - mean) * (d - mean))
                        .sum();
        return Math.sqrt(sum / (v.size() - 1));
    }
}
