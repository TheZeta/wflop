package org.zafer.wflopmodel.problem;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.zafer.wflopmodel.wind.WindProfile;

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
    private final int numberOfTurbines;
    private final WindProfile[] windProfiles;

    private final double axialInductionFactor;
    private final double entrainmentConstant;

    private final double[][] turbineCoordinates;        // [turbine][0=x, 1=y]
    private final double[][][][] distanceMatrix;        // [turbine][turbine][angle][0=xDist, 1=yDist]
    private final double[][][] intersectedAreaMatrix;   // [turbine][turbine][angle]

    private final boolean useDistanceMatrix;
    private final boolean useIntersectedAreaMatrix;

    private final int indX = 0;
    private final int indY = 1;

    @JsonCreator
    public WFLOP(
            @JsonProperty("rotorRadius") double rotorRadius,
            @JsonProperty("hubHeight") double hubHeight,
            @JsonProperty("rotorEfficiency") double rotorEfficiency,
            @JsonProperty("thrustCoefficient") double thrustCoefficient,
            @JsonProperty("airDensity") double airDensity,
            @JsonProperty("surfaceRoughness") double surfaceRoughness,
            @JsonProperty("gridWidth") double gridWidth,
            @JsonProperty("dimension") int dimension,
            @JsonProperty("numberOfTurbines") int numberOfTurbines,
            @JsonProperty("windProfiles") WindProfile[] windProfiles,
            @JsonProperty("useDistanceMatrix") boolean useDistanceMatrix,
            @JsonProperty("useIntersectedAreaMatrix") boolean useIntersectedAreaMatrix) {

        this.rotorRadius = rotorRadius;
        this.hubHeight = hubHeight;
        this.rotorEfficiency = rotorEfficiency;
        this.thrustCoefficient = thrustCoefficient;
        this.airDensity = airDensity;
        this.surfaceRoughness = surfaceRoughness;
        this.gridWidth = gridWidth;
        this.dimension = dimension;
        this.cellCount = dimension * dimension;

        if (numberOfTurbines > this.cellCount) {
            throw new IllegalArgumentException("numberOfTurbines (" + numberOfTurbines +
                    ") cannot exceed grid cell count (" + this.cellCount + ")");
        } else {
            this.numberOfTurbines = numberOfTurbines;
        }

        this.windProfiles = windProfiles;

        this.axialInductionFactor = 1 - Math.sqrt(1 - thrustCoefficient);
        this.entrainmentConstant = 0.5 / Math.log(hubHeight / surfaceRoughness);

        this.turbineCoordinates = new double[cellCount][2];
        this.useDistanceMatrix = useDistanceMatrix;
        this.useIntersectedAreaMatrix = useIntersectedAreaMatrix;

        this.distanceMatrix = useDistanceMatrix ? new double[cellCount][cellCount][360][2] : null;
        this.intersectedAreaMatrix = useIntersectedAreaMatrix ? new double[cellCount][cellCount][360] : null;

        initializeTurbineCoordinates();
        if (useDistanceMatrix) initializeDistanceMatrix();
        if (useIntersectedAreaMatrix) initializeIntersectedAreaMatrix();
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
            for (int j = 0; j < cellCount; j++) {
                for (int k = 0; k < 360; k++) {
                    distanceMatrix[i][j][k] = computeRotatedDistance(i, j, k);
                }
            }
        }
    }

    private void initializeIntersectedAreaMatrix() {
        for (int i = 0; i < cellCount; i++) {
            for (int j = 0; j < cellCount; j++) {
                for (int k = 0; k < 360; k++) {
                    double[] dist = useDistanceMatrix ?
                            distanceMatrix[i][j][k] :
                            computeRotatedDistance(i, j, k);
                    intersectedAreaMatrix[i][j][k] = computeIntersectedArea(dist[indX], dist[indY]);
                }
            }
        }
    }

    public double[] computeRotatedDistance(int from, int to, int angle) {
        double x1 = turbineCoordinates[from][indX];
        double y1 = turbineCoordinates[from][indY];
        double x2 = turbineCoordinates[to][indX];
        double y2 = turbineCoordinates[to][indY];

        double dx = x2 - x1;
        double dy = y2 - y1;

        double rad = Math.toRadians(angle);
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);

        return new double[] {
                dx * cos - dy * sin,
                dx * sin + dy * cos
        };
    }

    public double computeIntersectedArea(double dx, double dy) {
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

    public int getCellCount() {
        return cellCount;
    }

    public int getNumberOfTurbines() {
        return numberOfTurbines;
    }

    public WindProfile[] getWindProfiles() {
        return windProfiles;
    }

    public boolean getUseDistanceMatrix() {
        return useDistanceMatrix;
    }

    public boolean getUseIntersectedAreaMatrix() {
        return useIntersectedAreaMatrix;
    }
}
