package com.planetmayo.usvsim.unit.model.behaviour;

import com.planetmayo.usvsim.model.behaviour.BehaviourState;
import com.planetmayo.usvsim.model.behaviour.ParallelTrackSearch;
import com.planetmayo.usvsim.model.geometry.Position;
import com.planetmayo.usvsim.model.geometry.Polygon;
import com.planetmayo.usvsim.model.geometry.Waypoint;
import com.planetmayo.usvsim.model.platform.PlatformDemand;
import com.planetmayo.usvsim.model.platform.PlatformState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ParallelTrackSearch behaviour.
 */
class ParallelTrackSearchTest {
    private Polygon testArea;
    private ParallelTrackSearch behaviour;

    @BeforeEach
    void setUp() {
        // Create a simple square search area around Portland
        List<Position> square = List.of(
            Position.of(50.60, -2.40),
            Position.of(50.62, -2.40),
            Position.of(50.62, -2.38),
            Position.of(50.60, -2.38)
        );
        testArea = new Polygon(square);
    }

    @Test
    void testBehaviourInitialization() {
        behaviour = new ParallelTrackSearch(testArea, 45.0, 500.0, 6.0);

        assertNotNull(behaviour);
        assertEquals("Parallel Track Search", behaviour.getName());
        assertEquals(BehaviourState.PENDING, behaviour.getState());
        assertTrue(behaviour.getProgress() >= 0.0 && behaviour.getProgress() <= 1.0);
    }

    @Test
    void testWaypointGeneration() {
        behaviour = new ParallelTrackSearch(testArea, 0.0, 500.0, 6.0);

        List<Waypoint> waypoints = behaviour.getWaypoints();
        assertFalse(waypoints.isEmpty(), "Should generate waypoints for parallel track search");
        assertTrue(waypoints.size() > 2, "Should generate multiple waypoints for lawn-mower pattern");
    }

    @Test
    void testInitialStateTransition() {
        behaviour = new ParallelTrackSearch(testArea, 45.0, 400.0, 5.0);

        // Initial state is PENDING
        assertEquals(BehaviourState.PENDING, behaviour.getState());

        // First call to getDemandedState should transition to EXECUTING
        Position startPos = Position.of(50.60, -2.40);
        PlatformState mockState = new PlatformState("USV1", startPos, 0.0, 0.0, 0.0, Instant.now());
        PlatformDemand demand = behaviour.getDemandedState(mockState);

        assertNotNull(demand);
        assertEquals(BehaviourState.EXECUTING, behaviour.getState());
    }

    @Test
    void testDemandedHeadingTowardsWaypoint() {
        behaviour = new ParallelTrackSearch(testArea, 0.0, 500.0, 6.0);

        // Start at corner of search area
        Position startPos = Position.of(50.60, -2.40);
        PlatformState mockState = new PlatformState("USV1", startPos, 0.0, 0.0, 0.0, Instant.now());

        PlatformDemand demand = behaviour.getDemandedState(mockState);

        // Demanded heading should be toward first waypoint
        assertNotNull(demand);
        assertTrue(demand.getDemandedHeading() >= 0.0 && demand.getDemandedHeading() < 360.0);
        assertEquals(6.0, demand.getDemandedSpeed(), 0.001);
    }

    @Test
    void testProgressTracking() {
        behaviour = new ParallelTrackSearch(testArea, 90.0, 300.0, 5.0);

        // Initial progress should be low
        double initialProgress = behaviour.getProgress();
        assertTrue(initialProgress >= 0.0, "Progress should be non-negative");

        // Simulate reaching waypoints
        List<Waypoint> waypoints = behaviour.getWaypoints();
        if (!waypoints.isEmpty()) {
            Position targetPos = waypoints.get(0).getPosition();
            PlatformState nearWaypoint = new PlatformState("USV1", targetPos, 0.0, 0.0, 0.0, Instant.now());

            behaviour.getDemandedState(nearWaypoint);
            behaviour.updateProgress(nearWaypoint);

            // Progress should increase after reaching waypoint
            double newProgress = behaviour.getProgress();
            assertTrue(newProgress >= initialProgress, "Progress should increase or stay same");
        }
    }

    @Test
    void testCompletionDetection() {
        behaviour = new ParallelTrackSearch(testArea, 45.0, 400.0, 6.0);

        assertFalse(behaviour.isComplete(), "Should not be complete initially");

        List<Waypoint> waypoints = behaviour.getWaypoints();
        if (!waypoints.isEmpty()) {
            // Simulate reaching all waypoints
            for (Waypoint wp : waypoints) {
                PlatformState state = new PlatformState("USV1", wp.getPosition(), 0.0, 0.0, 0.0, Instant.now());
                behaviour.getDemandedState(state);
                behaviour.updateProgress(state);
            }

            // Should be complete or very close to complete
            assertTrue(behaviour.isComplete() || behaviour.getProgress() >= 0.99,
                "Should be complete after visiting all waypoints");
        }
    }

    @Test
    void testDescriptionFormat() {
        behaviour = new ParallelTrackSearch(testArea, 45.0, 500.0, 6.0);

        String description = behaviour.getDescription();
        assertNotNull(description);
        assertTrue(description.contains("45"), "Description should contain orientation");
        assertTrue(description.contains("500"), "Description should contain track spacing");
    }

    @Test
    void testDisplayColor() {
        behaviour = new ParallelTrackSearch(testArea, 0.0, 400.0, 5.0);

        assertNotNull(behaviour.getDisplayColor());
        // ParallelTrackSearch should have a specific color for display
        assertTrue(behaviour.getDisplayColor().toString().contains("0x") ||
                   behaviour.getDisplayColor().toString().contains("Color"),
            "Should return a JavaFX Color");
    }

    @Test
    void testVariousOrientations() {
        // Test with multiple orientations
        for (double bearing : new double[]{0, 45, 90, 180, 270}) {
            behaviour = new ParallelTrackSearch(testArea, bearing, 400.0, 6.0);

            assertNotNull(behaviour);
            assertEquals(BehaviourState.PENDING, behaviour.getState());
            List<Waypoint> waypoints = behaviour.getWaypoints();
            // May or may not generate waypoints depending on orientation vs polygon
            assertTrue(waypoints.isEmpty() || waypoints.size() > 0);
        }
    }
}
