package org.zafer.wflopga.strategy.crossover;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.BeforeEach;
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
@DisplayName("Single Point Crossover Tests")
public class SinglePointCrossoverTest {

    private SinglePointCrossover crossover;
    private WFLOP mockProblem;

    @BeforeEach
    public void setup() {
        crossover = new SinglePointCrossover(1.0); // 100% crossover rate for deterministic tests
        mockProblem = mock(WFLOP.class);
        when(mockProblem.getNumberOfTurbines()).thenReturn(4);
        when(mockProblem.getCellCount()).thenReturn(16);
    }

    @Test
    @DisplayName("Crossover should produce valid offspring")
    @Description("Verifies that single point crossover produces a valid individual with correct number of turbines")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Crossover Operation")
    public void shouldProduceValidOffspring() {
        // Arrange
        Individual parent1 = new Individual(Arrays.asList(0, 1, 2, 3));
        Individual parent2 = new Individual(Arrays.asList(4, 5, 6, 7));

        // Act
        Individual offspring = crossover.crossover(parent1, parent2, mockProblem);

        // Assert
        assertNotNull(offspring, "Offspring should not be null");
        assertEquals(4, offspring.getTurbineIndices().size(), "Offspring should have correct number of turbines");

        // Check that the offspring contains elements from both parents
        boolean hasElementsFromParent1 = false;
        boolean hasElementsFromParent2 = false;

        for (Integer index : offspring.getTurbineIndices()) {
            if (parent1.getTurbineIndices().contains(index)) {
                hasElementsFromParent1 = true;
            }
            if (parent2.getTurbineIndices().contains(index)) {
                hasElementsFromParent2 = true;
            }
        }

        assertTrue(hasElementsFromParent1, "Offspring should inherit genes from parent1");
        assertTrue(hasElementsFromParent2, "Offspring should inherit genes from parent2");
    }

    @Test
    @DisplayName("Crossover should handle duplicate indices")
    @Description("Tests that crossover correctly handles potential duplicate indices from parents")
    @Severity(SeverityLevel.NORMAL)
    @Story("Crossover Operation")
    public void shouldHandleDuplicateIndices() {
        // Arrange
        Individual parent1 = new Individual(Arrays.asList(0, 1, 2, 3));
        Individual parent2 = new Individual(Arrays.asList(2, 3, 4, 5));

        // Act
        Individual offspring = crossover.crossover(parent1, parent2, mockProblem);

        // Assert
        List<Integer> offspringIndices = offspring.getTurbineIndices();
        assertEquals(4, offspringIndices.size(), "Offspring should have correct number of turbines");
        assertEquals(4, new HashSet<>(offspringIndices).size(), "Offspring should not have duplicate indices");
    }

    @Test
    @DisplayName("Crossover should preserve good genes")
    @Description("Tests that single point crossover preserves some genes from both parents")
    @Severity(SeverityLevel.NORMAL)
    @Story("Crossover Operation")
    public void shouldPreserveGenes() {
        // Arrange
        Individual parent1 = new Individual(Arrays.asList(0, 1, 2, 3));
        Individual parent2 = new Individual(Arrays.asList(12, 13, 14, 15));

        // Act
        Individual offspring = crossover.crossover(parent1, parent2, mockProblem);

        // Assert
        List<Integer> offspringIndices = offspring.getTurbineIndices();

        // Count genes from each parent
        long genesFromParent1 = offspringIndices.stream()
                .filter(gene -> parent1.getTurbineIndices().contains(gene))
                .count();

        long genesFromParent2 = offspringIndices.stream()
                .filter(gene -> parent2.getTurbineIndices().contains(gene))
                .count();

        assertTrue(genesFromParent1 > 0, "Offspring should contain genes from parent1");
        assertTrue(genesFromParent2 > 0, "Offspring should contain genes from parent2");
    }
}
