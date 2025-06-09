package org.zafer.wflopcore.calculator;

import org.zafer.wflopmodel.problem.WFLOP;

public class ConfigurableWakeCalculatorProvider implements WakeCalculatorProvider {

    private final WakeCalculationPolicy policy;

    public ConfigurableWakeCalculatorProvider(WakeCalculationPolicy policy) {
        this.policy = policy;
    }

    @Override
    public WakeCalculatorJensen create(WFLOP wflop) {
        return new WakeCalculatorJensen(wflop, policy);
    }
}
