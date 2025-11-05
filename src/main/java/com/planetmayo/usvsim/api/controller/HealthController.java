package com.planetmayo.usvsim.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for health check.
 *
 * Endpoints:
 * - GET /api/health - Health status (used by Heroku, monitoring, etc.)
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    /**
     * Health check endpoint.
     *
     * @return status: "UP", timestamp, version
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", Instant.now().toString());
        response.put("version", "1.0.0");
        response.put("service", "usv-mission-planner-api");
        return ResponseEntity.ok(response);
    }
}
