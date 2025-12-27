package org.zafer.wflopcore.wake;

public class WakeModelPolicy {

    private final boolean useDistanceMatrix;
    private final boolean useIntersectedAreaMatrix;

    public WakeModelPolicy(boolean useDistanceMatrix, boolean useIntersectedAreaMatrix) {
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
