package org.zafer.wflopexperiments.model;

import java.util.List;

public class RunResult {

    private final int runIndex;
    private final List<ListenerData> listenerData;
    private final List<Integer> layout;

    public RunResult(int runIndex, List<ListenerData> listenerData, List<Integer> layout) {
        this.runIndex = runIndex;
        this.listenerData = listenerData;
        this.layout = layout;
    }

    public int getRunIndex() {
        return runIndex;
    }

    public List<ListenerData> getListenerData() {
        return listenerData;
    }

    public List<Integer> getLayout() {
        return layout;
    }
}
