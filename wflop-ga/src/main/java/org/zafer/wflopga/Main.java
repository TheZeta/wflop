package org.zafer.wflopga;

import com.fasterxml.jackson.core.type.TypeReference;
import org.zafer.wflopconfig.ConfigLoader;
import org.zafer.wflopga.strategy.crossover.CrossoverStrategy;
import org.zafer.wflopga.strategy.crossover.SinglePointCrossover;
import org.zafer.wflopga.strategy.mutation.MutationStrategy;
import org.zafer.wflopga.strategy.mutation.RandomReplacementMutation;
import org.zafer.wflopga.strategy.selection.SelectionStrategy;
import org.zafer.wflopga.strategy.selection.TournamentSelection;
import org.zafer.wflopmetaheuristic.ResultEntry;
import org.zafer.wflopmetaheuristic.ResultExporter;
import org.zafer.wflopmodel.problem.WFLOP;

import java.nio.file.Paths;
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
            List<ResultEntry> allResults = new ArrayList<>();

            // Configuration 1
            System.out.println("\nRunning Configuration 1 (Crossover: 0.7, Mutation: 0.3)...");
            List<ResultEntry> config1Results = runConfiguration(problem, 1, CROSSOVER_RATE_1, MUTATION_RATE_1);
            allResults.addAll(config1Results);

            // Configuration 2
            System.out.println("\nRunning Configuration 2 (Crossover: 0.3, Mutation: 0.7)...");
            List<ResultEntry> config2Results = runConfiguration(problem, 2, CROSSOVER_RATE_2, MUTATION_RATE_2);
            allResults.addAll(config2Results);

            // Configuration 3
            System.out.println("\nRunning Configuration 3 (Crossover: 0.5, Mutation: 0.5)...");
            List<ResultEntry> config3Results = runConfiguration(problem, 3, CROSSOVER_RATE_3, MUTATION_RATE_3);
            allResults.addAll(config3Results);

            // Export results to CSV and JSON
            String csvFileName = "ga_benchmark_results.csv";
            String jsonFileName = "ga_benchmark_results.json";
            
            try {
                ResultExporter.exportToCsv(allResults, Paths.get(csvFileName));
                ResultExporter.exportToJson(allResults, Paths.get(jsonFileName));
                System.out.println("\nResults exported to: " + csvFileName + " and " + jsonFileName);
            } catch (Exception e) {
                System.err.println("Warning: Could not export results to files: " + e.getMessage());
            }

            // Print summary statistics
            printSummaryStatistics(config1Results, config2Results, config3Results);
            
            // Print summary report
            System.out.println("\n" + ResultExporter.generateSummaryReport(allResults));

        } catch (Exception e) {
            System.err.println("Error running genetic algorithm benchmark: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static List<ResultEntry> runConfiguration(WFLOP problem, int configId,
                                                      double crossoverRate, double mutationRate) {
        List<ResultEntry> results = new ArrayList<>();
        String configDescription = String.format("crossover=%.1f,mutation=%.1f,pop=%d,gen=%d", 
                crossoverRate, mutationRate, POPULATION_SIZE, GENERATION_COUNT);

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
            String solutionJson = bestSolution.getTurbineIndices().toString();

            ResultEntry result = new ResultEntry(
                    "GA",                    // algorithm
                    configDescription,       // configuration
                    run,                     // runNumber
                    bestSolution.getFitness(), // fitness
                    executionTime,           // runtimeSeconds
                    solutionJson             // solution
            );

            results.add(result);

            System.out.println("    Best fitness: " + String.format("%.2f", bestSolution.getFitness()) +
                    " MW, Time: " + String.format("%.2f", executionTime) + "s");
        }

        return results;
    }

    private static void printSummaryStatistics(List<ResultEntry> config1, List<ResultEntry> config2, List<ResultEntry> config3) {
        System.out.println("\n=== SUMMARY STATISTICS ===");

        printConfigStats("Configuration 1 (0.7/0.3)", config1);
        printConfigStats("Configuration 2 (0.3/0.7)", config2);
        printConfigStats("Configuration 3 (0.5/0.5)", config3);
    }

    private static void printConfigStats(String configName, List<ResultEntry> results) {
        double avgFitness = results.stream().mapToDouble(ResultEntry::fitness).average().orElse(0.0);
        double maxFitness = results.stream().mapToDouble(ResultEntry::fitness).max().orElse(0.0);
        double minFitness = results.stream().mapToDouble(ResultEntry::fitness).min().orElse(0.0);
        double avgTime = results.stream().mapToDouble(ResultEntry::runtimeSeconds).average().orElse(0.0);

        System.out.println("\n" + configName + ":");
        System.out.println("  Average Fitness: " + String.format("%.2f", avgFitness) + " MW");
        System.out.println("  Best Fitness: " + String.format("%.2f", maxFitness) + " MW");
        System.out.println("  Worst Fitness: " + String.format("%.2f", minFitness) + " MW");
        System.out.println("  Average Time: " + String.format("%.2f", avgTime) + " seconds");
    }

}