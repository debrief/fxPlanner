package com.planetmayo.usvsim.unit.model.behaviour;

import com.planetmayo.usvsim.model.behaviour.WaypointTransit;
import com.planetmayo.usvsim.model.behaviour.BehaviourState;
import com.planetmayo.usvsim.model.geometry.Position;
import com.planetmayo.usvsim.model.geometry.Waypoint;
import com.planetmayo.usvsim.model.platform.PlatformDemand;
import com.planetmayo.usvsim.model.platform.PlatformState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for WaypointTransit behaviour.
 *
 * Tests:
 * - Simple point-to-point navigation
 * - Waypoint sequencing
 * - Progress tracking
 * - Completion detection
 */
class WaypointTransitTest {
    private WaypointTransit behaviour;
    private List<Position> waypoints;
    private PlatformState platformState;

    @BeforeEach
    void setup() {
        // Create a simple waypoint sequence
        waypoints = new ArrayList<>();
        waypoints.add(Position.of(50.6, -2.4));    // Start
        waypoints.add(Position.of(50.601, -2.4));  // First waypoint
        waypoints.add(Position.of(50.602, -2.4));  // Second waypoint

        behaviour = new WaypointTransit(waypoints, 5.0);  // 5 knots

        // Platform at start position
        platformState = new PlatformState(
            "USV-1",
            Position.of(50.6, -2.4),
            0.0,
            0.0,
            0.0,
            Instant.now()
        );
    }

    @Test
    void testBehaviourInitialState() {
        assertEquals(BehaviourState.PENDING, behaviour.getState());
        assertEquals("Waypoint Transit", behaviour.getName());
        assertFalse(behaviour.isComplete());
    }

    @Test
    void testWaypointsGenerated() {
        List<Waypoint> generatedWaypoints = behaviour.getWaypoints();
        assertEquals(3, generatedWaypoints.size(), "Should have 3 waypoints");

        // Each waypoint should have correct position
        for (int i = 0; i < generatedWaypoints.size(); i++) {
            assertEquals(waypoints.get(i), generatedWaypoints.get(i).getPosition());
        }
    }

    @Test
    void testGetDemandedState() {
        behaviour.updateProgress(platformState);  // Activate behaviour

        PlatformDemand demand = behaviour.getDemandedState(platformState);

        assertNotNull(demand);
        assertEquals(5.0, demand.getDemandedSpeed(), 0.1, "Should demand 5 knots");
        assertTrue(demand.getDemandedHeading() >= 0 && demand.getDemandedHeading() < 360,
            "Heading should be valid [0, 360)");
    }

    @Test
    void testProgressTracking() {
        assertEquals(0.0, behaviour.getProgress(), "Should start at 0% progress");

        // Simulate reaching first waypoint
        behaviour.updateProgress(platformState);
        behaviour.updateProgress(platformState);  // Move to next

        double progress = behaviour.getProgress();
        assertTrue(progress > 0 && progress < 1.0, "Progress should be between 0 and 1");
    }

    @Test
    void testWaypointAcceptanceRadius() {
        List<Waypoint> waypts = behaviour.getWaypoints();
        for (Waypoint wp : waypts) {
            assertTrue(wp.getAcceptanceRadius() > 0, "Acceptance radius should be positive");
        }
    }

    @Test
    void testBehaviourCompletion() {
        // Simulate platform reaching all waypoints
        for (Position waypoint : waypoints) {
            PlatformState state = new PlatformState(
                "USV-1",
                waypoint,
                0.0,
                0.0,
                0.0,
                Instant.now()
            );
            behaviour.updateProgress(state);
        }

        assertTrue(behaviour.isComplete(), "Behaviour should be complete after reaching all waypoints");
    }

    @Test
    void testSpeedSetting() {
        behaviour = new WaypointTransit(waypoints, 8.0);  // 8 knots
        List<Waypoint> waypts = behaviour.getWaypoints();

        for (Waypoint wp : waypts) {
            assertEquals(8.0, wp.getSpeed(), 0.1, "All waypoints should have specified speed");
        }
    }

    @Test
    void testStateTransition() {
        assertEquals(BehaviourState.PENDING, behaviour.getState());

        // Update progress activates behaviour
        behaviour.updateProgress(platformState);
        assertEquals(BehaviourState.EXECUTING, behaviour.getState());

        // Reach all waypoints
        for (Position waypoint : waypoints) {
            PlatformState state = new PlatformState(
                "USV-1",
                waypoint,
                0.0,
                0.0,
                0.0,
                Instant.now()
            );
            behaviour.updateProgress(state);
        }

        assertEquals(BehaviourState.COMPLETE, behaviour.getState());
    }

    @Test
    void testDescription() {
        String desc = behaviour.getDescription();
        assertNotNull(desc);
        assertTrue(desc.length() > 0, "Description should not be empty");
    }

    @Test
    void testSingleWaypoint() {
        // Behaviour with single waypoint
        List<Position> single = new ArrayList<>();
        single.add(Position.of(50.61, -2.4));

        WaypointTransit singleWaypoint = new WaypointTransit(single, 5.0);
        assertEquals(1, singleWaypoint.getWaypoints().size());

        assertFalse(singleWaypoint.isComplete());
        singleWaypoint.updateProgress(new PlatformState(
            "USV-1",
            Position.of(50.61, -2.4),
            0.0, 0.0, 0.0,
            Instant.now()
        ));
        assertTrue(singleWaypoint.isComplete(), "Should complete with single waypoint");
    }
}
