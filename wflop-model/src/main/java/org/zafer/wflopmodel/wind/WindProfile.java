package org.zafer.wflopmodel.wind;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class WindProfile {

    private final double speed;
    private final int angle;

    @JsonCreator
    public WindProfile(
            @JsonProperty("speed") double speed,
            @JsonProperty("angle") int angle) {

        this.speed = speed;
        this.angle = angle;
    }

    public double getSpeed() {
        return speed;
    }

    public int getAngle() {
        return angle;
    }
}
