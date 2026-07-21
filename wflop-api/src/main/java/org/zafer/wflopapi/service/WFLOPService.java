package org.zafer.wflopapi.service;

import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import org.zafer.wflopalgorithms.factory.AlgorithmFactory;
import org.zafer.wflopalgorithms.factory.AlgorithmLoadException;
import org.zafer.wflopapi.dto.AlgorithmDTO;
import org.zafer.wflopapi.dto.ProblemDTO;
import org.zafer.wflopapi.dto.SolutionDTO;
import org.zafer.wflopalgorithms.common.ga.solution.Individual;
import org.zafer.wflopcore.power.PowerCalculator;
import org.zafer.wflopmetaheuristic.Metaheuristic;
import org.zafer.wflopmetaheuristic.Solution;
import org.zafer.wflopmodel.layout.TurbineLayout;
import org.zafer.wflopmodel.problem.WFLOP;
import org.zafer.wflopmodel.wind.WindProfile;

@Service
public class WFLOPService {

    private final AlgorithmFactory algorithmFactory;

    public WFLOPService(AlgorithmFactory algorithmFactory) {
        this.algorithmFactory = algorithmFactory;
    }

    public SolutionDTO solve(ProblemDTO problemDTO, AlgorithmDTO algorithmDTO) {
        WFLOP problem = createWFLOP(problemDTO);

        Metaheuristic algorithm;
        try {
            algorithm = algorithmFactory.load(algorithmDTO.path);
        } catch (AlgorithmLoadException e) {
            throw new RuntimeException(e);
        }

        Solution solution = algorithm.run(problem);
        Individual individualSolution = (Individual) solution;
        return new SolutionDTO(individualSolution.getList(), individualSolution.getFitness());
    }

    public SolutionDTO evaluate(ProblemDTO problemDTO, SolutionDTO solutionDTO) {
        WFLOP problem = createWFLOP(problemDTO);

        Individual individual = new Individual(solutionDTO.layout);
        PowerCalculator calculator = new PowerCalculator(problem);
        TurbineLayout layout = new TurbineLayout(individual.getList());
        double fitness = calculator.calculateTotalPower(layout);

        return new SolutionDTO(solutionDTO.layout, fitness);
    }

    private WFLOP createWFLOP(ProblemDTO problemDTO) {
        return new WFLOP(
            problemDTO.rotorRadius,
            problemDTO.hubHeight,
            problemDTO.rotorEfficiency,
            problemDTO.thrustCoefficient,
            problemDTO.airDensity,
            problemDTO.surfaceRoughness,
            problemDTO.gridWidth,
            problemDTO.dimension,
            problemDTO.numberOfTurbines,
            problemDTO.windProfiles.stream()
                .map(wp -> new WindProfile(wp.speed, wp.angle, wp.probability))
                .collect(Collectors.toList())
        );
    }
}
