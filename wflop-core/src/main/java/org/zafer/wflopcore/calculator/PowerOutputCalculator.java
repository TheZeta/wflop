package org.zafer.wflopcore.calculator;

import org.zafer.wflopcore.model.PowerModel;
import org.zafer.wflopmodel.solution.Solution;
import org.zafer.wflopmodel.wind.WindProfile;

public class PowerOutputCalculator {

    private WakeCalculatorJensen wakeCalculatorJensen;
    private PowerModel powerModel;

    public PowerOutputCalculator(WakeCalculatorJensen wakeCalculatorJensen, PowerModel powerModel) {
        this.wakeCalculatorJensen = wakeCalculatorJensen;
        this.powerModel = powerModel;
    }

    /**
     * This is the objective function to maximize.
     *
     * @param solution
     * @param windProfiles
     * @return totalPower: The total power output of the layout under the given wind conditions.
     */
    public double calculateTotalPowerOutput(Solution solution, WindProfile[] windProfiles) {
        double totalPower = 0;
        int[] turbineIndices = solution.getTurbineIndices();
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
