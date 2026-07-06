package org.zafer.wflopalgorithms.algorithms.ga;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.zafer.wflopalgorithms.common.AbstractMetaheuristic;
import org.zafer.wflopalgorithms.common.ga.solution.Individual;
import org.zafer.wflopalgorithms.common.ga.strategy.CrossoverStrategy;
import org.zafer.wflopalgorithms.common.ga.strategy.MutationStrategy;
import org.zafer.wflopalgorithms.common.ga.strategy.RandomReplacementMutation;
import org.zafer.wflopalgorithms.common.ga.strategy.SelectionStrategy;
import org.zafer.wflopalgorithms.common.ga.strategy.SinglePointCrossover;
import org.zafer.wflopalgorithms.common.ga.strategy.SwapMutation;
import org.zafer.wflopalgorithms.common.ga.strategy.TournamentSelection;
import org.zafer.wflopcore.power.PowerCalculator;
import org.zafer.wflopcore.wake.DefaultWakeModelProvider;
import org.zafer.wflopcore.wake.WakeOptimization;
import org.zafer.wflopmetaheuristic.ProgressEvent;
import org.zafer.wflopmetaheuristic.listener.ProgressListener;
import org.zafer.wflopmetaheuristic.Solution;
import org.zafer.wflopmetaheuristic.termination.TerminationCondition;
import org.zafer.wflopmetaheuristic.termination.TerminationConditionConfig;
import org.zafer.wflopmetaheuristic.termination.TerminationConditionFactory;
import org.zafer.wflopmetaheuristic.termination.TerminationProgress;
import org.zafer.wflopmodel.layout.TurbineLayout;
import org.zafer.wflopmodel.problem.WFLOP;

public class GA extends AbstractMetaheuristic {

    private final int populationSize;
    private final double crossoverRate;
    private final double mutationRate;
    private final String selectionStrategy;
    private final String crossoverStrategy;
    private final String mutationStrategy;

    private SelectionStrategy selectionStrategyImpl;
    private CrossoverStrategy crossoverStrategyImpl;
    private MutationStrategy mutationStrategyImpl;

    private List<Individual> population;
    private Individual bestIndividual;

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
        super(TerminationConditionFactory.fromConfig(terminationConfig));

        this.populationSize = populationSize;
        this.crossoverRate = crossoverRate;
        this.mutationRate = mutationRate;
        this.selectionStrategy = selectionStrategy != null ? selectionStrategy : "tournament";
        this.crossoverStrategy = crossoverStrategy != null ? crossoverStrategy : "singlepoint";
        this.mutationStrategy = mutationStrategy != null ? mutationStrategy : "randomreplacement";
    }

    @Override
    protected PowerCalculator createPowerCalculator() {
        return new PowerCalculator(
            getProblem(),
            new DefaultWakeModelProvider(),
            WakeOptimization.NONE
        );
    }

    @Override
    protected void init() {
        this.selectionStrategyImpl = createSelectionStrategy();
        this.crossoverStrategyImpl = createCrossoverStrategy();
        this.mutationStrategyImpl = createMutationStrategy();

        initializePopulation();
        evaluatePopulation();

        this.bestIndividual = Collections.max(population, Comparator.comparingDouble(Individual::getFitness));
    }

    @Override
    protected void step() {
        List<Individual> newPopulation = new ArrayList<>();

        while (newPopulation.size() < this.populationSize) {
            Individual parent1 = this.selectionStrategyImpl.select(this.population);
            Individual parent2 = this.selectionStrategyImpl.select(this.population);
            Individual child;
            if (getRandom().nextDouble() < this.crossoverRate) {
                child = this.crossoverStrategyImpl.crossover(parent1, parent2, getProblem());
            } else {
                child = new Individual(parent1.getGenes());
            }

            if (getRandom().nextDouble() < this.mutationRate) {
                child = this.mutationStrategyImpl.mutate(child, getProblem());
            }

            double fitness = computeFitness(child);
            child.setFitness(fitness);
            newPopulation.add(child);
        }

        this.population = newPopulation;

        Individual currentBest =
            Collections.max(this.population, Comparator.comparingDouble(Individual::getFitness));

        if (currentBest.getFitness() > this.bestIndividual.getFitness()) {
            this.bestIndividual = currentBest;
        }
    }

    @Override
    protected Solution getBestSolution() {
        return this.bestIndividual;
    }

    private void initializePopulation() {
        this.population = new ArrayList<>();
        int layoutSize = getProblem().getCellCount();
        int turbineCount = getProblem().getNumberOfTurbines();

        for (int i = 0; i < this.populationSize; i++) {
            Set<Integer> indices = new LinkedHashSet<>();
            while (indices.size() < turbineCount) {
                indices.add(getRandom().nextInt(layoutSize));
            }
            Individual individual = new Individual(new ArrayList<>(indices));
            this.population.add(individual);
        }
    }

    private void evaluatePopulation() {
        for (Individual individual : this.population) {
            double fitness = computeFitness(individual);
            individual.setFitness(fitness);
        }
    }

    private double computeFitness(Individual individual) {
        TurbineLayout layout = new TurbineLayout(individual.getGenes());
        return getPowerCalculator().calculateTotalPower(layout);
    }

    private SelectionStrategy createSelectionStrategy() {
        long seed = getRandom().nextLong();
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
        return switch (crossoverStrategy.toLowerCase()) {
            case "singlepoint", "single-point", "single_point" ->
                    new SinglePointCrossover(seed);
            default ->
                    new SinglePointCrossover(seed);
        };
    }

    private MutationStrategy createMutationStrategy() {
        long seed = getRandom().nextLong();
        return switch (mutationStrategy.toLowerCase()) {
            case "randomreplacement", "random-replacement", "random_replacement" ->
                    new RandomReplacementMutation(seed);
            case "swap" ->
                    new SwapMutation(seed);
            default ->
                    new RandomReplacementMutation(seed);
        };
    }
}
