package org.zafer.wflopalgorithms.algorithms.sa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Random;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

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

public class SA implements Metaheuristic {

    private final String algorithm;
    private final double initialTemperature;
    private final double coolingRate;
    private final int innerIterations;

    private final TerminationCondition terminationCondition;
    private final Random random;

    @JsonCreator
    public SA(
            @JsonProperty("algorithm") String algorithm,
            @JsonProperty("initialTemperature") double initialTemperature,
            @JsonProperty("coolingRate") double coolingRate,
            @JsonProperty("innerIterations") int innerIterations,
            @JsonProperty("termination") TerminationConditionConfig terminationConfig
    ) {
        this.algorithm = algorithm;
        this.initialTemperature = initialTemperature;
        this.coolingRate = coolingRate;
        this.innerIterations = innerIterations;
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
    public Solution runWithListeners(WFLOP problem, List<ProgressListener> listeners) {
        PowerCalculator calculator = new PowerCalculator(problem);
        return runInternal(problem, calculator, listeners);
    }

    private Solution runInternal(
        WFLOP problem,
        PowerCalculator calculator,
        List<ProgressListener> listeners
    ) {
        terminationCondition.onStart();

        AnnealingState current = randomInitialState(problem, calculator);
        AnnealingState best = current;

        double temperature = initialTemperature;

        double totalPowerWithoutWake = calculator.calculateTotalPowerWithoutWake(
            problem.getNumberOfTurbines()
        );
        int gen = 0;
        while (!terminationCondition.shouldTerminate()) {
            for (int i = 0; i < innerIterations; i++) {
                AnnealingState neighbor = generateNeighbor(current, problem, calculator);
                double delta = neighbor.getFitness() - current.getFitness();

                if (delta > 0 || random.nextDouble() < acceptanceProbability(delta, temperature)) {
                    current = neighbor;
                }

                if (current.getFitness() > best.getFitness()) {
                    best = current;
                }
            }

            temperature *= coolingRate;
            terminationCondition.onGeneration(++gen);

            if (!listeners.isEmpty()) {
                TerminationProgress tp = terminationCondition.getProgress();
                ProgressEvent event =
                    new ProgressEvent(
                        gen,
                        best.getFitness(),
                        current.getFitness(),
                        totalPowerWithoutWake,
                        tp
                    );

                for (ProgressListener l : listeners) {
                    l.onIteration(event);
                }
            }
        }

        return best;
    }

    private AnnealingState randomInitialState(WFLOP problem, PowerCalculator calculator) {
        int cells = problem.getCellCount();
        int turbines = problem.getNumberOfTurbines();

        Set<Integer> indices = new LinkedHashSet<>();
        while (indices.size() < turbines) {
            indices.add(random.nextInt(cells));
        }

        AnnealingState state = new AnnealingState(new ArrayList<>(indices));

        evaluate(state, calculator);
        return state;
    }

    private AnnealingState generateNeighbor(
        AnnealingState current,
        WFLOP problem,
        PowerCalculator calculator
    ) {
        List<Integer> newLayout = new ArrayList<>(current.getLayout());
        int cells = problem.getCellCount();
        int pos = random.nextInt(newLayout.size());
        int newCell = random.nextInt(cells);
        newLayout.set(pos, newCell);

        // repair duplicates
        Set<Integer> unique = new LinkedHashSet<>(newLayout);
        while (unique.size() < newLayout.size()) {
            unique.add(random.nextInt(cells));
        }

        AnnealingState neighbor = new AnnealingState(new ArrayList<>(unique));

        evaluate(neighbor, calculator);
        return neighbor;
    }

    private void evaluate(AnnealingState state, PowerCalculator calculator) {
        TurbineLayout layout = new TurbineLayout(state.getLayout());
        state.setFitness(calculator.calculateTotalPower(layout));
    }

    private double acceptanceProbability(double delta, double temperature) {
        return Math.exp(delta / temperature);
    }
}
