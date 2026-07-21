package org.zafer.wflopalgorithms.algorithms.wdga.strategy;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import org.zafer.wflopalgorithms.common.ga.solution.Individual;
import org.zafer.wflopcore.power.PowerCalculator;
import org.zafer.wflopmodel.problem.WFLOP;
import org.zafer.wflopmodel.wind.WindProfile;

import java.util.List;
import java.util.Set;

class WakeBasedMutationTest {

    @Test
    void producesValidMutant_WhenMutated() {
        // Given
        int dimension = 3;
        int turbineCount = 2;

        WFLOP problem = new WFLOP(
            40.0,
            100.0,
            0.9,
            0.8,
            1.225,
            0.1,
            200.0,
            dimension,
            turbineCount,
            List.of(
                new WindProfile(5.8, 180, 1.0)
            )
        );
        PowerCalculator calculator = new PowerCalculator(problem);
        WakeBasedMutationStrategy strategy = new WakeBasedMutationStrategy(
            0.5,
            1.0,
            calculator
        );

        Individual originalIndividual = new Individual(List.of(1, 4));
        List<Integer> expectedGenes = List.of(0, 1);

        // When
        Individual mutatedIndividual = strategy.mutate(originalIndividual, problem);
        List<Integer> actualGenes = mutatedIndividual.getList();

	    // Then
        assertEquals(turbineCount, actualGenes.size(), "Turbine count should not change after mutation");
        assertEquals(turbineCount, Set.copyOf(actualGenes).size(), "Should not contain duplicates");
        assertTrue(
            actualGenes.contains(expectedGenes.get(0)) && actualGenes.contains(expectedGenes.get(1))
        );
    }
}
