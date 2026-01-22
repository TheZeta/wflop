package org.zafer.wflopmodel.wind;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class WindProfile {

    private final double speed;
    private final int angle;
    private final double probability;

    private static int angleCount;
    private int index;

    @JsonCreator
    public WindProfile(
        @JsonProperty("speed") double speed,
        @JsonProperty("angle") int angle,
        @JsonProperty("probability") double probability
    ) {
        this.speed = speed;
        this.angle = angle;
        this.probability = probability;
    }

    public double getSpeed() {
        return speed;
    }

    public int getAngle() {
        return angle;
    }

    public double getProbability() {
        return probability;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public static int getAngleCount() {
        return angleCount;
    }

    public static void setAngleCount(int angleCount) {
        WindProfile.angleCount = angleCount;
    }
}
