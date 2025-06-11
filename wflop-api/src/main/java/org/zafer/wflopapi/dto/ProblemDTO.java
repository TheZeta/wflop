package org.zafer.wflopapi.dto;

import java.util.List;

public class ProblemDTO {

    public double rotorRadius;
    public double hubHeight;
    public double rotorEfficiency;
    public double thrustCoefficient;
    public double airDensity;
    public double surfaceRoughness;
    public double gridWidth;
    public int dimension;
    public int numberOfTurbines;
    public List<WindProfileDTO> windProfiles;
}
