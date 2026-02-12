package org.zafer.wflopmetaheuristic.listener;

import org.zafer.wflopmetaheuristic.ProgressEvent;

import java.util.ArrayList;
import java.util.List;

public class ConvergenceListener implements ProgressListener {

    public static final class DataPoint {
        private final int iteration;
        private final double elapsedTimeSeconds;
        private final double bestFitness;
        private final double bestFitnessAchievedAt;
        private final double totalPowerWithoutWake;

        public DataPoint(
            int iteration,
            double elapsedTimeSeconds,
            double bestFitness,
            double bestFitnessAchievedAt,
            double totalPowerWithoutWake
        ) {
            this.iteration = iteration;
            this.elapsedTimeSeconds = elapsedTimeSeconds;
            this.bestFitness = bestFitness;
            this.bestFitnessAchievedAt = bestFitnessAchievedAt;
            this.totalPowerWithoutWake = totalPowerWithoutWake;
        }

        public int getIteration() {
            return iteration;
        }

        public double getElapsedTimeSeconds() {
            return elapsedTimeSeconds;
        }

        public double getBestFitness() {
            return bestFitness;
        }

        public double getBestFitnessAchievedAt() {
            return bestFitnessAchievedAt;
        }

        public double getTotalPowerWithoutWake() {
            return totalPowerWithoutWake;
        }
    }

    private final List<DataPoint> data = new ArrayList<>();
    private long startTimeMs = -1;
    private double bestFitness = Double.NEGATIVE_INFINITY;
    private double bestFitnessAchievedAt;

    @Override
    public void onIteration(ProgressEvent event) {
        if (startTimeMs < 0) {
            startTimeMs = System.currentTimeMillis();
        }

        double elapsed = (System.currentTimeMillis() - startTimeMs) / 1000.0;

        if (event.getBestFitness() > bestFitness) {
            bestFitness = event.getBestFitness();
            bestFitnessAchievedAt = event.getIteration();
        }

        data.add(
            new DataPoint(
                event.getIteration(),
                elapsed,
                event.getBestFitness(),
                bestFitnessAchievedAt,
                event.getTotalPowerWithoutWake()
            )
        );
    }

    public List<DataPoint> getData() {
        return List.copyOf(data);
    }
}
