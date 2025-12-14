package org.zafer.wflopcore.wake;

import java.util.List;

import org.zafer.wflopmodel.wind.WindProfile;

public interface WakeCalculator {

    double calculateEffectiveSpeed(int turbine, List<Integer> turbines, WindProfile windProfile);
}
