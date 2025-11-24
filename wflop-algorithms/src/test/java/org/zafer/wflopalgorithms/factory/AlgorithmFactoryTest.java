package org.zafer.wflopalgorithms.factory;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.zafer.wflopalgorithms.algorithms.standardga.StandardGA;
import org.zafer.wflopalgorithms.algorithms.pso.PSO;
import org.zafer.wflopconfig.ConfigLoader;
import org.zafer.wflopmetaheuristic.Metaheuristic;
import org.zafer.wflopmetaheuristic.Solution;
import org.zafer.wflopmodel.problem.WFLOP;

import com.fasterxml.jackson.core.type.TypeReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AlgorithmFactory.
 * Tests the registration-based factory pattern with reflection and caching.
 */
class AlgorithmFactoryTest {

    @BeforeEach
    void setUp() {
        // Clear cache before each test to ensure isolation
        AlgorithmFactory.clearCache();
    }

    @AfterEach
    void tearDown() {
        // Clear cache after each test
        AlgorithmFactory.clearCache();
    }

    @Test
    void testLoadStandardGAFromJson() throws AlgorithmLoadException {
        // Given
        String jsonPath = "org/zafer/wflopalgorithms/algorithms/standardga/algorithm_instance.json";

        // When
        Metaheuristic algorithm = AlgorithmFactory.loadFromJson(jsonPath);

        // Then
        assertNotNull(algorithm);
        assertInstanceOf(StandardGA.class, algorithm);

        StandardGA ga = (StandardGA) algorithm;
        assertEquals("StandardGA", ga.getAlgorithm());
        assertEquals(100, ga.getPopulationSize());
        assertEquals(50, ga.getGenerations());
        assertEquals(0.8, ga.getCrossoverRate(), 0.001);
        assertEquals(0.01, ga.getMutationRate(), 0.001);
        assertEquals("tournament", ga.getSelectionStrategy());
    }

    @Test
    void testLoadPSOFromJson() throws AlgorithmLoadException {
        // Given
        String jsonPath = "org/zafer/wflopalgorithms/algorithms/pso/algorithm_instance.json";

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
    void testCachingMechanism() throws AlgorithmLoadException {
        // Given
        String jsonPath = "org/zafer/wflopalgorithms/algorithms/standardga/algorithm_instance.json";

        // When - First load (should use reflection)
        assertEquals(0, AlgorithmFactory.getCacheSize());
        Metaheuristic algorithm1 = AlgorithmFactory.loadFromJson(jsonPath);
        assertEquals(1, AlgorithmFactory.getCacheSize());

        // Second load (should use cache)
        Metaheuristic algorithm2 = AlgorithmFactory.loadFromJson(jsonPath);
        assertEquals(1, AlgorithmFactory.getCacheSize());

        // Then
        assertNotNull(algorithm1);
        assertNotNull(algorithm2);
        assertInstanceOf(StandardGA.class, algorithm1);
        assertInstanceOf(StandardGA.class, algorithm2);
    }

    @Test
    void testMultipleAlgorithmTypesInCache() throws AlgorithmLoadException {
        // Given
        String gaJsonPath = "org/zafer/wflopalgorithms/algorithms/standardga/algorithm_instance.json";
        String psoJsonPath = "org/zafer/wflopalgorithms/algorithms/pso/algorithm_instance.json";

        // When
        assertEquals(0, AlgorithmFactory.getCacheSize());
        
        Metaheuristic ga = AlgorithmFactory.loadFromJson(gaJsonPath);
        assertEquals(1, AlgorithmFactory.getCacheSize());
        
        Metaheuristic pso = AlgorithmFactory.loadFromJson(psoJsonPath);
        assertEquals(2, AlgorithmFactory.getCacheSize());

        // Then
        assertNotNull(ga);
        assertNotNull(pso);
        assertInstanceOf(StandardGA.class, ga);
        assertInstanceOf(PSO.class, pso);
    }

    @Test
    void testFileNotFound() {
        // Given
        String invalidPath = "nonexistent/path/algorithm.json";

        // When & Then
        AlgorithmLoadException exception = assertThrows(
            AlgorithmLoadException.class,
            () -> AlgorithmFactory.loadFromJson(invalidPath)
        );

        assertTrue(exception.getMessage().contains("JSON resource not found"));
    }

    @Test
    void testMissingAlgorithmTypeProperty() {
        // Given - A JSON file without algorithm property
        String jsonPath = "org/zafer/wflopalgorithms/factory/test_missing_type.json";

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
        String jsonPath = "org/zafer/wflopalgorithms/factory/test_invalid_type.json";

        // When & Then
        AlgorithmLoadException exception = assertThrows(
            AlgorithmLoadException.class,
            () -> AlgorithmFactory.loadFromJson(jsonPath)
        );

        assertTrue(exception.getMessage().contains("Algorithm class not found") ||
                   exception.getMessage().contains("JSON resource not found"));
    }

    @Test
    void testStandardGAExecutesRun() throws AlgorithmLoadException {
        // Given
        String jsonPath = "org/zafer/wflopalgorithms/algorithms/standardga/algorithm_instance.json";
        Metaheuristic algorithm = AlgorithmFactory.loadFromJson(jsonPath);
        WFLOP problem = ConfigLoader.loadFromResource(
                "wflop_problem.json",
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
        String jsonPath = "org/zafer/wflopalgorithms/algorithms/pso/algorithm_instance.json";
        Metaheuristic algorithm = AlgorithmFactory.loadFromJson(jsonPath);
        WFLOP problem = ConfigLoader.loadFromResource(
                "wflop_problem.json",
                new TypeReference<WFLOP>() {});

        // When
        Solution solution = algorithm.run(problem);

        // Then
        assertNotNull(solution);
        assertTrue(solution.getFitness() > 0);
    }

    @Test
    void testClearCache() throws AlgorithmLoadException {
        // Given
        String jsonPath = "org/zafer/wflopalgorithms/algorithms/standardga/algorithm_instance.json";
        AlgorithmFactory.loadFromJson(jsonPath);
        assertEquals(1, AlgorithmFactory.getCacheSize());

        // When
        AlgorithmFactory.clearCache();

        // Then
        assertEquals(0, AlgorithmFactory.getCacheSize());
    }
}


