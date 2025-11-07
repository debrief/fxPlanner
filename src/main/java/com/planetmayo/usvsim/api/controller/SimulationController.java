package com.planetmayo.usvsim.api.controller;

import com.planetmayo.usvsim.api.dto.SimulationTickRequest;
import com.planetmayo.usvsim.api.dto.SimulationTickResponse;
import com.planetmayo.usvsim.api.service.SimulationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * REST controller for stateless simulation tick execution.
 *
 * Endpoints:
 * - POST /api/simulation/tick - Execute single simulation timestep
 */
@RestController
@RequestMapping("/api/simulation")
public class SimulationController {

    private final SimulationService simulationService;

    @Autowired
    public SimulationController(SimulationService simulationService) {
        this.simulationService = simulationService;
    }

    /**
     * Execute single simulation tick.
     *
     * Stateless operation: client sends current state, server returns next state.
     *
     * @param request contains platform state, behavior state, deltaTime
     * @return updated platform state, behavior state, demand, completion flags
     */
    @PostMapping("/tick")
    public ResponseEntity<SimulationTickResponse> tick(@Valid @RequestBody SimulationTickRequest request) {
        SimulationTickResponse response = simulationService.tick(request);
        return ResponseEntity.ok(response);
    }
}
