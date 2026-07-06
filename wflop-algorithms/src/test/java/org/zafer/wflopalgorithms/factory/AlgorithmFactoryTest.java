package org.zafer.wflopalgorithms.factory;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.zafer.wflopalgorithms.algorithms.wdga.WDGA;
import org.zafer.wflopmetaheuristic.Metaheuristic;
import org.zafer.wflopmetaheuristic.termination.GenerationBasedTermination;
import org.zafer.wflopmetaheuristic.termination.TerminationCondition;

@ExtendWith(MockitoExtension.class)
class AlgorithmFactoryTest {

    @Mock
    private AlgorithmRegistry registry;

    @InjectMocks
    private AlgorithmFactory factory;

    @Test
    void createsWDGA_whenValidJsonProvided() throws JsonProcessingException, AlgorithmLoadException {
        // Given
        doReturn(WDGA.class).when(registry).getAlgorithmClass("WDGA");

        String algorithm = "WDGA";
        int populationSize = 100;
        double crossoverRate = 0.3;
        double mutationRate = 0.1;
        double smartMutationRate = 0.6;
        String selectionStrategy = "tournament";
        double wakeAnalysisPercentage = 0.1;
        double mutationSelectionPercentage = 0.5;
        String type = "generation";
        int maxGenerations = 200;

        String json = """
        {
          "algorithm": "%s",
          "populationSize": %d,
          "crossoverRate": %f,
          "mutationRate": %f,
          "smartMutationRate": %f,
          "selectionStrategy": "%s",
          "wakeAnalysisPercentage": %f,
          "mutationSelectionPercentage": %f,
          "termination": {
            "type": "%s",
            "maxGenerations": %d
          }
        }
        """.formatted(
            algorithm, populationSize, crossoverRate, mutationRate,
            smartMutationRate, selectionStrategy, wakeAnalysisPercentage,
            mutationSelectionPercentage, type, maxGenerations
        );

        JsonNode node = new ObjectMapper().readTree(json);

        // When
        Metaheuristic metaheuristic = factory.load(node);

        // Then
        assertInstanceOf(WDGA.class, metaheuristic);
    }

    @Test
    void throwsException_whenAlgorithmFieldMissing() {
        // Given: empty JSON object (i.e., {})
        ObjectNode node = JsonNodeFactory.instance.objectNode();

        // When & Then
        AlgorithmLoadException ex = assertThrows(
            AlgorithmLoadException.class,
            () -> factory.load(node)
        );

        assertTrue(ex.getMessage().contains("Missing required field: 'algorithm'"));
    }
}
