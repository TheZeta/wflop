package org.zafer.wflopalgorithms.algorithms.fode;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.zafer.wflopalgorithms.algorithms.fode.solution.FODEIndividual;
import org.zafer.wflopcore.power.PowerCalculator;
import org.zafer.wflopmetaheuristic.ProgressEvent;
import org.zafer.wflopmetaheuristic.listener.ProgressListener;
import org.zafer.wflopmetaheuristic.Metaheuristic;
import org.zafer.wflopmetaheuristic.Solution;
import org.zafer.wflopmetaheuristic.termination.TerminationCondition;
import org.zafer.wflopmetaheuristic.termination.TerminationConditionConfig;
import org.zafer.wflopmetaheuristic.termination.TerminationConditionFactory;
import org.zafer.wflopmetaheuristic.termination.TerminationProgress;
import org.zafer.wflopmodel.layout.TurbineLayout;
import org.zafer.wflopmodel.problem.WFLOP;

/**
 * Fractional-Order Differential Evolution (FODE)
 * Based on LSHADE with fractional-order difference mutation.
 */
public class FODE implements Metaheuristic {

    private final String algorithm;
    private final int maxPopulationSize;
    private final int minPopulationSize = 4;

    /* === LSHADE memory === */
    private final int H = 5;
    private double[] MF;
    private double[] MCR;
    private int memoryIndex = 0;

    /* === FODE parameters === */
    private final double fractionalOrder = 0.8; // a
    private final int historyDepth = 5;          // m

    /* === Difference history === */
    private final Deque<double[]> pbestDiffHistory = new ArrayDeque<>();
    private final Deque<double[]> randDiffHistory  = new ArrayDeque<>();

    private final TerminationCondition terminationCondition;
    private final Random random;

    @JsonCreator
    public FODE(
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
        return runInternal(problem, new PowerCalculator(problem), Collections.emptyList());
    }

    @Override
    public Solution runWithListeners(
        WFLOP problem,
        List<ProgressListener> listeners
    ) {
        return runInternal(problem, new PowerCalculator(problem), listeners);
    }

    private Solution runInternal(
        WFLOP problem,
        PowerCalculator calculator,
        List<ProgressListener> listeners
    ) {
        terminationCondition.onStart();

        List<FODEIndividual> population = initializePopulation(problem);
        evaluatePopulation(population, problem, calculator);

        FODEIndividual best = getBest(population);
        int gen = 0;

        while (!terminationCondition.shouldTerminate()) {
            int targetSize = computePopulationSize();
            List<FODEIndividual> nextPop = new ArrayList<>();

            List<Double> successfulF = new ArrayList<>();
            List<Double> successfulCR = new ArrayList<>();
            List<Double> fitnessGains = new ArrayList<>();

            for (FODEIndividual xi : population) {

                sampleParameters(xi);
                double[] trial = generateFractionalTrial(population, xi);

                enforceBounds(trial, problem.getCellCount());
                double trialFitness = evaluate(trial, problem, calculator);

                if (trialFitness > xi.getFitness()) {
                    FODEIndividual child = new FODEIndividual(trial);
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
                    .mapToDouble(FODEIndividual::getFitness)
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

    private List<FODEIndividual> initializePopulation(WFLOP problem) {
        List<FODEIndividual> population = new ArrayList<>();

        int turbines = problem.getNumberOfTurbines();
        int cellCount = problem.getCellCount();

        for (int i = 0; i < maxPopulationSize; i++) {
            double[] vector = new double[turbines];
            for (int d = 0; d < turbines; d++) {
                vector[d] = random.nextDouble() * cellCount;
            }
            population.add(new FODEIndividual(vector));
        }

        return population;
    }

    /* ============================================================
       Fractional-Order Mutation
       ============================================================ */

    private double[] generateFractionalTrial(
        List<FODEIndividual> pop,
        FODEIndividual xi
    ) {
        int p = Math.max(2, (int) (0.11 * pop.size()));

        List<FODEIndividual> sorted = pop.stream()
            .sorted(Comparator.comparingDouble(FODEIndividual::getFitness).reversed())
            .toList();

        FODEIndividual pbest = sorted.get(random.nextInt(p));

        FODEIndividual r1, r2;
        do { r1 = pop.get(random.nextInt(pop.size())); } while (r1 == xi);
        do { r2 = pop.get(random.nextInt(pop.size())); } while (r2 == xi || r2 == r1);

        double[] dpbest = subtract(pbest.getVector(), xi.getVector());
        double[] drand  = subtract(r1.getVector(), r2.getVector());

        pushHistory(pbestDiffHistory, dpbest);
        pushHistory(randDiffHistory, drand);

        double[] fracPbest = fractionalDifference(pbestDiffHistory);
        double[] fracRand  = fractionalDifference(randDiffHistory);

        double[] trial = new double[xi.getVector().length];
        int jRand = random.nextInt(trial.length);

        for (int j = 0; j < trial.length; j++) {
            if (random.nextDouble() < xi.getCR() || j == jRand) {
                trial[j] = xi.getVector()[j] + xi.getF() * (fracPbest[j] + fracRand[j]);
            } else {
                trial[j] = xi.getVector()[j];
            }
        }

        return trial;
    }

    private double[] fractionalDifference(Deque<double[]> history) {
        int dim = history.peekLast().length;
        double[] result = new double[dim];

        // Use descending iterator to ensure j=0 is the NEWEST difference
        Iterator<double[]> it = history.descendingIterator();
        int j = 0;
        while (it.hasNext()) {
            double[] h = it.next();
            double coeff = fractionalCoeff(0.8, j); // fractionalOrder = 0.8
            for (int d = 0; d < dim; d++) {
                result[d] += coeff * h[d];
            }
            j++;
        }
        return result;
    }

//    private double[] fractionalDifference(Deque<double[][]> history) {
//        int dim = history.peekLast()[0].length;
//        double[] result = new double[dim];
//
//        int j = 0;
//        for (double[][] h : history) {
//            double coeff = fractionalCoeff(fractionalOrder, j);
//            for (int d = 0; d < dim; d++) {
//                result[d] += coeff * h[0][d];
//            }
//            j++;
//        }
//
//        return result;
//    }
//
//    private double[] fractionalDifference(Deque<double[][]> history) {
//        int dim = history.peekLast().length;
//        double[] result = new double[dim];
//
//        // Use descending iterator to ensure j=0 is the NEWEST difference
//        Iterator<double[][]> it = history.descendingIterator();
//        int j = 0;
//        while (it.hasNext()) {
//            double[][] h = it.next();
//            double coeff = fractionalCoeff(0.8, j); // fractionalOrder = 0.8
//            for (int d = 0; d < dim; d++) {
//                result[d] += coeff * h[d];
//            }
//            j++;
//        }
//        return result;
//    }

    private double fractionalCoeff(double a, int j) {
        if (j == 0) return a;
        double num = 1.0;
        for (int i = 0; i < j; i++) {
            num *= (a - i);
        }
        return num / factorial(j);
    }

    private double factorial(int n) {
        double f = 1.0;
        for (int i = 2; i <= n; i++) f *= i;
        return f;
    }

    private void pushHistory(Deque<double[]> history, double[] diff) {
        history.addLast(diff.clone());
        if (history.size() > historyDepth) {
            history.removeFirst();
        }
    }

    /* ============================================================
       Utility / duplicated LSHADE code
       ============================================================ */

    private double[] subtract(double[] a, double[] b) {
        double[] r = new double[a.length];
        for (int i = 0; i < a.length; i++) r[i] = a[i] - b[i];
        return r;
    }

    private void sampleParameters(FODEIndividual ind) {
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
        double meanF = 0, meanCR = 0;

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

    private List<FODEIndividual> reducePopulation(
        List<FODEIndividual> pop,
        int targetSize
    ) {
        return pop.stream()
            .sorted(Comparator.comparingDouble(FODEIndividual::getFitness).reversed())
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
        List<FODEIndividual> pop,
        WFLOP problem,
        PowerCalculator calculator
    ) {
        for (FODEIndividual ind : pop) {
            ind.setFitness(evaluate(ind.getVector(), problem, calculator));
        }
    }

    private FODEIndividual getBest(List<FODEIndividual> pop) {
        return Collections.max(pop, Comparator.comparingDouble(FODEIndividual::getFitness));
    }
}
