package org.zafer.wflopapi.controller;

import org.zafer.wflopapi.dto.AlgorithmDTO;
import org.zafer.wflopapi.dto.EvaluationRequestDTO;
import org.zafer.wflopapi.dto.ProblemDTO;
import org.zafer.wflopapi.dto.SolutionDTO;
import org.zafer.wflopapi.service.WFLOPService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wflop")
@CrossOrigin(origins = "*")
public class WFLOPController {

    private final WFLOPService wflopService;

    public WFLOPController(WFLOPService wflopService) {
        this.wflopService = wflopService;
    }

    @PostMapping("/solve")
    public ResponseEntity<SolutionDTO> solve(
        @RequestBody ProblemDTO problemDTO,
        @RequestBody AlgorithmDTO algorithmDTO
    ) {
        return ResponseEntity.ok(wflopService.solve(problemDTO, algorithmDTO));
    }

    @PostMapping("/evaluate")
    public ResponseEntity<SolutionDTO> evaluate(@RequestBody EvaluationRequestDTO request) {
        return ResponseEntity.ok(wflopService.evaluate(request.problem, request.solution));
    }
}