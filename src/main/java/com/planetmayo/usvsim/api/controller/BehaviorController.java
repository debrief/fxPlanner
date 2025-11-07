package com.planetmayo.usvsim.api.controller;

import com.planetmayo.usvsim.api.dto.ExpandingSquareRequest;
import com.planetmayo.usvsim.api.dto.ParallelTrackRequest;
import com.planetmayo.usvsim.api.dto.PatternResponse;
import com.planetmayo.usvsim.api.service.PatternGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * REST controller for behavior pattern generation.
 *
 * Endpoints:
 * - POST /api/behaviors/parallel-track/generate - Generate parallel track pattern
 * - POST /api/behaviors/expanding-square/generate - Generate expanding square pattern
 */
@RestController
@RequestMapping("/api/behaviors")
public class BehaviorController {

    private final PatternGenerationService patternGenerationService;

    @Autowired
    public BehaviorController(PatternGenerationService patternGenerationService) {
        this.patternGenerationService = patternGenerationService;
    }

    /**
     * Generate parallel track search pattern.
     *
     * @param request contains search area, track orientation, spacing, speed
     * @return generated waypoints with estimated duration
     */
    @PostMapping("/parallel-track/generate")
    public ResponseEntity<PatternResponse> generateParallelTrack(
            @Valid @RequestBody ParallelTrackRequest request) {
        PatternResponse response = patternGenerationService.generateParallelTrack(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Generate expanding square search pattern.
     *
     * @param request contains search area, initial direction, leg increment, speed
     * @return generated waypoints with estimated duration
     */
    @PostMapping("/expanding-square/generate")
    public ResponseEntity<PatternResponse> generateExpandingSquare(
            @Valid @RequestBody ExpandingSquareRequest request) {
        PatternResponse response = patternGenerationService.generateExpandingSquare(request);
        return ResponseEntity.ok(response);
    }
}
