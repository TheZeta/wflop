package org.zafer.wflopga;

import com.fasterxml.jackson.core.type.TypeReference;
import org.zafer.wflopconfig.ConfigLoader;
import org.zafer.wflopga.strategy.crossover.CrossoverStrategy;
import org.zafer.wflopga.strategy.crossover.SinglePointCrossover;
import org.zafer.wflopga.strategy.mutation.MutationStrategy;
import org.zafer.wflopga.strategy.mutation.RandomReplacementMutation;
import org.zafer.wflopga.strategy.selection.SelectionStrategy;
import org.zafer.wflopga.strategy.selection.TournamentSelection;
import org.zafer.wflopmodel.problem.WFLOP;

import java.util.ArrayList;
import java.util.List;

public class Main {

    // GA Configuration parameters
    private static final int POPULATION_SIZE = 100;
    private static final int GENERATION_COUNT = 100;

    // Configuration 1: High crossover, low mutation
    private static final double CROSSOVER_RATE_1 = 0.7;
    private static final double MUTATION_RATE_1 = 0.3;

    // Configuration 2: Low crossover, high mutation
    private static final double CROSSOVER_RATE_2 = 0.3;
    private static final double MUTATION_RATE_2 = 0.7;

    // Configuration 3: Balanced rates
    private static final double CROSSOVER_RATE_3 = 0.5;
    private static final double MUTATION_RATE_3 = 0.5;

    // Number of runs per configuration for statistical significance
    private static final int RUNS_PER_CONFIG = 10;

    public static void main(String[] args) {
        try {
            // Load problem from testdata resources
            String wflopFile = "wflop_problem.json";
            WFLOP problem = ConfigLoader.loadFromResource(wflopFile, new TypeReference<WFLOP>() {});

            System.out.println("Starting Genetic Algorithm benchmark suite...");
            System.out.println("Problem loaded: " + problem.getNumberOfTurbines() + " turbines, " +
                    problem.getDimension() + "x" + problem.getDimension() + " grid");

            // Run all configurations
            List<GAResult> allResults = new ArrayList<>();

            // Configuration 1
            System.out.println("\nRunning Configuration 1 (Crossover: 0.7, Mutation: 0.3)...");
            List<GAResult> config1Results = runConfiguration(problem, 1, CROSSOVER_RATE_1, MUTATION_RATE_1);
            allResults.addAll(config1Results);

            // Configuration 2
            System.out.println("\nRunning Configuration 2 (Crossover: 0.3, Mutation: 0.7)...");
            List<GAResult> config2Results = runConfiguration(problem, 2, CROSSOVER_RATE_2, MUTATION_RATE_2);
            allResults.addAll(config2Results);

            // Configuration 3
            System.out.println("\nRunning Configuration 3 (Crossover: 0.5, Mutation: 0.5)...");
            List<GAResult> config3Results = runConfiguration(problem, 3, CROSSOVER_RATE_3, MUTATION_RATE_3);
            allResults.addAll(config3Results);

            // Export results to Excel
            String excelFileName = "ga_benchmark_results.xlsx";
//            exportToExcel(allResults, excelFileName);

            System.out.println("\nBenchmark completed! Results exported to: " + excelFileName);

            // Print summary statistics
            printSummaryStatistics(config1Results, config2Results, config3Results);

        } catch (Exception e) {
            System.err.println("Error running genetic algorithm benchmark: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static List<GAResult> runConfiguration(WFLOP problem, int configId,
                                                   double crossoverRate, double mutationRate) {
        List<GAResult> results = new ArrayList<>();

        for (int run = 1; run <= RUNS_PER_CONFIG; run++) {
            System.out.println("  Run " + run + "/" + RUNS_PER_CONFIG);

            // Create strategies with the specified rates
            CrossoverStrategy crossover = new SinglePointCrossover(crossoverRate);
            MutationStrategy mutation = new RandomReplacementMutation(mutationRate);
            SelectionStrategy selection = new TournamentSelection(3); // Tournament size of 3

            // Create and run GA
            GeneticAlgorithm ga = new GeneticAlgorithm(
                    problem, POPULATION_SIZE, GENERATION_COUNT,
                    crossover, mutation, selection
            );

            long startTime = System.currentTimeMillis();
            Individual bestSolution = ga.run();
            long endTime = System.currentTimeMillis();

            double executionTime = (endTime - startTime) / 1000.0; // Convert to seconds

            GAResult result = new GAResult(
                    configId, run, crossoverRate, mutationRate,
                    POPULATION_SIZE, GENERATION_COUNT,
                    bestSolution.getFitness(), executionTime,
                    bestSolution.getTurbineIndices()
            );

            results.add(result);

            System.out.println("    Best fitness: " + String.format("%.2f", bestSolution.getFitness()) +
                    " MW, Time: " + String.format("%.2f", executionTime) + "s");
        }

        return results;
    }

    private static void printSummaryStatistics(List<GAResult> config1, List<GAResult> config2, List<GAResult> config3) {
        System.out.println("\n=== SUMMARY STATISTICS ===");

        printConfigStats("Configuration 1 (0.7/0.3)", config1);
        printConfigStats("Configuration 2 (0.3/0.7)", config2);
        printConfigStats("Configuration 3 (0.5/0.5)", config3);
    }

    private static void printConfigStats(String configName, List<GAResult> results) {
        double avgFitness = results.stream().mapToDouble(r -> r.bestFitness).average().orElse(0.0);
        double maxFitness = results.stream().mapToDouble(r -> r.bestFitness).max().orElse(0.0);
        double minFitness = results.stream().mapToDouble(r -> r.bestFitness).min().orElse(0.0);
        double avgTime = results.stream().mapToDouble(r -> r.executionTime).average().orElse(0.0);

        System.out.println("\n" + configName + ":");
        System.out.println("  Average Fitness: " + String.format("%.2f", avgFitness) + " MW");
        System.out.println("  Best Fitness: " + String.format("%.2f", maxFitness) + " MW");
        System.out.println("  Worst Fitness: " + String.format("%.2f", minFitness) + " MW");
        System.out.println("  Average Time: " + String.format("%.2f", avgTime) + " seconds");
    }

    // Inner class to hold GA results
    private static class GAResult {
        final int configurationId;
        final int runNumber;
        final double crossoverRate;
        final double mutationRate;
        final int populationSize;
        final int generations;
        final double bestFitness;
        final double executionTime;
        final List<Integer> solution;

        GAResult(int configurationId, int runNumber, double crossoverRate, double mutationRate,
                 int populationSize, int generations, double bestFitness, double executionTime,
                 List<Integer> solution) {
            this.configurationId = configurationId;
            this.runNumber = runNumber;
            this.crossoverRate = crossoverRate;
            this.mutationRate = mutationRate;
            this.populationSize = populationSize;
            this.generations = generations;
            this.bestFitness = bestFitness;
            this.executionTime = executionTime;
            this.solution = new ArrayList<>(solution);
        }
    }
}