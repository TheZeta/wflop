package org.zafer.wflopexperiments.processor.impl;

import org.zafer.wflopexperiments.model.*;
import org.zafer.wflopexperiments.processor.ExperimentProcessor;
import org.zafer.wflopexperiments.util.RunResultUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class WilcoxonProcessor implements ExperimentProcessor {

    private final String outputPath;

    public WilcoxonProcessor(String outputPath) {
        this.outputPath = outputPath;
    }

    @Override
    public void process(ExperimentResult result) {

        List<AlgorithmResult> algos = result.getAlgorithmResults();
        int n = algos.size();

        double[][] pValues = new double[n][n];

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                pValues[i][j] =
                        wilcoxon(
                                finalFitness(algos.get(i)),
                                finalFitness(algos.get(j))
                        );
            }
        }

        exportCsv(algos, pValues);
    }

    private List<Double> finalFitness(AlgorithmResult algo) {
        return algo.getRuns().stream()
                .map(RunResultUtils::finalBestFitness)
                .toList();
    }

    private double wilcoxon(List<Double> a, List<Double> b) {
        int n = Math.min(a.size(), b.size());

        List<Double> diffs = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            double d = a.get(i) - b.get(i);
            if (d != 0) diffs.add(d);
        }

        int m = diffs.size();
        if (m == 0) return 1.0;

        List<Double> abs =
                diffs.stream().map(Math::abs).sorted().toList();

        Map<Double, Integer> rank = new HashMap<>();
        for (int i = 0; i < abs.size(); i++) {
            rank.putIfAbsent(abs.get(i), i + 1);
        }

        double wPlus = 0;
        double wMinus = 0;

        for (double d : diffs) {
            double r = rank.get(Math.abs(d));
            if (d > 0) wPlus += r;
            else wMinus += r;
        }

        double w = Math.min(wPlus, wMinus);

        double mean = m * (m + 1) / 4.0;
        double var = m * (m + 1) * (2 * m + 1) / 24.0;

        double z = (w - mean) / Math.sqrt(var);
        return 2 * normalCdf(-Math.abs(z));
    }

    private double normalCdf(double z) {
        return 0.5 * (1 + erf(z / Math.sqrt(2)));
    }

    private double erf(double x) {
        double t = 1 / (1 + 0.5 * Math.abs(x));
        double tau = t * Math.exp(
                -x * x - 1.26551223 +
                t * (1.00002368 +
                t * (0.37409196 +
                t * (0.09678418 +
                t * (-0.18628806 +
                t * (0.27886807 +
                t * (-1.13520398 +
                t * (1.48851587 +
                t * (-0.82215223 +
                t * 0.17087277))))))))
        );
        return x >= 0 ? 1 - tau : tau - 1;
    }

    private void exportCsv(
            List<AlgorithmResult> algos,
            double[][] p
    ) {
        try (FileWriter w = new FileWriter(outputPath)) {

            w.write("algo");
            for (AlgorithmResult a : algos) {
                w.write("," + a.getAlgorithmId());
            }
            w.write("\n");

            for (int i = 0; i < algos.size(); i++) {
                w.write(algos.get(i).getAlgorithmId());
                for (int j = 0; j < algos.size(); j++) {
                    w.write(j <= i ? ",-" : "," + p[i][j]);
                }
                w.write("\n");
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
