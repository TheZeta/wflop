package org.zafer.wflopalgorithms.algorithms.wdga.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.zafer.wflopalgorithms.common.ga.solution.Individual;
import org.zafer.wflopmodel.problem.WFLOP;
import org.zafer.wflopmodel.wind.WindProfile;

class WakeBasedCrossoverTest {

    private static final int DIMENSION = 4;
    private static final int TOTAL_CELLS = DIMENSION * DIMENSION;
    private static final int EXPECTED_TURBINE_COUNT = 4;

    @ParameterizedTest(name = "{index} => {0}")
    @MethodSource("crossoverScenarios")
    @DisplayName("Crossover should produce valid hybrid offspring")
    void shouldProduceValidHybridOffspring(
        String scenarioName,
        WFLOP wflop,
        Individual parent1,
        Individual parent2,
        List<Individual> expectedOffsprings
    ) {
        // Given
        WakeBasedCrossoverStrategy strategy = new WakeBasedCrossoverStrategy();

        // When
        Individual actualOffspring = strategy.crossover(parent1, parent2, wflop);

        // Then
        assertValidHybridOffspring(
            actualOffspring,
            parent1,
            parent2,
            expectedOffsprings,
            EXPECTED_TURBINE_COUNT,
            TOTAL_CELLS
        );
    }

    private static Stream<Arguments> crossoverScenarios() {
        return Stream.of(
            Arguments.of(
                "single wind profile",
                createWflop(
                    List.of(
                        new WindProfile(5.8, 180, 1.0)
                    )
                ),
                new Individual(List.of(0, 1, 14, 15)),  /* Parent 1 */
                new Individual(List.of(2, 3, 12, 13)),  /* Parent 2 */
                List.of(
                    new Individual(List.of(0, 1, 2, 3)) /* Possible Offsprings */
                )
            ),
            Arguments.of(
                "uniform wind profiles",
                createWflop(
                    List.of(
                        new WindProfile(12.0, 0, 0.25),
                        new WindProfile(12.0, 90, 0.25),
                        new WindProfile(12.0, 180, 0.25),
                        new WindProfile(12.0, 270, 0.25)
                    )
                ),
                new Individual(List.of(0, 1, 14, 15)),  /* Parent 1 */
                new Individual(List.of(2, 3, 12, 13)),  /* Parent 2 */
                List.of(                                /* Possible Offsprings */
                    new Individual(List.of(12, 13, 14, 15)),    /* When 0 degrees selected */
                    new Individual(List.of(2, 3, 14, 15)),      /* When 90 degrees selected */
                    new Individual(List.of(0, 1, 2, 3)),        /* When 180 degrees selected */
                    new Individual(List.of(2, 3, 12, 13))       /* When 270 degrees selected */
                )
            ),
            Arguments.of(
                "turbine on division border",
                createWflop(
                    List.of(
                        new WindProfile(12.0, 45, 1.0)
                    )
                ),
                new Individual(List.of(0, 1, 9, 12)),   /* Parent 1 */
                new Individual(List.of(3, 4, 5, 6)),    /* Parent 2 */
                List.of(
                    new Individual(List.of(4, 5, 9, 12)) /* Possible Offsprings */
                )
            )
        );
    }

    private static WFLOP createWflop(List<WindProfile> windProfiles) {
        return new WFLOP(
            40.0,
            100.0,
            0.9,
            0.8,
            1.225,
            0.1,
            200.0,
            DIMENSION,
            DIMENSION,
            windProfiles
        );
    }

    private static void assertValidHybridOffspring(
        Individual actualOffspring,
        Individual parent1,
        Individual parent2,
        List<Individual> expectedOffsprings,
        int expectedSize,
        int totalCells
    ) {
        Set<Integer> actualGenes = Set.copyOf(actualOffspring.getGenes());

        Set<Integer> parent1Genes = Set.copyOf(parent1.getGenes());
        Set<Integer> parent2Genes = Set.copyOf(parent2.getGenes());

        List<Set<Integer>> expectedGenesList = expectedOffsprings.stream()
            .map(individual -> Set.copyOf(individual.getGenes()))
            .toList();

        long genesFromParent1 = actualGenes.stream()
            .filter(parent1Genes::contains)
            .count();

        long genesFromParent2 = actualGenes.stream()
            .filter(parent2Genes::contains)
            .count();

        assertEquals(
            expectedSize,
            actualOffspring.getGenes().size(),
            "Offspring should preserve exact turbine count"
        );

        assertEquals(
            expectedSize,
            actualGenes.size(),
            "Offspring should not contain duplicate turbines"
        );

        assertTrue(
            actualGenes.stream().allMatch(gene ->
                gene >= 0 && gene < totalCells
            ),
            "All genes must be within valid grid bounds"
        );

        assertTrue(
            genesFromParent1 > 0,
            "Offspring should inherit at least one gene from parent1"
        );

        assertTrue(
            genesFromParent2 > 0,
            "Offspring should inherit at least one gene from parent2"
        );

        assertTrue(expectedGenesList.contains(actualGenes),
            () -> String.format("Parent1: %s | Parent2: %s | ExpectedGenes: %s | ActualGenes: %s",
                parent1Genes, parent2Genes, expectedGenesList, actualGenes)
        );
    }
}
