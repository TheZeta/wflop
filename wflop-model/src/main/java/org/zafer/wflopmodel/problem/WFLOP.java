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
            @JsonProperty("windProfiles") WindProfile[] windProfiles
    ) {
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
        }

        this.numberOfTurbines = numberOfTurbines;
        this.windProfiles = windProfiles;

        this.axialInductionFactor = 1 - Math.sqrt(1 - thrustCoefficient);
        this.entrainmentConstant = 0.5 / Math.log(hubHeight / surfaceRoughness);
    }

    // Accessors
    public double getRotorRadius() { return rotorRadius; }
    public double getGridWidth() { return gridWidth; }
    public int getDimension() { return dimension; }
    public int getCellCount() { return cellCount; }
    public int getNumberOfTurbines() { return numberOfTurbines; }
    public WindProfile[] getWindProfiles() { return windProfiles; }
    public double getEntrainmentConstant() { return entrainmentConstant; }
}
