package org.zafer.wflopexperiments.config;

import java.util.Map;

public class ProcessorConfig {

    private String id;
    private Map<String, Object> params;
    private boolean incremental = true;  // default: true (process immediately after each algorithm)

    public String getId() {
        return id;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public boolean isIncremental() {
        return incremental;
    }
}
