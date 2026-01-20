package org.zafer.wflopalgorithms.algorithms.wdga.strategy;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.zafer.wflopalgorithms.common.ga.solution.Individual;
import org.zafer.wflopconfig.ConfigLoader;
import org.zafer.wflopcore.power.PowerCalculator;
import org.zafer.wflopmodel.problem.WFLOP;

class WakeBasedMutationTest {

    private WFLOP wflop;
    private PowerCalculator powerCalculator;
    private WakeBasedMutationStrategy wakeBasedMutationStrategy;

    @BeforeEach
    void setUp() {
        wflop = ConfigLoader.loadFromResource(
            "configs/test_problem_small.json",
            new TypeReference<WFLOP>() {}
        );
        powerCalculator = new PowerCalculator(wflop);
        wakeBasedMutationStrategy = new WakeBasedMutationStrategy(
            0.5,
            1.0,
            powerCalculator);

        if (wflop == null) {
            System.out.println("WFLOP is null!");
        }
        if (powerCalculator == null) {
            System.out.println("PowerCalculator is null!");
        }
        if (wakeBasedMutationStrategy == null) {
            System.out.println("WakeBasedMutationStrategy is null!");
        }
    }

    @Test
    void testWakeBasedMutationStrategyWithSmallProblemInstance() {
        // Given
        Individual originalIndividual = new Individual(new ArrayList<>(Arrays.asList(1, 4)));

        // When
        Individual mutatedIndividual = wakeBasedMutationStrategy.mutate(originalIndividual, wflop);

	    // Then
        assertEquals(new HashSet(Arrays.asList(0, 1)), new HashSet(mutatedIndividual.getGenes()));
    }
}
