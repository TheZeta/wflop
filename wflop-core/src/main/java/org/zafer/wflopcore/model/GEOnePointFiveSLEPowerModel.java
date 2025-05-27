package org.zafer.wflopcore.model;

public class GEOnePointFiveSLEPowerModel implements PowerModel {

    @Override
    public double getPowerOutput(double speed) {
        if (speed >= 2 && speed < 12.8) {
            return 0.3 * Math.pow(speed, 3);
        } else if (speed >= 12.8 && speed <= 18) {
            return 629.1;
        } else {
            return 0;
        }
    }
}
