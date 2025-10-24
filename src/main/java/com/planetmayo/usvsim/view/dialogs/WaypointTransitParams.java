package com.planetmayo.usvsim.view.dialogs;

import com.planetmayo.usvsim.model.geometry.Position;
import java.util.List;

/**
 * Result class for waypoint transit configuration
 */
public class WaypointTransitParams {
    public final List<Position> waypoints;
    public final double speed;

    public WaypointTransitParams(List<Position> waypoints, double speed) {
        this.waypoints = waypoints;
        this.speed = speed;
    }
}
