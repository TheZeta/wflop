package org.zafer.wflopalgorithms.algorithms.novelga.strategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.zafer.wflopalgorithms.common.ga.solution.Individual;
import org.zafer.wflopalgorithms.common.ga.strategy.MutationStrategy;
import org.zafer.wflopcore.calculator.PowerOutputCalculator;
import org.zafer.wflopmodel.problem.WFLOP;

public class WakeBasedMutationStrategy implements MutationStrategy {

    private final Random random;
    private final double wakeAnalysisPercentage; // Percentage of turbines to analyze
    private final double mutationSelectionPercentage; // Percentage of analyzed turbines to mutate
    private final PowerOutputCalculator powerOutputCalculator;

    public WakeBasedMutationStrategy(
        double wakeAnalysisPercentage,
        double mutationSelectionPercentage,
        PowerOutputCalculator powerOutputCalculator
    ) {

        this.wakeAnalysisPercentage = wakeAnalysisPercentage;
        this.mutationSelectionPercentage = mutationSelectionPercentage;
        this.random = new Random();
        this.powerOutputCalculator = powerOutputCalculator;
    }

    public WakeBasedMutationStrategy(
        double wakeAnalysisPercentage,
        double mutationSelectionPercentage,
        long seed,
        PowerOutputCalculator powerOutputCalculator
    ) {

        this.wakeAnalysisPercentage = wakeAnalysisPercentage;
        this.mutationSelectionPercentage = mutationSelectionPercentage;
        this.random = new Random(seed);
        this.powerOutputCalculator = powerOutputCalculator;
    }

    @Override
    public Individual mutate(Individual individual, WFLOP problem) {

        List<Integer> turbines = new ArrayList<>(individual.getGenes());
        int countForAnalysis = (int) (turbines.size() * wakeAnalysisPercentage);
        int countForMutation = (int) (countForAnalysis * mutationSelectionPercentage);

        List<Integer> turbinesToRemove = findTurbinesWithLowestPowerOutput(
            powerOutputCalculator,
            turbines,
            countForAnalysis,
            countForMutation);
        turbines.removeAll(turbinesToRemove);

        List<Integer> turbinesToAdd = findCellsWithHighestPowerOutput(
            powerOutputCalculator,
            turbines,
            countForAnalysis,
	    countForMutation,
            problem.getCellCount());
	turbines.addAll(turbinesToAdd);

        return new Individual(turbines);
    }

    private List<Integer> findTurbinesWithLowestPowerOutput(
        PowerOutputCalculator powerOutputCalculator,
        List<Integer> turbines,
        int countForAnalysis,
        int countForMutation
    ) {

        Map<Integer, Double> turbinePowerOutputMap = new HashMap<>();
        for(Integer turbine : turbines) {
            turbinePowerOutputMap.put(turbine, powerOutputCalculator.calculatePowerOutput(
                turbine,
                turbines
            ));
        }

        List<Integer> lowest = turbinePowerOutputMap.entrySet()
            .stream()
            .sorted(Map.Entry.comparingByValue())
            .limit(countForAnalysis)
            .map(Map.Entry::getKey)
            .toList();

        List<Integer> modifiableList = new ArrayList<>(lowest);
        Collections.shuffle(modifiableList);
        List<Integer> turbinesToRemove = modifiableList.subList(0, countForMutation);
        return turbinesToRemove;
    }

    private List<Integer> findCellsWithHighestPowerOutput( 
        PowerOutputCalculator powerOutputCalculator,
        List<Integer> turbines,
        int countForAnalysis,
        int countForMutation,
        int cellCount
    ) {

        Set<Integer> excludeSet = new HashSet<>(turbines);
        List<Integer> cells = new ArrayList<>(cellCount);
        for (int i = 0; i < cellCount; i++) {
            if (!excludeSet.contains(i)) {
                cells.add(i);
            }
        }

        Map<Integer, Double> cellPowerOutputMap = new HashMap<>();
        for(Integer cell : cells) {
            cellPowerOutputMap.put(cell, powerOutputCalculator.calculatePowerOutput(
                cell,
                turbines
            ));
        }

        List<Integer> highest = cellPowerOutputMap.entrySet()
            .stream()
            .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
            .limit(countForAnalysis)
            .map(Map.Entry::getKey)
            .toList();

        List<Integer> modifiableList = new ArrayList<>(highest);
        Collections.shuffle(modifiableList);
        List<Integer> turbinesToAdd = modifiableList.subList(0, countForMutation);
        return turbinesToAdd;
    }
}
