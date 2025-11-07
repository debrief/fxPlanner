package com.planetmayo.usvsim.unit;

import com.planetmayo.usvsim.model.behaviour.*;
import com.planetmayo.usvsim.model.geometry.Position;
import com.planetmayo.usvsim.model.platform.PlatformDemand;
import com.planetmayo.usvsim.model.platform.PlatformState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ReturnToBase stateless interface methods.
 */
@DisplayName("ReturnToBase Stateless Interface Tests")
class ReturnToBaseTest {

    private ReturnToBase behavior;
    private Position baseLocation;
    private final double testSpeed = 3.0;  // m/s

    @BeforeEach
    void setUp() {
        // Base at Portland Harbour
        baseLocation = Position.of(50.6000, -2.4000);
        behavior = new ReturnToBase(baseLocation, testSpeed);
    }

    // ===================================================================
    // calculateDemand() Tests
    // ===================================================================

    @Test
    @DisplayName("calculateDemand: demands bearing to base")
    void testCalculateDemand_DemandsBearingToBase() {
        // Given: Platform away from base
        PlatformState platformState = new PlatformState(
            "platform-1",
            Position.of(50.6050, -2.4050),  // 50m northeast of base
            0.0,
            testSpeed,
            0.0,
            Instant.now()
        );

        BehaviourExecutionState execState = BehaviourExecutionState.initial("test-id")
            .withWaypointIndex(0)
            .withState(BehaviourState.EXECUTING);

        // When: Calculate demand
        PlatformDemand demand = behavior.calculateDemand(execState, platformState);

        // Then: Should demand bearing to base and specified speed
        assertNotNull(demand);
        assertEquals(testSpeed, demand.getDemandedSpeed(), 0.01);
        assertTrue(demand.getDemandedHeading() >= 0.0 && demand.getDemandedHeading() <= 360.0);
    }

    @Test
    @DisplayName("calculateDemand: at base, demands zero speed")
    void testCalculateDemand_AtBase() {
        // Given: Platform at base location
        PlatformState platformState = new PlatformState(
            "platform-1",
            baseLocation,
            0.0,
            testSpeed,
            0.0,
            Instant.now()
        );

        BehaviourExecutionState execState = BehaviourExecutionState.initial("test-id")
            .withWaypointIndex(1)  // Past single waypoint
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
            Position.of(50.6050, -2.4050),
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
    @DisplayName("updateProgress: does NOT complete when far from base")
    void testUpdateProgress_FarFromBase() {
        // Given: Platform far from base
        PlatformState platformState = new PlatformState(
            "platform-1",
            Position.of(50.7000, -2.5000),  // Far away
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

        // Then: Should remain EXECUTING
        assertNotNull(newState);
        assertEquals(BehaviourState.EXECUTING, newState.state());
        assertEquals(0, newState.currentWaypointIndex());
    }

    @Test
    @DisplayName("updateProgress: transitions to COMPLETE when base reached")
    void testUpdateProgress_BaseReached() {
        // Given: Platform at base
        PlatformState platformState = new PlatformState(
            "platform-1",
            baseLocation,  // Exactly at base
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

        // Then: Should transition to COMPLETE
        assertNotNull(newState);
        assertEquals(BehaviourState.COMPLETE, newState.state());
        assertEquals(1, newState.currentWaypointIndex());
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
    @DisplayName("isComplete: returns false for EXECUTING state before base reached")
    void testIsComplete_ExecutingNotComplete() {
        BehaviourExecutionState execState = BehaviourExecutionState.initial("test-id")
            .withState(BehaviourState.EXECUTING)
            .withWaypointIndex(0);  // Still at waypoint 0

        boolean complete = behavior.isComplete(execState);

        assertFalse(complete);
    }

    @Test
    @DisplayName("isComplete: returns true when state is COMPLETE")
    void testIsComplete_CompleteState() {
        BehaviourExecutionState execState = BehaviourExecutionState.initial("test-id")
            .withState(BehaviourState.COMPLETE)
            .withWaypointIndex(1);

        boolean complete = behavior.isComplete(execState);

        assertTrue(complete);
    }

    @Test
    @DisplayName("isComplete: returns true when waypoint index exceeds 0 (base reached)")
    void testIsComplete_IndexExceedsZero() {
        BehaviourExecutionState execState = BehaviourExecutionState.initial("test-id")
            .withState(BehaviourState.EXECUTING)
            .withWaypointIndex(1);  // Past waypoint 0 (base)

        boolean complete = behavior.isComplete(execState);

        assertTrue(complete);
    }

    // ===================================================================
    // Edge Cases
    // ===================================================================

    @Test
    @DisplayName("ReturnToBase has single waypoint (base location)")
    void testReturnToBase_HasSingleWaypoint() {
        // Then: Should have exactly one waypoint
        assertNotNull(behavior.getWaypoints());
        assertEquals(1, behavior.getWaypoints().size());
        assertEquals(baseLocation, behavior.getWaypoints().get(0).getPosition());
    }

    @Test
    @DisplayName("Stateless methods do not modify input state")
    void testStatelessMethods_DoNotModifyInput() {
        // Given: Initial state
        BehaviourExecutionState originalState = BehaviourExecutionState.initial("test-id");
        PlatformState platformState = new PlatformState(
            "platform-1",
            Position.of(50.6050, -2.4050),
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
        assertNotSame(originalState, newState);
    }
}
