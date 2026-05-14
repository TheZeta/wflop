package org.zafer.wflopalgorithms.factory;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.zafer.wflopalgorithms.algorithms.ga.GA;
import org.zafer.wflopmetaheuristic.Metaheuristic;
import org.zafer.wflopmetaheuristic.termination.GenerationBasedTermination;
import org.zafer.wflopmetaheuristic.termination.TerminationCondition;

class AlgorithmFactoryIT {

    @TempDir
    Path tempDir;

    private AlgorithmFactory factory;

    @BeforeEach
    void setup() {
        factory = new AlgorithmFactory(new DefaultAlgorithmRegistry());
    }

    @Test
    void createsGA_whenValidJsonProvided() throws Exception {
        // Given
        Path jsonFile = tempDir.resolve("ga.json");

        String algorithm = "GA";
        int populationSize = 100;
        double crossoverRate = 0.8;
        double mutationRate = 0.01;
        String selectionStrategy = "tournament";
        String crossoverStrategy = "singlepoint";
        String mutationStrategy = "randomreplacement";
        String type = "generation";
        int maxGenerations = 200;

        TerminationCondition termination = new GenerationBasedTermination(maxGenerations);

        String json = """
        {
          "algorithm": "%s",
          "populationSize": %d,
          "crossoverRate": %f,
          "mutationRate": %f,
          "selectionStrategy": "%s",
          "crossoverStrategy": "%s",
          "mutationStrategy": "%s",
          "termination": {
            "type": "%s",
            "maxGenerations": %s
          }
        }
        """.formatted(algorithm, populationSize, crossoverRate,
            mutationRate,selectionStrategy, crossoverStrategy,
            mutationStrategy, type, maxGenerations);

        Files.writeString(jsonFile, json);

        // When
        Metaheuristic metaheuristic = factory.load(jsonFile.toString());

        // Then
        assertInstanceOf(GA.class, metaheuristic);

        GA ga = (GA) metaheuristic;
        assertEquals(populationSize, ga.getPopulationSize());
        assertEquals(crossoverRate, ga.getCrossoverRate(), 0.001);
        assertEquals(mutationRate, ga.getMutationRate(), 0.001);
        assertEquals(selectionStrategy, ga.getSelectionStrategy());
        assertEquals(crossoverStrategy, ga.getCrossoverStrategy());
        assertEquals(mutationStrategy, ga.getMutationStrategy());
        assertEquals(termination, ga.getTerminationCondition());
    }

    @Test
    void throwsException_WhenAlgorithmFieldMissing() throws IOException {
        // Given
        Path jsonFile = tempDir.resolve("missing_type.json");

        String json = """
        {
          "populationSize": 100,
          "crossoverRate": 0.8,
          "mutationRate": 0.01,
          "selectionStrategy": "tournament"
        }
        """;

        Files.writeString(jsonFile, json);

        // When & Then
        AlgorithmLoadException exception = assertThrows(
            AlgorithmLoadException.class,
            () -> factory.load(jsonFile.toString())
        );

        assertTrue(exception.getMessage().contains("algorithm"));
    }

    @Test
    void throwsException_WhenAlgorithmUnregistered() throws IOException {
        // Given
        Path jsonFile = tempDir.resolve("unregistered_algo.json");
        String unregistered = "UNREGISTERED_ALGO";

        String json = """
        {
          "algorithm": "%s"
        }
        """.formatted(unregistered);

        Files.writeString(jsonFile, json);

        // When
        AlgorithmLoadException exception = assertThrows(
            AlgorithmLoadException.class,
            () -> factory.load(jsonFile.toString())
        );

        // Then
        assertTrue(exception.getMessage().contains("is not registered") ||
            exception.getMessage().contains("JSON resource not found"));
    }
}
