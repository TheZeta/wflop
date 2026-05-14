package org.zafer.wflopalgorithms.factory;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.zafer.wflopalgorithms.algorithms.de.DE;
import org.zafer.wflopalgorithms.algorithms.fode.FODE;
import org.zafer.wflopalgorithms.algorithms.ga.GA;
import org.zafer.wflopalgorithms.algorithms.lshade.LSHADE;
import org.zafer.wflopalgorithms.algorithms.pso.PSO;
import org.zafer.wflopalgorithms.algorithms.sa.SA;
import org.zafer.wflopalgorithms.algorithms.wdga.WDGA;
import org.zafer.wflopmetaheuristic.Metaheuristic;

public class DefaultAlgorithmRegistryTest {

    private static final DefaultAlgorithmRegistry REGISTRY = new DefaultAlgorithmRegistry();
    private static final Map<String, Class<? extends Metaheuristic>> EXPECTED = Map.of(
        "GA", GA.class,
        "WDGA", WDGA.class,
        "PSO", PSO.class,
        "SA", SA.class,
        "DE", DE.class,
        "LSHADE", LSHADE.class,
        "FODE", FODE.class
    );

    @ParameterizedTest
    @MethodSource("provideExpectedRegistryMappings")
    void mapsAlgorithmsToCorrectClasses(String name, Class<? extends Metaheuristic> expectedClass)
        throws AlgorithmLoadException {

        assertEquals(expectedClass, REGISTRY.getAlgorithmClass(name));
    }

    private static Stream<Arguments> provideExpectedRegistryMappings() {
        return EXPECTED.entrySet().stream()
            .map(e -> Arguments.of(e.getKey(), e.getValue()));
    }

    @Test
    void throwsException_whenAlgorithmUnregistered() {
        // Given
        String unregistered = "UNREGISTERED_ALGO";
        AlgorithmLoadException ex = assertThrows(
            AlgorithmLoadException.class, () -> REGISTRY.getAlgorithmClass(unregistered)
        );

        assertTrue(ex.getMessage().contains(
            String.format("Algorithm '%s' is not registered", unregistered)
        ));
    }

    @Test
    void listsAvailableAlgorithms_whenAlgorithmUnregistered() {
        AlgorithmLoadException ex = assertThrows(
            AlgorithmLoadException.class, () -> REGISTRY.getAlgorithmClass("UNREGISTERED_ALGO")
        );

        assertTrue(ex.getMessage().contains("Available algorithms: "));
    }
}
