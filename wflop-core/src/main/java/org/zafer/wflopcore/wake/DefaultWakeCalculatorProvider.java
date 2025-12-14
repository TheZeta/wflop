package org.zafer.wflopcore.wake;

import org.zafer.wflopmodel.problem.WFLOP;

public class DefaultWakeCalculatorProvider implements WakeCalculatorProvider {

    @Override
    public WakeCalculator create(WFLOP wflop, WakeCalculationPolicy policy) {
        return new JensenWakeCalculator(wflop, policy);
    }
}
