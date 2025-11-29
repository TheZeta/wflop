package org.zafer.wflopcore.model;

public class GEOnePointFiveSLEPowerModel implements PowerModel {

    @Override
    public double getPowerOutput(double speed) {
        if (speed >= 2 && speed < 12.8) {
            return 0.3 * speed * speed * speed;
        } else if (speed >= 12.8 && speed <= 18) {
            return 629.1;
        } else {
            return 0;
        }
    }
}
