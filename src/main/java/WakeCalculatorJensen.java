public class WakeCalculatorJensen {

    private final WFLOP wflop;

    private final double[][][][] distanceMatrix;
    private final int indX = 0;
    private final int indY = 1;

    private final double[][][] intersectedAreaMatrix;

    private final double rotorRadius;
    private final double turbineSurfaceArea;
    private final double entrainmentConstant;

    public WakeCalculatorJensen(WFLOP wflop) {
        this.wflop = wflop;
        this.distanceMatrix = wflop.getDistanceMatrix();
        this.intersectedAreaMatrix = wflop.getIntersectedAreaMatrix();

        this.rotorRadius = wflop.getRotorRadius();
        this.turbineSurfaceArea = Math.PI * this.rotorRadius * this.rotorRadius;
        this.entrainmentConstant = wflop.getEntrainmentConstant();
    }

    public double calculateReducedSpeedMultiple(
            WindProfile windProfile,
            int downwindTurbineIndex,
            int[] turbineIndices) {

        int windAngle = windProfile.getAngle();
        double speedUnreduced = windProfile.getSpeed();
        double summation = 0;

        for (int upwindTurbineIndex : turbineIndices) {
            // Checks if downwind turbine is ahead of or in the same row as upwind turbine
            if (this.distanceMatrix[downwindTurbineIndex][upwindTurbineIndex][windAngle][indY] <= 0) {
                summation += 0; // Reduced speed is 0 if downwind turbine is ahead of or in the same row as upwind turbine
            } else {
                double singleWake = calculateReducedSpeedSingle(downwindTurbineIndex, upwindTurbineIndex, speedUnreduced, windAngle);
                summation += Math.pow(1 - (singleWake / speedUnreduced), 2)
                        * (calculateIntersectedArea(downwindTurbineIndex, upwindTurbineIndex, windAngle) / this.turbineSurfaceArea);
            }
        }

        return speedUnreduced * (1 - Math.sqrt(summation));
    }

    private double calculateReducedSpeedSingle(
            int downwindTurbineIndex,
            int upwindTurbineIndex,
            double speedUnreduced,
            int windAngle) {

        double radiusWake = rotorRadius + entrainmentConstant
                * this.distanceMatrix[downwindTurbineIndex][upwindTurbineIndex][windAngle][indY];
        return speedUnreduced * (1 - Math.pow(rotorRadius / radiusWake, 2) * 2 / 3);
    }

    private double calculateIntersectedArea(
            int downwindTurbineIndex,
            int upwindTurbineIndex,
            int windAngle) {

        return this.intersectedAreaMatrix[downwindTurbineIndex][upwindTurbineIndex][windAngle];
    }
}
