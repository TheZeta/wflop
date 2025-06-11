package org.zafer.wflopapi.service;

import org.springframework.stereotype.Service;
import org.zafer.wflopapi.dto.ProblemDTO;
import org.zafer.wflopapi.dto.SolutionDTO;
import org.zafer.wflopga.GeneticAlgorithm;
import org.zafer.wflopga.Individual;
import org.zafer.wflopga.strategy.crossover.SinglePointCrossover;
import org.zafer.wflopga.strategy.mutation.RandomReplacementMutation;
import org.zafer.wflopga.strategy.selection.TournamentSelection;
import org.zafer.wflopmodel.problem.WFLOP;
import org.zafer.wflopmodel.wind.WindProfile;

import java.util.stream.Collectors;

@Service
public class WFLOPService {

    public SolutionDTO solve(ProblemDTO dto) {
        WFLOP problem = new WFLOP(
                dto.rotorRadius,
                dto.hubHeight,
                dto.rotorEfficiency,
                dto.thrustCoefficient,
                dto.airDensity,
                dto.surfaceRoughness,
                dto.gridWidth,
                dto.dimension,
                dto.numberOfTurbines,
                dto.windProfiles.stream()
                        .map(wp -> new WindProfile(wp.speed, wp.angle))
                        .collect(Collectors.toList())
        );

        GeneticAlgorithm ga = new GeneticAlgorithm(
                problem,
                100, // population size
                200, // number of generations
                new SinglePointCrossover(),
                new RandomReplacementMutation(0.1),
                new TournamentSelection(3)
        );

        Individual solution = ga.run();

        return new SolutionDTO(solution.getTurbineIndices(), solution.getFitness());
    }

    public SolutionDTO evaluate(ProblemDTO problemDto, SolutionDTO solutionDto) {
        // Convert DTO to domain objects
        WFLOP problem = new WFLOP(
                problemDto.rotorRadius,
                problemDto.hubHeight,
                problemDto.rotorEfficiency,
                problemDto.thrustCoefficient,
                problemDto.airDensity,
                problemDto.surfaceRoughness,
                problemDto.gridWidth,
                problemDto.dimension,
                problemDto.numberOfTurbines,
                problemDto.windProfiles.stream()
                        .map(wp -> new WindProfile(wp.speed, wp.angle))
                        .collect(Collectors.toList())
        );

        // Create an Individual with the given layout and evaluate its fitness
        Individual individual = new Individual(solutionDto.layout);
        
        // Create a temporary GA instance to access the fitness computation
        GeneticAlgorithm ga = new GeneticAlgorithm(
                problem,
                1, // minimal population size since we're only evaluating one
                1, // minimal generations since we're only evaluating
                new SinglePointCrossover(),
                new RandomReplacementMutation(0.1),
                new TournamentSelection(3)
        );

        // Evaluate the fitness using the GA's fitness computation
        double fitness = ga.computeFitness(individual);

        return new SolutionDTO(solutionDto.layout, fitness);
    }
}