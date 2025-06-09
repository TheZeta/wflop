package org.zafer.wflopcore;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;
import org.zafer.wflopconfig.ConfigLoader;
import org.zafer.wflopcore.calculator.PowerOutputCalculator;
import org.zafer.wflopcore.calculator.WakeCalculatorJensen;
import org.zafer.wflopcore.model.GEOnePointFiveSLEPowerModel;
import org.zafer.wflopmodel.problem.WFLOP;
import org.zafer.wflopmodel.layout.TurbineLayout;
import org.zafer.wflopmodel.wind.WindProfile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WFLOPPrecomputationTest {

    @Test
    void shouldNotThrowIndexOutOfBoundsException() {
        WFLOP wflop = ConfigLoader.loadFromResource(
                "wflop_distance_true_area_true.json",
                new TypeReference<WFLOP>() {});

        TurbineLayout turbineLayout = ConfigLoader.loadFromResource(
                "solution.json",
                new TypeReference<TurbineLayout>() {});

        PowerOutputCalculator powerOutputCalculator = new PowerOutputCalculator(
                wflop,
                new GEOnePointFiveSLEPowerModel());

        assertDoesNotThrow(() -> powerOutputCalculator.calculateTotalPowerOutput(turbineLayout));
    }

    @Test
    void shouldInvokeComputeIntersectedAreaWhenUseIntersectedAreaMatrixIsFalse() {
        WFLOP wflop = ConfigLoader.loadFromResource(
                "wflop_distance_true_area_false.json",
                new TypeReference<WFLOP>() {});

        TurbineLayout turbineLayout = ConfigLoader.loadFromResource(
                "solution.json",
                new TypeReference<TurbineLayout>() {});

        WFLOP wflopSpy = spy(wflop);
        WindProfile[] windProfiles = wflopSpy.getWindProfiles();

        WakeCalculatorJensen wakeCalculatorJensen = new WakeCalculatorJensen(wflopSpy);
        wakeCalculatorJensen.calculateReducedSpeedMultiple(
                windProfiles[0],
                turbineLayout.getTurbineIndices().get(0),
                turbineLayout.getTurbineIndices());

        verify(wflopSpy, atLeastOnce()).computeIntersectedArea(anyDouble(), anyDouble());
    }

    @Test
    void shouldNotInvokeComputeIntersectedAreaWhenUseIntersectedAreaMatrixIsTrue() {
        WFLOP wflop = ConfigLoader.loadFromResource(
                "wflop_distance_true_area_true.json",
                new TypeReference<WFLOP>() {});

        TurbineLayout turbineLayout = ConfigLoader.loadFromResource(
                "solution.json",
                new TypeReference<TurbineLayout>() {});

        WFLOP wflopSpy = spy(wflop);
        WindProfile[] windProfiles = wflopSpy.getWindProfiles();

        WakeCalculatorJensen wakeCalculatorJensen = new WakeCalculatorJensen(wflopSpy);
        wakeCalculatorJensen.calculateReducedSpeedMultiple(
                windProfiles[0],
                turbineLayout.getTurbineIndices().get(0),
                turbineLayout.getTurbineIndices());

        verify(wflopSpy, never()).computeIntersectedArea(anyDouble(), anyDouble());
    }
}
