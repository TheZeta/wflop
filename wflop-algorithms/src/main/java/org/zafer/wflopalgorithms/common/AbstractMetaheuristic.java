package org.zafer.wflopalgorithms.common;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.zafer.wflopcore.power.PowerCalculator;
import org.zafer.wflopmetaheuristic.Metaheuristic;
import org.zafer.wflopmetaheuristic.ProgressEvent;
import org.zafer.wflopmetaheuristic.Solution;
import org.zafer.wflopmetaheuristic.listener.ProgressListener;
import org.zafer.wflopmetaheuristic.termination.TerminationCondition;
import org.zafer.wflopmodel.problem.WFLOP;

public abstract class AbstractMetaheuristic implements Metaheuristic {

    private final TerminationCondition terminationCondition;
    private final Random random;

    private WFLOP problem;
    private PowerCalculator powerCalculator;

    private double totalPowerWithoutWake;

    public AbstractMetaheuristic(TerminationCondition terminationCondition) {
        this.terminationCondition = terminationCondition;
        this.random = new Random();
    }

    public final void setSeed(long seed) {
        this.random.setSeed(seed);
    }

    @Override
    public final Solution run(WFLOP problem) {
        return runInternal(problem, Collections.emptyList());
    }

    @Override
    public final Solution runWithListeners(WFLOP problem, List<ProgressListener> listeners) {
        return runInternal(problem, listeners);
    }

    private Solution runInternal(WFLOP problem, List<ProgressListener> listeners) {
        this.terminationCondition.onStart();
        this.problem = problem;
        this.powerCalculator = createPowerCalculator();
        this.totalPowerWithoutWake = this.powerCalculator.
            calculateTotalPowerWithoutWake(getProblem().getNumberOfTurbines());

        init();

        while (!this.terminationCondition.shouldTerminate()) {
            step();
            notifyListeners(listeners);
            this.terminationCondition.onGeneration();
        }

        return getBestSolution();
    }

    private void notifyListeners(List<ProgressListener> listeners) {
        if (!listeners.isEmpty()) {
            ProgressEvent event = new ProgressEvent(
                getBestSolution().getFitness(),
                this.totalPowerWithoutWake,
                this.terminationCondition.getTerminationProgress()
            );

            for (ProgressListener listener : listeners) {
                listener.onIteration(event);
            }
        }
    }

    protected abstract PowerCalculator createPowerCalculator();
    protected abstract void init();
    protected abstract void step();
    protected abstract Solution getBestSolution();

    protected Random getRandom() { return this.random; }
    protected WFLOP getProblem() { return this.problem; }
    protected PowerCalculator getPowerCalculator() { return this.powerCalculator; }
    protected double getProgress() { return this.terminationCondition.getProgress(); }
}
