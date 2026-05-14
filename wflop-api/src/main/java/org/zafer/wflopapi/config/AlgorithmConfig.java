package org.zafer.wflopapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import org.zafer.wflopalgorithms.factory.AlgorithmFactory;
import org.zafer.wflopalgorithms.factory.AlgorithmRegistry;
import org.zafer.wflopalgorithms.factory.DefaultAlgorithmRegistry;

@Component
public class AlgorithmConfig {

    @Bean
    public AlgorithmRegistry algorithmRegistry() {
        return new DefaultAlgorithmRegistry();
    }

    @Bean
    public AlgorithmFactory algorithmFactory(AlgorithmRegistry algorithmRegistry) {
        return new AlgorithmFactory(algorithmRegistry);
    }
}
