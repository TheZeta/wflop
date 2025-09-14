package org.zafer.wflopapi.service;

import org.springframework.stereotype.Service;
import org.zafer.wflopapi.dto.ProblemDTO;
import org.zafer.wflopapi.dto.SolutionDTO;
import org.zafer.wflopga.Individual;
import org.zafer.wflopmetaheuristic.Metaheuristic;
import org.zafer.wflopmetaheuristic.MetaheuristicRunner;
import org.zafer.wflopmetaheuristic.RunResult;
import org.zafer.wflopmodel.problem.WFLOP;
import org.zafer.wflopmodel.wind.WindProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Collectors;

@Service
public class WFLOPService {

    private final WFLOPAlgorithmFactory algorithmFactory;
    private static final Logger log = LoggerFactory.getLogger(WFLOPService.class);

    public WFLOPService(WFLOPAlgorithmFactory algorithmFactory) {
        this.algorithmFactory = algorithmFactory;
    }

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

        Metaheuristic<Individual> algorithm = algorithmFactory.createDefaultGA(problem);
        MetaheuristicRunner<Individual> runner = new MetaheuristicRunner<>(algorithm);

        final double[] firstBest = new double[] { Double.NaN };
        final double[] lastBest = new double[] { Double.NaN };
        runner.addListener(evt -> {
            if (Double.isNaN(firstBest[0])) firstBest[0] = evt.getBestFitness();
            lastBest[0] = evt.getBestFitness();
        });

        RunResult<Individual> result = runner.run();
        long durationMs = result.getMetrics().getDurationMs();
        int iterations = result.getMetrics().getIterations();
        double bestFitness = result.getMetrics().getBestFitness();
        double convergencePerIter = (iterations > 0 && !Double.isNaN(firstBest[0])) ?
                (lastBest[0] - firstBest[0]) / iterations : 0.0;
        double itersPerSecond = durationMs > 0 ? (iterations * 1000.0 / durationMs) : 0.0;
        log.info("WFLOP solve metrics - durationMs={}, iterations={}, bestFitness={}, convergencePerIter={}, itersPerSec={}",
                durationMs, iterations, bestFitness, convergencePerIter, itersPerSecond);

        Individual solution = result.getBestSolution();
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
        
        // Evaluate fitness using the same calculator as GA would use
        org.zafer.wflopcore.calculator.PowerOutputCalculator calculator = new org.zafer.wflopcore.calculator.PowerOutputCalculator(problem);
        double fitness = calculator.calculateTotalPowerOutput(individual.getSolution());

        return new SolutionDTO(solutionDto.layout, fitness);
    }
}