package org.zafer.wflopexperiments.config;

public class ProblemConfig {

    private String id;
    private String path;

    // Required by Jackson
    public ProblemConfig() {
    }

    public ProblemConfig(String id, String path) {
        this.id = id;
        this.path = path;
    }

    public String getId() {
        return id;
    }

    public String getPath() {
        return path;
    }
}
