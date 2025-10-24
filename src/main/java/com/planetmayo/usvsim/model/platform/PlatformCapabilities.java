package com.planetmayo.usvsim.model.platform;

/**
 * Static characteristics/capabilities of a USV platform.
 */
public final class PlatformCapabilities {
    private final String platformType;
    private final double maxSpeed;
    private final double minSpeed;
    private final double maxDepth;
    private final double turnRadius;
    private final double acceleration;
    private final double deceleration;

    public PlatformCapabilities(String platformType, double maxSpeed, double minSpeed,
                               double maxDepth, double turnRadius, double acceleration,
                               double deceleration) {
        if (maxSpeed <= 0 || minSpeed < 0 || turnRadius <= 0 ||
            acceleration <= 0 || deceleration <= 0 || maxDepth < 0) {
            throw new IllegalArgumentException("All positive parameters must be > 0");
        }
        this.platformType = platformType;
        this.maxSpeed = maxSpeed;
        this.minSpeed = minSpeed;
        this.maxDepth = maxDepth;
        this.turnRadius = turnRadius;
        this.acceleration = acceleration;
        this.deceleration = deceleration;
    }

    public static PlatformCapabilities defaultUSV() {
        return new PlatformCapabilities("USV", 8.0, 0.0, 0.0, 200.0, 0.5, 1.0);
    }

    public String getPlatformType() { return platformType; }
    public double getMaxSpeed() { return maxSpeed; }
    public double getMinSpeed() { return minSpeed; }
    public double getMaxDepth() { return maxDepth; }
    public double getTurnRadius() { return turnRadius; }
    public double getAcceleration() { return acceleration; }
    public double getDeceleration() { return deceleration; }

    @Override
    public String toString() {
        return String.format("PlatformCapabilities(%s, max %.1f knots, turn radius %.0fm)",
            platformType, maxSpeed, turnRadius);
    }
}
