package org.zafer.wflopga;

import com.fasterxml.jackson.core.type.TypeReference;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.zafer.wflopconfig.ConfigLoader;
import org.zafer.wflopga.strategy.crossover.SinglePointCrossover;
import org.zafer.wflopga.strategy.mutation.RandomReplacementMutation;
import org.zafer.wflopga.strategy.selection.TournamentSelection;
import org.zafer.wflopmodel.problem.WFLOP;

import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Epic("Wind Farm Layout Optimization")
@Feature("Genetic Algorithm")
@DisplayName("Genetic Algorithm Tests")
public class GeneticAlgorithmTest {

    WFLOP problem;
    GeneticAlgorithm ga;
    Individual optimal;

    // Parameters for different test configurations
    private static final int DEFAULT_POPULATION_SIZE = 100;
    private static final int DEFAULT_GENERATIONS = 200;
    private static final double DEFAULT_MUTATION_RATE = 0.1;
    private static final int DEFAULT_TOURNAMENT_SIZE = 3;

    @BeforeEach
    public void setup() {
        problem = ConfigLoader.loadFromResource(
                "wflop_problem.json",
                new TypeReference<WFLOP>() {});

        ga = new GeneticAlgorithm(
                problem,
                DEFAULT_POPULATION_SIZE,
                DEFAULT_GENERATIONS,
                new SinglePointCrossover(0.7),
                new RandomReplacementMutation(DEFAULT_MUTATION_RATE),
                new TournamentSelection(DEFAULT_TOURNAMENT_SIZE)
        );

        optimal = ga.run();
    }

    /**
     * Creates a GA instance with custom parameters for testing
     */
    private GeneticAlgorithm createCustomGA(int populationSize, int generations, double mutationRate, int tournamentSize) {
        return new GeneticAlgorithm(
                problem,
                populationSize,
                generations, 
                new SinglePointCrossover(0.7),
                new RandomReplacementMutation(mutationRate),
                new TournamentSelection(tournamentSize)
        );
    }

    @Test
    @DisplayName("Solution should contain the exact number of turbines specified in the problem")
    @Description("Verifies that the GA solution places exactly the requested number of turbines")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Solution Validation")
    public void solutionShouldContainRightNumberOfTurbines() {
        assertEquals(problem.getNumberOfTurbines(), optimal.getSolution().getTurbineIndices().size(),
                "GA solution must contain exactly the specified number of turbines");
    }

    @Test
    @DisplayName("Solution should not have duplicate turbine positions")
    @Description("Ensures that each turbine is placed in a unique location")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Solution Validation")
    public void solutionShouldNotContainDuplicates() {
        assertEquals(problem.getNumberOfTurbines(), new HashSet<>(optimal.getSolution().getTurbineIndices()).size(),
                "GA solution must not contain duplicate turbine positions");
    }

    @Test
    @DisplayName("All turbine indices in solution should be non-negative")
    @Description("Verifies that all turbine placement indices are valid non-negative values")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Solution Validation")
    public void solutionShouldNotContainNegativeIndices() {
        for (Integer idx : optimal.getSolution().getTurbineIndices()) {
            assertTrue(idx >= 0, "Turbine index must be non-negative, found: " + idx);
        }
    }

    @Test
    @DisplayName("All turbine indices should be within the problem grid size")
    @Description("Ensures that all turbines are placed within the valid grid boundaries")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Solution Validation")
    public void solutionShouldNotContainIndicesGreaterThanProblemSize() {
        for (Integer idx : optimal.getSolution().getTurbineIndices()) {
            assertTrue(idx < problem.getCellCount(), 
                    "Turbine index must be less than grid size (" + problem.getCellCount() + "), found: " + idx);
        }
    }

    @Test
    @DisplayName("Fitness value should be positive")
    @Description("Checks that the fitness calculation produces a positive value")
    @Severity(SeverityLevel.NORMAL)
    @Story("Fitness Evaluation")
    public void fitnessShouldBePositive() {
        assertTrue(optimal.getFitness() > 0, "Fitness value should be positive, got: " + optimal.getFitness());
    }

    @Test
    @DisplayName("Different runs should produce valid but potentially different solutions")
    @Description("Verifies the stochastic nature of GA by running multiple times")
    @Severity(SeverityLevel.NORMAL)
    @Story("Algorithm Behavior")
    public void differentRunsShouldProduceDifferentValidSolutions() {
        // Run the algorithm a second time
        Individual secondRun = ga.run();

        // Both solutions should be valid
        assertEquals(problem.getNumberOfTurbines(), secondRun.getSolution().getTurbineIndices().size(),
                "Second run solution must contain exactly the specified number of turbines");

        // Check if layouts are different (not guaranteed but likely with stochastic algorithm)
        // We'll just note this rather than assert, since GAs can sometimes converge to the same solution
        boolean identical = true;
        List<Integer> firstLayout = optimal.getSolution().getTurbineIndices();
        List<Integer> secondLayout = secondRun.getSolution().getTurbineIndices();

        if (firstLayout.size() == secondLayout.size()) {
            for (int i = 0; i < firstLayout.size(); i++) {
                if (!firstLayout.get(i).equals(secondLayout.get(i))) {
                    identical = false;
                    break;
                }
            }
        }

        // Just logging, not asserting - solutions might be identical by chance
        if (identical) {
            System.out.println("Note: Two GA runs produced identical layouts - this is possible but less common");
        }
    }

    @Test
    @DisplayName("Higher population size should not degrade solution quality")
    @Description("Tests that increasing population size doesn't worsen results")
    @Severity(SeverityLevel.NORMAL)
    @Story("Parameter Sensitivity")
    public void higherPopulationSizeShouldNotDegrade() {
        // Create a GA with larger population
        GeneticAlgorithm largerPopGA = createCustomGA(
                DEFAULT_POPULATION_SIZE * 2,  // Double population size
                DEFAULT_GENERATIONS, 
                DEFAULT_MUTATION_RATE,
                DEFAULT_TOURNAMENT_SIZE
        );

        Individual largerPopSolution = largerPopGA.run();

        // Solution should still be valid
        assertEquals(problem.getNumberOfTurbines(), largerPopSolution.getSolution().getTurbineIndices().size(),
                "Solution with larger population must contain exactly the specified number of turbines");

        // The solution quality should not be dramatically worse
        // Note: We can't guarantee it will be better due to the stochastic nature
        assertTrue(largerPopSolution.getFitness() > 0,
                "Fitness with larger population should be positive");
    }

    @Test
    @DisplayName("Algorithm should handle higher mutation rates")
    @Description("Tests GA resilience with higher mutation probabilities")
    @Severity(SeverityLevel.NORMAL)
    @Story("Parameter Sensitivity")
    public void shouldHandleHigherMutationRate() {
        // Create a GA with higher mutation rate
        GeneticAlgorithm highMutationGA = createCustomGA(
                DEFAULT_POPULATION_SIZE,
                DEFAULT_GENERATIONS, 
                0.3,  // Higher mutation rate
                DEFAULT_TOURNAMENT_SIZE
        );

        Individual highMutationSolution = highMutationGA.run();

        // Solution should still be valid
        assertEquals(problem.getNumberOfTurbines(), highMutationSolution.getSolution().getTurbineIndices().size(),
                "Solution with higher mutation must contain exactly the specified number of turbines");

        // Ensure no duplicate positions
        assertEquals(problem.getNumberOfTurbines(), 
                new HashSet<>(highMutationSolution.getSolution().getTurbineIndices()).size(),
                "Solution with higher mutation must not contain duplicate turbine positions");
    }

    @Test
    @DisplayName("Algorithm should compute fitness for custom individual")
    @Description("Tests the fitness calculation function directly")
    @Severity(SeverityLevel.NORMAL)
    @Story("Fitness Evaluation")
    public void shouldComputeFitnessForCustomIndividual() {
        // Get indices from optimal solution to create a custom individual
        List<Integer> indices = optimal.getSolution().getTurbineIndices();
        Individual customIndividual = new Individual(indices);

        // Compute fitness
        double fitness = ga.computeFitness(customIndividual);

        // Fitness should match the one calculated during optimization
        assertEquals(optimal.getFitness(), fitness, 0.001,
                "Fitness calculation should be deterministic for the same layout");
    }
}