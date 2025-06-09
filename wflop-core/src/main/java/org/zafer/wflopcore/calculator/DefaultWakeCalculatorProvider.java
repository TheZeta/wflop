package org.zafer.wflopcore.calculator;

import org.zafer.wflopmodel.problem.WFLOP;

public class DefaultWakeCalculatorProvider implements WakeCalculatorProvider {
    @Override
    public WakeCalculatorJensen create(WFLOP wflop) {
        WakeCalculationPolicy defaultPolicy = new WakeCalculationPolicy(true, true);
        return new WakeCalculatorJensen(wflop, defaultPolicy);
    }
}
