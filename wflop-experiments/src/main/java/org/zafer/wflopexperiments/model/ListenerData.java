package org.zafer.wflopexperiments.model;

public class ListenerData {

    private final String listenerId;
    private final Object payload;

    public ListenerData(String listenerId, Object payload) {
        this.listenerId = listenerId;
        this.payload = payload;
    }

    public String getListenerId() {
        return listenerId;
    }

    public Object getPayload() {
        return payload;
    }
}
