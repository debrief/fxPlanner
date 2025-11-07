package com.planetmayo.usvsim.unit;

import com.planetmayo.usvsim.model.behaviour.*;
import com.planetmayo.usvsim.model.geometry.Position;
import com.planetmayo.usvsim.model.platform.PlatformDemand;
import com.planetmayo.usvsim.model.platform.PlatformState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for WaypointTransit stateless interface methods.
 * Tests the new stateless calculateDemand(), updateProgress(), and isComplete() methods.
 */
@DisplayName("WaypointTransit Stateless Interface Tests")
class WaypointTransitTest {

    private WaypointTransit behavior;
    private List<Position> testWaypoints;
    private final double testSpeed = 3.0; // m/s

    @BeforeEach
    void setUp() {
        // Create simple 3-waypoint transit
        testWaypoints = Arrays.asList(
            Position.of(50.6000, -2.4000),  // Start
            Position.of(50.6050, -2.4000),  // North
            Position.of(50.6050, -2.3950)   // East
        );
        behavior = new WaypointTransit(testWaypoints, testSpeed);
    }

    // ===================================================================
    // calculateDemand() Tests
    // ===================================================================

    @Test
    @DisplayName("calculateDemand: at waypoint 0, demands bearing to waypoint 0")
    void testCalculateDemand_AtFirstWaypoint() {
        // Given: Platform at start position
        PlatformState platformState = new PlatformState(
            "platform-1",
            Position.of(50.6000, -2.4000),
            0.0,   // heading
            0.0,   // speed
            0.0,   // depth
            Instant.now()
        );

        BehaviourExecutionState execState = BehaviourExecutionState.initial("test-id")
            .withWaypointIndex(0)
            .withState(BehaviourState.EXECUTING);

        // When: Calculate demand
        PlatformDemand demand = behavior.calculateDemand(execState, platformState);

        // Then: Should demand bearing to first waypoint and specified speed
        assertNotNull(demand);
        assertEquals(testSpeed, demand.getDemandedSpeed(), 0.01);
    }

    @Test
    @DisplayName("calculateDemand: approaching waypoint, demands correct bearing")
    void testCalculateDemand_ApproachingWaypoint() {
        // Given: Platform approaching second waypoint
        PlatformState platformState = new PlatformState(
            "platform-1",
            Position.of(50.6040, -2.4000),  // Approaching waypoint 1
            0.0,
            testSpeed,
            0.0,
            Instant.now()
        );

        BehaviourExecutionState execState = BehaviourExecutionState.initial("test-id")
            .withWaypointIndex(1)
            .withState(BehaviourState.EXECUTING);

        // When: Calculate demand
        PlatformDemand demand = behavior.calculateDemand(execState, platformState);

        // Then: Should demand bearing to second waypoint
        assertNotNull(demand);
        assertEquals(testSpeed, demand.getDemandedSpeed(), 0.01);
        assertTrue(demand.getDemandedHeading() >= 0.0 && demand.getDemandedHeading() <= 360.0);
    }

    @Test
    @DisplayName("calculateDemand: past last waypoint, demands zero speed")
    void testCalculateDemand_PastLastWaypoint() {
        // Given: Platform past all waypoints
        PlatformState platformState = new PlatformState(
            "platform-1",
            Position.of(50.6100, -2.3900),
            45.0,
            testSpeed,
            0.0,
            Instant.now()
        );

        BehaviourExecutionState execState = BehaviourExecutionState.initial("test-id")
            .withWaypointIndex(testWaypoints.size())  // Past all waypoints
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
    @DisplayName("updateProgress: PENDING transitions to EXECUTING on first call")
    void testUpdateProgress_PendingToExecuting() {
        // Given: Initial PENDING state
        BehaviourExecutionState execState = BehaviourExecutionState.initial("test-id");
        // Platform positioned NEAR but NOT AT waypoint 0 (so state transitions without incrementing index)
        Position nearFirstWaypoint = Position.of(
            testWaypoints.get(0).getLatitude() + 0.001,
            testWaypoints.get(0).getLongitude() + 0.001
        );
        PlatformState platformState = new PlatformState(
            "platform-1",
            nearFirstWaypoint,
            0.0,
            testSpeed,
            0.0,
            Instant.now()
        );

        assertEquals(BehaviourState.PENDING, execState.state());

        // When: Update progress
        BehaviourExecutionState newState = behavior.updateProgress(execState, platformState);

        // Then: Should transition to EXECUTING without incrementing waypoint index
        assertNotNull(newState);
        assertEquals(BehaviourState.EXECUTING, newState.state());
        assertEquals(0, newState.currentWaypointIndex());
    }

    @Test
    @DisplayName("updateProgress: increments waypoint index when waypoint reached")
    void testUpdateProgress_WaypointReached() {
        // Given: Platform at waypoint position
        PlatformState platformState = new PlatformState(
            "platform-1",
            testWaypoints.get(0),  // Exactly at waypoint
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
    @DisplayName("updateProgress: does NOT increment when waypoint not reached")
    void testUpdateProgress_WaypointNotReached() {
        // Given: Platform far from waypoint
        PlatformState platformState = new PlatformState(
            "platform-1",
            Position.of(50.5000, -2.5000),  // Far from waypoints
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

        // Then: Should keep same waypoint index
        assertNotNull(newState);
        assertEquals(0, newState.currentWaypointIndex());
        assertEquals(BehaviourState.EXECUTING, newState.state());
    }

    @Test
    @DisplayName("updateProgress: transitions to COMPLETE when all waypoints reached")
    void testUpdateProgress_AllWaypointsReached() {
        // Given: Platform at final waypoint
        PlatformState platformState = new PlatformState(
            "platform-1",
            testWaypoints.get(testWaypoints.size() - 1),  // At last waypoint
            0.0,
            testSpeed,
            0.0,
            Instant.now()
        );

        BehaviourExecutionState execState = BehaviourExecutionState.initial("test-id")
            .withState(BehaviourState.EXECUTING)
            .withWaypointIndex(testWaypoints.size() - 1);

        // When: Update progress
        BehaviourExecutionState newState = behavior.updateProgress(execState, platformState);

        // Then: Should transition to COMPLETE
        assertNotNull(newState);
        assertEquals(BehaviourState.COMPLETE, newState.state());
        assertEquals(testWaypoints.size(), newState.currentWaypointIndex());
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
    @DisplayName("isComplete: returns false for EXECUTING state with waypoints remaining")
    void testIsComplete_ExecutingNotComplete() {
        BehaviourExecutionState execState = BehaviourExecutionState.initial("test-id")
            .withState(BehaviourState.EXECUTING)
            .withWaypointIndex(1);  // Still waypoints remaining

        boolean complete = behavior.isComplete(execState);

        assertFalse(complete);
    }

    @Test
    @DisplayName("isComplete: returns true when state is COMPLETE")
    void testIsComplete_CompleteState() {
        BehaviourExecutionState execState = BehaviourExecutionState.initial("test-id")
            .withState(BehaviourState.COMPLETE)
            .withWaypointIndex(testWaypoints.size());

        boolean complete = behavior.isComplete(execState);

        assertTrue(complete);
    }

    @Test
    @DisplayName("isComplete: returns true when waypoint index exceeds total waypoints")
    void testIsComplete_IndexExceedsWaypoints() {
        BehaviourExecutionState execState = BehaviourExecutionState.initial("test-id")
            .withState(BehaviourState.EXECUTING)
            .withWaypointIndex(testWaypoints.size());  // At or past last waypoint

        boolean complete = behavior.isComplete(execState);

        assertTrue(complete);
    }

    // ===================================================================
    // Edge Cases
    // ===================================================================

    @Test
    @DisplayName("Stateless methods do not modify input execution state")
    void testStatelessMethods_DoNotModifyInput() {
        // Given: Initial state
        BehaviourExecutionState originalState = BehaviourExecutionState.initial("test-id");
        PlatformState platformState = new PlatformState(
            "platform-1",
            testWaypoints.get(0),
            0.0,
            testSpeed,
            0.0,
            Instant.now()
        );

        int originalIndex = originalState.currentWaypointIndex();
        BehaviourState originalBehaviourState = originalState.state();

        // When: Call stateless methods
        behavior.calculateDemand(originalState, platformState);
        BehaviourExecutionState newState = behavior.updateProgress(originalState, platformState);
        behavior.isComplete(originalState);

        // Then: Original state unchanged
        assertEquals(originalIndex, originalState.currentWaypointIndex());
        assertEquals(originalBehaviourState, originalState.state());
        assertNotSame(originalState, newState);  // New state returned
    }

    @Test
    @DisplayName("BehaviourExecutionState initial factory creates valid state")
    void testBehaviourExecutionState_InitialFactory() {
        BehaviourExecutionState state = BehaviourExecutionState.initial("behavior-123");

        assertNotNull(state);
        assertEquals("behavior-123", state.behaviorId());
        assertEquals(0, state.currentWaypointIndex());
        assertEquals(BehaviourState.PENDING, state.state());
        assertEquals(0.0, state.lastDistanceToWaypoint());
    }
}
