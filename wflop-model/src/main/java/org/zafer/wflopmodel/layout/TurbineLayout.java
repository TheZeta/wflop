package org.zafer.wflopmodel.layout;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class TurbineLayout {

    private final List<Integer> turbineIndices;

    @JsonCreator
    public TurbineLayout(
            @JsonProperty("turbineIndices") List<Integer> turbineIndices) {

        this.turbineIndices = turbineIndices;
    }

    public List<Integer> getTurbineIndices() {
        return turbineIndices;
    }
}
