package com.planetmayo.usvsim.util;

import com.planetmayo.usvsim.model.geometry.Position;

/**
 * Utility class for geographic calculations using great circle formulas.
 *
 * Uses Haversine and forward calculation formulas for accuracy.
 * Suitable for local operations (< 100 km).
 *
 * All distances in metres, bearings in degrees [0, 360).
 */
public final class GeoUtils {
    /** Earth's mean radius in metres */
    private static final double EARTH_RADIUS_METRES = 6_371_008.8;

    private GeoUtils() {
        // Utility class - no instantiation
    }

    /**
     * Calculates great circle distance between two positions using Haversine.
     *
     * @param from start position
     * @param to end position
     * @return distance in metres
     */
    public static double distance(Position from, Position to) {
        if (from.equals(to)) {
            return 0.0;
        }

        double lat1 = Math.toRadians(from.getLatitude());
        double lat2 = Math.toRadians(to.getLatitude());
        double deltaLat = Math.toRadians(to.getLatitude() - from.getLatitude());
        double deltaLon = Math.toRadians(to.getLongitude() - from.getLongitude());

        // Haversine formula
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                   Math.cos(lat1) * Math.cos(lat2) *
                   Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_METRES * c;
    }

    /**
     * Calculates initial bearing from one position to another.
     *
     * @param from start position
     * @param to end position
     * @return bearing in degrees [0, 360)
     */
    public static double bearing(Position from, Position to) {
        double lat1 = Math.toRadians(from.getLatitude());
        double lat2 = Math.toRadians(to.getLatitude());
        double deltaLon = Math.toRadians(to.getLongitude() - from.getLongitude());

        // Initial bearing formula
        double y = Math.sin(deltaLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) -
                   Math.sin(lat1) * Math.cos(lat2) * Math.cos(deltaLon);

        double bearingRad = Math.atan2(y, x);
        double bearingDeg = Math.toDegrees(bearingRad);

        // Normalize to [0, 360)
        return (bearingDeg + 360) % 360;
    }

    /**
     * Calculates destination position given distance and bearing.
     *
     * Uses forward calculation (direct problem).
     *
     * @param from start position
     * @param distance in metres
     * @param bearing in degrees [0, 360)
     * @return destination Position
     */
    public static Position destination(Position from, double distance, double bearing) {
        double lat1 = Math.toRadians(from.getLatitude());
        double lon1 = Math.toRadians(from.getLongitude());
        double d = distance / EARTH_RADIUS_METRES; // Angular distance
        double brng = Math.toRadians(bearing);

        // Forward calculation
        double lat2 = Math.asin(Math.sin(lat1) * Math.cos(d) +
                               Math.cos(lat1) * Math.sin(d) * Math.cos(brng));

        double lon2 = lon1 + Math.atan2(Math.sin(brng) * Math.sin(d) * Math.cos(lat1),
                                       Math.cos(d) - Math.sin(lat1) * Math.sin(lat2));

        double lat2Deg = Math.toDegrees(lat2);
        double lon2Deg = Math.toDegrees(lon2);

        // Normalize longitude to [-180, 180]
        lon2Deg = ((lon2Deg + 540) % 360) - 180;

        return Position.of(lat2Deg, lon2Deg);
    }

    /**
     * Normalizes an angle to [0, 360) degrees.
     *
     * @param angle in degrees
     * @return normalized angle [0, 360)
     */
    public static double normalizeAngle(double angle) {
        return ((angle % 360) + 360) % 360;
    }

    /**
     * Calculates the shortest angular difference between two bearings.
     *
     * @param from bearing in degrees
     * @param to bearing in degrees
     * @return delta in degrees [-180, 180]
     */
    public static double angleDelta(double from, double to) {
        double delta = normalizeAngle(to - from);
        if (delta > 180) {
            delta -= 360;
        }
        return delta;
    }
}
