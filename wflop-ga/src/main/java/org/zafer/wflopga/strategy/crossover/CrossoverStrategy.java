package org.zafer.wflopga.strategy.crossover;

import org.zafer.wflopga.Individual;
import org.zafer.wflopmodel.problem.WFLOP;

public interface CrossoverStrategy {

    Individual crossover(Individual p1, Individual p2, WFLOP problem);
}
