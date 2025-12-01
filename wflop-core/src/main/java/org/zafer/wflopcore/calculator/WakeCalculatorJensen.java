package org.zafer.wflopcore.calculator;

import org.zafer.wflopmodel.problem.WFLOP;
import org.zafer.wflopmodel.wind.WindProfile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WakeCalculatorJensen {

    private final WFLOP wflop;
    private final boolean useDistanceMatrix;
    private final boolean useIntersectedAreaMatrix;

    private final double[][][][] distanceMatrix;
    private final double[][][] intersectedAreaMatrix;

    private final double rotorRadius;
    private final double turbineSurfaceArea;
    private final double entrainmentConstant;

    private final int dimension;
    private final double gridWidth;
    private final List<Integer> profileIndexToAngle;  // angle per profile in WFLOP order
    private final double[] profileIndexToSpeed;       // speed per profile in WFLOP order
    private final int profileCount;
    private final int[] profileToAngleIndex;          // maps profile index -> angle index for matrix lookup
    private final Map<Long, Integer> profileKeyToIndex;  // maps (angle, speed bits) -> profile index
    private final double inverseTurbineSurfaceArea;

    private final int indX = 0;
    private final int indY = 1;

    public WakeCalculatorJensen(WFLOP wflop, WakeCalculationPolicy policy) {
        this.wflop = wflop;
        this.rotorRadius = wflop.getRotorRadius();
        this.turbineSurfaceArea = Math.PI * rotorRadius * rotorRadius;
        this.entrainmentConstant = wflop.getEntrainmentConstant();

        useDistanceMatrix = policy.useDistanceMatrix();
        useIntersectedAreaMatrix = policy.useIntersectedAreaMatrix();

        this.dimension = wflop.getDimension();
        this.gridWidth = wflop.getGridWidth();

        // Build profile-indexed arrays from WFLOP wind profiles
        List<WindProfile> windProfiles = wflop.getWindProfiles();
        if (windProfiles == null || windProfiles.isEmpty()) {
            throw new IllegalArgumentException("WFLOP must contain at least one wind profile");
        }
        this.profileCount = windProfiles.size();
        this.profileIndexToAngle = new ArrayList<>(profileCount);
        this.profileIndexToSpeed = new double[profileCount];

        // Build angle deduplication for matrix indexing
        List<Integer> uniqueAngles = new ArrayList<>();
        Map<Integer, Integer> angleToMatrixIndex = new HashMap<>();
        this.profileToAngleIndex = new int[profileCount];
        this.profileKeyToIndex = new HashMap<>();

        for (int i = 0; i < profileCount; i++) {
            WindProfile profile = windProfiles.get(i);
            int angle = profile.getAngle();
            double speed = profile.getSpeed();
            profileIndexToAngle.add(angle);
            profileIndexToSpeed[i] = speed;

            // Build profile key map for O(1) lookup
            long profileKey = makeProfileKey(angle, speed);
            profileKeyToIndex.put(profileKey, i);

            Integer matrixIndex = angleToMatrixIndex.get(angle);
            if (matrixIndex == null) {
                matrixIndex = uniqueAngles.size();
                uniqueAngles.add(angle);
                angleToMatrixIndex.put(angle, matrixIndex);
            }
            profileToAngleIndex[i] = matrixIndex;
        }

        this.inverseTurbineSurfaceArea = 1.0 / turbineSurfaceArea;

        this.distanceMatrix = useDistanceMatrix ? initializeDistanceMatrix(uniqueAngles) : null;
        this.intersectedAreaMatrix = useIntersectedAreaMatrix ? initializeIntersectedAreaMatrix(uniqueAngles) : null;
    }

    public double calculateReducedSpeedMultiple(WindProfile windProfile, int downwind, List<Integer> upwindTurbines) {
        // Find the profile index by matching angle and speed
        int angle = windProfile.getAngle();
        double speed = windProfile.getSpeed();
        int profileIndex = findProfileIndex(angle, speed);
        return calculateReducedSpeedMultipleByProfileIndex(profileIndex, downwind, upwindTurbines);
    }

    /**
     * Calculate reduced speed using profile index directly.
     * This method uses the profile index to select precomputed matrix slices.
     *
     * @param profileIndex the index of the wind profile in WFLOP order
     * @param downwind the downwind turbine index
     * @param upwindTurbines list of upwind turbine indices
     * @return the reduced wind speed at the downwind turbine
     */
    public double calculateReducedSpeedMultipleByProfileIndex(int profileIndex, int downwind, List<Integer> upwindTurbines) {
        int angle = profileIndexToAngle.get(profileIndex);
        int angleIndex = profileToAngleIndex[profileIndex];
        double baseSpeed = profileIndexToSpeed[profileIndex];
        double sum = 0;

        for (int upwind : upwindTurbines) {
            double[] rotated = useDistanceMatrix && distanceMatrix != null
                    ? distanceMatrix[downwind][upwind][angleIndex]
                    : computeRotatedDistance(downwind, upwind, angle);

            if (rotated[indY] <= 0) continue;

            double single = calculateReducedSpeedSingle(rotated[indY], baseSpeed);
            double overlap = useIntersectedAreaMatrix && intersectedAreaMatrix != null
                    ? intersectedAreaMatrix[downwind][upwind][angleIndex]
                    : computeIntersectedArea(rotated[indX], rotated[indY]);

            double ratio = 1 - single / baseSpeed;
            sum += ratio * ratio * (overlap * inverseTurbineSurfaceArea);
        }

        if (sum == 0) return baseSpeed;

        return baseSpeed * (1 - Math.sqrt(sum));
    }

    /**
     * Returns the number of wind profiles.
     *
     * @return the profile count
     */
    public int getProfileCount() {
        return profileCount;
    }

    private int findProfileIndex(int angle, double speed) {
        long profileKey = makeProfileKey(angle, speed);
        Integer index = profileKeyToIndex.get(profileKey);
        if (index == null) {
            throw new IllegalArgumentException("Wind profile with angle " + angle + " and speed " + speed + " is not part of the WFLOP wind profiles");
        }
        return index;
    }

    private static long makeProfileKey(int angle, double speed) {
        // Use angle in upper bits and raw double bits for speed to avoid floating-point comparison issues
        return ((long) angle << 32) | (Double.doubleToRawLongBits(speed) & 0xFFFFFFFFL);
    }

    private double calculateReducedSpeedSingle(double yDist, double baseSpeed) {
        double wakeRadius = rotorRadius + entrainmentConstant * yDist;
        double ratio = rotorRadius / wakeRadius;
        return baseSpeed * (1 - ratio * ratio * 2 / 3);
    }

    private double[][][][] initializeDistanceMatrix(List<Integer> uniqueAngles) {
        int cellCount = wflop.getCellCount();
        int angleCount = uniqueAngles.size();
        double[][][][] matrix = new double[cellCount][cellCount][angleCount][2];
        for (int i = 0; i < cellCount; i++) {
            for (int j = 0; j < cellCount; j++) {
                for (int k = 0; k < angleCount; k++) {
                    int angle = uniqueAngles.get(k);
                    matrix[i][j][k] = computeRotatedDistance(i, j, angle);
                }
            }
        }
        return matrix;
    }

    private double[][][] initializeIntersectedAreaMatrix(List<Integer> uniqueAngles) {
        int cellCount = wflop.getCellCount();
        int angleCount = uniqueAngles.size();
        double[][][] matrix = new double[cellCount][cellCount][angleCount];
        for (int i = 0; i < cellCount; i++) {
            for (int j = 0; j < cellCount; j++) {
                for (int k = 0; k < angleCount; k++) {
                    int angle = uniqueAngles.get(k);
                    double[] dist = useDistanceMatrix && distanceMatrix != null
                            ? distanceMatrix[i][j][k]
                            : computeRotatedDistance(i, j, angle);
                    matrix[i][j][k] = computeIntersectedArea(dist[indX], dist[indY]);
                }
            }
        }
        return matrix;
    }

    private double[] computeRotatedDistance(int from, int to, int angle) {
        double x1 = (from % dimension + 0.5) * gridWidth;
        double y1 = ((double) from / dimension + 0.5) * gridWidth;
        double x2 = (to % dimension + 0.5) * gridWidth;
        double y2 = ((double) to / dimension + 0.5) * gridWidth;

        double dx = x2 - x1;
        double dy = y2 - y1;

        double rad = Math.toRadians(angle);
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);

        return new double[]{
                dx * cos - dy * sin,
                dx * sin + dy * cos
        };
    }

    private double computeIntersectedArea(double dx, double dy) {
        double wakeRadius = rotorRadius + entrainmentConstant * dy;
        double centerDist = Math.abs(dx);

        if (centerDist <= Math.abs(wakeRadius - rotorRadius)) {
            return Math.PI * rotorRadius * rotorRadius;
        } else if (centerDist < Math.sqrt(wakeRadius * wakeRadius - rotorRadius * rotorRadius)) {
            return calculateS2(wakeRadius, rotorRadius, centerDist);
        } else if (centerDist < wakeRadius + rotorRadius) {
            return calculateS1(wakeRadius, rotorRadius, centerDist);
        } else {
            return 0;
        }
    }

    private double calculateS1(double R, double r, double dx) {
        double beta = Math.acos((R * R + dx * dx - r * r) / (2 * R * dx));
        double gamma = Math.acos((r * r + dx * dx - R * R) / (2 * r * dx));
        return R * R * beta + r * r * gamma - R * dx * Math.sin(beta);
    }

    private double calculateS2(double R, double r, double dx) {
        double beta = Math.acos((R * R + dx * dx - r * r) / (2 * R * dx));
        double gamma = Math.acos((r * r + dx * dx - R * R) / (2 * r * dx));
        return Math.PI * r * r - (Math.PI - gamma) * r * r + R * dx * Math.sin(beta) - beta * R * R;
    }
}
