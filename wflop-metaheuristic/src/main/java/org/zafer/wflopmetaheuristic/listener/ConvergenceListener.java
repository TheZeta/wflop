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
        double totalPowerWithoutWake
    ) {}

    private final List<DataPoint> data = new ArrayList<>();
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

        if (event.getBestFitness() > bestFitness) {
            bestFitness = event.getBestFitness();
            bestFitnessAchievedAtIteration = event.getIteration();
            bestFitnessAchievedAtTime = elapsed;
        }

        data.add(
            new DataPoint(
                event.getIteration(),
                elapsed,
                event.getBestFitness(),
                bestFitnessAchievedAtIteration,
                bestFitnessAchievedAtTime,
                event.getTotalPowerWithoutWake()
            )
        );
    }

    public List<DataPoint> getData() {
        return List.copyOf(data);
    }
}
