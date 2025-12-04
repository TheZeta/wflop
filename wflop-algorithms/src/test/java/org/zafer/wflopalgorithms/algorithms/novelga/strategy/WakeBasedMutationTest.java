package org.zafer.wflopalgorithms.algorithms.novelga.strategy;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.zafer.wflopalgorithms.common.ga.solution.Individual;
import org.zafer.wflopalgorithms.algorithms.novelga.strategy.WakeBasedMutationStrategy;
import org.zafer.wflopconfig.ConfigLoader;
import org.zafer.wflopcore.calculator.PowerOutputCalculator;
import org.zafer.wflopmodel.problem.WFLOP;

class WakeBasedMutationTest {

    private WFLOP wflop;
    private PowerOutputCalculator powerOutputCalculator;
    private WakeBasedMutationStrategy wakeBasedMutationStrategy;

    @BeforeEach
    void setUp() {
        wflop = ConfigLoader.loadFromResource(
            "org/zafer/wflopalgorithms/algorithms/novelga/strategy/problem_instance_small.json",
            new TypeReference<WFLOP>() {});
        powerOutputCalculator = new PowerOutputCalculator(wflop); 
        wakeBasedMutationStrategy = new WakeBasedMutationStrategy(
            0.5,
            1.0,
            powerOutputCalculator);

        if (wflop == null) {
            System.out.println("WFLOP is null!");
        }
        if (powerOutputCalculator == null) {
            System.out.println("PowerOutputCalculator is null!");
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
