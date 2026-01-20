package org.zafer.wflopalgorithms.algorithms.wdga;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.zafer.wflopalgorithms.algorithms.wdga.strategy.WakeBasedCrossoverStrategy;
import org.zafer.wflopalgorithms.algorithms.wdga.strategy.WakeBasedMutationStrategy;
import org.zafer.wflopalgorithms.common.ga.solution.Individual;
import org.zafer.wflopalgorithms.common.ga.strategy.CrossoverStrategy;
import org.zafer.wflopalgorithms.common.ga.strategy.MutationStrategy;
import org.zafer.wflopalgorithms.common.ga.strategy.SelectionStrategy;
import org.zafer.wflopalgorithms.common.ga.strategy.TournamentSelection;
import org.zafer.wflopcore.power.PowerCalculator;
import org.zafer.wflopmetaheuristic.listener.ProgressListener;
import org.zafer.wflopmetaheuristic.Metaheuristic;
import org.zafer.wflopmetaheuristic.ProgressEvent;
import org.zafer.wflopmetaheuristic.Solution;
import org.zafer.wflopmetaheuristic.termination.TerminationCondition;
import org.zafer.wflopmetaheuristic.termination.TerminationConditionConfig;
import org.zafer.wflopmetaheuristic.termination.TerminationConditionFactory;
import org.zafer.wflopmetaheuristic.termination.TerminationProgress;
import org.zafer.wflopmodel.layout.TurbineLayout;
import org.zafer.wflopmodel.problem.WFLOP;

public class WDGA implements Metaheuristic {

    private final String algorithm;
    private final int populationSize;
    private final double crossoverRate;
    private final double mutationRate;
    private final String selectionStrategy;
    private final TerminationCondition terminationCondition;
    private final Random random;

    private final double wakeAnalysisPercentage; // Percentage of turbines to analyze
    private final double mutationSelectionPercentage; // Percentage of analyzed turbines to mutate

    @JsonCreator
    public WDGA(
        @JsonProperty("algorithm") String algorithm,
        @JsonProperty("populationSize") int populationSize,
        @JsonProperty("crossoverRate") double crossoverRate,
        @JsonProperty("mutationRate") double mutationRate,
        @JsonProperty("selectionStrategy") String selectionStrategy,
        @JsonProperty("wakeAnalysisPercentage") Double wakeAnalysisPercentage,
        @JsonProperty("mutationSelectionPercentage") Double mutationSelectionPercentage,
        @JsonProperty("termination") TerminationConditionConfig terminationConfig
    ) {
        this.algorithm = algorithm;
        this.populationSize = populationSize;
        this.crossoverRate = crossoverRate;
        this.mutationRate = mutationRate;
        this.selectionStrategy = selectionStrategy != null ? selectionStrategy : "tournament";
        this.terminationCondition = TerminationConditionFactory.fromConfig(terminationConfig);
        this.random = new Random();
        this.wakeAnalysisPercentage = wakeAnalysisPercentage != null
            ? wakeAnalysisPercentage
            : 0.1;
        this.mutationSelectionPercentage = mutationSelectionPercentage != null
            ? mutationSelectionPercentage
            : 0.5;
    }

    public void setSeed(long seed) {
        this.random.setSeed(seed);
    }

    @Override
    public Solution run(WFLOP problem) {
        PowerCalculator calculator = new PowerCalculator(problem);
        return runInternal(problem, calculator, Collections.emptyList());
    }

    @Override
    public Solution runWithListeners(WFLOP problem, List<ProgressListener> listeners) {
        PowerCalculator calculator = new PowerCalculator(problem);
        return runInternal(problem, calculator, listeners);
    }

    public Solution run(WFLOP problem, PowerCalculator calculator) {
        return runInternal(problem, calculator, Collections.emptyList());
    }

    public Solution runWithListeners(WFLOP problem, PowerCalculator calculator, List<ProgressListener> listeners) {
        return runInternal(problem, calculator, listeners);
    }

    private Solution runInternal(WFLOP problem, PowerCalculator calculator, List<ProgressListener> listeners) {
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

    private void evaluateFitness(List<Individual> population, PowerCalculator calculator) {
        for (Individual individual : population) {
            double fitness = computeFitness(individual, calculator);
            individual.setFitness(fitness);
        }
    }

    private double computeFitness(Individual individual, PowerCalculator calculator) {
        TurbineLayout layout = new TurbineLayout(individual.getGenes());
        return calculator.calculateTotalPower(layout);
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

    private CrossoverStrategy createCrossoverStrategy() {
        long seed = getRandom().nextLong();
        return new WakeBasedCrossoverStrategy(seed);
    }

    private MutationStrategy createMutationStrategy(
        PowerCalculator powerCalculator
    ) {
        long seed = getRandom().nextLong();
        return new WakeBasedMutationStrategy(
            wakeAnalysisPercentage,
            mutationSelectionPercentage,
            seed,
            powerCalculator
        );
    }

    // Getters for testing and validation
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

    public TerminationCondition getTerminationCondition() {
        return terminationCondition;
    }

    public Random getRandom() {
        return random;
    }

    public double getWakeAnalysisPercentage() {
        return wakeAnalysisPercentage;
    }

    public double getMutationSelectionPercentage() {
        return mutationSelectionPercentage;
    }
}
