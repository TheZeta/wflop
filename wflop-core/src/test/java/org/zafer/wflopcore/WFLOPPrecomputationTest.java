package org.zafer.wflopcore;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;
import org.zafer.wflopconfig.ConfigLoader;
import org.zafer.wflopcore.calculator.PowerOutputCalculator;
import org.zafer.wflopcore.calculator.WakeCalculatorJensen;
import org.zafer.wflopcore.model.GEOnePointFiveSLEPowerModel;
import org.zafer.wflopmodel.problem.WFLOP;
import org.zafer.wflopmodel.solution.Solution;
import org.zafer.wflopmodel.wind.WindProfile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WFLOPPrecomputationTest {

    @Test
    void shouldNotThrowIndexOutOfBoundsException() {
        WFLOP wflop = ConfigLoader.loadFromResource(
                "wflop_distance_true_area_true.json",
                new TypeReference<WFLOP>() {});

        Solution solution = ConfigLoader.loadFromResource(
                "solution.json",
                new TypeReference<Solution>() {});

        WindProfile[] windProfiles = ConfigLoader.loadFromResource(
                "wind_profiles.json",
                new TypeReference<WindProfile[]>() {});

        WakeCalculatorJensen wakeCalculatorJensen = new WakeCalculatorJensen(wflop);

        PowerOutputCalculator powerOutputCalculator = new PowerOutputCalculator(
                wakeCalculatorJensen,
                new GEOnePointFiveSLEPowerModel());

        assertDoesNotThrow(() -> powerOutputCalculator.calculateTotalPowerOutput(solution, windProfiles));
    }

    @Test
    void shouldInvokeComputeIntersectedAreaWhenUseIntersectedAreaMatrixIsFalse() {
        WFLOP wflop = ConfigLoader.loadFromResource(
                "wflop_distance_true_area_false.json",
                new TypeReference<WFLOP>() {});

        Solution solution = ConfigLoader.loadFromResource(
                "solution.json",
                new TypeReference<Solution>() {});

        WindProfile[] windProfiles = ConfigLoader.loadFromResource(
                "wind_profiles.json",
                new TypeReference<WindProfile[]>() {});

        WFLOP wflopSpy = spy(wflop);
        WakeCalculatorJensen wakeCalculatorJensen = new WakeCalculatorJensen(wflopSpy);
        wakeCalculatorJensen.calculateReducedSpeedMultiple(
                windProfiles[0],
                solution.getTurbineIndices()[0],
                solution.getTurbineIndices());

        verify(wflopSpy, atLeastOnce()).computeIntersectedArea(anyDouble(), anyDouble());
    }

    @Test
    void shouldNotInvokeComputeIntersectedAreaWhenUseIntersectedAreaMatrixIsTrue() {
        WFLOP wflop = ConfigLoader.loadFromResource(
                "wflop_distance_true_area_true.json",
                new TypeReference<WFLOP>() {});

        Solution solution = ConfigLoader.loadFromResource(
                "solution.json",
                new TypeReference<Solution>() {});

        WindProfile[] windProfiles = ConfigLoader.loadFromResource(
                "wind_profiles.json",
                new TypeReference<WindProfile[]>() {});

        WFLOP wflopSpy = spy(wflop);
        WakeCalculatorJensen wakeCalculatorJensen = new WakeCalculatorJensen(wflopSpy);
        wakeCalculatorJensen.calculateReducedSpeedMultiple(
                windProfiles[0],
                solution.getTurbineIndices()[0],
                solution.getTurbineIndices());

        verify(wflopSpy, never()).computeIntersectedArea(anyDouble(), anyDouble());
    }
}
