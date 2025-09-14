package org.zafer.wflopga.strategy.mutation;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.zafer.wflopga.Individual;
import org.zafer.wflopmodel.problem.WFLOP;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Epic("Wind Farm Layout Optimization")
@Feature("Genetic Algorithm Components")
@DisplayName("Random Replacement Mutation Tests")
public class RandomReplacementMutationTest {

    @Test
    @DisplayName("Mutation with rate 0 should not change individual")
    @Description("Tests that mutation with rate 0 preserves the original individual unchanged")
    @Severity(SeverityLevel.NORMAL)
    @Story("Mutation Operation")
    public void shouldNotChangeIndividualWithZeroRate() {
        // Arrange
        List<Integer> originalIndices = Arrays.asList(1, 3, 5, 7);
        Individual original = new Individual(originalIndices);
        RandomReplacementMutation mutation = new RandomReplacementMutation(0.0);
        WFLOP mockProblem = mock(WFLOP.class);

        // Act
        Individual mutated = mutation.mutate(original, mockProblem);

        // Assert
        assertEquals(originalIndices, mutated.getTurbineIndices(), 
                "Individual should remain unchanged with mutation rate 0");
    }

    @Test
    @DisplayName("Mutation with rate 1 should change individual")
    @Description("Tests that mutation with rate 1 produces changes while maintaining correct number of turbines")
    @Severity(SeverityLevel.NORMAL)
    @Story("Mutation Operation")
    public void shouldChangeIndividualWithRateOne() {
        // Arrange
        List<Integer> originalIndices = Arrays.asList(1, 3, 5, 7);
        Individual original = new Individual(originalIndices);
        RandomReplacementMutation mutation = new RandomReplacementMutation(1.0);

        WFLOP mockProblem = mock(WFLOP.class);
        when(mockProblem.getCellCount()).thenReturn(20);

        // Act
        Individual mutated = mutation.mutate(original, mockProblem);

        // Assert
        assertNotEquals(originalIndices, mutated.getTurbineIndices(), 
                "Individual should be changed with mutation rate 1");
        assertEquals(originalIndices.size(), mutated.getTurbineIndices().size(), 
                "Mutated individual should maintain the same number of turbines");
    }

    @Test
    @DisplayName("Mutation should produce valid solutions")
    @Description("Tests that mutation produces valid solutions with no duplicates")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Mutation Operation")
    public void shouldProduceValidSolutions() {
        // Arrange
        List<Integer> originalIndices = Arrays.asList(1, 3, 5, 7);
        Individual original = new Individual(originalIndices);
        RandomReplacementMutation mutation = new RandomReplacementMutation(0.5);

        WFLOP mockProblem = mock(WFLOP.class);
        when(mockProblem.getCellCount()).thenReturn(20);
        when(mockProblem.getNumberOfTurbines()).thenReturn(4);

        // Act
        Individual mutated = mutation.mutate(original, mockProblem);

        // Assert
        List<Integer> mutatedIndices = mutated.getTurbineIndices();
        assertEquals(4, mutatedIndices.size(), 
                "Mutated individual should have correct number of turbines");
        assertEquals(4, new HashSet<>(mutatedIndices).size(), 
                "Mutated individual should not have duplicate indices");

        for (Integer index : mutatedIndices) {
            assertTrue(index >= 0 && index < 20, 
                    "All indices should be within problem bounds");
        }
    }

    @Test
    @DisplayName("Mutation should handle extreme rates")
    @Description("Tests that mutation handles extreme mutation rates gracefully")
    @Severity(SeverityLevel.NORMAL)
    @Story("Mutation Operation")
    public void shouldHandleExtremeRates() {
        // Arrange
        List<Integer> originalIndices = Arrays.asList(1, 3, 5, 7);
        Individual original = new Individual(originalIndices);

        // Create mutation with rate > 1
        RandomReplacementMutation extremeMutation = new RandomReplacementMutation(1.5);

        WFLOP mockProblem = mock(WFLOP.class);
        when(mockProblem.getCellCount()).thenReturn(20);

        // Act - this should not throw exceptions
        Individual mutated = extremeMutation.mutate(original, mockProblem);

        // Assert
        assertEquals(originalIndices.size(), mutated.getTurbineIndices().size(), 
                "Mutated individual should maintain the same number of turbines even with extreme rates");
    }
}
