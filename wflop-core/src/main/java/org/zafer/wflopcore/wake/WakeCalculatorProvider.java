package org.zafer.wflopcore.wake;

import org.zafer.wflopmodel.problem.WFLOP;

public interface WakeCalculatorProvider {

    WakeCalculator create(WFLOP wflop, WakeCalculationPolicy policy);
}
