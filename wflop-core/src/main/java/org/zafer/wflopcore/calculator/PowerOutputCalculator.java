package org.zafer.wflopcore.calculator;

import org.zafer.wflopcore.model.DefaultPowerModelProvider;
import org.zafer.wflopcore.model.PowerModel;
import org.zafer.wflopcore.model.PowerModelProvider;
import org.zafer.wflopmodel.problem.WFLOP;
import org.zafer.wflopmodel.layout.TurbineLayout;

import java.util.List;

public class PowerOutputCalculator {

    private final WakeCalculatorJensen wakeCalculatorJensen;
    private final PowerModel powerModel;
    private final WFLOP wflop;

    public PowerOutputCalculator(WFLOP wflop) {
        this(wflop, new DefaultPowerModelProvider(), new DefaultWakeCalculatorProvider());
    }

    public PowerOutputCalculator(
            WFLOP wflop,
            PowerModelProvider powerModelProvider,
            WakeCalculatorProvider calculatorProvider
    ) {
        this.wflop = wflop;
        this.powerModel = powerModelProvider.create();
        this.wakeCalculatorJensen = calculatorProvider.create(wflop);
    }

    /**
     * This is the objective function to maximize.
     *
     * @param turbineLayout the layout configuration containing the positions of wind turbines
     * @return totalPower: The total power output of the layout under the given wind conditions.
     */
    public double calculateTotalPowerOutput(TurbineLayout turbineLayout) {
        List<Integer> turbines = turbineLayout.getTurbineIndices();
        double totalPower = 0;
        for (int turbine : turbines) {
            totalPower += calculatePowerOutput(turbine, turbines);
        }
        return totalPower;
    }

    public double calculatePowerOutput(int turbine, List<Integer> turbines) {
        int profileCount = wakeCalculatorJensen.getProfileCount();
        double power = 0.0;
        for (int profileIndex = 0; profileIndex < profileCount; profileIndex++) {
            double turbineSpeed = wakeCalculatorJensen.calculateReducedSpeedMultipleByProfileIndex(
                profileIndex,
                turbine,
                turbines
            );
            power += powerModel.getPowerOutput(turbineSpeed);
        }
        return power;
    }
}
