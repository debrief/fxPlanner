package com.planetmayo.usvsim.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot main application for USV Mission Planner web API.
 *
 * Provides stateless REST endpoints for:
 * - Simulation tick execution
 * - Pattern generation (parallel track, expanding square)
 * - Mission serialization/deserialization (GeoJSON)
 *
 * Configuration:
 * - PORT binding via application.properties (defaults to 8080)
 * - CORS enabled for frontend development (localhost:3000)
 * - Static resources served from /static (embedded React build)
 */
@SpringBootApplication(scanBasePackages = "com.planetmayo.usvsim")
public class USVWebApplication {
    public static void main(String[] args) {
        SpringApplication.run(USVWebApplication.class, args);
    }
}
