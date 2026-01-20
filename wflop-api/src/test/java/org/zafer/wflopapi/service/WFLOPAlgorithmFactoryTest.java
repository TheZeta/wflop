package org.zafer.wflopapi.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import org.zafer.wflopalgorithms.algorithms.wdga.WDGA;
import org.zafer.wflopmetaheuristic.termination.GenerationBasedTermination;

public class WFLOPAlgorithmFactoryTest {

    @Test
    public void testLoadAlgorithmFactory() {
        // Given
        WFLOPAlgorithmFactory factory = new WFLOPAlgorithmFactory();

        // When
        WDGA algorithm = (WDGA) factory.createWDGA();

        // Then
        assertEquals(WDGA.class, algorithm.getClass());
        assertEquals("WDGA", algorithm.getAlgorithm());
        assertEquals(100, algorithm.getPopulationSize());
        assertEquals(0.3, algorithm.getCrossoverRate(), 0.001);
        assertEquals(0.1, algorithm.getMutationRate(), 0.001);
        assertEquals("tournament", algorithm.getSelectionStrategy());
        assertEquals(new GenerationBasedTermination(200), algorithm.getTerminationCondition());
    }
}
