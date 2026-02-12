package org.zafer.wflopcore.power;

import org.zafer.wflopcore.wake.*;
import org.zafer.wflopmodel.problem.WFLOP;
import org.zafer.wflopmodel.layout.TurbineLayout;
import org.zafer.wflopmodel.wind.WindProfile;

import java.util.List;

public class PowerCalculator {

    private final WakeModel wakeModel;
    private final PowerModel powerModel;
    private final WFLOP wflop;

    public PowerCalculator(WFLOP wflop) {
        this(
                wflop,
                new DefaultWakeModelProvider(),
                WakeOptimization.BOTH,
                new DefaultPowerModelProvider()
        );
    }

    public PowerCalculator(
            WFLOP wflop,
            WakeModelProvider wakeModelProvider
    ) {
        this(
                wflop,
                wakeModelProvider,
                WakeOptimization.BOTH,
                new DefaultPowerModelProvider()
        );
    }

    public PowerCalculator(
            WFLOP wflop,
            WakeModelProvider wakeModelProvider,
            WakeOptimization optimization
    ) {
        this(
                wflop,
                wakeModelProvider,
                optimization,
                new DefaultPowerModelProvider()
        );
    }

    public PowerCalculator(
            WFLOP wflop,
            WakeModelProvider wakeModelProvider,
            WakeOptimization optimization,
            PowerModelProvider powerModelProvider
    ) {
        this.wflop = wflop;
        this.wakeModel = wakeModelProvider.create(wflop, optimization);
        this.powerModel = powerModelProvider.create();
    }

    /**
     * This is the objective function to maximize.
     *
     * @param turbineLayout the layout configuration containing the positions of wind turbines
     * @return totalPower: The total power output of the layout under the given wind conditions.
     */
    public double calculateTotalPower(TurbineLayout turbineLayout) {
        List<Integer> turbines = turbineLayout.getTurbineIndices();
        double totalPower = 0;
        for (int turbine : turbines) {
            totalPower += calculatePower(turbine, turbines);
        }
        return totalPower;
    }

    public double calculatePower(int turbine, List<Integer> turbines) {
        List<WindProfile> windProfiles = wflop.getWindProfiles();
        double power = 0.0;
        for (WindProfile windProfile : windProfiles) {
            double turbineSpeed = wakeModel.calculateEffectiveSpeed(
                turbine,
                turbines,
                windProfile
            );
            power += windProfile.getProbability() * powerModel.getPowerOutput(turbineSpeed);
        }
        return power;
    }

    public double calculateTotalPowerWithoutWake(int turbineCount) {
        List<WindProfile> windProfiles = wflop.getWindProfiles();
        double power = 0.0;
        for (WindProfile windProfile : windProfiles) {
            double unreducedSpeed = windProfile.getSpeed();
            power += turbineCount
                * windProfile.getProbability()
                * powerModel.getPowerOutput(unreducedSpeed);
        }
        return power;
    }
}
