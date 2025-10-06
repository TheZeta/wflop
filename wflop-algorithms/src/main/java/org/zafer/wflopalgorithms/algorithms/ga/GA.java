package org.zafer.wflopalgorithms.algorithms.ga;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.zafer.wflopalgorithms.algorithms.ga.strategy.*;
import org.zafer.wflopcore.calculator.PowerOutputCalculator;
import org.zafer.wflopmetaheuristic.Metaheuristic;
import org.zafer.wflopmetaheuristic.ProgressEvent;
import org.zafer.wflopmetaheuristic.ProgressListener;
import org.zafer.wflopmetaheuristic.Solution;
import org.zafer.wflopmodel.layout.TurbineLayout;
import org.zafer.wflopmodel.problem.WFLOP;

import java.util.*;

/**
 * Genetic Algorithm implementation for WFLOP that can be loaded from JSON.
 * This is a stateless, reusable algorithm instance - thread-safe and can run on multiple problems.
 * 
 * Usage:
 *   Metaheuristic ga = AlgorithmFactory.loadFromJson("path/to/ga_config.json");
 *   Solution solution = ga.run(wflopInstance);
 */
public class GA implements Metaheuristic {

    // Algorithm parameters (loaded from JSON) - immutable
    private final String algorithm;
    private final int populationSize;
    private final int generations;
    private final double crossoverRate;
    private final double mutationRate;
    private final String selectionStrategy;
    private final String crossoverStrategy;
    private final String mutationStrategy;
    private final Random random;

    @JsonCreator
    public GA(
            @JsonProperty("algorithm") String algorithm,
            @JsonProperty("populationSize") int populationSize,
            @JsonProperty("generations") int generations,
            @JsonProperty("crossoverRate") double crossoverRate,
            @JsonProperty("mutationRate") double mutationRate,
            @JsonProperty("selectionStrategy") String selectionStrategy,
            @JsonProperty("crossoverStrategy") String crossoverStrategy,
            @JsonProperty("mutationStrategy") String mutationStrategy
    ) {
        this.algorithm = algorithm;
        this.populationSize = populationSize;
        this.generations = generations;
        this.crossoverRate = crossoverRate;
        this.mutationRate = mutationRate;
        this.selectionStrategy = selectionStrategy != null ? selectionStrategy : "tournament";
        this.crossoverStrategy = crossoverStrategy != null ? crossoverStrategy : "singlepoint";
        this.mutationStrategy = mutationStrategy != null ? mutationStrategy : "randomreplacement";
        this.random = new Random();
    }

    /**
     * Sets the random seed for reproducibility.
     * Call this before run() if you need deterministic results.
     * 
     * @param seed The random seed
     */
    public void setSeed(long seed) {
        this.random.setSeed(seed);
    }

    @Override
    public Solution run(WFLOP problem) {
        PowerOutputCalculator calculator = new PowerOutputCalculator(problem);
        return runInternal(problem, calculator, Collections.emptyList());
    }

    @Override
    public Solution runWithListeners(WFLOP problem, List<ProgressListener> listeners) {
        PowerOutputCalculator calculator = new PowerOutputCalculator(problem);
        return runInternal(problem, calculator, listeners);
    }

    /**
     * Advanced: Run with custom fitness calculator.
     * Useful for testing or using different wake models.
     * 
     * @param problem The WFLOP problem instance
     * @param calculator Custom power output calculator
     * @return The solution found by the algorithm
     */
    public Solution run(WFLOP problem, PowerOutputCalculator calculator) {
        return runInternal(problem, calculator, Collections.emptyList());
    }

    /**
     * Advanced: Run with custom calculator and listeners.
     */
    public Solution runWithListeners(WFLOP problem, PowerOutputCalculator calculator, List<ProgressListener> listeners) {
        return runInternal(problem, calculator, listeners);
    }

    private Solution runInternal(WFLOP problem, PowerOutputCalculator calculator, List<ProgressListener> listeners) {
        // Create strategies for this run
        CrossoverStrategy crossoverStrategyImpl = createCrossoverStrategy();
        MutationStrategy mutationStrategyImpl = createMutationStrategy();
        SelectionStrategy selectionStrategyImpl = createSelectionStrategy();
        
        List<Individual> population = initializePopulation(problem);
        evaluateFitness(population, calculator);

        Individual best = Collections.max(population, Comparator.comparingDouble(Individual::getFitness));

        for (int gen = 0; gen < generations; gen++) {
            List<Individual> newPopulation = new ArrayList<>();

            while (newPopulation.size() < populationSize) {
                Individual parent1 = selectionStrategyImpl.select(population);
                Individual parent2 = selectionStrategyImpl.select(population);

                Individual child = crossoverStrategyImpl.crossover(parent1, parent2, problem);
                child = mutationStrategyImpl.mutate(child, problem);

                double fitness = computeFitness(child, calculator);
                child.setFitness(fitness);
                newPopulation.add(child);
            }

            population = newPopulation;

            Individual currentBest = Collections.max(population, Comparator.comparingDouble(Individual::getFitness));
            if (currentBest.getFitness() > best.getFitness()) {
                best = currentBest;
            }

            // Notify listeners if present
            if (!listeners.isEmpty()) {
                double avg = population.stream().mapToDouble(Individual::getFitness).average().orElse(0);
                ProgressEvent event = new ProgressEvent(gen + 1, best.getFitness(), avg);
                for (ProgressListener listener : listeners) {
                    listener.onIteration(event);
                }
            }
        }

        return best;
    }

    private List<Individual> initializePopulation(WFLOP problem) {
        List<Individual> population = new ArrayList<>();
        int layoutSize = problem.getCellCount();
        int turbineCount = problem.getNumberOfTurbines();

        for (int i = 0; i < populationSize; i++) {
            Set<Integer> indices = new LinkedHashSet<>();
            while (indices.size() < turbineCount) {
                indices.add(random.nextInt(layoutSize));
            }
            Individual individual = new Individual(new ArrayList<>(indices));
            population.add(individual);
        }
        return population;
    }

    private void evaluateFitness(List<Individual> population, PowerOutputCalculator calculator) {
        for (Individual individual : population) {
            double fitness = computeFitness(individual, calculator);
            individual.setFitness(fitness);
        }
    }

    private double computeFitness(Individual individual, PowerOutputCalculator calculator) {
        TurbineLayout layout = new TurbineLayout(individual.getGenes());
        return calculator.calculateTotalPowerOutput(layout);
    }

    private CrossoverStrategy createCrossoverStrategy() {
        long seed = random.nextLong();
        return switch (crossoverStrategy.toLowerCase()) {
            case "singlepoint", "single-point", "single_point" -> 
                new SinglePointCrossover(crossoverRate, seed);
            default -> 
                new SinglePointCrossover(crossoverRate, seed);
        };
    }

    private MutationStrategy createMutationStrategy() {
        long seed = random.nextLong();
        return switch (mutationStrategy.toLowerCase()) {
            case "randomreplacement", "random-replacement", "random_replacement" -> 
                new RandomReplacementMutation(mutationRate, seed);
            case "swap" -> 
                new SwapMutation(mutationRate, seed);
            default -> 
                new RandomReplacementMutation(mutationRate, seed);
        };
    }

    private SelectionStrategy createSelectionStrategy() {
        long seed = random.nextLong();
        int tournamentSize = 3; // Default tournament size
        return switch (selectionStrategy.toLowerCase()) {
            case "tournament" -> 
                new TournamentSelection(tournamentSize, seed);
            default -> 
                new TournamentSelection(tournamentSize, seed);
        };
    }

    // Getters for testing and validation
    public String getAlgorithm() {
        return algorithm;
    }

    public int getPopulationSize() {
        return populationSize;
    }

    public int getGenerations() {
        return generations;
    }

    public double getCrossoverRate() {
        return crossoverRate;
    }

    public double getMutationRate() {
        return mutationRate;
    }

    public String getSelectionStrategy() {
        return selectionStrategy;
    }

    public String getCrossoverStrategy() {
        return crossoverStrategy;
    }

    public String getMutationStrategy() {
        return mutationStrategy;
    }
}
