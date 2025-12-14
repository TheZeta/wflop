package org.zafer.wflopcore.power;

public class DefaultPowerModelProvider implements PowerModelProvider {

    @Override
    public PowerModel create() {
        return new GEOnePointFiveSLEPowerModel();
    }
}
