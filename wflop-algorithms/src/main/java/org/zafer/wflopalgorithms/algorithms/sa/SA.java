package org.zafer.wflopalgorithms.algorithms.sa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Random;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.zafer.wflopalgorithms.common.AbstractMetaheuristic;
import org.zafer.wflopcore.power.PowerCalculator;
import org.zafer.wflopcore.wake.DefaultWakeModelProvider;
import org.zafer.wflopcore.wake.WakeOptimization;
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

public class SA extends AbstractMetaheuristic {

    private final double initialTemperature;
    private final double coolingRate;
    private final int innerIterations;

    private double temperature;
    private AnnealingState currentSolution;
    private AnnealingState bestSolution;

    @JsonCreator
    public SA(
        @JsonProperty("algorithm") String algorithm,
        @JsonProperty("initialTemperature") double initialTemperature,
        @JsonProperty("coolingRate") double coolingRate,
        @JsonProperty("innerIterations") int innerIterations,
        @JsonProperty("termination") TerminationConditionConfig terminationConfig
    ) {
        super(TerminationConditionFactory.fromConfig(terminationConfig));

        this.initialTemperature = initialTemperature;
        this.coolingRate = coolingRate;
        this.innerIterations = innerIterations;
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
        this.currentSolution = randomInitialState();
        this.bestSolution = this.currentSolution;
        this.temperature = this.initialTemperature;
    }

    @Override
    protected void step() {
        for (int i = 0; i < this.innerIterations; i++) {
            AnnealingState neighbor = generateNeighbor();
            double delta = neighbor.getFitness() - this.currentSolution.getFitness();

            if (delta > 0 || getRandom().nextDouble() < acceptanceProbability(delta, this.temperature)) {
                this.currentSolution = neighbor;
            }

            if (this.currentSolution.getFitness() > this.bestSolution.getFitness()) {
                this.bestSolution = this.currentSolution;
            }
        }

        this.temperature *= this.coolingRate;
    }

    @Override
    protected Solution getBestSolution() {
        return this.bestSolution;
    }

    private AnnealingState randomInitialState() {
        int cells = getProblem().getCellCount();
        int turbines = getProblem().getNumberOfTurbines();

        Set<Integer> indices = new LinkedHashSet<>();
        while (indices.size() < turbines) {
            indices.add(getRandom().nextInt(cells));
        }

        AnnealingState state = new AnnealingState(new ArrayList<>(indices));

        evaluate(state);
        return state;
    }

    private AnnealingState generateNeighbor() {
        List<Integer> newLayout = new ArrayList<>(this.currentSolution.getLayout());
        int cells = getProblem().getCellCount();
        int pos = getRandom().nextInt(newLayout.size());
        int newCell = getRandom().nextInt(cells);
        newLayout.set(pos, newCell);

        // repair duplicates
        Set<Integer> unique = new LinkedHashSet<>(newLayout);
        while (unique.size() < newLayout.size()) {
            unique.add(getRandom().nextInt(cells));
        }

        AnnealingState neighbor = new AnnealingState(new ArrayList<>(unique));

        evaluate(neighbor);
        return neighbor;
    }

    private void evaluate(AnnealingState state) {
        TurbineLayout layout = new TurbineLayout(state.getLayout());
        state.setFitness(getPowerCalculator().calculateTotalPower(layout));
    }

    private double acceptanceProbability(double delta, double temperature) {
        return Math.exp(delta / temperature);
    }
}
