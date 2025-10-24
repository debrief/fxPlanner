package com.planetmayo.usvsim.model.behaviour;

import com.planetmayo.usvsim.model.geometry.Polygon;
import com.planetmayo.usvsim.model.geometry.Waypoint;
import com.planetmayo.usvsim.model.platform.PlatformDemand;
import com.planetmayo.usvsim.model.platform.PlatformState;
import com.planetmayo.usvsim.model.platform.TurnDirection;
import com.planetmayo.usvsim.util.GeoUtils;
import com.planetmayo.usvsim.util.SearchPatternGenerator;
import javafx.scene.paint.Color;

import java.util.List;

/**
 * Behaviour for parallel track search pattern execution.
 */
public class ParallelTrackSearch implements Behaviour {
    private final String name = "Parallel Track Search";
    private final Polygon searchArea;
    private final double trackOrientation;
    private final double trackSpacing;
    private final double platformSpeed;
    private final double acceptanceRadius;

    private List<Waypoint> waypoints;
    private int currentWaypointIndex = 0;
    private BehaviourState state = BehaviourState.PENDING;

    public ParallelTrackSearch(Polygon searchArea, double trackOrientation,
                              double trackSpacing, double platformSpeed) {
        this.searchArea = searchArea;
        this.trackOrientation = trackOrientation;
        this.trackSpacing = trackSpacing;
        this.platformSpeed = platformSpeed;
        this.acceptanceRadius = 50.0; // metres

        // Generate waypoints
        this.waypoints = SearchPatternGenerator.generateParallelTracks(
            searchArea, trackOrientation, trackSpacing, platformSpeed
        );
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return String.format("%.0f°, %.0fm spacing", trackOrientation, trackSpacing);
    }

    @Override
    public BehaviourState getState() {
        return state;
    }

    @Override
    public double getProgress() {
        if (waypoints.isEmpty()) return 1.0;
        return (double) currentWaypointIndex / waypoints.size();
    }

    @Override
    public PlatformDemand getDemandedState(PlatformState currentState) {
        if (state == BehaviourState.PENDING) {
            state = BehaviourState.EXECUTING;
        }

        if (waypoints.isEmpty() || currentWaypointIndex >= waypoints.size()) {
            return new PlatformDemand(currentState.getHeading(), 0.0, 0.0, TurnDirection.SHORTEST);
        }

        Waypoint targetWaypoint = waypoints.get(currentWaypointIndex);
        double desiredHeading = currentState.getPosition().bearingTo(targetWaypoint.getPosition());
        double desiredSpeed = platformSpeed;

        return new PlatformDemand(desiredHeading, desiredSpeed, 0.0, TurnDirection.SHORTEST);
    }

    @Override
    public void updateProgress(PlatformState currentState) {
        if (waypoints.isEmpty()) {
            state = BehaviourState.COMPLETE;
            return;
        }

        if (currentWaypointIndex < waypoints.size()) {
            Waypoint current = waypoints.get(currentWaypointIndex);
            double distance = currentState.getPosition().distanceTo(current.getPosition());

            if (distance <= acceptanceRadius) {
                currentWaypointIndex++;
            }
        }

        if (currentWaypointIndex >= waypoints.size()) {
            state = BehaviourState.COMPLETE;
        }
    }

    @Override
    public boolean isComplete() {
        return state == BehaviourState.COMPLETE;
    }

    @Override
    public List<Waypoint> getWaypoints() {
        return waypoints;
    }

    @Override
    public Color getDisplayColor() {
        return Color.BLUE;
    }

    // Getters for editing support
    public Polygon getSearchArea() {
        return searchArea;
    }

    public double getTrackOrientation() {
        return trackOrientation;
    }

    public double getTrackSpacing() {
        return trackSpacing;
    }

    public double getPlatformSpeed() {
        return platformSpeed;
    }
}
