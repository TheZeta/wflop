package org.zafer.wflopalgorithms.algorithms.wdga.strategy;

import java.util.*;

import org.zafer.wflopalgorithms.common.ga.solution.Individual;
import org.zafer.wflopalgorithms.common.ga.strategy.CrossoverStrategy;
import org.zafer.wflopmodel.problem.WFLOP;
import org.zafer.wflopmodel.wind.WindProfile;

/**
 * Wind-angle-based crossover strategy for WDGA.
 * Calculates the resultant wind vector from all wind profiles and uses its angle
 * to create a perpendicular slice line through the grid center. Divides solutions
 * into two halves based on which side of the slice line each cell falls on.
 * If offspring has incorrect number of turbines, applies repair operations.
 */
public class WakeBasedCrossoverStrategy implements CrossoverStrategy {

    private final Random random;

    public WakeBasedCrossoverStrategy() {
        this.random = new Random();
    }

    public WakeBasedCrossoverStrategy(long seed) {
        this.random = new Random(seed);
    }

    @Override
    public Individual crossover(Individual parent1, Individual parent2, WFLOP problem) {
        List<Integer> parent1Genes = parent1.getGenes();
        List<Integer> parent2Genes = parent2.getGenes();

        // Calculate resultant wind angle from all wind profiles
        int windAngle = calculateResultantWind(problem);

        Set<Integer> childGenes = new HashSet<>();

        // Find center point (e.g., for size 4, center is 1.5; for size 5, center is 2.0)
        double center = (problem.getDimension() - 1) / 2.0;

        // Convert custom angle to math radians
        double radians = Math.toRadians(windAngle - 90);
        double dx = Math.cos(radians);
        double dy = Math.sin(radians);

        for (int i = 0; i < problem.getCellCount(); i++) {
            // Get coordinates based on current size
            double x = i % problem.getDimension();
            double y = i / problem.getDimension();

            // Check which side of the center-point line the cell falls on
            double side = (x - center) * dy - (y - center) * dx;

            if (side < 1e-6) {
                if (parent1Genes.contains(i)) childGenes.add(i);
            } else {
                if (parent2Genes.contains(i)) childGenes.add(i);
            }
        }

        // Repair if necessary
        int targetTurbines = problem.getNumberOfTurbines();
        if (childGenes.size() < targetTurbines) {
            // Add turbines to reach target count
            addRandomTurbines(childGenes, targetTurbines, problem);
        } else if (childGenes.size() > targetTurbines) {
            // Remove excess turbines randomly
            removeRandomTurbines(childGenes, targetTurbines);
        }

        return new Individual(new ArrayList<>(childGenes));
    }

    /**
     * Calculates the resultant wind angle from all wind profiles.
     * Uses custom angle convention: 0° = North→South, 90° = West→East, 180° = South→North, 270° = East→West.
     * 
     * Wind vector for each profile: (sin(angle), -cos(angle))
     * Resultant angle is computed as: atan2(-resultant_y, resultant_x)
     * 
     * If the resultant magnitude is negligible (< 1e-6), a random wind profile is selected.
     * 
     * @param problem The WFLOP problem instance containing wind profiles
     * @return The wind angle in degrees (0-359)
     */
    private int calculateResultantWind(WFLOP problem) {
        List<WindProfile> windProfiles = problem.getWindProfiles();

        // Sum all wind profile vectors weighted by speed and probability
        double resultantX = 0.0;
        double resultantY = 0.0;

        for (WindProfile profile : windProfiles) {
            double angle = Math.toRadians(profile.getAngle());
            double windX = Math.sin(angle);
            double windY = -Math.cos(angle);

            double weight = profile.getSpeed() * profile.getProbability();
            resultantX += weight * windX;
            resultantY += weight * windY;
        }

        // Compute magnitude of resultant vector
        double magnitude = Math.sqrt(resultantX * resultantX + resultantY * resultantY);

        // If magnitude is negligible, pick a random wind profile
        if (magnitude < 1e-6) {
            int randomIndex = random.nextInt(windProfiles.size());
            return windProfiles.get(randomIndex).getAngle();
        }

        // Compute angle from resultant vector
        // Since vx = sin(θ) and vy = -cos(θ), we have θ = atan2(vx, -vy) = atan2(resultantX, -resultantY)
        double angleRadians = Math.atan2(resultantX, -resultantY);
        double angleDegrees = Math.toDegrees(angleRadians);

        // Normalize to [0, 360)
        int angle = (int) Math.round(angleDegrees);
        angle = ((angle % 360) + 360) % 360;

        return angle;
    }

    private void addRandomTurbines(Set<Integer> genes, int targetCount, WFLOP problem) {
        int layoutSize = problem.getCellCount();
        while (genes.size() < targetCount) {
            int randomGene = random.nextInt(layoutSize);
            genes.add(randomGene);
        }
    }

    private void removeRandomTurbines(Set<Integer> genes, int targetCount) {
        List<Integer> geneList = new ArrayList<>(genes);
        while (genes.size() > targetCount) {
            int randomIndex = random.nextInt(geneList.size());
            genes.remove(geneList.get(randomIndex));
            geneList.remove(randomIndex);
        }
    }
}
