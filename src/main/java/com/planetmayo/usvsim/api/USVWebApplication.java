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
 *
 * Note: Scans only api, model, and util packages to avoid JavaFX desktop dependencies.
 * Desktop application uses Main.java as entry point.
 */
@SpringBootApplication(scanBasePackages = {
    "com.planetmayo.usvsim.api",
    "com.planetmayo.usvsim.model",
    "com.planetmayo.usvsim.util"
})
public class USVWebApplication {
    public static void main(String[] args) {
        System.out.println("=== USVWebApplication.main() STARTED ===");
        System.out.println("Java version: " + System.getProperty("java.version"));
        System.out.println("Args: " + java.util.Arrays.toString(args));

        try {
            System.out.println("=== About to call SpringApplication.run() ===");
            SpringApplication.run(USVWebApplication.class, args);
            System.out.println("=== SpringApplication.run() COMPLETED ===");
        } catch (Throwable t) {
            System.err.println("=== EXCEPTION in SpringApplication.run() ===");
            t.printStackTrace();
            throw t;
        }

        System.out.println("=== USVWebApplication.main() COMPLETED ===");
    }
}
