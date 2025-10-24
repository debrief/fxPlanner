package com.planetmayo.usvsim.model.geometry;

import com.planetmayo.usvsim.util.GeoUtils;
import java.util.Objects;

/**
 * Immutable geographic position (latitude/longitude).
 *
 * Uses decimal degrees for coordinate representation. Suitable for local
 * operations (< 100 km) using great circle calculations.
 */
public final class Position {
    /** Latitude in decimal degrees [-90, 90] */
    private final double latitude;

    /** Longitude in decimal degrees [-180, 180] */
    private final double longitude;

    /**
     * Creates a new Position.
     *
     * @param latitude decimal degrees (must be in [-90, 90])
     * @param longitude decimal degrees (must be in [-180, 180])
     * @throws IllegalArgumentException if coordinates out of valid range
     */
    private Position(double latitude, double longitude) {
        validateCoordinates(latitude, longitude);
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Factory method to create a new Position.
     *
     * @param latitude decimal degrees
     * @param longitude decimal degrees
     * @return new Position instance
     */
    public static Position of(double latitude, double longitude) {
        return new Position(latitude, longitude);
    }

    /**
     * Validates latitude/longitude are within acceptable ranges.
     *
     * @param latitude to validate
     * @param longitude to validate
     * @throws IllegalArgumentException if out of range
     */
    private static void validateCoordinates(double latitude, double longitude) {
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Latitude must be in [-90, 90], got " + latitude);
        }
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Longitude must be in [-180, 180], got " + longitude);
        }
    }

    /**
     * Gets the latitude.
     *
     * @return latitude in decimal degrees
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Gets the longitude.
     *
     * @return longitude in decimal degrees
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * Calculates great circle distance to another position.
     *
     * Uses Haversine formula for accuracy. Distance in metres.
     *
     * @param other target position
     * @return distance in metres
     */
    public double distanceTo(Position other) {
        return GeoUtils.distance(this, other);
    }

    /**
     * Calculates initial bearing to another position.
     *
     * @param other target position
     * @return bearing in degrees [0, 360)
     */
    public double bearingTo(Position other) {
        return GeoUtils.bearing(this, other);
    }

    /**
     * Calculates destination position given distance and bearing.
     *
     * @param distance in metres
     * @param bearing in degrees
     * @return destination Position
     */
    public Position destination(double distance, double bearing) {
        return GeoUtils.destination(this, distance, bearing);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Position)) return false;
        Position position = (Position) o;
        return Double.compare(position.latitude, latitude) == 0 &&
                Double.compare(position.longitude, longitude) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude);
    }

    @Override
    public String toString() {
        return String.format("Position(%.6f, %.6f)", latitude, longitude);
    }
}
