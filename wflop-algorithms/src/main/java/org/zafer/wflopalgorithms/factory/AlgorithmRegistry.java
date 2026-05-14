package org.zafer.wflopalgorithms.factory;

import org.zafer.wflopmetaheuristic.Metaheuristic;

public interface AlgorithmRegistry {

    Class<? extends Metaheuristic> getAlgorithmClass(String name) throws AlgorithmLoadException;
}
