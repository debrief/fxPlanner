package com.planetmayo.usvsim.model.geometry;

import java.util.Objects;

/**
 * Navigation target with metadata for platform routing.
 */
public final class Waypoint {
    private final Position position;
    private final double speed; // knots
    private final double acceptanceRadius; // metres
    private final WaypointType type;

    /**
     * Creates a new Waypoint.
     *
     * @param position target location
     * @param speed transit speed in knots
     * @param acceptanceRadius distance threshold for "reached" in metres
     * @param type waypoint classification
     */
    public Waypoint(Position position, double speed, double acceptanceRadius, WaypointType type) {
        this.position = Objects.requireNonNull(position, "Position cannot be null");
        if (speed <= 0) throw new IllegalArgumentException("Speed must be > 0");
        if (acceptanceRadius <= 0) throw new IllegalArgumentException("Acceptance radius must be > 0");
        this.speed = speed;
        this.acceptanceRadius = acceptanceRadius;
        this.type = Objects.requireNonNull(type, "Type cannot be null");
    }

    /**
     * Factory for search pattern waypoints.
     */
    public static Waypoint search(Position position, double speed) {
        return new Waypoint(position, speed, 50.0, WaypointType.SEARCH);
    }

    /**
     * Factory for transit waypoints.
     */
    public static Waypoint transit(Position position, double speed) {
        return new Waypoint(position, speed, 50.0, WaypointType.TRANSIT);
    }

    /**
     * Factory for base waypoints.
     */
    public static Waypoint base(Position position, double speed) {
        return new Waypoint(position, speed, 100.0, WaypointType.BASE);
    }

    public Position getPosition() {
        return position;
    }

    public double getSpeed() {
        return speed;
    }

    public double getAcceptanceRadius() {
        return acceptanceRadius;
    }

    public WaypointType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Waypoint)) return false;
        Waypoint waypoint = (Waypoint) o;
        return Double.compare(waypoint.speed, speed) == 0 &&
               Double.compare(waypoint.acceptanceRadius, acceptanceRadius) == 0 &&
               Objects.equals(position, waypoint.position) &&
               type == waypoint.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(position, speed, acceptanceRadius, type);
    }

    @Override
    public String toString() {
        return String.format("Waypoint(%s, %.1f knots, %.0fm, %s)",
            position, speed, acceptanceRadius, type);
    }
}
