package com.planetmayo.usvsim.model.behaviour;

import com.planetmayo.usvsim.controller.BehaviourExecutor;
import com.planetmayo.usvsim.model.geometry.Position;
import com.planetmayo.usvsim.model.geometry.Waypoint;
import com.planetmayo.usvsim.model.geometry.WaypointType;
import com.planetmayo.usvsim.model.platform.PlatformDemand;
import com.planetmayo.usvsim.model.platform.PlatformState;
import com.planetmayo.usvsim.model.platform.TurnDirection;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

/**
 * Return to base behaviour.
 *
 * Navigates directly to a designated base location.
 * Used at end of mission or emergency procedures.
 *
 * Characteristics:
 * - Single waypoint (base location)
 * - Direct navigation
 * - Configurable speed
 * - Simple completion condition (reached base)
 */
public class ReturnToBase implements Behaviour {
    private final Position baseLocation;
    private final double platformSpeed;
    private final List<Waypoint> waypoints;
    private BehaviourState state;
    private boolean reachedBase;
    private boolean completionLogged = false;
    private long lastLogTime = 0;

    /**
     * Create return to base behaviour.
     *
     * @param baseLocation position of base/return point
     * @param speed platform speed (knots)
     */
    public ReturnToBase(Position baseLocation, double speed) {
        this.baseLocation = baseLocation;
        this.platformSpeed = speed;
        this.state = BehaviourState.PENDING;
        this.reachedBase = false;

        // Create single waypoint for base location
        this.waypoints = new ArrayList<>();
        Waypoint baseWaypoint = new Waypoint(baseLocation, speed, 50.0, WaypointType.BASE);
        this.waypoints.add(baseWaypoint);
    }

    @Override
    public String getName() {
        return "Return to Base";
    }

    @Override
    public String getDescription() {
        return String.format("Base: %.2f°N, %.2f°E at %g knots",
            baseLocation.getLatitude(),
            baseLocation.getLongitude(),
            platformSpeed);
    }

    @Override
    public BehaviourState getState() {
        return state;
    }

    @Override
    public double getProgress() {
        if (reachedBase) {
            return 1.0;
        }
        // Return 0% until reaching base (simple but correct)
        return 0.0;
    }

    @Override
    public PlatformDemand getDemandedState(PlatformState currentState) {
        if (reachedBase) {
            return new PlatformDemand(currentState.getHeading(), 0.0, 0.0, TurnDirection.SHORTEST);
        }

        PlatformDemand demand = BehaviourExecutor.getDemandedState(currentState, waypoints.get(0));

        // Debug logging every 5 seconds
        long now = System.currentTimeMillis();
        if (lastLogTime == 0 || now - lastLogTime > 5000) {
            double distance = currentState.getPosition().distanceTo(waypoints.get(0).getPosition());
            System.out.println("RTB: distance=" + String.format("%.1fm", distance) +
                             ", demanded_speed=" + String.format("%.1fkn", demand.getDemandedSpeed()) +
                             ", actual_speed=" + String.format("%.1fkn", currentState.getSpeed()));
            lastLogTime = now;
        }

        return demand;
    }

    @Override
    public void updateProgress(PlatformState currentState) {
        if (state == BehaviourState.PENDING) {
            state = BehaviourState.EXECUTING;
        }

        Waypoint baseWaypoint = waypoints.get(0);
        double distance = currentState.getPosition().distanceTo(baseWaypoint.getPosition());
        double speed = currentState.getSpeed();

        // Multiple completion criteria for robustness
        boolean withinRadius = BehaviourExecutor.isWaypointReached(currentState, baseWaypoint);
        boolean nearlyStopped = speed < 0.5;  // knots
        boolean closeToBase = distance <= baseWaypoint.getAcceptanceRadius() * 2.0;  // Within 2x radius
        boolean veryClose = distance <= 100.0;  // Within 100m - very generous
        boolean almostStopped = speed < 1.0;  // < 1 knot

        // Complete if: within radius, OR (nearly stopped AND close), OR (very close AND almost stopped)
        if (withinRadius || (nearlyStopped && closeToBase) || (veryClose && almostStopped)) {
            reachedBase = true;
            state = BehaviourState.COMPLETE;

            // Log completion only once
            if (!completionLogged) {
                System.out.println("Return to Base complete - distance: " + String.format("%.1fm", distance) +
                                 ", speed: " + String.format("%.2fkn", speed));
                completionLogged = true;
            }
        }
    }

    @Override
    public boolean isComplete() {
        return reachedBase || state == BehaviourState.COMPLETE;
    }

    @Override
    public List<Waypoint> getWaypoints() {
        return new ArrayList<>(waypoints);
    }

    @Override
    public Color getDisplayColor() {
        return Color.web("#F44336");  // Red
    }

    // Getters for editing support
    public Position getBaseLocation() {
        return baseLocation;
    }

    public double getPlatformSpeed() {
        return platformSpeed;
    }
}
