package org.zafer.wflopcore.power;

import org.zafer.wflopcore.wake.DefaultWakeCalculatorProvider;
import org.zafer.wflopcore.wake.WakeCalculatorJensen;
import org.zafer.wflopcore.wake.WakeCalculatorProvider;
import org.zafer.wflopmodel.problem.WFLOP;
import org.zafer.wflopmodel.layout.TurbineLayout;
import org.zafer.wflopmodel.wind.WindProfile;

import java.util.List;

public class PowerOutputCalculator {

    private final WakeCalculatorJensen wakeCalculatorJensen;
    private final PowerModel powerModel;
    private final WFLOP wflop;

    public PowerOutputCalculator(WFLOP wflop) {
        this(wflop, new DefaultWakeCalculatorProvider(), new DefaultPowerModelProvider());
    }

    public PowerOutputCalculator(WFLOP wflop, WakeCalculatorProvider wakeCalculatorProvider) {
        this(wflop, wakeCalculatorProvider, new DefaultPowerModelProvider());
    }

    public PowerOutputCalculator(
            WFLOP wflop,
            WakeCalculatorProvider calculatorProvider,
            PowerModelProvider powerModelProvider
    ) {
        this.wflop = wflop;
        this.wakeCalculatorJensen = calculatorProvider.create(wflop);
        this.powerModel = powerModelProvider.create();
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
        List<WindProfile> windProfiles = wflop.getWindProfiles();
        double power = 0.0;
        for (WindProfile windProfile : windProfiles) {
            double turbineSpeed = wakeCalculatorJensen.calculateReducedSpeedMultiple(
                windProfile,
                turbine,
                turbines
            );
            power += powerModel.getPowerOutput(turbineSpeed);
        }
        return power;
    }

    public WakeCalculatorJensen getWakeCalculatorJensen() {
        return wakeCalculatorJensen;
    }
}
