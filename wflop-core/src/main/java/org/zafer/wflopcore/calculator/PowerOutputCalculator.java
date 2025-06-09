package org.zafer.wflopcore.calculator;

import org.zafer.wflopcore.model.DefaultPowerModelProvider;
import org.zafer.wflopcore.model.PowerModel;
import org.zafer.wflopcore.model.PowerModelProvider;
import org.zafer.wflopmodel.problem.WFLOP;
import org.zafer.wflopmodel.layout.TurbineLayout;
import org.zafer.wflopmodel.wind.WindProfile;

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
     * @param turbineLayout
     * @return totalPower: The total power output of the layout under the given wind conditions.
     */
    public double calculateTotalPowerOutput(TurbineLayout turbineLayout) {
        double totalPower = 0;
        List<Integer> turbineIndices = turbineLayout.getTurbineIndices();
        WindProfile[] windProfiles = wflop.getWindProfiles();

        for (WindProfile windProfile : windProfiles) {
            for (int downwindTurbineIndex : turbineIndices) {
                double downwindTurbineSpeed = wakeCalculatorJensen.calculateReducedSpeedMultiple(
                        windProfile,
                        downwindTurbineIndex,
                        turbineIndices);

                totalPower += powerModel.getPowerOutput(downwindTurbineSpeed);
            }
        }
        return totalPower;
    }
}
