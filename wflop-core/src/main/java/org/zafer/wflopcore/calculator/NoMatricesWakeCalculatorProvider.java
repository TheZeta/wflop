package org.zafer.wflopcore.calculator;

import org.zafer.wflopmodel.problem.WFLOP;

public class NoMatricesWakeCalculatorProvider implements WakeCalculatorProvider {

    @Override
    public WakeCalculatorJensen create(WFLOP wflop) {
        WakeCalculationPolicy policy = new WakeCalculationPolicy(false, false);
        return new WakeCalculatorJensen(wflop, policy);
    }
}