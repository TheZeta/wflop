package org.zafer.wflopcore.wake;

import org.zafer.wflopmodel.problem.WFLOP;

public class DefaultWakeModelProvider implements WakeModelProvider {

    @Override
    public WakeModel create(WFLOP wflop, WakeOptimization optimization) {
        return new JensenWakeModel(wflop, optimization);
    }
}
