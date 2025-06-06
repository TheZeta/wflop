package org.zafer.wflopcore.calculator;

import org.zafer.wflopcore.model.PowerModel;
import org.zafer.wflopmodel.problem.WFLOP;
import org.zafer.wflopmodel.solution.Solution;
import org.zafer.wflopmodel.wind.WindProfile;

public class PowerOutputCalculator {

    private final WakeCalculatorJensen wakeCalculatorJensen;
    private final PowerModel powerModel;
    private final WFLOP wflop;

    public PowerOutputCalculator(WFLOP wflop, PowerModel powerModel) {
        this.wflop = wflop;
        this.wakeCalculatorJensen = new WakeCalculatorJensen(wflop);
        this.powerModel = powerModel;
    }

    /**
     * This is the objective function to maximize.
     *
     * @param solution
     * @return totalPower: The total power output of the layout under the given wind conditions.
     */
    public double calculateTotalPowerOutput(Solution solution) {
        double totalPower = 0;
        int[] turbineIndices = solution.getTurbineIndices();
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
