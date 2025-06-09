package org.zafer.wflopcore.calculator;

import org.zafer.wflopmodel.problem.WFLOP;

public interface WakeCalculatorProvider {
    WakeCalculatorJensen create(WFLOP wflop);
}
