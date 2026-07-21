package org.zafer.wflopmetaheuristic;

import java.util.List;

public interface Solution {

    double getFitness();
    List<Integer> getList();
}
