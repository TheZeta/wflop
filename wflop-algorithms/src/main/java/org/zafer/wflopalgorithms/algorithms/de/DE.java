package org.zafer.wflopalgorithms.algorithms.de;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.zafer.wflopalgorithms.algorithms.de.solution.DEIndividual;
import org.zafer.wflopalgorithms.common.AbstractMetaheuristic;
import org.zafer.wflopcore.power.PowerCalculator;
import org.zafer.wflopcore.wake.DefaultWakeModelProvider;
import org.zafer.wflopcore.wake.WakeOptimization;
import org.zafer.wflopmetaheuristic.Solution;
import org.zafer.wflopmetaheuristic.termination.TerminationConditionConfig;
import org.zafer.wflopmetaheuristic.termination.TerminationConditionFactory;
import org.zafer.wflopmodel.layout.TurbineLayout;

public class DE extends AbstractMetaheuristic {

    private final int populationSize;
    private final double F;   // mutation factor
    private final double CR;  // crossover rate

    private List<DEIndividual> population;
    private DEIndividual bestIndividual;

    @JsonCreator
    public DE(
        @JsonProperty("algorithm") String algorithm,
        @JsonProperty("populationSize") int populationSize,
        @JsonProperty("f") double F,
        @JsonProperty("cr") double CR,
        @JsonProperty("termination") TerminationConditionConfig terminationConfig
    ) {
        super(TerminationConditionFactory.fromConfig(terminationConfig));

        this.populationSize = populationSize;
        this.F = F;
        this.CR = CR;
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
        initializePopulation();
        evaluatePopulation();

        this.bestIndividual = getBest();
    }

    @Override
    protected void step() {
        List<DEIndividual> nextPopulation = new ArrayList<>();

        for (int i = 0; i < this.populationSize; i++) {

            DEIndividual target = this.population.get(i);

            double[] mutant = mutate(this.population, i);
            double[] trial = crossover(target.getVector(), mutant);

            enforceBounds(trial);

            double trialFitness = evaluate(trial);

            if (trialFitness > target.getFitness()) {
                DEIndividual offspring = new DEIndividual(trial);
                offspring.setFitness(trialFitness);
                nextPopulation.add(offspring);
            } else {
                nextPopulation.add(target);
            }
        }

        this.population = nextPopulation;
        DEIndividual currentBest = getBest();
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
        int turbines = getProblem().getNumberOfTurbines();
        int cellCount = getProblem().getCellCount();

        for (int i = 0; i < this.populationSize; i++) {
            double[] vector = new double[turbines];
            for (int d = 0; d < turbines; d++) {
                vector[d] = getRandom().nextDouble() * cellCount;
            }
            this.population.add(new DEIndividual(vector));
        }
    }

    private double[] mutate(List<DEIndividual> pop, int targetIdx) {
        int a, b, c;
        int size = pop.size();

        do { a = getRandom().nextInt(size); } while (a == targetIdx);
        do { b = getRandom().nextInt(size); } while (b == targetIdx || b == a);
        do { c = getRandom().nextInt(size); } while (c == targetIdx || c == a || c == b);

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
        int jRand = getRandom().nextInt(target.length);

        for (int j = 0; j < target.length; j++) {
            if (getRandom().nextDouble() < CR || j == jRand) {
                trial[j] = mutant[j];
            } else {
                trial[j] = target[j];
            }
        }

        return trial;
    }

    private double evaluate(double[] vector) {
        int[] layout = discretize(vector);
        List<Integer> list = Arrays.stream(layout)
            .boxed()
            .toList();
        TurbineLayout tl = new TurbineLayout(new ArrayList<>(list));
        return getPowerCalculator().calculateTotalPower(tl);
    }

    private int[] discretize(double[] vector) {
        int cellCount = getProblem().getCellCount();
        int[] layout = new int[vector.length];
        boolean[] occupied = new boolean[cellCount];

        for (int i = 0; i < vector.length; i++) {
            int cell = (int) Math.floor(vector[i]);
            cell = Math.clamp(cell, 0, cellCount - 1);

            while (occupied[cell]) {
                cell = (cell + 1) % cellCount;
            }

            occupied[cell] = true;
            layout[i] = cell;
        }

        return layout;
    }

    private void enforceBounds(double[] vector) {
        for (int i = 0; i < vector.length; i++) {
            if (vector[i] < 0) {
                vector[i] = 0;
            } else if (vector[i] >= getProblem().getCellCount()) {
                vector[i] = getProblem().getCellCount() - 1e-9;
            }
        }
    }

    private void evaluatePopulation() {
        for (DEIndividual ind : this.population) {
            ind.setFitness(evaluate(ind.getVector()));
        }
    }

    private DEIndividual getBest() {
        return Collections.max(this.population, Comparator.comparingDouble(DEIndividual::getFitness));
    }
}
