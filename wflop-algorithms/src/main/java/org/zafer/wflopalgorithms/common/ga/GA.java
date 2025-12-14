package org.zafer.wflopalgorithms.common.ga;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.zafer.wflopalgorithms.common.ga.solution.Individual;
import org.zafer.wflopalgorithms.common.ga.strategy.CrossoverStrategy;
import org.zafer.wflopalgorithms.common.ga.strategy.MutationStrategy;
import org.zafer.wflopalgorithms.common.ga.strategy.RandomReplacementMutation;
import org.zafer.wflopalgorithms.common.ga.strategy.SelectionStrategy;
import org.zafer.wflopalgorithms.common.ga.strategy.SinglePointCrossover;
import org.zafer.wflopalgorithms.common.ga.strategy.SwapMutation;
import org.zafer.wflopalgorithms.common.ga.strategy.TournamentSelection;
import org.zafer.wflopcore.power.PowerOutputCalculator;
import org.zafer.wflopmetaheuristic.Metaheuristic;
import org.zafer.wflopmetaheuristic.ProgressEvent;
import org.zafer.wflopmetaheuristic.ProgressListener;
import org.zafer.wflopmetaheuristic.Solution;
import org.zafer.wflopmetaheuristic.termination.TerminationCondition;
import org.zafer.wflopmetaheuristic.termination.TerminationConditionConfig;
import org.zafer.wflopmetaheuristic.termination.TerminationConditionFactory;
import org.zafer.wflopmetaheuristic.termination.TerminationProgress;
import org.zafer.wflopmodel.layout.TurbineLayout;
import org.zafer.wflopmodel.problem.WFLOP;

public abstract class GA implements Metaheuristic {

    private final String algorithm;
    private final int populationSize;
    private final double crossoverRate;
    private final double mutationRate;
    private final String selectionStrategy;
    private final String crossoverStrategy;
    private final String mutationStrategy;
    private final TerminationCondition terminationCondition;
    private final Random random;

    @JsonCreator
    public GA(
            @JsonProperty("algorithm") String algorithm,
            @JsonProperty("populationSize") int populationSize,
            @JsonProperty("crossoverRate") double crossoverRate,
            @JsonProperty("mutationRate") double mutationRate,
            @JsonProperty("selectionStrategy") String selectionStrategy,
            @JsonProperty("crossoverStrategy") String crossoverStrategy,
            @JsonProperty("mutationStrategy") String mutationStrategy,
            @JsonProperty("termination") TerminationConditionConfig terminationConfig
    ) {
        this.algorithm = algorithm;
        this.populationSize = populationSize;
        this.crossoverRate = crossoverRate;
        this.mutationRate = mutationRate;
        this.selectionStrategy = selectionStrategy != null ? selectionStrategy : "tournament";
        this.crossoverStrategy = crossoverStrategy != null ? crossoverStrategy : "singlepoint";
        this.mutationStrategy = mutationStrategy != null ? mutationStrategy : "randomreplacement";
        this.terminationCondition = TerminationConditionFactory.fromConfig(terminationConfig);
        this.random = new Random();
    }

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

    public Solution run(WFLOP problem, PowerOutputCalculator calculator) {
        return runInternal(problem, calculator, Collections.emptyList());
    }

    public Solution runWithListeners(WFLOP problem, PowerOutputCalculator calculator, List<ProgressListener> listeners) {
        return runInternal(problem, calculator, listeners);
    }

    private Solution runInternal(WFLOP problem, PowerOutputCalculator calculator, List<ProgressListener> listeners) {
        SelectionStrategy selectionStrategyImpl = createSelectionStrategy();
        CrossoverStrategy crossoverStrategyImpl = createCrossoverStrategy();
        MutationStrategy mutationStrategyImpl = createMutationStrategy(calculator);

        terminationCondition.onStart();

        List<Individual> population = initializePopulation(problem);
        evaluateFitness(population, calculator);

        Individual best = Collections.max(population, Comparator.comparingDouble(Individual::getFitness));

        int gen = 0;
        while (!terminationCondition.shouldTerminate()) {
            List<Individual> newPopulation = new ArrayList<>();

            while (newPopulation.size() < populationSize) {
                Individual parent1 = selectionStrategyImpl.select(population);
                Individual parent2 = selectionStrategyImpl.select(population);
                Individual child;
                if (random.nextDouble() < crossoverRate) {
                    child = crossoverStrategyImpl.crossover(parent1, parent2, problem);
                } else {
                    child = new Individual(parent1.getGenes());
                }

                if (random.nextDouble() < mutationRate) {
                    child = mutationStrategyImpl.mutate(child, problem);
                }

                double fitness = computeFitness(child, calculator);
                child.setFitness(fitness);
                newPopulation.add(child);
            }

            population = newPopulation;

            Individual currentBest = Collections.max(population, Comparator.comparingDouble(Individual::getFitness));
            if (currentBest.getFitness() > best.getFitness()) {
                best = currentBest;
            }
            terminationCondition.onGeneration(++gen);

            // Notify listeners if present
            if (!listeners.isEmpty()) {
                double avg = population.stream().mapToDouble(Individual::getFitness).average().orElse(0);
                TerminationProgress tp = terminationCondition.getProgress();

                ProgressEvent event = new ProgressEvent(
                        gen,
                        best.getFitness(),
                        avg,
                        tp
                );

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


    protected SelectionStrategy createSelectionStrategy() {
        long seed = random.nextLong();
        int tournamentSize = 3; // Default tournament size
        return switch (selectionStrategy.toLowerCase()) {
            case "tournament" -> 
                new TournamentSelection(tournamentSize, seed);
            default -> 
                new TournamentSelection(tournamentSize, seed);
        };
    }

    protected CrossoverStrategy createCrossoverStrategy() {
        long seed = random.nextLong();
        return switch (crossoverStrategy.toLowerCase()) {
            case "singlepoint", "single-point", "single_point" -> 
                new SinglePointCrossover(seed);
            default -> 
                new SinglePointCrossover(seed);
        };
    }

    protected MutationStrategy createMutationStrategy(
        PowerOutputCalculator powerOutputCalculator
    ) {

        long seed = random.nextLong();
        return switch (mutationStrategy.toLowerCase()) {
            case "randomreplacement", "random-replacement", "random_replacement" ->
                new RandomReplacementMutation(seed);
            case "swap" ->
                new SwapMutation(seed);
            default -> 
                new RandomReplacementMutation(seed);
        };
    }


    public String getAlgorithm() {
        return algorithm;
    }

    public int getPopulationSize() {
        return populationSize;
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

    public TerminationCondition getTerminationCondition() {
        return terminationCondition;
    }

    public Random getRandom() {
        return random;
    }
}
