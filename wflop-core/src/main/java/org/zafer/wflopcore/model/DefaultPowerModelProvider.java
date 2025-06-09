package org.zafer.wflopcore.model;

import org.zafer.wflopmodel.problem.WFLOP;

public class DefaultPowerModelProvider implements PowerModelProvider {

    @Override
    public PowerModel create() {
        return new GEOnePointFiveSLEPowerModel();
    }
}
