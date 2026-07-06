package org.zafer.wflopalgorithms.algorithms.wdga;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.zafer.wflopalgorithms.algorithms.wdga.strategy.WakeBasedCrossoverStrategy;
import org.zafer.wflopalgorithms.algorithms.wdga.strategy.WakeBasedMutationStrategy;
import org.zafer.wflopalgorithms.common.AbstractMetaheuristic;
import org.zafer.wflopalgorithms.common.ga.solution.*;
import org.zafer.wflopalgorithms.common.ga.strategy.*;
import org.zafer.wflopcore.power.PowerCalculator;
import org.zafer.wflopcore.wake.DefaultWakeModelProvider;
import org.zafer.wflopcore.wake.WakeOptimization;
import org.zafer.wflopmetaheuristic.Solution;
import org.zafer.wflopmetaheuristic.termination.TerminationConditionConfig;
import org.zafer.wflopmetaheuristic.termination.TerminationConditionFactory;
import org.zafer.wflopmodel.layout.TurbineLayout;

public class WDGA extends AbstractMetaheuristic {

    private final int populationSize;
    private final double crossoverRate;
    private final double mutationRate;
    private final double smartMutationRate;
    private final String selectionStrategy;

    private final double wakeAnalysisPercentage; // Percentage of turbines to analyze
    private final double mutationSelectionPercentage; // Percentage of analyzed turbines to mutate

    private SelectionStrategy selectionStrategyImpl;
    private CrossoverStrategy crossoverStrategyImpl;
    private MutationStrategy mutationStrategyImpl;
    private RandomReplacementMutation randomReplacementImpl;

    private List<Individual> population;
    private Individual bestIndividual;

    @JsonCreator
    public WDGA(
        @JsonProperty("algorithm") String algorithm,
        @JsonProperty("populationSize") int populationSize,
        @JsonProperty("crossoverRate") double crossoverRate,
        @JsonProperty("mutationRate") double mutationRate,
        @JsonProperty("smartMutationRate") double  smartMutationRate,
        @JsonProperty("selectionStrategy") String selectionStrategy,
        @JsonProperty("wakeAnalysisPercentage") Double wakeAnalysisPercentage,
        @JsonProperty("mutationSelectionPercentage") Double mutationSelectionPercentage,
        @JsonProperty("termination") TerminationConditionConfig terminationConfig
    ) {
        super(TerminationConditionFactory.fromConfig(terminationConfig));

        this.populationSize = populationSize;
        this.crossoverRate = crossoverRate;
        this.smartMutationRate = smartMutationRate;
        this.mutationRate = mutationRate;
        this.selectionStrategy = selectionStrategy != null ? selectionStrategy : "tournament";
        this.wakeAnalysisPercentage = wakeAnalysisPercentage != null ? wakeAnalysisPercentage : 0.1;
        this.mutationSelectionPercentage = mutationSelectionPercentage != null ? mutationSelectionPercentage : 0.5;
    }

    @Override
    protected PowerCalculator createPowerCalculator() {
        return new PowerCalculator(
            getProblem(),
            new DefaultWakeModelProvider(),
            WakeOptimization.BOTH
        );
    }

    @Override
    protected void init() {
        this.selectionStrategyImpl = createSelectionStrategy();
        this.crossoverStrategyImpl = createCrossoverStrategy();
        this.mutationStrategyImpl = createMutationStrategy(getPowerCalculator());
        this.randomReplacementImpl = new RandomReplacementMutation();

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
                if (getRandom().nextDouble() < this.smartMutationRate) {
                    child = this.mutationStrategyImpl.mutate(child, getProblem());
                } else {
                    child = this.randomReplacementImpl.mutate(child, getProblem());
                }
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
        return new WakeBasedCrossoverStrategy(seed);
    }

    private MutationStrategy createMutationStrategy(PowerCalculator powerCalculator) {
        long seed = getRandom().nextLong();
        return new WakeBasedMutationStrategy(
            wakeAnalysisPercentage,
            mutationSelectionPercentage,
            seed,
            powerCalculator
        );
    }
}
