package org.zafer.wflopmetaheuristic.listener;

import org.zafer.wflopmetaheuristic.ProgressEvent;

import java.util.ArrayList;
import java.util.List;

public class ConvergenceListener implements ProgressListener {

    public record DataPoint(
        int iteration,
        double elapsedTimeSeconds,
        double bestFitness,
        double bestFitnessAchievedAtIteration,
        double bestFitnessAchievedAtTime,
        double totalPowerWithoutWake,
        double matrixInitTime
    ) {}

    private final List<DataPoint> data = new ArrayList<>();
    private int iteration = 0;
    private long startTimeMs = -1;
    private double bestFitness = Double.NEGATIVE_INFINITY;
    private double bestFitnessAchievedAtIteration;
    private double bestFitnessAchievedAtTime;

    @Override
    public void onIteration(ProgressEvent event) {
        if (startTimeMs < 0) {
            startTimeMs = System.currentTimeMillis();
        }

        double elapsed = (System.currentTimeMillis() - startTimeMs) / 1000.0;

        iteration++;

        if (event.getBestFitness() > bestFitness) {
            bestFitness = event.getBestFitness();
            bestFitnessAchievedAtIteration = iteration;
            bestFitnessAchievedAtTime = elapsed;
        }

        data.add(
            new DataPoint(
                iteration,
                elapsed,
                event.getBestFitness(),
                bestFitnessAchievedAtIteration,
                bestFitnessAchievedAtTime,
                event.getTotalPowerWithoutWake(),
                event.getMatrixInitTime()
            )
        );
    }

    public List<DataPoint> getData() {
        return List.copyOf(data);
    }
}
