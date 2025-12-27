package org.zafer.wflopcore.wake;

import org.zafer.wflopmodel.problem.WFLOP;

public class DefaultWakeModelProvider implements WakeModelProvider {

    @Override
    public WakeModel create(WFLOP wflop, WakeModelPolicy policy) {
        return new JensenWakeModel(wflop, policy);
    }
}
