package org.zafer.wflopmetaheuristic.listener.registry;

import org.zafer.wflopmetaheuristic.listener.ConvergenceListener;
import org.zafer.wflopmetaheuristic.listener.ProgressBarListener;
import org.zafer.wflopmetaheuristic.listener.ProgressListener;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class ListenerRegistry {

    private static final Map<String, Supplier<ProgressListener>> REGISTRY = new HashMap<>();

    static {
        REGISTRY.put("convergence", ConvergenceListener::new);
        REGISTRY.put("progressBar", ProgressBarListener::new);
    }

    public static ProgressListener create(String id) {
        Supplier<ProgressListener> supplier = REGISTRY.get(id);
        if (supplier == null) {
            throw new IllegalArgumentException("Unknown listener: " + id);
        }
        return supplier.get();
    }
}
