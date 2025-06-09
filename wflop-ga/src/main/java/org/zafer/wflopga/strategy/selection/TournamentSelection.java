package org.zafer.wflopga.strategy.selection;

import org.zafer.wflopga.Individual;

import java.util.List;
import java.util.Random;

public class TournamentSelection implements SelectionStrategy {

    private final int tournamentSize;
    private final Random random = new Random();

    public TournamentSelection(int tournamentSize) {
        this.tournamentSize = tournamentSize;
    }

    @Override
    public Individual select(List<Individual> population) {
        Individual best = null;
        for (int i = 0; i < tournamentSize; i++) {
            Individual candidate = population.get(random.nextInt(population.size()));
            if (best == null || candidate.getFitness() > best.getFitness()) {
                best = candidate;
            }
        }
        return best;
    }
}
