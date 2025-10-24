package com.planetmayo.usvsim.unit.model.behaviour;

import com.planetmayo.usvsim.model.behaviour.ReturnToBase;
import com.planetmayo.usvsim.model.behaviour.BehaviourState;
import com.planetmayo.usvsim.model.geometry.Position;
import com.planetmayo.usvsim.model.geometry.Waypoint;
import com.planetmayo.usvsim.model.platform.PlatformDemand;
import com.planetmayo.usvsim.model.platform.PlatformState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ReturnToBase behaviour.
 *
 * Tests:
 * - Navigation to base location
 * - Base location configuration
 * - Completion when reaching base
 * - Single waypoint (base location)
 */
class ReturnToBaseTest {
    private ReturnToBase behaviour;
    private Position baseLocation;
    private Position platformStart;
    private PlatformState platformState;

    @BeforeEach
    void setup() {
        baseLocation = Position.of(50.6, -2.4);
        platformStart = Position.of(50.605, -2.405);

        behaviour = new ReturnToBase(baseLocation, 5.0);  // 5 knots

        platformState = new PlatformState(
            "USV-1",
            platformStart,
            0.0,
            0.0,
            0.0,
            Instant.now()
        );
    }

    @Test
    void testBehaviourInitialState() {
        assertEquals(BehaviourState.PENDING, behaviour.getState());
        assertEquals("Return to Base", behaviour.getName());
        assertFalse(behaviour.isComplete());
    }

    @Test
    void testBaseLocationIsWaypoint() {
        List<Waypoint> waypoints = behaviour.getWaypoints();
        assertEquals(1, waypoints.size(), "Should have single waypoint (base location)");

        Waypoint baseWaypoint = waypoints.get(0);
        assertEquals(baseLocation, baseWaypoint.getPosition());
    }

    @Test
    void testGetDemandedState() {
        behaviour.updateProgress(platformState);  // Activate

        PlatformDemand demand = behaviour.getDemandedState(platformState);

        assertNotNull(demand);
        assertEquals(5.0, demand.getDemandedSpeed(), 0.1, "Should demand 5 knots");

        // Should be heading towards base
        double bearing = platformStart.bearingTo(baseLocation);
        assertEquals(bearing, demand.getDemandedHeading(), 0.1,
            "Should demand heading towards base");
    }

    @Test
    void testProgressTracking() {
        assertEquals(0.0, behaviour.getProgress(), "Should start at 0% progress");

        // Not yet at base
        behaviour.updateProgress(platformState);
        assertEquals(0.0, behaviour.getProgress(), "Should still be 0% when not at base");

        // Move closer to base (but still not there)
        PlatformState closer = new PlatformState(
            "USV-1",
            Position.of(50.601, -2.401),  // Closer to base but still outside acceptance radius
            0.0,
            0.0,
            0.0,
            Instant.now()
        );

        behaviour.updateProgress(closer);
        double progress = behaviour.getProgress();
        assertEquals(0.0, progress, "Should be 0% until actually at base (within acceptance radius)");
    }

    @Test
    void testBaseReachedCompletion() {
        // Simulate reaching base
        PlatformState atBase = new PlatformState(
            "USV-1",
            baseLocation,
            0.0,
            0.0,
            0.0,
            Instant.now()
        );

        behaviour.updateProgress(atBase);
        assertTrue(behaviour.isComplete(), "Behaviour should be complete when at base");
    }

    @Test
    void testAcceptanceRadius() {
        List<Waypoint> waypoints = behaviour.getWaypoints();
        Waypoint baseWaypoint = waypoints.get(0);

        assertTrue(baseWaypoint.getAcceptanceRadius() > 0,
            "Base waypoint should have positive acceptance radius");
    }

    @Test
    void testSpeedSetting() {
        behaviour = new ReturnToBase(baseLocation, 8.0);  // 8 knots

        List<Waypoint> waypoints = behaviour.getWaypoints();
        assertEquals(8.0, waypoints.get(0).getSpeed(), 0.1,
            "Base waypoint should have specified speed");
    }

    @Test
    void testStateTransition() {
        assertEquals(BehaviourState.PENDING, behaviour.getState());

        behaviour.updateProgress(platformState);
        assertEquals(BehaviourState.EXECUTING, behaviour.getState());

        // Move to base
        PlatformState atBase = new PlatformState(
            "USV-1",
            baseLocation,
            0.0,
            0.0,
            0.0,
            Instant.now()
        );

        behaviour.updateProgress(atBase);
        assertEquals(BehaviourState.COMPLETE, behaviour.getState());
    }

    @Test
    void testDescription() {
        String desc = behaviour.getDescription();
        assertNotNull(desc);
        assertTrue(desc.length() > 0, "Description should not be empty");
        assertTrue(desc.contains("base") || desc.contains("Base"),
            "Description should mention base");
    }

    @Test
    void testWithinAcceptanceRadius() {
        // Get acceptance radius
        List<Waypoint> waypoints = behaviour.getWaypoints();
        double radius = waypoints.get(0).getAcceptanceRadius();

        // Position very close to base (within radius)
        Position nearBase = baseLocation.destination(radius / 2, 0.0);

        PlatformState nearState = new PlatformState(
            "USV-1",
            nearBase,
            0.0,
            0.0,
            0.0,
            Instant.now()
        );

        behaviour.updateProgress(nearState);
        assertTrue(behaviour.isComplete(),
            "Should be complete when within acceptance radius of base");
    }

    @Test
    void testReturnFromFarAway() {
        // Platform very far from base
        Position farAway = Position.of(51.0, -2.0);  // About 50km away

        PlatformState farState = new PlatformState(
            "USV-1",
            farAway,
            0.0,
            0.0,
            0.0,
            Instant.now()
        );

        behaviour.updateProgress(farState);
        assertFalse(behaviour.isComplete(), "Should not be complete when far from base");
        assertEquals(0.0, behaviour.getProgress(), "Should show 0% progress when very far");

        // Move closer step by step
        for (int i = 0; i < 5; i++) {
            Position closer = farAway.destination(-100000 * (i + 1), 180.0);  // Move closer
            PlatformState state = new PlatformState(
                "USV-1",
                closer,
                0.0,
                0.0,
                0.0,
                Instant.now()
            );
            behaviour.updateProgress(state);
        }

        assertFalse(behaviour.isComplete(), "Still not at base after a few steps");
    }
}
