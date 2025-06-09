package org.zafer.wflopga.strategy.selection;

import org.zafer.wflopga.Individual;

import java.util.List;

public interface SelectionStrategy {

    Individual select(List<Individual> population);
}
