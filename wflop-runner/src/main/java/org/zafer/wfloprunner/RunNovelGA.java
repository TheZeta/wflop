package org.zafer.wfloprunner;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.ArrayList;
import java.util.Arrays;

import org.zafer.wflopalgorithms.algorithms.novelga.NovelGA;
import org.zafer.wflopalgorithms.factory.AlgorithmFactory;
import org.zafer.wflopalgorithms.factory.AlgorithmLoadException;
import org.zafer.wflopconfig.ConfigLoader;
import org.zafer.wflopmetaheuristic.ConvergenceGraphListener;
import org.zafer.wflopmetaheuristic.Metaheuristic;
import org.zafer.wflopmetaheuristic.ProgressBarListener;
import org.zafer.wflopmetaheuristic.Solution;
import org.zafer.wflopmodel.problem.WFLOP;

public class RunNovelGA {

    public static void main(String[] args) {
        System.out.println("Starting Novel GA...");
        String jsonPath = "org/zafer/wflopalgorithms/algorithms/novelga/algorithm_instance.json";
        Metaheuristic algorithm = null;
        try {
            algorithm = AlgorithmFactory.loadFromJson(jsonPath);
        } catch(AlgorithmLoadException e) {
            System.out.println(e);
        }
        System.out.println("Loading problem instance...");
        WFLOP problem = ConfigLoader.loadFromResource(
                "wflop_problem.json",
                new TypeReference<WFLOP>() {});

        if (problem == null) {
            System.out.println("Problem instance not loaded");
        } else {
            System.out.println("Cell count: " + problem.getCellCount());
            if (algorithm != null) {
                ConvergenceGraphListener graphListener = new ConvergenceGraphListener(
                    "convergence_novelga",
                    "NovelGA",
                    ConvergenceGraphListener.ExportFormat.HTML,
                    ConvergenceGraphListener.XAxisType.BOTH
                );
                ProgressBarListener progressBarListener = new ProgressBarListener();
                Solution solution = algorithm.runWithListeners(problem, new ArrayList<>(Arrays.asList(graphListener, progressBarListener)));
                System.out.println(solution.getFitness());
                try {
                    graphListener.export();
                    System.out.println("Convergence graphs exported to:");
                    System.out.println("  - convergence_novelga.html");
                } catch (java.io.IOException e) {
                    System.err.println("Failed to export convergence graphs: " + e.getMessage());
                }
            } else {
                System.out.println("null algorithm instance");
            }
        }
    }
}
