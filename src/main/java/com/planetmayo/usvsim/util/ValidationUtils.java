package com.planetmayo.usvsim.util;

import com.planetmayo.usvsim.model.geometry.Position;

/**
 * Utility class for input validation.
 * Provides validation methods for mission parameters.
 */
public class ValidationUtils {

    private ValidationUtils() {
        // Utility class, no instantiation
    }

    /**
     * Validate track orientation angle
     *
     * @param orientation Angle in degrees
     * @return true if valid (0-359)
     */
    public static boolean isValidOrientation(double orientation) {
        return orientation >= 0 && orientation < 360;
    }

    /**
     * Validate positive distance
     *
     * @param distance Distance in metres
     * @return true if valid (> 0)
     */
    public static boolean isValidDistance(double distance) {
        return distance > 0;
    }

    /**
     * Validate positive speed
     *
     * @param speed Speed in knots
     * @return true if valid (> 0 and <= 20)
     */
    public static boolean isValidSpeed(double speed) {
        return speed > 0 && speed <= 20;
    }

    /**
     * Validate latitude coordinate
     *
     * @param latitude Latitude in degrees (-90 to 90)
     * @return true if valid
     */
    public static boolean isValidLatitude(double latitude) {
        return latitude >= -90 && latitude <= 90;
    }

    /**
     * Validate longitude coordinate
     *
     * @param longitude Longitude in degrees (-180 to 180)
     * @return true if valid
     */
    public static boolean isValidLongitude(double longitude) {
        return longitude >= -180 && longitude <= 180;
    }

    /**
     * Validate position
     *
     * @param position Position to validate
     * @return true if position is valid
     */
    public static boolean isValidPosition(Position position) {
        if (position == null) return false;
        return isValidLatitude(position.getLatitude()) &&
                isValidLongitude(position.getLongitude());
    }

    /**
     * Validate turn radius
     *
     * @param turnRadius Turn radius in metres
     * @return true if valid (>= 50 and <= 500)
     */
    public static boolean isValidTurnRadius(double turnRadius) {
        return turnRadius >= 50 && turnRadius <= 500;
    }

    /**
     * Validate acceleration
     *
     * @param acceleration Acceleration in m/s²
     * @return true if valid (> 0 and <= 1.0)
     */
    public static boolean isValidAcceleration(double acceleration) {
        return acceleration > 0 && acceleration <= 1.0;
    }

    /**
     * Format validation error message
     *
     * @param fieldName Name of the field
     * @param validRange Description of valid range
     * @return Formatted error message
     */
    public static String formatErrorMessage(String fieldName, String validRange) {
        return String.format("%s must be %s", fieldName, validRange);
    }
}
