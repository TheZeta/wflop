public class WFLOP {

    private final double rotorRadius;
    private final double hubHeight;
    private final double rotorEfficiency;
    private final double thrustCoefficient;
    private final double airDensity;
    private final double surfaceRoughness;
    private final double gridWidth;
    private final int dimension;
    private final int cellCount;

    private final double axialInductionFactor;
    private final double entrainmentConstant;

    private final double[][] turbineCoordinates;        // [turbine][0=x, 1=y]
    private final double[][][][] distanceMatrix;        // [turbine][turbine][0=xDist, 1=yDist]
    private final double[][][] intersectedAreaMatrix;   // [turbine][turbine][angle]

    private final int indX = 0;
    private final int indY = 1;

    public WFLOP(
            double rotorRadius,
            double hubHeight,
            double rotorEfficiency,
            double thrustCoefficient,
            double airDensity,
            double surfaceRoughness,
            double gridWidth,
            int dimension) {

        this.rotorRadius = rotorRadius;
        this.hubHeight = hubHeight;
        this.rotorEfficiency = rotorEfficiency;
        this.thrustCoefficient = thrustCoefficient;
        this.airDensity = airDensity;
        this.surfaceRoughness = surfaceRoughness;
        this.gridWidth = gridWidth;
        this.dimension = dimension;

        this.cellCount = dimension * dimension;
        this.axialInductionFactor = 1 - Math.sqrt(1 - thrustCoefficient);
        this.entrainmentConstant = 0.5 / Math.log(hubHeight / surfaceRoughness);

        this.turbineCoordinates = new double[cellCount][2]; // 2 for x and y distances
        this.distanceMatrix = new double[cellCount][cellCount][360][2]; // 2 for x and y distances
        this.intersectedAreaMatrix = new double[cellCount][cellCount][360];

        initializeTurbineCoordinates();
        initializeDistanceMatrix();
        initializeIntersectedAreaMatrix();
    }

    private void initializeTurbineCoordinates() {
        for (int i = 0; i < cellCount; i++) {
            double x = (i % dimension + 0.5) * gridWidth;
            double y = (i / dimension + 0.5) * gridWidth;
            turbineCoordinates[i][indX] = x;
            turbineCoordinates[i][indY] = y;
        }
    }

    private void initializeDistanceMatrix() {
        for (int i = 0; i < cellCount; i++) {
            double xi = turbineCoordinates[i][indX];
            double yi = turbineCoordinates[i][indY];

            for (int j = 0; j < cellCount; j++) {
                double xj = turbineCoordinates[j][indX];
                double yj = turbineCoordinates[j][indY];

                double xDistance = xj - xi;
                double yDistance = yj - yi;

                for (int k = 0; k < 360; k++) {
                    double angleRad = Math.toRadians(k);
                    double cos = Math.cos(angleRad);
                    double sin = Math.sin(angleRad);

                    // Rotate (dx, dy) vector to align wind to y-axis
                    double rotatedX = xDistance * cos - yDistance * sin;
                    double rotatedY = xDistance * sin + yDistance * cos;

                    distanceMatrix[i][j][k][indX] = rotatedX;
                    distanceMatrix[i][j][k][indY] = rotatedY;
                }
            }
        }
    }

    private void initializeIntersectedAreaMatrix() {
        for (int i = 0; i < cellCount; i++) {
            for (int j = 0; j < cellCount; j++) {
                for (int k = 0; k < 360; k++) {
                    double wakeRadius = this.rotorRadius + entrainmentConstant
                            * distanceMatrix[i][j][k][indY];

                    double distanceBetweenCenters =
                            Math.abs(distanceMatrix[i][j][k][indX]);

                    double intersectedArea = 0;
                    if (distanceBetweenCenters <= Math.abs(wakeRadius - this.rotorRadius)) {
                        intersectedArea = Math.PI * Math.pow(this.rotorRadius, 2);
                    } else if (distanceBetweenCenters < Math.sqrt(Math.pow(wakeRadius, 2) - Math.pow(this.rotorRadius, 2))) {
                        intersectedArea = calculateS2(distanceBetweenCenters, wakeRadius, this.rotorRadius);
                    } else if (distanceBetweenCenters < wakeRadius + this.rotorRadius) {
                        intersectedArea = calculateS1(distanceBetweenCenters, wakeRadius, this.rotorRadius);
                    } else if (distanceBetweenCenters >= wakeRadius + this.rotorRadius){
                        intersectedArea = 0;
                    } else {
                        intersectedArea = -1; // Indicates invalid result
                    }
                    this.intersectedAreaMatrix[i][j][k] = intersectedArea;
                }
            }
        }
    }

    // Placeholder for the calculation of S1 (needs definition)
    private double calculateS1(double dx, double R, double r) {
        // Implement the logic for S1 here
        return 0; // Placeholder
    }

    // Placeholder for the calculation of S2 (needs definition)
    private double calculateS2(double dx, double R, double r) {
        // Implement the logic for S2 here
        return 0; // Placeholder
    }
    
    public double[][][][] getDistanceMatrix() {
        return distanceMatrix;
    }

    public double[][][] getIntersectedAreaMatrix() {
        return intersectedAreaMatrix;
    }

    public double getRotorRadius() {
        return rotorRadius;
    }

    public double getEntrainmentConstant() {
        return entrainmentConstant;
    }
}
