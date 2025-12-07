package org.zafer.wflopapi.service;

import org.zafer.wflopalgorithms.factory.AlgorithmFactory;
import org.zafer.wflopalgorithms.factory.AlgorithmLoadException;
import org.zafer.wflopmetaheuristic.Metaheuristic;
import org.springframework.stereotype.Component;

@Component
public class WFLOPAlgorithmFactory {

    public Metaheuristic createNovelGA() {
        String jsonPath = "configs/novelga.json";
        Metaheuristic algorithm = null;

        try {
            algorithm = AlgorithmFactory.loadFromJson(jsonPath);
        } catch (AlgorithmLoadException e) {
            throw new RuntimeException(e);
        }

        return algorithm;
    }
}
