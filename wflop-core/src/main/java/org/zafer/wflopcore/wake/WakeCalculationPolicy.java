package org.zafer.wflopcore.wake;

public class WakeCalculationPolicy {

    private final boolean useDistanceMatrix;
    private final boolean useIntersectedAreaMatrix;

    public WakeCalculationPolicy(boolean useDistanceMatrix, boolean useIntersectedAreaMatrix) {
        this.useDistanceMatrix = useDistanceMatrix;
        this.useIntersectedAreaMatrix = useIntersectedAreaMatrix;
    }

    public boolean useDistanceMatrix() {
        return useDistanceMatrix;
    }

    public boolean useIntersectedAreaMatrix() {
        return useIntersectedAreaMatrix;
    }
}
