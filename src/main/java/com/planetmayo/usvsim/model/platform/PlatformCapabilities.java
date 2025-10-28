package com.planetmayo.usvsim.model.platform;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Static characteristics/capabilities of a USV platform.
 *
 * Supports persistence to/from platform.properties file for user customization.
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

    // ========== Persistence Methods ==========

    private static final String PROPERTIES_FILE = "platform.properties";
    private static final String USER_HOME_DIR = System.getProperty("user.home");
    private static final Path PROPERTIES_PATH = Paths.get(USER_HOME_DIR, ".usv-planner", PROPERTIES_FILE);

    /**
     * Save platform capabilities to properties file in user home directory.
     * File location: ~/.usv-planner/platform.properties
     *
     * @throws IOException if file cannot be written
     */
    public void save() throws IOException {
        Properties props = new Properties();
        props.setProperty("platformType", platformType);
        props.setProperty("maxSpeed", String.valueOf(maxSpeed));
        props.setProperty("minSpeed", String.valueOf(minSpeed));
        props.setProperty("maxDepth", String.valueOf(maxDepth));
        props.setProperty("turnRadius", String.valueOf(turnRadius));
        props.setProperty("acceleration", String.valueOf(acceleration));
        props.setProperty("deceleration", String.valueOf(deceleration));

        // Ensure parent directory exists
        Files.createDirectories(PROPERTIES_PATH.getParent());

        // Write to file
        try (FileOutputStream out = new FileOutputStream(PROPERTIES_PATH.toFile())) {
            props.store(out, "USV Platform Configuration");
        }
    }

    /**
     * Load platform capabilities from properties file.
     * If file doesn't exist, returns default USV configuration.
     *
     * @return loaded PlatformCapabilities, or default if file not found
     */
    public static PlatformCapabilities load() {
        // Check if properties file exists
        if (!Files.exists(PROPERTIES_PATH)) {
            return defaultUSV();
        }

        try (FileInputStream in = new FileInputStream(PROPERTIES_PATH.toFile())) {
            Properties props = new Properties();
            props.load(in);

            String platformType = props.getProperty("platformType", "USV");
            double maxSpeed = Double.parseDouble(props.getProperty("maxSpeed", "8.0"));
            double minSpeed = Double.parseDouble(props.getProperty("minSpeed", "0.0"));
            double maxDepth = Double.parseDouble(props.getProperty("maxDepth", "0.0"));
            double turnRadius = Double.parseDouble(props.getProperty("turnRadius", "200.0"));
            double acceleration = Double.parseDouble(props.getProperty("acceleration", "0.5"));
            double deceleration = Double.parseDouble(props.getProperty("deceleration", "1.0"));

            return new PlatformCapabilities(platformType, maxSpeed, minSpeed, maxDepth,
                                           turnRadius, acceleration, deceleration);

        } catch (IOException | NumberFormatException e) {
            System.err.println("Failed to load platform configuration: " + e.getMessage());
            System.err.println("Using default configuration");
            return defaultUSV();
        }
    }

    /**
     * Get the path to the properties file.
     *
     * @return Path to platform.properties file
     */
    public static Path getPropertiesPath() {
        return PROPERTIES_PATH;
    }
}
