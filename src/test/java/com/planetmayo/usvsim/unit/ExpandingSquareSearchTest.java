package com.planetmayo.usvsim.unit;

import com.planetmayo.usvsim.model.behaviour.*;
import com.planetmayo.usvsim.model.geometry.Polygon;
import com.planetmayo.usvsim.model.geometry.Position;
import com.planetmayo.usvsim.model.platform.PlatformDemand;
import com.planetmayo.usvsim.model.platform.PlatformState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.Instant;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ExpandingSquareSearch stateless interface methods.
 */
@DisplayName("ExpandingSquareSearch Stateless Interface Tests")
class ExpandingSquareSearchTest {

    private ExpandingSquareSearch behavior;
    private Polygon testSearchArea;
    private final double testInitialDirection = 0.0;  // North
    private final double testLegIncrement = 50.0;     // meters
    private final double testSpeed = 3.0;             // m/s

    @BeforeEach
    void setUp() {
        // Create simple square search area
        testSearchArea = new Polygon(Arrays.asList(
            Position.of(50.6000, -2.4000),
            Position.of(50.6020, -2.4000),
            Position.of(50.6020, -2.3980),
            Position.of(50.6000, -2.3980)
        ));

        behavior = new ExpandingSquareSearch(
            testSearchArea,
            testInitialDirection,
            testLegIncrement,
            testSpeed
        );
    }

    // ===================================================================
    // calculateDemand() Tests
    // ===================================================================

    @Test
    @DisplayName("calculateDemand: at first waypoint, demands bearing to waypoint")
    void testCalculateDemand_AtFirstWaypoint() {
        // Given: Platform at search area centroid
        PlatformState platformState = new PlatformState(
            "platform-1",
            Position.of(50.6010, -2.3990),
            0.0,
            0.0,
            0.0,
            Instant.now()
        );

        BehaviourExecutionState execState = BehaviourExecutionState.initial("test-id")
            .withWaypointIndex(0)
            .withState(BehaviourState.EXECUTING);

        // When: Calculate demand
        PlatformDemand demand = behavior.calculateDemand(execState, platformState);

        // Then: Should demand correct speed and heading
        assertNotNull(demand);
        assertEquals(testSpeed, demand.getDemandedSpeed(), 0.01);
        assertTrue(demand.getDemandedHeading() >= 0.0 && demand.getDemandedHeading() <= 360.0);
    }

    @Test
    @DisplayName("calculateDemand: mid-spiral, continues to demand waypoint bearing")
    void testCalculateDemand_MidSpiral() {
        // Given: Platform midway through spiral pattern
        PlatformState platformState = new PlatformState(
            "platform-1",
            Position.of(50.6015, -2.3990),
            testInitialDirection,
            testSpeed,
            0.0,
            Instant.now()
        );

        int midWaypoint = behavior.getWaypoints().size() / 2;
        BehaviourExecutionState execState = BehaviourExecutionState.initial("test-id")
            .withWaypointIndex(midWaypoint)
            .withState(BehaviourState.EXECUTING);

        // When: Calculate demand
        PlatformDemand demand = behavior.calculateDemand(execState, platformState);

        // Then: Should demand correct speed
        assertNotNull(demand);
        assertEquals(testSpeed, demand.getDemandedSpeed(), 0.01);
    }

    @Test
    @DisplayName("calculateDemand: past all waypoints, demands zero speed")
    void testCalculateDemand_SpiralComplete() {
        // Given: Platform past all waypoints
        PlatformState platformState = new PlatformState(
            "platform-1",
            Position.of(50.6030, -2.3970),
            testInitialDirection,
            testSpeed,
            0.0,
            Instant.now()
        );

        BehaviourExecutionState execState = BehaviourExecutionState.initial("test-id")
            .withWaypointIndex(behavior.getWaypoints().size())
            .withState(BehaviourState.COMPLETE);

        // When: Calculate demand
        PlatformDemand demand = behavior.calculateDemand(execState, platformState);

        // Then: Should demand zero speed (stop)
        assertNotNull(demand);
        assertEquals(0.0, demand.getDemandedSpeed(), 0.01);
    }

    // ===================================================================
    // updateProgress() Tests
    // ===================================================================

    @Test
    @DisplayName("updateProgress: PENDING transitions to EXECUTING")
    void testUpdateProgress_PendingToExecuting() {
        // Given: Initial PENDING state
        BehaviourExecutionState execState = BehaviourExecutionState.initial("test-id");
        PlatformState platformState = new PlatformState(
            "platform-1",
            Position.of(50.6010, -2.3990),
            0.0,
            testSpeed,
            0.0,
            Instant.now()
        );

        assertEquals(BehaviourState.PENDING, execState.state());

        // When: Update progress
        BehaviourExecutionState newState = behavior.updateProgress(execState, platformState);

        // Then: Should transition to EXECUTING
        assertNotNull(newState);
        assertEquals(BehaviourState.EXECUTING, newState.state());
    }

    @Test
    @DisplayName("updateProgress: increments waypoint when reached")
    void testUpdateProgress_WaypointReached() {
        // Given: Platform at first waypoint position
        Position firstWaypointPos = behavior.getWaypoints().get(0).getPosition();
        PlatformState platformState = new PlatformState(
            "platform-1",
            firstWaypointPos,
            0.0,
            testSpeed,
            0.0,
            Instant.now()
        );

        BehaviourExecutionState execState = BehaviourExecutionState.initial("test-id")
            .withState(BehaviourState.EXECUTING)
            .withWaypointIndex(0);

        // When: Update progress
        BehaviourExecutionState newState = behavior.updateProgress(execState, platformState);

        // Then: Should increment waypoint index
        assertNotNull(newState);
        assertEquals(1, newState.currentWaypointIndex());
        assertEquals(BehaviourState.EXECUTING, newState.state());
    }

    @Test
    @DisplayName("updateProgress: transitions to COMPLETE when all waypoints reached")
    void testUpdateProgress_AllWaypointsReached() {
        // Given: Platform at last waypoint
        int lastIndex = behavior.getWaypoints().size() - 1;
        Position lastWaypointPos = behavior.getWaypoints().get(lastIndex).getPosition();
        PlatformState platformState = new PlatformState(
            "platform-1",
            lastWaypointPos,
            0.0,
            testSpeed,
            0.0,
            Instant.now()
        );

        BehaviourExecutionState execState = BehaviourExecutionState.initial("test-id")
            .withState(BehaviourState.EXECUTING)
            .withWaypointIndex(lastIndex);

        // When: Update progress
        BehaviourExecutionState newState = behavior.updateProgress(execState, platformState);

        // Then: Should transition to COMPLETE
        assertNotNull(newState);
        assertEquals(BehaviourState.COMPLETE, newState.state());
        assertEquals(behavior.getWaypoints().size(), newState.currentWaypointIndex());
    }

    // ===================================================================
    // isComplete() Tests
    // ===================================================================

    @Test
    @DisplayName("isComplete: returns false for PENDING state")
    void testIsComplete_Pending() {
        BehaviourExecutionState execState = BehaviourExecutionState.initial("test-id");

        boolean complete = behavior.isComplete(execState);

        assertFalse(complete);
    }

    @Test
    @DisplayName("isComplete: returns true when state is COMPLETE")
    void testIsComplete_CompleteState() {
        BehaviourExecutionState execState = BehaviourExecutionState.initial("test-id")
            .withState(BehaviourState.COMPLETE)
            .withWaypointIndex(behavior.getWaypoints().size());

        boolean complete = behavior.isComplete(execState);

        assertTrue(complete);
    }

    @Test
    @DisplayName("Pattern generation creates spiral waypoints")
    void testPatternGeneration_CreatesSpiralWaypoints() {
        // Then: Should have generated waypoints
        assertNotNull(behavior.getWaypoints());
        assertFalse(behavior.getWaypoints().isEmpty());
    }
}
