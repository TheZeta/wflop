package org.zafer.wflopmetaheuristic.listener;

import org.zafer.wflopmetaheuristic.ProgressEvent;

import java.util.ArrayList;
import java.util.List;

public class ConvergenceListener implements ProgressListener {

    public static final class DataPoint {
        private final int iteration;
        private final double elapsedTimeSeconds;
        private final double bestFitness;

        public DataPoint(int iteration, double elapsedTimeSeconds, double bestFitness) {
            this.iteration = iteration;
            this.elapsedTimeSeconds = elapsedTimeSeconds;
            this.bestFitness = bestFitness;
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
    }

    private final List<DataPoint> data = new ArrayList<>();
    private long startTimeMs = -1;

    @Override
    public void onIteration(ProgressEvent event) {
        if (startTimeMs < 0) {
            startTimeMs = System.currentTimeMillis();
        }

        double elapsed = (System.currentTimeMillis() - startTimeMs) / 1000.0;

        data.add(new DataPoint(event.getIteration(),elapsed,event.getBestFitness()));
    }

    public List<DataPoint> getData() {
        return List.copyOf(data);
    }
}
