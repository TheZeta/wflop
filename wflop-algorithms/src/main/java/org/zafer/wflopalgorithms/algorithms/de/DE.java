package org.zafer.wflopalgorithms.algorithms.de;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.zafer.wflopalgorithms.algorithms.de.solution.DEIndividual;
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

public class DE implements Metaheuristic {

    private final String algorithm;
    private final int populationSize;
    private final double F;   // mutation factor
    private final double CR;  // crossover rate
    private final TerminationCondition terminationCondition;
    private final Random random;

    @JsonCreator
    public DE(
        @JsonProperty("algorithm") String algorithm,
        @JsonProperty("populationSize") int populationSize,
        @JsonProperty("f") double F,
        @JsonProperty("cr") double CR,
        @JsonProperty("termination") TerminationConditionConfig terminationConfig
    ) {
        this.algorithm = algorithm;
        this.populationSize = populationSize;
        this.F = F;
        this.CR = CR;
        this.terminationCondition = TerminationConditionFactory.fromConfig(terminationConfig);
        this.random = new Random();
    }

    public void setSeed(long seed) {
        random.setSeed(seed);
    }

    @Override
    public Solution run(WFLOP problem) {
        PowerCalculator calculator = new PowerCalculator(problem);
        return runInternal(problem, calculator, Collections.emptyList());
    }

    @Override
    public Solution runWithListeners(
        WFLOP problem,
        List<ProgressListener> listeners
    ) {
        PowerCalculator calculator = new PowerCalculator(problem);
        return runInternal(problem, calculator, listeners);
    }

    private Solution runInternal(
        WFLOP problem,
        PowerCalculator calculator,
        List<ProgressListener> listeners
    ) {
        terminationCondition.onStart();

        List<DEIndividual> population = initializePopulation(problem);
        evaluatePopulation(population, problem, calculator);

        DEIndividual best = getBest(population);

        int gen = 0;
        while (!terminationCondition.shouldTerminate()) {

            List<DEIndividual> nextPopulation = new ArrayList<>();

            for (int i = 0; i < populationSize; i++) {

                DEIndividual target = population.get(i);

                double[] mutant = mutate(population, i);
                double[] trial = crossover(target.getVector(), mutant);

                enforceBounds(trial, problem.getCellCount());

                double trialFitness = evaluate(trial, problem, calculator);

                if (trialFitness > target.getFitness()) {
                    DEIndividual offspring = new DEIndividual(trial);
                    offspring.setFitness(trialFitness);
                    nextPopulation.add(offspring);
                } else {
                    nextPopulation.add(target);
                }
            }

            population = nextPopulation;
            DEIndividual currentBest = getBest(population);
            if (currentBest.getFitness() > best.getFitness()) {
                best = currentBest;
            }

            terminationCondition.onGeneration(++gen);

            if (!listeners.isEmpty()) {
                double avg = population
                    .stream()
                    .mapToDouble(DEIndividual::getFitness)
                    .average()
                    .orElse(0);

                TerminationProgress tp = terminationCondition.getProgress();

                ProgressEvent event = new ProgressEvent(gen, best.getFitness(), avg, tp);

                for (ProgressListener listener : listeners) {
                    listener.onIteration(event);
                }
            }
        }

        return best;
    }

    private List<DEIndividual> initializePopulation(WFLOP problem) {
        List<DEIndividual> population = new ArrayList<>();

        int turbines = problem.getNumberOfTurbines();
        int cellCount = problem.getCellCount();

        for (int i = 0; i < populationSize; i++) {
            double[] vector = new double[turbines];
            for (int d = 0; d < turbines; d++) {
                vector[d] = random.nextDouble() * cellCount;
            }
            population.add(new DEIndividual(vector));
        }

        return population;
    }

    private double[] mutate(List<DEIndividual> pop, int targetIdx) {
        int a, b, c;
        int size = pop.size();

        do { a = random.nextInt(size); } while (a == targetIdx);
        do { b = random.nextInt(size); } while (b == targetIdx || b == a);
        do { c = random.nextInt(size); } while (c == targetIdx || c == a || c == b);

        double[] A = pop.get(a).getVector();
        double[] B = pop.get(b).getVector();
        double[] C = pop.get(c).getVector();

        double[] mutant = new double[A.length];
        for (int i = 0; i < A.length; i++) {
            mutant[i] = A[i] + F * (B[i] - C[i]);
        }

        return mutant;
    }

    private double[] crossover(double[] target, double[] mutant) {
        double[] trial = new double[target.length];
        int jRand = random.nextInt(target.length);

        for (int j = 0; j < target.length; j++) {
            if (random.nextDouble() < CR || j == jRand) {
                trial[j] = mutant[j];
            } else {
                trial[j] = target[j];
            }
        }

        return trial;
    }

    private double evaluate(
            double[] vector,
            WFLOP problem,
            PowerCalculator calculator
    ) {
        int[] layout = discretize(vector, problem);
        List<Integer> list = Arrays.stream(layout)
            .boxed()
            .toList();
        TurbineLayout tl = new TurbineLayout(new ArrayList<>(list));
        return calculator.calculateTotalPower(tl);
    }

    private int[] discretize(double[] vector, WFLOP problem) {
        int cellCount = problem.getCellCount();
        int[] layout = new int[vector.length];
        boolean[] occupied = new boolean[cellCount];

        for (int i = 0; i < vector.length; i++) {
            int cell = (int) Math.floor(vector[i]);
            cell = Math.max(0, Math.min(cell, cellCount - 1));

            while (occupied[cell]) {
                cell = (cell + 1) % cellCount;
            }

            occupied[cell] = true;
            layout[i] = cell;
        }

        return layout;
    }

    private void enforceBounds(double[] vector, int cellCount) {
        for (int i = 0; i < vector.length; i++) {
            if (vector[i] < 0) {
                vector[i] = 0;
            } else if (vector[i] >= cellCount) {
                vector[i] = cellCount - 1e-9;
            }
        }
    }

    private void evaluatePopulation(
        List<DEIndividual> pop,
        WFLOP problem,
        PowerCalculator calculator
    ) {
        for (DEIndividual ind : pop) {
            ind.setFitness(evaluate(ind.getVector(), problem, calculator));
        }
    }

    private DEIndividual getBest(List<DEIndividual> pop) {
        return Collections.max(pop, Comparator.comparingDouble(DEIndividual::getFitness));
    }
}
