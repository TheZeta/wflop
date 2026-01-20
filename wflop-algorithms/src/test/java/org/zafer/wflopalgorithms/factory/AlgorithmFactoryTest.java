package org.zafer.wflopalgorithms.factory;

import org.junit.jupiter.api.Test;
import org.zafer.wflopalgorithms.algorithms.ga.GA;
import org.zafer.wflopalgorithms.algorithms.pso.PSO;
import org.zafer.wflopconfig.ConfigLoader;
import org.zafer.wflopmetaheuristic.Metaheuristic;
import org.zafer.wflopmetaheuristic.Solution;
import org.zafer.wflopmetaheuristic.termination.GenerationBasedTermination;
import org.zafer.wflopmodel.problem.WFLOP;

import com.fasterxml.jackson.core.type.TypeReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AlgorithmFactory.
 * Tests the registration-based factory pattern using AlgorithmRegistry.
 */
class AlgorithmFactoryTest {

    @Test
    void testLoadGAFromJson() throws AlgorithmLoadException {
        // Given
        String jsonPath = "configs/test_ga.json";

        // When
        Metaheuristic algorithm = AlgorithmFactory.loadFromJson(jsonPath);

        // Then
        assertNotNull(algorithm);
        assertInstanceOf(GA.class, algorithm);

        GA ga = (GA) algorithm;
        assertEquals("GA", ga.getAlgorithm());
        assertEquals(100, ga.getPopulationSize());
        assertEquals(0.8, ga.getCrossoverRate(), 0.001);
        assertEquals(0.01, ga.getMutationRate(), 0.001);
        assertEquals("tournament", ga.getSelectionStrategy());
        assertEquals(new GenerationBasedTermination(200), ga.getTerminationCondition());
    }

    @Test
    void testLoadPSOFromJson() throws AlgorithmLoadException {
        // Given
        String jsonPath = "configs/test_pso.json";

        // When
        Metaheuristic algorithm = AlgorithmFactory.loadFromJson(jsonPath);

        // Then
        assertNotNull(algorithm);
        assertInstanceOf(PSO.class, algorithm);

        PSO pso = (PSO) algorithm;
        assertEquals("PSO", pso.getAlgorithm());
        assertEquals(50, pso.getSwarmSize());
        assertEquals(100, pso.getMaxIterations());
        assertEquals(0.729, pso.getInertiaWeight(), 0.001);
        assertEquals(1.49445, pso.getCognitiveComponent(), 0.001);
        assertEquals(1.49445, pso.getSocialComponent(), 0.001);
    }

    @Test
    void testMultipleAlgorithmTypes() throws AlgorithmLoadException {
        // Given
        String gaJsonPath = "configs/test_ga.json";
        String psoJsonPath = "configs/test_pso.json";

        // When
        Metaheuristic ga = AlgorithmFactory.loadFromJson(gaJsonPath);
        Metaheuristic pso = AlgorithmFactory.loadFromJson(psoJsonPath);

        // Then
        assertNotNull(ga);
        assertNotNull(pso);
        assertInstanceOf(GA.class, ga);
        assertInstanceOf(PSO.class, pso);
    }

    @Test
    void testMissingAlgorithmTypeProperty() {
        // Given - A JSON file without algorithm property
        String jsonPath = "configs/test_missing_type.json";

        // When & Then
        AlgorithmLoadException exception = assertThrows(
            AlgorithmLoadException.class,
            () -> AlgorithmFactory.loadFromJson(jsonPath)
        );

        assertTrue(exception.getMessage().contains("algorithm"));
    }

    @Test
    void testInvalidAlgorithmType() {
        // Given - A JSON file with an invalid algorithm type
        String jsonPath = "configs/test_invalid_type.json";

        // When & Then
        AlgorithmLoadException exception = assertThrows(
            AlgorithmLoadException.class,
            () -> AlgorithmFactory.loadFromJson(jsonPath)
        );

        assertTrue(exception.getMessage().contains("is not registered") ||
                   exception.getMessage().contains("JSON resource not found"));
    }

    @Test
    void testGAExecutesRun() throws AlgorithmLoadException {
        // Given
        String jsonPath = "configs/test_ga.json";
        Metaheuristic algorithm = AlgorithmFactory.loadFromJson(jsonPath);
        WFLOP problem = ConfigLoader.loadFromResource(
                "configs/test_problem.json",
                new TypeReference<WFLOP>() {});

        // When
        Solution solution = algorithm.run(problem);

        // Then
        assertNotNull(solution);
        assertTrue(solution.getFitness() > 0);
    }

    @Test
    void testPSOExecutesRun() throws AlgorithmLoadException {
        // Given
        String jsonPath = "configs/test_pso.json";
        Metaheuristic algorithm = AlgorithmFactory.loadFromJson(jsonPath);
        WFLOP problem = ConfigLoader.loadFromResource(
                "configs/test_problem.json",
                new TypeReference<WFLOP>() {});

        // When
        Solution solution = algorithm.run(problem);

        // Then
        assertNotNull(solution);
        assertTrue(solution.getFitness() > 0);
    }

    @Test
    void testRegistryContainsAlgorithms() {
        // Given & When
        assertTrue(AlgorithmRegistry.isRegistered("GA"));
        assertTrue(AlgorithmRegistry.isRegistered("PSO"));
        assertTrue(AlgorithmRegistry.isRegistered("WDGA"));
        assertFalse(AlgorithmRegistry.isRegistered("NonExistentAlgorithm"));
    }
}