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
        if (population.isEmpty()) {
            return null;
        }
        
        Individual best = null;
        int effectiveTournamentSize = Math.max(1, Math.abs(tournamentSize));
        
        // If tournament size is >= population size, select the best from entire population
        if (effectiveTournamentSize >= population.size()) {
            for (Individual candidate : population) {
                if (best == null || candidate.getFitness() > best.getFitness()) {
                    best = candidate;
                }
            }
        } else {
            // Otherwise, do random tournament selection
            for (int i = 0; i < effectiveTournamentSize; i++) {
                Individual candidate = population.get(random.nextInt(population.size()));
                if (best == null || candidate.getFitness() > best.getFitness()) {
                    best = candidate;
                }
            }
        }
        return best;
    }
}
