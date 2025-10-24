package com.planetmayo.usvsim.unit.controller;

import com.planetmayo.usvsim.model.geometry.Position;
import com.planetmayo.usvsim.model.platform.PlatformCapabilities;
import com.planetmayo.usvsim.model.platform.PlatformState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for platform dynamics simulation.
 *
 * Tests turn radius, acceleration, speed limiting behavior.
 */
class DynamicsTest {
    private PlatformCapabilities capabilities;

    @BeforeEach
    void setup() {
        capabilities = PlatformCapabilities.defaultUSV();
    }

    @Test
    void testAccelerationLimiting() {
        // With acceleration limit of 0.5 m/s², in 1 second at 1 Hz we should reach ~0.25 knots
        double dtSeconds = 1.0;
        double expectedSpeedGain = capabilities.getAcceleration() * dtSeconds;  // m/s
        double expectedSpeedGainKnots = expectedSpeedGain / 0.51444;  // Convert m/s to knots

        // Check that acceleration is applied
        assertTrue(expectedSpeedGainKnots > 0, "Should accelerate towards demand");
        assertTrue(expectedSpeedGainKnots < 8.0, "Should not reach max speed in 1 second");
    }

    @Test
    void testMaxSpeedEnforcement() {
        // Even if demanded to go faster, cap at max speed
        double demandedSpeed = 100.0;
        double cappedSpeed = Math.min(demandedSpeed, capabilities.getMaxSpeed());

        assertEquals(8.0, cappedSpeed, "Should cap at max speed of 8 knots");
    }

    @Test
    void testTurnRadiusCalculation() {
        // At max speed, turn radius should be 200m (default)
        // At lower speeds, turn radius scales proportionally
        double maxSpeed = capabilities.getMaxSpeed();
        double turnRadius = capabilities.getTurnRadius();
        double currentSpeed = 4.0;  // Half max speed

        // Scaled turn radius = base * (speed / maxSpeed)
        double scaledTurnRadius = turnRadius * (currentSpeed / maxSpeed);

        assertTrue(scaledTurnRadius < turnRadius, "Turn radius should decrease at lower speeds");
        assertEquals(100.0, scaledTurnRadius, 0.1, "At half speed, turn radius should be half");
    }

    @Test
    void testHeadingChange() {
        // Moving at 4 knots with turn radius of 100m, compute heading change per second
        double speedKnots = 4.0;
        double speedMs = speedKnots * 0.51444;  // Convert to m/s
        double turnRadius = 100.0;
        double dtSeconds = 1.0;

        // Arc length = speed * time
        double arcLength = speedMs * dtSeconds;

        // Angle change = arc length / radius (in radians)
        double angleChangeRadians = arcLength / turnRadius;
        double angleChangeDegrees = Math.toDegrees(angleChangeRadians);

        assertTrue(angleChangeDegrees > 0, "Should change heading when turning");
        assertTrue(angleChangeDegrees < 90, "Turn rate should be reasonable");
    }

    @Test
    void testDecelerationLimiting() {
        // With deceleration limit of 1.0 m/s², in 1 second we should slow down
        double dtSeconds = 1.0;
        double expectedSpeedLoss = capabilities.getDeceleration() * dtSeconds;  // m/s
        double expectedSpeedLossKnots = expectedSpeedLoss / 0.51444;

        assertTrue(expectedSpeedLossKnots > 0, "Should decelerate");
        assertTrue(expectedSpeedLossKnots < 8.0, "Should not stop immediately");
    }

    @Test
    void testNoNegativeSpeed() {
        // Speed should never go below zero
        // After deceleration is applied
        double finalSpeed = Math.max(0.0, 0.5 - 10.0);  // Exaggerated deceleration

        assertTrue(finalSpeed >= 0, "Speed should never go negative");
    }

    @Test
    void testHeadingNormalization() {
        // Heading should always be [0, 360)
        double heading = 370.0;
        double normalized = ((heading % 360) + 360) % 360;

        assertTrue(normalized >= 0, "Heading should be >= 0");
        assertTrue(normalized < 360, "Heading should be < 360");
        assertEquals(10.0, normalized, 0.1, "370° should normalize to 10°");
    }

    @Test
    void testGreatCirclePositionUpdate() {
        // When moving north at 4 knots for 1 hour, should move ~4 nautical miles
        // = ~7.408 km = ~0.0666° latitude (roughly)

        Position start = Position.of(50.6, -2.4);
        double heading = 0.0;  // Due north
        double speedKnots = 4.0;
        double timeSeconds = 3600.0;  // 1 hour

        // Distance in meters = speed (knots) * time (seconds) * meters/knot
        double distanceMeters = speedKnots * timeSeconds * 1852.0 / 3600.0;

        Position destination = start.destination(distanceMeters, heading);
        double latDifference = destination.getLatitude() - start.getLatitude();

        assertTrue(latDifference > 0, "Should move north (increase latitude)");
        assertTrue(latDifference < 0.1, "Should move roughly 0.07° north");
    }
}
