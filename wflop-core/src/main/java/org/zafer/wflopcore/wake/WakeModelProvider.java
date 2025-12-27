package org.zafer.wflopcore.wake;

import org.zafer.wflopmodel.problem.WFLOP;

public interface WakeModelProvider {

    WakeModel create(WFLOP wflop, WakeOptimization optimization);
}
