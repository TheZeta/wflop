package org.zafer.wflopalgorithms.algorithms.lshade;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.zafer.wflopalgorithms.algorithms.lshade.solution.LSHADEIndividual;
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

public class LSHADE implements Metaheuristic {

    private final String algorithm;
    private final int maxPopulationSize;
    private final int minPopulationSize = 4;
    private final int H = 5; // memory size

    private final TerminationCondition terminationCondition;
    private final Random random;

    private double[] MF;
    private double[] MCR;
    private int memoryIndex = 0;

    @JsonCreator
    public LSHADE(
        @JsonProperty("algorithm") String algorithm,
        @JsonProperty("populationSize") int populationSize,
        @JsonProperty("termination") TerminationConditionConfig terminationConfig
    ) {
        this.algorithm = algorithm;
        this.maxPopulationSize = populationSize;
        this.terminationCondition = TerminationConditionFactory.fromConfig(terminationConfig);
        this.random = new Random();

        MF = new double[H];
        MCR = new double[H];
        Arrays.fill(MF, 0.5);
        Arrays.fill(MCR, 0.5);
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

        List<LSHADEIndividual> population = initializePopulation(problem);
    evaluatePopulation(population, problem, calculator);

        LSHADEIndividual best = getBest(population);

        int gen = 0;
        while (!terminationCondition.shouldTerminate()) {

            int targetSize = computePopulationSize();
            List<LSHADEIndividual> nextPop = new ArrayList<>();

            List<Double> successfulF = new ArrayList<>();
            List<Double> successfulCR = new ArrayList<>();
            List<Double> fitnessGains = new ArrayList<>();

            for (int i = 0; i < population.size(); i++) {
                LSHADEIndividual xi = population.get(i);
                sampleParameters(xi);

                double[] trial = generateTrial(population, xi);
                enforceBounds(trial, problem.getCellCount());

                double trialFitness = evaluate(trial, problem, calculator);

                if (trialFitness > xi.getFitness()) {
                    LSHADEIndividual child = new LSHADEIndividual(trial);
                    child.setFitness(trialFitness);
                    nextPop.add(child);

                    successfulF.add(xi.getF());
                    successfulCR.add(xi.getCR());
                    fitnessGains.add(trialFitness - xi.getFitness());
                } else {
                    nextPop.add(xi);
                }
            }

            updateMemories(successfulF, successfulCR, fitnessGains);

            population = reducePopulation(nextPop, targetSize);
            best = getBest(population);

            terminationCondition.onGeneration(++gen);
            if (!listeners.isEmpty()) {
                double avg = population
                    .stream()
                    .mapToDouble(LSHADEIndividual::getFitness)
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

    private List<LSHADEIndividual> initializePopulation(WFLOP problem) {
        List<LSHADEIndividual> population = new ArrayList<>();

        int turbines = problem.getNumberOfTurbines();
        int cellCount = problem.getCellCount();

        for (int i = 0; i < maxPopulationSize; i++) {
            double[] vector = new double[turbines];
            for (int d = 0; d < turbines; d++) {
                vector[d] = random.nextDouble() * cellCount;
            }
            population.add(new LSHADEIndividual(vector));
        }

        return population;
    }

    private void sampleParameters(LSHADEIndividual ind) {
        int r = random.nextInt(H);

        /* === Scale factor F: Cauchy distribution === */
        double F;
        do {
            F = MF[r] + 0.1 * Math.tan(Math.PI * (random.nextDouble() - 0.5));
        } while (F <= 0.0);
        F = Math.min(F, 1.0);

        /* === Crossover rate CR: Normal distribution === */
        double CR = MCR[r] + 0.1 * random.nextGaussian();
        CR = Math.max(0.0, Math.min(1.0, CR));

        ind.setF(F);
        ind.setCR(CR);
    }


    private double[] generateTrial(
        List<LSHADEIndividual> pop,
        LSHADEIndividual xi
    ) {
        int p = Math.max(2, (int) (0.2 * pop.size()));
        List<LSHADEIndividual> sorted =
            pop.stream()
                .sorted(Comparator.comparingDouble(LSHADEIndividual::getFitness).reversed())
                .toList();

        LSHADEIndividual pbest = sorted.get(random.nextInt(p));

        LSHADEIndividual r1, r2;
        do {
            r1 = pop.get(random.nextInt(pop.size()));
        } while (r1 == xi);
        do {
            r2 = pop.get(random.nextInt(pop.size()));
        } while (r2 == xi || r2 == r1);

        double[] trial = new double[xi.getVector().length];
        int jRand = random.nextInt(trial.length);

        for (int j = 0; j < trial.length; j++) {
            if (random.nextDouble() < xi.getCR() || j == jRand) {
                trial[j] =
                    xi.getVector()[j]
                        + xi.getF() * (pbest.getVector()[j] - xi.getVector()[j])
                        + xi.getF() * (r1.getVector()[j] - r2.getVector()[j]);
            } else {
                trial[j] = xi.getVector()[j];
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

    private void updateMemories(
        List<Double> SF,
        List<Double> SCR,
        List<Double> gains
    ) {
        if (SF.isEmpty()) return;

        double sumGain = gains.stream().mapToDouble(Double::doubleValue).sum();

        double meanF = 0;
        double meanCR = 0;

        for (int i = 0; i < SF.size(); i++) {
            double w = gains.get(i) / sumGain;
            meanF += w * SF.get(i) * SF.get(i);
            meanCR += w * SCR.get(i);
        }

        MF[memoryIndex] = meanF / SF.stream().mapToDouble(Double::doubleValue).sum();
        MCR[memoryIndex] = meanCR;

        memoryIndex = (memoryIndex + 1) % H;
    }

    private int computePopulationSize() {
        double progress = terminationCondition.getProgress().getProgress();
        // progress ∈ [0, 1]

        int size = (int) Math.floor(
            minPopulationSize
            + (maxPopulationSize - minPopulationSize) * (1.0 - progress)
        );

        return Math.max(minPopulationSize, size);
    }

    private List<LSHADEIndividual> reducePopulation(
        List<LSHADEIndividual> pop,
        int targetSize
    ) {
        return pop.stream()
            .sorted(Comparator.comparingDouble(LSHADEIndividual::getFitness).reversed())
            .limit(targetSize)
            .toList();
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
        List<LSHADEIndividual> pop,
        WFLOP problem,
        PowerCalculator calculator
    ) {
        for (LSHADEIndividual ind : pop) {
            ind.setFitness(evaluate(ind.getVector(), problem, calculator));
        }
    }

    private LSHADEIndividual getBest(List<LSHADEIndividual> pop) {
        return Collections.max(pop, Comparator.comparingDouble(LSHADEIndividual::getFitness));
    }
}
