package org.zafer.wflopcore.wake;

import org.zafer.wflopmodel.problem.WFLOP;

public class NoMatricesWakeCalculatorProvider implements WakeCalculatorProvider {

    @Override
    public JensenWakeCalculator create(WFLOP wflop) {
        WakeCalculationPolicy policy = new WakeCalculationPolicy(false, false);
        return new JensenWakeCalculator(wflop, policy);
    }
}