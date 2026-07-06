package org.zafer.wflopalgorithms.algorithms.fode;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.zafer.wflopalgorithms.algorithms.fode.solution.FODEIndividual;
import org.zafer.wflopalgorithms.common.AbstractMetaheuristic;
import org.zafer.wflopcore.power.PowerCalculator;
import org.zafer.wflopcore.wake.DefaultWakeModelProvider;
import org.zafer.wflopcore.wake.WakeOptimization;
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
public class FODE extends AbstractMetaheuristic {

    private final int maxPopulationSize;
    private final int minPopulationSize = 4;

    /* === LSHADE memory === */
    private final int H = 5;
    private final double[] MF;
    private final double[] MCR;
    private int memoryIndex = 0;

    /* === FODE parameters === */
    private final double fractionalOrder = 0.8; // a
    private final int historyDepth = 5;          // m

    /* === Difference history === */
    private final Deque<double[]> pbestDiffHistory = new ArrayDeque<>();
    private final Deque<double[]> randDiffHistory  = new ArrayDeque<>();

    private List<FODEIndividual> population;
    private FODEIndividual bestIndividual;

    @JsonCreator
    public FODE(
        @JsonProperty("algorithm") String algorithm,
        @JsonProperty("populationSize") int populationSize,
        @JsonProperty("termination") TerminationConditionConfig terminationConfig
    ) {
        super(TerminationConditionFactory.fromConfig(terminationConfig));

        this.maxPopulationSize = populationSize;

        this.MF = new double[H];
        this.MCR = new double[H];
        Arrays.fill(this.MF, 0.5);
        Arrays.fill(this.MCR, 0.5);
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
        int targetSize = computePopulationSize();
        List<FODEIndividual> nextPop = new ArrayList<>();

        List<Double> successfulF = new ArrayList<>();
        List<Double> successfulCR = new ArrayList<>();
        List<Double> fitnessGains = new ArrayList<>();

        for (FODEIndividual xi : this.population) {
            sampleParameters(xi);
            double[] trial = generateFractionalTrial(xi);

            enforceBounds(trial);
            double trialFitness = evaluate(trial);

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

        this.population = reducePopulation(nextPop, targetSize);
        this.bestIndividual = getBest();
    }

    @Override
    protected Solution getBestSolution() {
        return this.bestIndividual;
    }

    private void initializePopulation() {
        this.population = new ArrayList<>();

        int turbines = getProblem().getNumberOfTurbines();
        int cellCount = getProblem().getCellCount();

        for (int i = 0; i < this.maxPopulationSize; i++) {
            double[] vector = new double[turbines];
            for (int d = 0; d < turbines; d++) {
                vector[d] = getRandom().nextDouble() * cellCount;
            }
            population.add(new FODEIndividual(vector));
        }
    }

    /* ============================================================
       Fractional-Order Mutation
       ============================================================ */

    private double[] generateFractionalTrial(FODEIndividual xi) {
        int p = Math.max(2, (int) (0.11 * this.population.size()));

        List<FODEIndividual> sorted = this.population.stream()
            .sorted(Comparator.comparingDouble(FODEIndividual::getFitness).reversed())
            .toList();

        FODEIndividual pbest = sorted.get(getRandom().nextInt(p));

        FODEIndividual r1, r2;
        do { r1 = this.population.get(getRandom().nextInt(this.population.size())); } while (r1 == xi);
        do { r2 = this.population.get(getRandom().nextInt(this.population.size())); } while (r2 == xi || r2 == r1);

        double[] dpbest = subtract(pbest.getVector(), xi.getVector());
        double[] drand  = subtract(r1.getVector(), r2.getVector());

        pushHistory(pbestDiffHistory, dpbest);
        pushHistory(randDiffHistory, drand);

        double[] fracPbest = fractionalDifference(pbestDiffHistory);
        double[] fracRand  = fractionalDifference(randDiffHistory);

        double[] trial = new double[xi.getVector().length];
        int jRand = getRandom().nextInt(trial.length);

        for (int j = 0; j < trial.length; j++) {
            if (getRandom().nextDouble() < xi.getCR() || j == jRand) {
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
        int r = getRandom().nextInt(H);

        /* === Scale factor F: Cauchy distribution === */
        double F;
        do {
            F = MF[r] + 0.1 * Math.tan(Math.PI * (getRandom().nextDouble() - 0.5));
        } while (F <= 0.0);
        F = Math.min(F, 1.0);

        /* === Crossover rate CR: Normal distribution === */
        double CR = MCR[r] + 0.1 * getRandom().nextGaussian();
        CR = Math.clamp(CR, 0.0, 1.0);

        ind.setF(F);
        ind.setCR(CR);
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
        // progress ∈ [0, 1]
        int size = (int) Math.floor(
            minPopulationSize
            + (maxPopulationSize - minPopulationSize) * (1.0 - getProgress())
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
        for (FODEIndividual ind : this.population) {
            ind.setFitness(evaluate(ind.getVector()));
        }
    }

    private FODEIndividual getBest() {
        return Collections.max(this.population, Comparator.comparingDouble(FODEIndividual::getFitness));
    }
}
