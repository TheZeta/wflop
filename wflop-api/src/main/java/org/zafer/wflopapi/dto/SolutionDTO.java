package org.zafer.wflopapi.dto;

import java.util.List;

public class SolutionDTO {

    public List<Integer> layout;
    public double fitness;

    // Default constructor for Jackson deserialization
    public SolutionDTO() {
    }

    public SolutionDTO(List<Integer> layout, double fitness) {
        this.layout = layout;
        this.fitness = fitness;
    }
}