package com.planetmayo.usvsim.unit.model.behaviour;

import com.planetmayo.usvsim.model.behaviour.ExpandingSquareSearch;
import com.planetmayo.usvsim.model.behaviour.BehaviourState;
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
 * Unit tests for ExpandingSquareSearch behaviour.
 *
 * Tests:
 * - Pattern generation from polygon centroid
 * - Expanding square spiral with configurable parameters
 * - Completion when all waypoints reached
 * - State transitions and progress tracking
 */
class ExpandingSquareSearchTest {
    private ExpandingSquareSearch behaviour;
    private Polygon searchArea;
    private PlatformState platformState;

    @BeforeEach
    void setup() {
        // Create a simple square search area centered at Portland
        List<Position> square = List.of(
            Position.of(50.60, -2.40),
            Position.of(50.65, -2.40),
            Position.of(50.65, -2.35),
            Position.of(50.60, -2.35)
        );
        searchArea = new Polygon(square);

        behaviour = new ExpandingSquareSearch(searchArea, 0.0, 500.0, 5.0);

        // Platform at area centroid
        platformState = new PlatformState(
            "USV-1",
            searchArea.centroid(),
            0.0,
            0.0,
            0.0,
            Instant.now()
        );
    }

    @Test
    void testBehaviourInitialState() {
        assertEquals(BehaviourState.PENDING, behaviour.getState());
        assertEquals("Expanding Square Search", behaviour.getName());
        assertFalse(behaviour.isComplete());
    }

    @Test
    void testWaypointsGenerated() {
        List<Waypoint> waypoints = behaviour.getWaypoints();
        assertFalse(waypoints.isEmpty(), "Should have generated waypoints");
        assertTrue(waypoints.size() >= 1, "Should have at least centroid as starting waypoint");
    }

    @Test
    void testPatternStartsAtCentroid() {
        List<Waypoint> waypoints = behaviour.getWaypoints();
        assertFalse(waypoints.isEmpty());

        Position centroid = searchArea.centroid();
        Position firstWaypoint = waypoints.get(0).getPosition();

        // Allow tolerance for centroid calculation
        assertTrue(Math.abs(firstWaypoint.getLatitude() - centroid.getLatitude()) < 0.01,
            "Pattern should start at centroid");
        assertTrue(Math.abs(firstWaypoint.getLongitude() - centroid.getLongitude()) < 0.01,
            "Pattern should start at centroid");
    }

    @Test
    void testGetDemandedState() {
        behaviour.updateProgress(platformState);

        PlatformDemand demand = behaviour.getDemandedState(platformState);

        assertNotNull(demand);
        assertEquals(5.0, demand.getDemandedSpeed(), 0.1, "Should demand 5 knots");
        assertTrue(demand.getDemandedHeading() >= 0 && demand.getDemandedHeading() < 360,
            "Heading should be valid [0, 360)");
    }

    @Test
    void testProgressTracking() {
        assertEquals(0.0, behaviour.getProgress(), "Should start at 0% progress");

        behaviour.updateProgress(platformState);
        assertTrue(behaviour.getProgress() >= 0.0 && behaviour.getProgress() <= 1.0,
            "Progress should be between 0 and 1");
    }

    @Test
    void testStateTransition() {
        assertEquals(BehaviourState.PENDING, behaviour.getState());

        behaviour.updateProgress(platformState);
        assertEquals(BehaviourState.EXECUTING, behaviour.getState(),
            "Should transition to EXECUTING on first update");
    }

    @Test
    void testWaypointAcceptanceRadius() {
        List<Waypoint> waypoints = behaviour.getWaypoints();
        for (Waypoint wp : waypoints) {
            assertTrue(wp.getAcceptanceRadius() > 0,
                "All waypoints should have positive acceptance radius");
        }
    }

    @Test
    void testValidSpeed() {
        double speed = 7.5;
        behaviour = new ExpandingSquareSearch(searchArea, 0.0, 500.0, speed);

        List<Waypoint> waypoints = behaviour.getWaypoints();
        for (Waypoint wp : waypoints) {
            assertEquals(speed, wp.getSpeed(), 0.1,
                "All waypoints should have correct speed");
        }
    }

    @Test
    void testDescription() {
        String desc = behaviour.getDescription();
        assertNotNull(desc);
        assertTrue(desc.length() > 0, "Description should not be empty");
        assertTrue(desc.contains("spiral") || desc.contains("square") || desc.contains("expanding"),
            "Description should mention expanding square search");
    }

    @Test
    void testDifferentInitialDirections() {
        // Test different initial directions
        for (double direction : new double[]{0, 45, 90, 180}) {
            ExpandingSquareSearch searchBehaviour = new ExpandingSquareSearch(
                searchArea, direction, 400.0, 5.0
            );
            assertFalse(searchBehaviour.getWaypoints().isEmpty(),
                "Should generate waypoints for direction " + direction);
        }
    }

    @Test
    void testDifferentLegIncrements() {
        // Small increment should generate more waypoints
        ExpandingSquareSearch smallIncrement = new ExpandingSquareSearch(
            searchArea, 0.0, 200.0, 5.0
        );

        // Large increment should generate fewer waypoints
        ExpandingSquareSearch largeIncrement = new ExpandingSquareSearch(
            searchArea, 0.0, 1000.0, 5.0
        );

        assertTrue(smallIncrement.getWaypoints().size() > largeIncrement.getWaypoints().size(),
            "Smaller leg increment should produce more waypoints");
    }

    @Test
    void testSmallSearchArea() {
        List<Position> small = List.of(
            Position.of(50.60, -2.40),
            Position.of(50.601, -2.40),
            Position.of(50.601, -2.399),
            Position.of(50.60, -2.399)
        );
        Polygon smallArea = new Polygon(small);

        behaviour = new ExpandingSquareSearch(smallArea, 0.0, 100.0, 5.0);

        assertFalse(behaviour.getWaypoints().isEmpty(),
            "Even small area should generate waypoints");
    }

    @Test
    void testLargeSearchArea() {
        List<Position> large = List.of(
            Position.of(50.55, -2.50),
            Position.of(50.75, -2.50),
            Position.of(50.75, -2.20),
            Position.of(50.55, -2.20)
        );
        Polygon largeArea = new Polygon(large);

        behaviour = new ExpandingSquareSearch(largeArea, 0.0, 500.0, 5.0);

        assertTrue(behaviour.getWaypoints().size() > 5,
            "Large area should generate multiple waypoints");
    }

    @Test
    void testWaypointOrdering() {
        List<Waypoint> waypoints = behaviour.getWaypoints();
        assertFalse(waypoints.isEmpty());

        // Waypoints should form a spiral pattern
        // First waypoint is at centroid
        Position centroid = searchArea.centroid();
        assertEquals(centroid.getLatitude(), waypoints.get(0).getPosition().getLatitude(), 0.01);
        assertEquals(centroid.getLongitude(), waypoints.get(0).getPosition().getLongitude(), 0.01);

        // Subsequent waypoints should be further from centroid (expanding)
        if (waypoints.size() > 1) {
            double prevDistance = centroid.distanceTo(waypoints.get(0).getPosition());
            for (int i = 1; i < Math.min(5, waypoints.size()); i++) {
                double currDistance = centroid.distanceTo(waypoints.get(i).getPosition());
                // Distance should generally increase (with some tolerance for spiral geometry)
                assertTrue(currDistance >= prevDistance * 0.8,
                    "Expanding square should spiral outward from centroid");
            }
        }
    }

    @Test
    void testComplexPolygon() {
        // L-shaped polygon
        List<Position> lshape = List.of(
            Position.of(50.60, -2.40),
            Position.of(50.62, -2.40),
            Position.of(50.62, -2.38),
            Position.of(50.61, -2.38),
            Position.of(50.61, -2.39),
            Position.of(50.60, -2.39)
        );
        Polygon complexArea = new Polygon(lshape);

        behaviour = new ExpandingSquareSearch(complexArea, 0.0, 300.0, 6.0);

        assertFalse(behaviour.getWaypoints().isEmpty(),
            "Complex polygon should generate expanding square waypoints");
    }
}
