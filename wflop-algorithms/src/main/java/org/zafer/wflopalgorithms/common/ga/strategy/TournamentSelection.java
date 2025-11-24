package org.zafer.wflopalgorithms.common.ga.strategy;

import org.zafer.wflopalgorithms.common.ga.solution.Individual;

import java.util.List;
import java.util.Random;

/**
 * Tournament selection strategy.
 * Selects the best individual from a random tournament subset of the population.
 */
public class TournamentSelection implements SelectionStrategy {

    private final int tournamentSize;
    private final Random random;

    public TournamentSelection(int tournamentSize) {
        this.tournamentSize = tournamentSize;
        this.random = new Random();
    }

    public TournamentSelection(int tournamentSize, long seed) {
        this.tournamentSize = tournamentSize;
        this.random = new Random(seed);
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

