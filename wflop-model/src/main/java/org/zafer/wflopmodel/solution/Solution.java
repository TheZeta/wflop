package org.zafer.wflopmodel.solution;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Solution {

    private final List<Integer> turbineIndices;

    @JsonCreator
    public Solution(
            @JsonProperty("turbineIndices") List<Integer> turbineIndices) {

        this.turbineIndices = turbineIndices;
    }

    public int[] getTurbineIndices() {
        return turbineIndices.stream().mapToInt(Integer::intValue).toArray();
    }
}
