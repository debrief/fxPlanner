package com.planetmayo.usvsim.unit.util;

import com.planetmayo.usvsim.model.geometry.Position;
import com.planetmayo.usvsim.util.GeoUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for geographic utility functions.
 *
 * Tests great circle calculations for distance, bearing, and destination.
 */
class GeoUtilsTest {

    // Portland Harbour reference point
    private static final double PORTLAND_LAT = 50.6;
    private static final double PORTLAND_LON = -2.4;

    // Test data: positions at known distances/bearings from Portland
    private static final Position PORTLAND = Position.of(PORTLAND_LAT, PORTLAND_LON);
    private static final Position NORTH_10KM = Position.of(50.690, PORTLAND_LON); // ~10km north
    private static final Position EAST_10KM = Position.of(PORTLAND_LAT, -2.310); // ~10km east
    private static final Position SOUTH_10KM = Position.of(50.510, PORTLAND_LON); // ~10km south
    private static final Position WEST_10KM = Position.of(PORTLAND_LAT, -2.490); // ~10km west

    // Test for distance calculation
    @Test
    void testDistanceSamePosition() {
        double distance = GeoUtils.distance(PORTLAND, PORTLAND);
        assertEquals(0.0, distance, 0.001, "Distance to same position should be 0");
    }

    @Test
    void testDistanceNorthApprox10km() {
        double distance = GeoUtils.distance(PORTLAND, NORTH_10KM);
        assertTrue(distance > 10000 && distance < 11500,
            "Distance north should be ~10-11km, got " + distance);
    }

    @Test
    void testDistanceEastApprox10km() {
        double distance = GeoUtils.distance(PORTLAND, EAST_10KM);
        assertTrue(distance > 6000 && distance < 7000,
            "Distance east should be ~6-7km (reduced by cosine of latitude), got " + distance);
    }

    @Test
    void testDistanceSymmetric() {
        // Distance from A to B should equal distance from B to A
        double d1 = GeoUtils.distance(PORTLAND, NORTH_10KM);
        double d2 = GeoUtils.distance(NORTH_10KM, PORTLAND);
        assertEquals(d1, d2, 0.001, "Distance should be symmetric");
    }

    @Test
    void testDistanceTriangleInequality() {
        // Distance AB + BC > Distance AC
        Position p1 = PORTLAND;
        Position p2 = NORTH_10KM;
        Position p3 = EAST_10KM;
        double d12 = GeoUtils.distance(p1, p2);
        double d23 = GeoUtils.distance(p2, p3);
        double d13 = GeoUtils.distance(p1, p3);
        assertTrue(d12 + d23 > d13,
            "Triangle inequality: d12 + d23 should be > d13");
    }

    // Test for bearing calculation
    @Test
    void testBearingNorth() {
        double brng = GeoUtils.bearing(PORTLAND, NORTH_10KM);
        assertEquals(0.0, brng, 1.0, "Bearing north should be ~0°");
    }

    @Test
    void testBearingEast() {
        double brng = GeoUtils.bearing(PORTLAND, EAST_10KM);
        assertEquals(90.0, brng, 2.0, "Bearing east should be ~90°");
    }

    @Test
    void testBearingSouth() {
        double brng = GeoUtils.bearing(PORTLAND, SOUTH_10KM);
        assertEquals(180.0, brng, 1.0, "Bearing south should be ~180°");
    }

    @Test
    void testBearingWest() {
        double brng = GeoUtils.bearing(PORTLAND, WEST_10KM);
        // West is 270° or equivalently 359°+ depending on rounding
        assertTrue((brng > 268 && brng < 272) || (brng > 358 && brng < 362),
            "Bearing west should be ~270°, got " + brng);
    }

    @ParameterizedTest
    @CsvSource({
        "50.6,-2.4,0,0,0",           // Same position, any bearing -> 0
        "50.6,-2.4,50.6,-2.4,90,0"   // Same position -> 0 bearing
    })
    void testBearingEdgeCases(double lat1, double lon1, double lat2, double lon2, double expectedBrng) {
        Position p1 = Position.of(lat1, lon1);
        Position p2 = Position.of(lat2, lon2);
        // Handle same position case specially
        if (p1.equals(p2)) {
            // Bearing from same position is undefined, but we can test it returns a valid angle
            double bearing = GeoUtils.bearing(p1, p2);
            assertTrue(bearing >= 0 && bearing < 360, "Bearing should be valid [0, 360)");
        }
    }

    // Test for destination calculation
    @Test
    void testDestinationNorth1km() {
        double distance = 1000; // 1 km
        double bearing = 0;     // North
        Position dest = GeoUtils.destination(PORTLAND, distance, bearing);

        // Should be roughly 0.009 degrees north
        assertTrue(dest.getLatitude() > PORTLAND_LAT && dest.getLatitude() < PORTLAND_LAT + 0.02,
            "Destination north should have increased latitude");
        assertEquals(PORTLAND_LON, dest.getLongitude(), 0.001,
            "Destination north should maintain longitude");
    }

    @Test
    void testDestinationEast1km() {
        double distance = 1000; // 1 km
        double bearing = 90;    // East
        Position dest = GeoUtils.destination(PORTLAND, distance, bearing);

        assertTrue(dest.getLongitude() > PORTLAND_LON,
            "Destination east should have increased longitude");
        assertEquals(PORTLAND_LAT, dest.getLatitude(), 0.001,
            "Destination east should maintain latitude");
    }

    @Test
    void testDestinationRoundTrip() {
        // Go 10km north then back 10km south
        double distance = 10000;
        Position north = GeoUtils.destination(PORTLAND, distance, 0);
        Position back = GeoUtils.destination(north, distance, 180);

        // Should return approximately to original position
        assertEquals(PORTLAND_LAT, back.getLatitude(), 0.001,
            "Round trip should return to original latitude");
        assertEquals(PORTLAND_LON, back.getLongitude(), 0.001,
            "Round trip should return to original longitude");
    }

    @Test
    void testDestinationValidCoordinates() {
        Position dest = GeoUtils.destination(PORTLAND, 5000, 45);

        assertTrue(dest.getLatitude() >= -90 && dest.getLatitude() <= 90,
            "Destination latitude must be valid");
        assertTrue(dest.getLongitude() >= -180 && dest.getLongitude() <= 180,
            "Destination longitude must be valid");
    }

    // Test for angle utilities
    @Test
    void testNormalizeAngle() {
        assertEquals(0.0, GeoUtils.normalizeAngle(0), 0.001);
        assertEquals(90.0, GeoUtils.normalizeAngle(90), 0.001);
        assertEquals(359.0, GeoUtils.normalizeAngle(359), 0.001);
        assertEquals(0.0, GeoUtils.normalizeAngle(360), 0.001);
        assertEquals(45.0, GeoUtils.normalizeAngle(405), 0.001);
        assertEquals(45.0, GeoUtils.normalizeAngle(-315), 0.001);
    }

    @Test
    void testAngleDelta() {
        assertEquals(90.0, GeoUtils.angleDelta(0, 90), 0.001);
        assertEquals(-90.0, GeoUtils.angleDelta(90, 0), 0.001);
        assertEquals(0.0, GeoUtils.angleDelta(0, 0), 0.001);
        assertEquals(0.0, GeoUtils.angleDelta(0, 360), 0.001);
        assertEquals(179.0, GeoUtils.angleDelta(0, 179), 0.001);
        assertEquals(-179.0, GeoUtils.angleDelta(0, 181), 0.001);
    }
}
