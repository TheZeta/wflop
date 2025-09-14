package org.zafer.wflopga;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.zafer.wflopmodel.layout.TurbineLayout;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Epic("Wind Farm Layout Optimization")
@Feature("GA Components")
@DisplayName("Individual Tests")
public class IndividualTest {

    @Test
    @DisplayName("Individual should be created with valid turbine indices")
    @Description("Tests that Individual constructor properly initializes with a list of indices")
    @Severity(SeverityLevel.NORMAL)
    @Story("Individual Creation")
    public void shouldCreateWithIndices() {
        // Arrange
        List<Integer> indices = Arrays.asList(0, 5, 10, 15, 20);

        // Act
        Individual individual = new Individual(indices);

        // Assert
        assertEquals(indices, individual.getTurbineIndices(), "Individual should contain the provided indices");
        assertEquals(indices.size(), individual.getTurbineIndices().size(), "Individual should maintain the same number of indices");
    }

    @Test
    @DisplayName("Individual should store and retrieve fitness value")
    @Description("Tests the fitness getter and setter functionality")
    @Severity(SeverityLevel.NORMAL)
    @Story("Fitness Handling")
    public void shouldStoreAndRetrieveFitness() {
        // Arrange
        List<Integer> indices = Arrays.asList(1, 2, 3);
        Individual individual = new Individual(indices);
        double expectedFitness = 123.45;

        // Act
        individual.setFitness(expectedFitness);

        // Assert
        assertEquals(expectedFitness, individual.getFitness(), 0.0001, "Individual should store and retrieve the fitness value");
    }

    @Test
    @DisplayName("Individual should be created with TurbineLayout")
    @Description("Tests that Individual can be created from a TurbineLayout object")
    @Severity(SeverityLevel.NORMAL)
    @Story("Individual Creation")
    public void shouldCreateWithTurbineLayout() {
        // Arrange
        List<Integer> indices = Arrays.asList(7, 14, 21, 28);
        TurbineLayout layout = new TurbineLayout(indices);

        // Act
        Individual individual = new Individual(layout);

        // Assert
        assertEquals(indices, individual.getTurbineIndices(), "Individual should contain the indices from the TurbineLayout");
        assertSame(layout, individual.getSolution(), "Individual should store the same TurbineLayout instance");
    }

    @Test
    @DisplayName("Individual should be created with layout and fitness")
    @Description("Tests that Individual can be created with both layout and fitness in one step")
    @Severity(SeverityLevel.NORMAL)
    @Story("Individual Creation")
    public void shouldCreateWithLayoutAndFitness() {
        // Arrange
        List<Integer> indices = Arrays.asList(3, 6, 9, 12);
        TurbineLayout layout = new TurbineLayout(indices);
        double expectedFitness = 987.65;

        // Act
        Individual individual = new Individual(layout, expectedFitness);

        // Assert
        assertEquals(indices, individual.getTurbineIndices(), "Individual should contain the indices from the TurbineLayout");
        assertEquals(expectedFitness, individual.getFitness(), 0.0001, "Individual should store the provided fitness value");
    }
}
