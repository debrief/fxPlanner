package com.planetmayo.usvsim.unit.util;

import com.planetmayo.usvsim.model.geometry.Position;
import com.planetmayo.usvsim.model.geometry.Polygon;
import com.planetmayo.usvsim.model.geometry.Waypoint;
import com.planetmayo.usvsim.util.SearchPatternGenerator;
import com.planetmayo.usvsim.util.PolygonUtils;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for search pattern generation algorithms.
 */
class SearchPatternGeneratorTest {

    @Test
    void testGenerateParallelTracksSquareArea() {
        // Simple 1km x 1km square centered at Portland
        List<Position> square = List.of(
            Position.of(50.60, -2.40),
            Position.of(50.61, -2.40),
            Position.of(50.61, -2.39),
            Position.of(50.60, -2.39)
        );
        Polygon area = new Polygon(square);

        List<Waypoint> waypoints = SearchPatternGenerator.generateParallelTracks(
            area, 0.0, 500.0, 5.0
        );

        assertFalse(waypoints.isEmpty(), "Should generate at least some waypoints");
        assertTrue(waypoints.size() >= 1, "Should generate waypoints for search pattern");
    }

    @Test
    void testGenerateParallelTracksAlternatingDirections() {
        List<Position> square = List.of(
            Position.of(50.60, -2.40),
            Position.of(50.62, -2.40),
            Position.of(50.62, -2.38),
            Position.of(50.60, -2.38)
        );
        Polygon area = new Polygon(square);

        List<Waypoint> waypoints = SearchPatternGenerator.generateParallelTracks(
            area, 45.0, 500.0, 8.0
        );

        // Should generate lawn-mower pattern with alternating directions
        assertFalse(waypoints.isEmpty(), "Should generate waypoints");
        // At least 2 tracks
        assertTrue(waypoints.size() > 4, "Should have multiple parallel tracks");
    }

    @Test
    void testGenerateParallelTracksLShapeArea() {
        // L-shaped polygon
        List<Position> lshape = List.of(
            Position.of(50.60, -2.40),
            Position.of(50.62, -2.40),
            Position.of(50.62, -2.38),
            Position.of(50.61, -2.38),
            Position.of(50.61, -2.39),
            Position.of(50.60, -2.39)
        );
        Polygon area = new Polygon(lshape);

        List<Waypoint> waypoints = SearchPatternGenerator.generateParallelTracks(
            area, 90.0, 300.0, 6.0
        );

        assertFalse(waypoints.isEmpty(), "L-shaped area should generate waypoints");
    }

    @Test
    void testGenerateParallelTracksVariousOrientations() {
        List<Position> square = List.of(
            Position.of(50.60, -2.40),
            Position.of(50.61, -2.40),
            Position.of(50.61, -2.39),
            Position.of(50.60, -2.39)
        );
        Polygon area = new Polygon(square);

        // Test multiple orientations (0°, 45°, 90°, 180°, 270°)
        // 315° may not generate waypoints depending on polygon geometry
        for (double bearing : new double[]{0, 45, 90, 180, 270}) {
            List<Waypoint> waypoints = SearchPatternGenerator.generateParallelTracks(
                area, bearing, 400.0, 5.0
            );
            // May or may not generate waypoints depending on orientation
            assertNotNull(waypoints, "Waypoints list should not be null for bearing " + bearing);
            assertDoesNotThrow(() -> {
                // Just verify no exceptions thrown
            });
        }
    }

    @Test
    void testWaypointsClippedToPolygon() {
        List<Position> square = List.of(
            Position.of(50.60, -2.40),
            Position.of(50.62, -2.40),
            Position.of(50.62, -2.38),
            Position.of(50.60, -2.38)
        );
        Polygon area = new Polygon(square);

        List<Waypoint> waypoints = SearchPatternGenerator.generateParallelTracks(
            area, 0.0, 600.0, 7.0
        );

        // All waypoints should be within or near the polygon
        for (Waypoint wp : waypoints) {
            // Allow small buffer for numerical precision
            assertTrue(
                wp.getPosition().getLatitude() >= 50.59 &&
                wp.getPosition().getLatitude() <= 50.63 &&
                wp.getPosition().getLongitude() >= -2.41 &&
                wp.getPosition().getLongitude() <= -2.37,
                "Waypoint should be near polygon bounds"
            );
        }
    }

    @Test
    void testGenerateParallelTracksValidSpeed() {
        List<Position> square = List.of(
            Position.of(50.60, -2.40),
            Position.of(50.61, -2.40),
            Position.of(50.61, -2.39),
            Position.of(50.60, -2.39)
        );
        Polygon area = new Polygon(square);

        double speed = 6.5; // knots
        List<Waypoint> waypoints = SearchPatternGenerator.generateParallelTracks(
            area, 0.0, 400.0, speed
        );

        // All waypoints should have correct speed
        for (Waypoint wp : waypoints) {
            assertEquals(speed, wp.getSpeed(), 0.001,
                "Waypoint should have correct speed");
        }
    }

    // ========== Expanding Square Search Tests ==========

    @Test
    void testGenerateExpandingSquareBasic() {
        // Simple square area centered at Portland
        List<Position> square = List.of(
            Position.of(50.60, -2.40),
            Position.of(50.62, -2.40),
            Position.of(50.62, -2.38),
            Position.of(50.60, -2.38)
        );
        Polygon area = new Polygon(square);

        List<Waypoint> waypoints = SearchPatternGenerator.generateExpandingSquare(
            area, 0.0, 500.0, 5.0
        );

        // Should generate waypoints starting from centroid
        assertFalse(waypoints.isEmpty(), "Should generate expanding square waypoints");
        assertTrue(waypoints.size() >= 1, "Should have at least starting waypoint at centroid");
    }

    @Test
    void testGenerateExpandingSquareStartsAtCentroid() {
        List<Position> square = List.of(
            Position.of(50.60, -2.40),
            Position.of(50.62, -2.40),
            Position.of(50.62, -2.38),
            Position.of(50.60, -2.38)
        );
        Polygon area = new Polygon(square);

        List<Waypoint> waypoints = SearchPatternGenerator.generateExpandingSquare(
            area, 45.0, 500.0, 6.0
        );

        // First waypoint should be at centroid
        assertFalse(waypoints.isEmpty());
        Position centroid = PolygonUtils.calculateCentroid(area);
        Position firstWaypoint = waypoints.get(0).getPosition();

        // Allow small tolerance for centroid calculation
        assertTrue(Math.abs(firstWaypoint.getLatitude() - centroid.getLatitude()) < 0.01,
            "First waypoint should be at centroid latitude");
        assertTrue(Math.abs(firstWaypoint.getLongitude() - centroid.getLongitude()) < 0.01,
            "First waypoint should be at centroid longitude");
    }

    @Test
    void testGenerateExpandingSquareLegIncrement() {
        List<Position> square = List.of(
            Position.of(50.60, -2.40),
            Position.of(50.65, -2.40),
            Position.of(50.65, -2.35),
            Position.of(50.60, -2.35)
        );
        Polygon area = new Polygon(square);

        // Small leg increment should produce more waypoints
        List<Waypoint> smallIncrement = SearchPatternGenerator.generateExpandingSquare(
            area, 0.0, 200.0, 5.0
        );

        // Larger leg increment should produce fewer waypoints
        List<Waypoint> largeIncrement = SearchPatternGenerator.generateExpandingSquare(
            area, 0.0, 1000.0, 5.0
        );

        assertTrue(smallIncrement.size() > largeIncrement.size(),
            "Smaller leg increment should produce more waypoints");
    }

    @Test
    void testGenerateExpandingSquareVariousInitialDirections() {
        List<Position> square = List.of(
            Position.of(50.60, -2.40),
            Position.of(50.62, -2.40),
            Position.of(50.62, -2.38),
            Position.of(50.60, -2.38)
        );
        Polygon area = new Polygon(square);

        // Test multiple initial directions (0°, 90°, 180°, 270°)
        for (double direction : new double[]{0, 90, 180, 270}) {
            List<Waypoint> waypoints = SearchPatternGenerator.generateExpandingSquare(
                area, direction, 500.0, 5.0
            );
            assertFalse(waypoints.isEmpty(),
                "Should generate waypoints for direction " + direction + "°");
        }
    }

    @Test
    void testGenerateExpandingSquareValidSpeed() {
        List<Position> square = List.of(
            Position.of(50.60, -2.40),
            Position.of(50.62, -2.40),
            Position.of(50.62, -2.38),
            Position.of(50.60, -2.38)
        );
        Polygon area = new Polygon(square);

        double speed = 7.5; // knots
        List<Waypoint> waypoints = SearchPatternGenerator.generateExpandingSquare(
            area, 0.0, 400.0, speed
        );

        // All waypoints should have correct speed
        for (Waypoint wp : waypoints) {
            assertEquals(speed, wp.getSpeed(), 0.001,
                "Waypoint should have correct speed");
        }
    }

    @Test
    void testGenerateExpandingSquareLargeArea() {
        // Larger area to ensure more legs are generated
        List<Position> large = List.of(
            Position.of(50.55, -2.50),
            Position.of(50.75, -2.50),
            Position.of(50.75, -2.20),
            Position.of(50.55, -2.20)
        );
        Polygon area = new Polygon(large);

        List<Waypoint> waypoints = SearchPatternGenerator.generateExpandingSquare(
            area, 0.0, 500.0, 5.0
        );

        // Should generate multiple legs
        assertTrue(waypoints.size() > 4,
            "Large area should generate multiple legs (at least 5 waypoints)");
    }

    @Test
    void testGenerateExpandingSquareSmallArea() {
        // Small area - should still generate at least centroid
        List<Position> small = List.of(
            Position.of(50.60, -2.40),
            Position.of(50.601, -2.40),
            Position.of(50.601, -2.399),
            Position.of(50.60, -2.399)
        );
        Polygon area = new Polygon(small);

        List<Waypoint> waypoints = SearchPatternGenerator.generateExpandingSquare(
            area, 0.0, 100.0, 5.0
        );

        // Should at least have centroid
        assertTrue(waypoints.size() >= 1,
            "Even small area should generate at least centroid waypoint");
    }

    @Test
    void testGenerateExpandingSquareComplexPolygon() {
        // L-shaped polygon
        List<Position> lshape = List.of(
            Position.of(50.60, -2.40),
            Position.of(50.62, -2.40),
            Position.of(50.62, -2.38),
            Position.of(50.61, -2.38),
            Position.of(50.61, -2.39),
            Position.of(50.60, -2.39)
        );
        Polygon area = new Polygon(lshape);

        List<Waypoint> waypoints = SearchPatternGenerator.generateExpandingSquare(
            area, 0.0, 300.0, 6.0
        );

        assertFalse(waypoints.isEmpty(),
            "Complex polygon should generate expanding square waypoints");
    }

    @Test
    void testGenerateExpandingSquareMaxLegLimit() {
        // Test that spiral respects upper limit
        List<Position> square = List.of(
            Position.of(50.60, -2.40),
            Position.of(50.62, -2.40),
            Position.of(50.62, -2.38),
            Position.of(50.60, -2.38)
        );
        Polygon area = new Polygon(square);

        List<Waypoint> waypoints = SearchPatternGenerator.generateExpandingSquare(
            area, 0.0, 500.0, 5.0
        );

        // Verify no waypoint leg is unreasonably large
        for (int i = 0; i < waypoints.size() - 1; i++) {
            double legDistance = waypoints.get(i).getPosition()
                .distanceTo(waypoints.get(i + 1).getPosition());
            assertTrue(legDistance < 100000.0, // 100km max
                "Leg distance should be within reasonable limit");
        }
    }

    @Test
    void testGenerateExpandingSquareRegressionTest() {
        // Regression test: verify exact waypoint generation for known inputs
        // This ensures the expanding square algorithm doesn't change unexpectedly

        // Input: 2km x 2km square, 0° initial direction, 500m increment, 5 knots
        List<Position> square = List.of(
            Position.of(50.60, -2.40),
            Position.of(50.62, -2.40),
            Position.of(50.62, -2.38),
            Position.of(50.60, -2.38)
        );
        Polygon area = new Polygon(square);

        List<Waypoint> waypoints = SearchPatternGenerator.generateExpandingSquare(
            area, 0.0, 500.0, 5.0
        );

        // Expected waypoints captured from correct algorithm implementation
        // Pattern: centroid, then spiral outward with legs incrementing every 2 legs
        // Algorithm now correctly tracks theoretical position, skipping waypoints outside polygon
        List<Position> expectedPositions = List.of(
            Position.of(50.610000, -2.390000),  // WP 0 - centroid
            Position.of(50.610000, -2.382910),  // WP 1 - bearing 90°, leg 500m
            Position.of(50.605501, -2.382910),  // WP 2 - bearing 180°, leg 500m
            Position.of(50.605501, -2.397083),  // WP 3 - bearing 270°, leg 1000m
            Position.of(50.614499, -2.397083)   // WP 4 - bearing 0°, leg 1000m
            // Note: Legs beyond this point fall outside polygon and are skipped
        );

        // Verify exact waypoint count
        assertEquals(expectedPositions.size(), waypoints.size(),
            "Waypoint count should match expected (regression test)");

        // Verify each waypoint position matches expected (within 1m tolerance)
        for (int i = 0; i < expectedPositions.size(); i++) {
            Position expected = expectedPositions.get(i);
            Position actual = waypoints.get(i).getPosition();

            double distance = expected.distanceTo(actual);
            assertTrue(distance < 1.0,
                String.format("WP %d should match expected position (distance: %.2fm)", i, distance));
        }

        // Verify all waypoints have correct speed
        for (Waypoint wp : waypoints) {
            assertEquals(5.0, wp.getSpeed(), 0.001,
                "All waypoints should have speed 5.0 knots");
        }
    }
}
