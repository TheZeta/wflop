package org.zafer.wfloprunner;

import java.util.ArrayList;
import java.util.Arrays;

import com.fasterxml.jackson.core.type.TypeReference;

import org.zafer.wflopalgorithms.factory.AlgorithmFactory;
import org.zafer.wflopalgorithms.factory.AlgorithmLoadException;
import org.zafer.wflopconfig.ConfigLoader;
import org.zafer.wflopmetaheuristic.ConvergenceGraphListener;
import org.zafer.wflopmetaheuristic.listener.ProgressBarListener;
import org.zafer.wflopmetaheuristic.Metaheuristic;
import org.zafer.wflopmetaheuristic.Solution;
import org.zafer.wflopmodel.problem.WFLOP;

public class Run {

    public static void main(String[] args) {
        String algoPath = args[0];
        String problemPath = args[1];

        Metaheuristic algorithm = null;
        try {
            algorithm = AlgorithmFactory.loadFromJson(algoPath);
        } catch(AlgorithmLoadException e) {
            System.out.println(e);
        }

        System.out.println("Loading problem instance...");
        WFLOP problem = ConfigLoader.load(
            problemPath,
            new TypeReference<WFLOP>() {}
        );

        if (problem == null) {
            System.out.println("Problem instance not loaded");
        } else {
            System.out.println("Cell count: " + problem.getCellCount());
            if (algorithm != null) {
                String algoName = algorithm.getClass().getSimpleName();
                System.out.println("Running algorithm: " + algoName);
                ConvergenceGraphListener graphListener = new ConvergenceGraphListener(
                    "convergence_" + algoName.toLowerCase(),
                    algoName,
                    ConvergenceGraphListener.ExportFormat.HTML,
                    ConvergenceGraphListener.XAxisType.BOTH
                );
                ProgressBarListener progressBarListener = new ProgressBarListener();
                Solution solution = algorithm.runWithListeners(
                    problem,
                    new ArrayList<>(Arrays.asList(graphListener, progressBarListener))
                );
                System.out.println(solution.getFitness());
                try {
                    graphListener.export();
                    System.out.println("Convergence graphs exported to:");
                    System.out.println("  - convergence_" + algoName.toLowerCase() + ".html");
                } catch (java.io.IOException e) {
                    System.err.println("Failed to export convergence graphs: " + e.getMessage());
                }
            } else {
                System.out.println("null algorithm instance");
            }
        }
    }
}
