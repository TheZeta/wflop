package org.zafer.wflopcore.model;

public class DefaultPowerModelProvider implements PowerModelProvider {

    @Override
    public PowerModel create() {
        return new GEOnePointFiveSLEPowerModel();
    }
}
