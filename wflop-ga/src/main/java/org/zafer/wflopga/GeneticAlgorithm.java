package org.zafer.wflopga;

import org.zafer.wflopga.strategy.crossover.CrossoverStrategy;
import org.zafer.wflopga.strategy.mutation.MutationStrategy;
import org.zafer.wflopga.strategy.selection.SelectionStrategy;
import org.zafer.wflopmetaheuristic.Metaheuristic;
import org.zafer.wflopmodel.problem.WFLOP;
import org.zafer.wflopcore.calculator.PowerOutputCalculator;

import java.util.*;

public class GeneticAlgorithm implements Metaheuristic {

    private final WFLOP problem;
    private final int populationSize;
    private final int generations;
    private final Random random = new Random();

    private final CrossoverStrategy crossoverStrategy;
    private final MutationStrategy mutationStrategy;
    private final SelectionStrategy selectionStrategy;

    private final PowerOutputCalculator fitnessCalculator;

    public GeneticAlgorithm(
            WFLOP problem,
            int populationSize,
            int generations,
            CrossoverStrategy crossoverStrategy,
            MutationStrategy mutationStrategy,
            SelectionStrategy selectionStrategy
    ) {
        this.problem = problem;
        this.populationSize = populationSize;
        this.generations = generations;
        this.crossoverStrategy = crossoverStrategy;
        this.mutationStrategy = mutationStrategy;
        this.selectionStrategy = selectionStrategy;
        this.fitnessCalculator = new PowerOutputCalculator(problem);
    }

    @Override
    public Individual run() {
        List<Individual> population = initializePopulation();
        evaluateFitness(population);

        Individual best = Collections.max(population, Comparator.comparingDouble(Individual::getFitness));

        for (int gen = 0; gen < generations; gen++) {
            List<Individual> newPopulation = new ArrayList<>();

            while (newPopulation.size() < populationSize) {
                Individual parent1 = selectionStrategy.select(population);
                Individual parent2 = selectionStrategy.select(population);

                Individual child = crossoverStrategy.crossover(parent1, parent2, problem);
                child = mutationStrategy.mutate(child, problem);

                double fitness = computeFitness(child);
                child.setFitness(fitness);
                newPopulation.add(child);
            }

            population = newPopulation;

            Individual currentBest = Collections.max(population, Comparator.comparingDouble(Individual::getFitness));
            if (currentBest.getFitness() > best.getFitness()) {
                best = currentBest;
            }
        }

        return best;
    }

    private List<Individual> initializePopulation() {
        List<Individual> population = new ArrayList<>();
        int layoutSize = problem.getCellCount();
        int turbineCount = problem.getNumberOfTurbines();

        for (int i = 0; i < populationSize; i++) {
            Set<Integer> indices = new LinkedHashSet<>();
            while (indices.size() < turbineCount) {
                indices.add(random.nextInt(layoutSize));
            }
            Individual individual = initializeIndividual(new ArrayList<>(indices));
            population.add(individual);
        }
        return population;
    }

    private Individual initializeIndividual(List<Integer> genes) {
        Individual individual = new Individual(genes);
        computeFitness(individual);
        individual.setFitness(computeFitness(individual));
        return individual;
    }

    private void evaluateFitness(List<Individual> population) {
        for (Individual ind : population) {
            double fitness = computeFitness(ind);
            ind.setFitness(fitness);
        }
    }

    private double computeFitness(Individual individual) {
        double fitness = fitnessCalculator.calculateTotalPowerOutput(individual.getSolution());
        individual.setFitness(fitness);
        return fitness;
    }
}
