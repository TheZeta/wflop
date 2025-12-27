package org.zafer.wflopcore.wake;

public enum WakeOptimization {

    NONE,
    DISTANCE_MATRIX,
    INTERSECTION_MATRIX,
    BOTH;

    public boolean useDistanceMatrix() {
        return this == DISTANCE_MATRIX || this == BOTH;
    }

    public boolean useIntersectionMatrix() {
        return this == INTERSECTION_MATRIX || this == BOTH;
    }
}
