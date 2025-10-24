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
 * Simple point-to-point navigation behaviour.
 *
 * Navigates sequentially through user-specified waypoints.
 * Used for manual mission planning or returning from search areas.
 *
 * Characteristics:
 * - No pattern generation (waypoints provided by user)
 * - Sequential waypoint visitation
 * - Configurable speed
 * - Simple and deterministic
 */
public class WaypointTransit implements Behaviour {
    private final List<Waypoint> waypoints;
    private final double platformSpeed;
    private BehaviourState state;
    private int currentWaypointIndex;

    /**
     * Create waypoint transit behaviour.
     *
     * @param userWaypoints list of positions to visit (in order)
     * @param speed platform speed (knots)
     */
    public WaypointTransit(List<Position> userWaypoints, double speed) {
        this.platformSpeed = speed;
        this.state = BehaviourState.PENDING;
        this.currentWaypointIndex = 0;

        // Convert positions to waypoints
        this.waypoints = new ArrayList<>();
        for (Position pos : userWaypoints) {
            Waypoint wp = new Waypoint(pos, speed, 50.0, WaypointType.TRANSIT);
            this.waypoints.add(wp);
        }
    }

    @Override
    public String getName() {
        return "Waypoint Transit";
    }

    @Override
    public String getDescription() {
        return String.format("%d waypoints, %g knots", waypoints.size(), platformSpeed);
    }

    @Override
    public BehaviourState getState() {
        return state;
    }

    @Override
    public double getProgress() {
        return BehaviourExecutor.calculateProgress(currentWaypointIndex, waypoints.size());
    }

    @Override
    public PlatformDemand getDemandedState(PlatformState currentState) {
        if (currentWaypointIndex >= waypoints.size()) {
            return new PlatformDemand(currentState.getHeading(), 0.0, 0.0, TurnDirection.SHORTEST);
        }

        Waypoint targetWaypoint = waypoints.get(currentWaypointIndex);
        return BehaviourExecutor.getDemandedState(currentState, targetWaypoint);
    }

    @Override
    public void updateProgress(PlatformState currentState) {
        if (state == BehaviourState.PENDING) {
            state = BehaviourState.EXECUTING;
        }

        // Check if current waypoint has been reached
        if (currentWaypointIndex < waypoints.size()) {
            Waypoint currentWaypoint = waypoints.get(currentWaypointIndex);
            if (BehaviourExecutor.isWaypointReached(currentState, currentWaypoint)) {
                currentWaypointIndex++;
            }
        }

        // Check if all waypoints reached
        if (currentWaypointIndex >= waypoints.size()) {
            state = BehaviourState.COMPLETE;
        }
    }

    @Override
    public boolean isComplete() {
        return state == BehaviourState.COMPLETE || currentWaypointIndex >= waypoints.size();
    }

    @Override
    public List<Waypoint> getWaypoints() {
        return new ArrayList<>(waypoints);
    }

    @Override
    public Color getDisplayColor() {
        return Color.web("#FF9800");  // Orange
    }

    // Getters for editing support
    public double getPlatformSpeed() {
        return platformSpeed;
    }

    public List<Position> getUserWaypoints() {
        List<Position> positions = new ArrayList<>();
        for (Waypoint wp : waypoints) {
            positions.add(wp.getPosition());
        }
        return positions;
    }
}
