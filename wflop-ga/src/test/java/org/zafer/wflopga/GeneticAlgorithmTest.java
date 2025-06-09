package org.zafer.wflopga;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.zafer.wflopconfig.ConfigLoader;
import org.zafer.wflopga.strategy.crossover.SinglePointCrossover;
import org.zafer.wflopga.strategy.mutation.RandomReplacementMutation;
import org.zafer.wflopga.strategy.selection.TournamentSelection;
import org.zafer.wflopmodel.problem.WFLOP;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GeneticAlgorithmTest {

    WFLOP problem;
    GeneticAlgorithm ga;
    Individual optimal;

    @BeforeEach
    public void setup() {
        problem = ConfigLoader.loadFromResource(
                "wflop_problem.json",
                new TypeReference<WFLOP>() {});

        ga = new GeneticAlgorithm(
                problem,
                100, // population size
                200, // number of generations
                new SinglePointCrossover(),
                new RandomReplacementMutation(0.1),
                new TournamentSelection(3)
        );

        optimal = ga.run();
    }

    @Test
    @DisplayName("Solution should contain the exact number of turbines specified in the problem")
    public void solutionShouldContainRightNumberOfTurbines() {
        assertEquals(problem.getNumberOfTurbines(), optimal.getSolution().getTurbineIndices().size());
    }

    @Test
    @DisplayName("Solution should not have duplicate turbine positions")
    public void solutionShouldNotContainDuplicates() {
        assertEquals(problem.getNumberOfTurbines(), new HashSet<>(optimal.getSolution().getTurbineIndices()).size());
    }

    @Test
    @DisplayName("All turbine indices in solution should be non-negative")
    public void solutionShouldNotContainNegativeIndices() {
        for (Integer idx : optimal.getSolution().getTurbineIndices()) {
            assert idx >= 0;
        }
    }

    @Test
    @DisplayName("All turbine indices should be within the problem grid size")
    public void solutionShouldNotContainIndicesGreaterThanProblemSize() {
        for (Integer idx : optimal.getSolution().getTurbineIndices()) {
            assert idx < problem.getCellCount();
        }
    }
}