package org.zafer.wflopga.strategy.mutation;

import org.zafer.wflopga.Individual;
import org.zafer.wflopmodel.problem.WFLOP;

public interface MutationStrategy {

    Individual mutate(Individual individual, WFLOP problem);
}
