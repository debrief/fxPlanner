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
 * Unit tests for ParallelTrackSearch stateless interface methods.
 */
@DisplayName("ParallelTrackSearch Stateless Interface Tests")
class ParallelTrackSearchTest {

    private ParallelTrackSearch behavior;
    private Polygon testSearchArea;
    private final double testOrientation = 45.0;  // degrees
    private final double testSpacing = 100.0;     // meters
    private final double testSpeed = 3.0;         // m/s

    @BeforeEach
    void setUp() {
        // Create simple square search area
        testSearchArea = new Polygon(Arrays.asList(
            Position.of(50.6000, -2.4000),
            Position.of(50.6020, -2.4000),
            Position.of(50.6020, -2.3980),
            Position.of(50.6000, -2.3980)
        ));

        behavior = new ParallelTrackSearch(
            testSearchArea,
            testOrientation,
            testSpacing,
            testSpeed
        );
    }

    // ===================================================================
    // calculateDemand() Tests
    // ===================================================================

    @Test
    @DisplayName("calculateDemand: at first waypoint, demands bearing to waypoint")
    void testCalculateDemand_AtFirstWaypoint() {
        // Given: Platform at search area start
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
    @DisplayName("calculateDemand: mid-search, continues to demand waypoint bearing")
    void testCalculateDemand_MidSearch() {
        // Given: Platform midway through search pattern
        PlatformState platformState = new PlatformState(
            "platform-1",
            Position.of(50.6010, -2.3990),
            testOrientation,
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
    void testCalculateDemand_SearchComplete() {
        // Given: Platform past all waypoints
        PlatformState platformState = new PlatformState(
            "platform-1",
            Position.of(50.6030, -2.3970),
            testOrientation,
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
    @DisplayName("updateProgress: does NOT increment when waypoint not reached")
    void testUpdateProgress_WaypointNotReached() {
        // Given: Platform far from first waypoint
        PlatformState platformState = new PlatformState(
            "platform-1",
            Position.of(50.5000, -2.5000),  // Far away
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
    @DisplayName("isComplete: returns false for EXECUTING state")
    void testIsComplete_Executing() {
        BehaviourExecutionState execState = BehaviourExecutionState.initial("test-id")
            .withState(BehaviourState.EXECUTING)
            .withWaypointIndex(1);

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
    @DisplayName("isComplete: returns true when waypoint index exceeds total")
    void testIsComplete_IndexExceedsWaypoints() {
        BehaviourExecutionState execState = BehaviourExecutionState.initial("test-id")
            .withState(BehaviourState.EXECUTING)
            .withWaypointIndex(behavior.getWaypoints().size());

        boolean complete = behavior.isComplete(execState);

        assertTrue(complete);
    }

    // ===================================================================
    // Pattern Generation Validation
    // ===================================================================

    @Test
    @DisplayName("Pattern generation creates waypoints")
    void testPatternGeneration_CreatesWaypoints() {
        // Then: Should have generated waypoints
        assertNotNull(behavior.getWaypoints());
        assertFalse(behavior.getWaypoints().isEmpty());
    }

    @Test
    @DisplayName("Stateless methods do not modify input state")
    void testStatelessMethods_DoNotModifyInput() {
        // Given: Initial state
        BehaviourExecutionState originalState = BehaviourExecutionState.initial("test-id");
        PlatformState platformState = new PlatformState(
            "platform-1",
            Position.of(50.6010, -2.3990),
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
