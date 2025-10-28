package com.planetmayo.usvsim.model.behaviour;

import com.planetmayo.usvsim.controller.BehaviourExecutor;
import com.planetmayo.usvsim.model.geometry.Polygon;
import com.planetmayo.usvsim.model.geometry.Waypoint;
import com.planetmayo.usvsim.model.platform.PlatformDemand;
import com.planetmayo.usvsim.model.platform.PlatformState;
import com.planetmayo.usvsim.model.platform.TurnDirection;
import com.planetmayo.usvsim.util.SearchPatternGenerator;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

/**
 * Expanding square search behaviour.
 *
 * Generates expanding square spiral pattern from polygon centroid for
 * contact investigation or detailed area search.
 *
 * Characteristics:
 * - Starts at polygon centroid
 * - Expands outward in square spiral pattern
 * - Configurable initial direction (bearing)
 * - Configurable leg increment (distance increase per leg pair)
 * - Configurable speed
 * - Completes when all waypoints reached
 */
public class ExpandingSquareSearch implements Behaviour {
    private final Polygon searchArea;
    private final double initialDirection;
    private final double legIncrement;
    private final double platformSpeed;
    private final List<Waypoint> waypoints;
    private BehaviourState state;
    private int currentWaypointIndex;

    /**
     * Create expanding square search behaviour.
     *
     * @param searchArea polygon defining search boundary
     * @param initialDirection initial bearing of first leg (degrees 0-360)
     * @param legIncrement distance increase per leg pair (metres)
     * @param speed platform speed (knots)
     */
    public ExpandingSquareSearch(Polygon searchArea, double initialDirection,
                                 double legIncrement, double speed) {
        this.searchArea = searchArea;
        this.initialDirection = initialDirection;
        this.legIncrement = legIncrement;
        this.platformSpeed = speed;
        this.state = BehaviourState.PENDING;
        this.currentWaypointIndex = 0;

        // Generate expanding square waypoints
        this.waypoints = new ArrayList<>(
            SearchPatternGenerator.generateExpandingSquare(
                searchArea, initialDirection, legIncrement, speed
            )
        );
    }

    @Override
    public String getName() {
        return "Expanding Square Search";
    }

    @Override
    public String getDescription() {
        return String.format("Expanding square spiral: %.0f° initial, %.0fm increment, %g knots",
            initialDirection, legIncrement, platformSpeed);
    }

    @Override
    public BehaviourState getState() {
        return state;
    }

    @Override
    public double getProgress() {
        if (waypoints.isEmpty()) {
            return 1.0; // No waypoints = complete
        }
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
        return Color.web("#9C27B0");  // Purple
    }

    // Getters for editing support
    public Polygon getSearchArea() {
        return searchArea;
    }

    public double getInitialDirection() {
        return initialDirection;
    }

    public double getLegIncrement() {
        return legIncrement;
    }

    public double getPlatformSpeed() {
        return platformSpeed;
    }
}
