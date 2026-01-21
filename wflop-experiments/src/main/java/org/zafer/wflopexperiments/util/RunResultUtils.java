package org.zafer.wflopexperiments.util;

import org.zafer.wflopmetaheuristic.listener.ConvergenceListener;
import org.zafer.wflopexperiments.model.ListenerData;
import org.zafer.wflopexperiments.model.RunResult;

import java.util.List;

public final class RunResultUtils {

    public static ConvergenceListener getConvergenceListener(RunResult run) {
        return run.getListenerData().stream()
                .map(ListenerData::getPayload)
                .filter(ConvergenceListener.class::isInstance)
                .map(ConvergenceListener.class::cast)
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "ConvergenceListener not attached to run " +
                                        run.getRunIndex()
                        )
                );
    }

    public static double finalBestFitness(RunResult run) {
        List<ConvergenceListener.DataPoint> data =
                getConvergenceListener(run).getData();

        if (data.isEmpty()) {
            throw new IllegalStateException("No convergence data");
        }

        return data.get(data.size() - 1).getBestFitness();
    }
}
