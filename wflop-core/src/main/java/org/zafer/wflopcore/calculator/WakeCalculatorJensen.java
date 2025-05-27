package org.zafer.wflopcore.calculator;

import org.zafer.wflopmodel.problem.WFLOP;
import org.zafer.wflopmodel.wind.WindProfile;

public class WakeCalculatorJensen {

    private final WFLOP wflop;
    private final double[][][][] distanceMatrix;
    private final double[][][] intersectedAreaMatrix;

    private final double rotorRadius;
    private final double turbineSurfaceArea;
    private final double entrainmentConstant;

    private final int indX = 0;
    private final int indY = 1;

    public WakeCalculatorJensen(WFLOP wflop) {
        this.wflop = wflop;
        this.distanceMatrix = wflop.getDistanceMatrix();
        this.intersectedAreaMatrix = wflop.getIntersectedAreaMatrix();
        this.rotorRadius = wflop.getRotorRadius();
        this.turbineSurfaceArea = Math.PI * rotorRadius * rotorRadius;
        this.entrainmentConstant = wflop.getEntrainmentConstant();
    }

    public double calculateReducedSpeedMultiple(WindProfile windProfile, int downwind, int[] upwindTurbines) {
        int angle = windProfile.getAngle();
        double baseSpeed = windProfile.getSpeed();
        double sum = 0;
        boolean useDistanceMatrix = wflop.getUseDistanceMatrix();
        boolean useIntersectedAreaMatrix = wflop.getUseIntersectedAreaMatrix();

        for (int upwind : upwindTurbines) {
            double[] rotated = useDistanceMatrix && distanceMatrix != null
                    ? distanceMatrix[downwind][upwind][angle]
                    : wflop.computeRotatedDistance(downwind, upwind, angle);

            if (rotated[indY] <= 0) continue;

            double single = calculateReducedSpeedSingle(rotated[indY], baseSpeed);
            double overlap = useIntersectedAreaMatrix && intersectedAreaMatrix != null
                    ? intersectedAreaMatrix[downwind][upwind][angle]
                    : wflop.computeIntersectedArea(rotated[indX], rotated[indY]);

            sum += Math.pow(1 - single / baseSpeed, 2) * (overlap / turbineSurfaceArea);
        }

        return baseSpeed * (1 - Math.sqrt(sum));
    }

    private double calculateReducedSpeedSingle(double yDist, double baseSpeed) {
        double wakeRadius = rotorRadius + entrainmentConstant * yDist;
        return baseSpeed * (1 - Math.pow(rotorRadius / wakeRadius, 2) * 2 / 3);
    }
}
