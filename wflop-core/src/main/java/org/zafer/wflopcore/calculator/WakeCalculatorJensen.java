package org.zafer.wflopcore.calculator;

import org.zafer.wflopmodel.problem.WFLOP;
import org.zafer.wflopmodel.wind.WindProfile;

import java.util.List;

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

        this.distanceMatrix = useDistanceMatrix ? initializeDistanceMatrix() : null;
        this.intersectedAreaMatrix = useIntersectedAreaMatrix ? initializeIntersectedAreaMatrix() : null;
    }

    public double calculateReducedSpeedMultiple(WindProfile windProfile, int downwind, List<Integer> upwindTurbines) {
        int angle = windProfile.getAngle();
        double baseSpeed = windProfile.getSpeed();
        double sum = 0;

        for (int upwind : upwindTurbines) {
            double[] rotated = useDistanceMatrix && distanceMatrix != null
                    ? distanceMatrix[downwind][upwind][angle]
                    : computeRotatedDistance(downwind, upwind, angle);

            if (rotated[indY] <= 0) continue;

            double single = calculateReducedSpeedSingle(rotated[indY], baseSpeed);
            double overlap = useIntersectedAreaMatrix && intersectedAreaMatrix != null
                    ? intersectedAreaMatrix[downwind][upwind][angle]
                    : computeIntersectedArea(rotated[indX], rotated[indY]);

            sum += Math.pow(1 - single / baseSpeed, 2) * (overlap / turbineSurfaceArea);
        }

        return baseSpeed * (1 - Math.sqrt(sum));
    }

    private double calculateReducedSpeedSingle(double yDist, double baseSpeed) {
        double wakeRadius = rotorRadius + entrainmentConstant * yDist;
        return baseSpeed * (1 - Math.pow(rotorRadius / wakeRadius, 2) * 2 / 3);
    }

    private double[][][][] initializeDistanceMatrix() {
        int cellCount = wflop.getCellCount();
        double[][][][] matrix = new double[cellCount][cellCount][360][2];
        for (int i = 0; i < cellCount; i++) {
            for (int j = 0; j < cellCount; j++) {
                for (int k = 0; k < 360; k++) {
                    matrix[i][j][k] = computeRotatedDistance(i, j, k);
                }
            }
        }
        return matrix;
    }

    private double[][][] initializeIntersectedAreaMatrix() {
        int cellCount = wflop.getCellCount();
        double[][][] matrix = new double[cellCount][cellCount][360];
        for (int i = 0; i < cellCount; i++) {
            for (int j = 0; j < cellCount; j++) {
                for (int k = 0; k < 360; k++) {
                    double[] dist = useDistanceMatrix && distanceMatrix != null
                            ? distanceMatrix[i][j][k]
                            : computeRotatedDistance(i, j, k);
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
