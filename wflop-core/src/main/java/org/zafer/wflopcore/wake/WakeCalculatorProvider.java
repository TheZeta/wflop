package org.zafer.wflopcore.wake;

import org.zafer.wflopmodel.problem.WFLOP;

public interface WakeCalculatorProvider {
    WakeCalculatorJensen create(WFLOP wflop);
}
