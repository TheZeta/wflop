package org.zafer.wflopexperiments.model;

import java.util.List;

public class RunResult {

    private final int runIndex;
    private final List<ListenerData> listenerData;

    public RunResult(int runIndex, List<ListenerData> listenerData) {
        this.runIndex = runIndex;
        this.listenerData = listenerData;
    }

    public int getRunIndex() {
        return runIndex;
    }

    public List<ListenerData> getListenerData() {
        return listenerData;
    }
}
