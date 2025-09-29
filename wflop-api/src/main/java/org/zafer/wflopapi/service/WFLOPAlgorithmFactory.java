package org.zafer.wflopapi.service;

import org.zafer.wflopga.GeneticAlgorithm;
import org.zafer.wflopga.strategy.crossover.SinglePointCrossover;
import org.zafer.wflopga.strategy.mutation.RandomReplacementMutation;
import org.zafer.wflopga.strategy.selection.TournamentSelection;
import org.zafer.wflopmetaheuristic.Metaheuristic;
import org.zafer.wflopmodel.problem.WFLOP;
import org.springframework.stereotype.Component;

@Component
public class WFLOPAlgorithmFactory {

    public Metaheuristic createDefaultGA(WFLOP problem) {
        return new GeneticAlgorithm(
                problem,
                100,
                200,
                new SinglePointCrossover(0.7),
                new RandomReplacementMutation(0.1),
                new TournamentSelection(3)
        );
    }
}


