package org.zafer.wflopcore.power;

import org.zafer.wflopcore.wake.DefaultWakeModelProvider;
import org.zafer.wflopcore.wake.WakeModelPolicy;
import org.zafer.wflopcore.wake.WakeModel;
import org.zafer.wflopcore.wake.WakeModelProvider;
import org.zafer.wflopmodel.problem.WFLOP;
import org.zafer.wflopmodel.layout.TurbineLayout;
import org.zafer.wflopmodel.wind.WindProfile;

import java.util.List;

public class PowerOutputCalculator {

    private final WakeModel wakeModel;
    private final PowerModel powerModel;
    private final WFLOP wflop;

    public PowerOutputCalculator(WFLOP wflop) {
        this(
                wflop,
                new DefaultWakeModelProvider(),
                new WakeModelPolicy(true, true),
                new DefaultPowerModelProvider()
        );
    }

    public PowerOutputCalculator(
            WFLOP wflop,
            WakeModelProvider wakeModelProvider
    ) {
        this(
                wflop,
                wakeModelProvider,
                new WakeModelPolicy(true, true),
                new DefaultPowerModelProvider()
        );
    }

    public PowerOutputCalculator(
            WFLOP wflop,
            WakeModelProvider wakeModelProvider,
            WakeModelPolicy policy
    ) {
        this(
                wflop,
                wakeModelProvider,
                policy,
                new DefaultPowerModelProvider()
        );
    }

    public PowerOutputCalculator(
            WFLOP wflop,
            WakeModelProvider wakeModelProvider,
            WakeModelPolicy policy,
            PowerModelProvider powerModelProvider
    ) {
        this.wflop = wflop;
        this.wakeModel = wakeModelProvider.create(wflop, policy);
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
            double turbineSpeed = wakeModel.calculateEffectiveSpeed(
                turbine,
                turbines,
                windProfile
            );
            power += powerModel.getPowerOutput(turbineSpeed);
        }
        return power;
    }
}
