package org.zafer.wflopga.strategy.selection;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.zafer.wflopga.Individual;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Epic("Wind Farm Layout Optimization")
@Feature("Genetic Algorithm Components")
@DisplayName("Tournament Selection Tests")
public class TournamentSelectionTest {

    @Test
    @DisplayName("Tournament should select best individual")
    @Description("Tests that tournament selection chooses the individual with highest fitness")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Selection Operation")
    public void shouldSelectBestIndividual() {
        // Arrange
        Individual bestIndividual = new Individual(Arrays.asList(1, 2, 3));
        bestIndividual.setFitness(100.0);

        Individual averageIndividual = new Individual(Arrays.asList(4, 5, 6));
        averageIndividual.setFitness(50.0);

        Individual worstIndividual = new Individual(Arrays.asList(7, 8, 9));
        worstIndividual.setFitness(25.0);

        List<Individual> population = Arrays.asList(
                worstIndividual, averageIndividual, bestIndividual
        );

        TournamentSelection selection = new TournamentSelection(3); // Tournament size equals population size

        // Act
        Individual selected = selection.select(population);

        // Assert
        assertSame(bestIndividual, selected, "Tournament should select the individual with highest fitness");
    }

    @Test
    @DisplayName("Tournament should work with different tournament sizes")
    @Description("Tests that tournament selection works with different tournament sizes")
    @Severity(SeverityLevel.NORMAL)
    @Story("Selection Operation")
    public void shouldWorkWithDifferentTournamentSizes() {
        // Arrange
        List<Individual> population = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Individual ind = new Individual(Arrays.asList(i));
            ind.setFitness(i * 10.0); // Fitness increases with index
            population.add(ind);
        }

        // Create selection with tournament size 1 (should be random selection)
        TournamentSelection smallTournament = new TournamentSelection(1);
        Individual selected1 = smallTournament.select(population);

        // Tournament size = population size (should select best)
        TournamentSelection largeTournament = new TournamentSelection(10);
        Individual selected10 = largeTournament.select(population);

        // Assert
        assertNotNull(selected1, "Selection with tournament size 1 should return an individual");
        assertEquals(90.0, selected10.getFitness(), "Selection with tournament size = population should select best");
    }

    @Test
    @DisplayName("Tournament should handle invalid tournament sizes")
    @Description("Tests that tournament selection gracefully handles invalid tournament sizes")
    @Severity(SeverityLevel.NORMAL)
    @Story("Selection Operation")
    public void shouldHandleInvalidTournamentSizes() {
        // Arrange
        List<Individual> population = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Individual ind = new Individual(Arrays.asList(i));
            ind.setFitness(i * 10.0);
            population.add(ind);
        }

        // Create selection with tournament size larger than population
        TournamentSelection tooLargeTournament = new TournamentSelection(10);

        // Act
        Individual selected = tooLargeTournament.select(population);

        // Assert
        assertNotNull(selected, "Selection should work even with tournament size > population");
    }

    @Test
    @DisplayName("Tournament should handle negative tournament sizes")
    @Description("Tests that tournament selection gracefully handles negative tournament sizes")
    @Severity(SeverityLevel.NORMAL)
    @Story("Selection Operation")
    public void shouldHandleNegativeTournamentSizes() {
        // Arrange
        List<Individual> population = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Individual ind = new Individual(Arrays.asList(i));
            ind.setFitness(i * 10.0);
            population.add(ind);
        }

        // Create selection with negative tournament size
        TournamentSelection negativeTournament = new TournamentSelection(-3);

        // Act
        Individual selected = negativeTournament.select(population);

        // Assert
        assertNotNull(selected, "Selection should work even with negative tournament size");
    }
}
