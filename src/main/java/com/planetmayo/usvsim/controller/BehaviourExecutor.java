package com.planetmayo.usvsim.controller;

import com.planetmayo.usvsim.model.geometry.Waypoint;
import com.planetmayo.usvsim.model.platform.PlatformDemand;
import com.planetmayo.usvsim.model.platform.PlatformState;
import com.planetmayo.usvsim.model.platform.TurnDirection;

/**
 * Executes behaviour waypoint navigation logic.
 *
 * Responsibilities:
 * - Compute demanded heading/speed to reach next waypoint
 * - Track progress through waypoint list
 * - Handle acceptance radius for waypoint completion
 * - Transition to next waypoint automatically
 */
public class BehaviourExecutor {
    /**
     * Compute demanded state to navigate towards a waypoint
     *
     * @param currentState current platform state
     * @param targetWaypoint target waypoint
     * @return demand (heading, speed, turn direction)
     */
    public static PlatformDemand getDemandedState(PlatformState currentState, Waypoint targetWaypoint) {
        // Calculate bearing to waypoint
        double bearingToWaypoint = currentState.getPosition().bearingTo(targetWaypoint.getPosition());

        // Get waypoint's demanded speed
        double speed = targetWaypoint.getSpeed();

        // Prefer shortest turn
        TurnDirection turnDir = TurnDirection.SHORTEST;

        return new PlatformDemand(bearingToWaypoint, speed, 0.0, turnDir);
    }

    /**
     * Check if waypoint has been reached (within acceptance radius)
     *
     * @param currentState current platform state
     * @param targetWaypoint target waypoint
     * @return true if distance <= acceptance radius
     */
    public static boolean isWaypointReached(PlatformState currentState, Waypoint targetWaypoint) {
        double distance = currentState.getPosition().distanceTo(targetWaypoint.getPosition());
        return distance <= targetWaypoint.getAcceptanceRadius();
    }

    /**
     * Calculate progress through a waypoint list
     *
     * @param currentWaypointIndex index of current waypoint (0-based)
     * @param totalWaypoints total number of waypoints
     * @return progress as 0.0 to 1.0
     */
    public static double calculateProgress(int currentWaypointIndex, int totalWaypoints) {
        if (totalWaypoints == 0) {
            return 1.0;
        }
        return Math.min(1.0, (double) currentWaypointIndex / totalWaypoints);
    }

    /**
     * Find next unreached waypoint
     *
     * @param currentState current platform state
     * @param waypoints list of waypoints
     * @param startIndex index to start searching from
     * @return index of next unreached waypoint, or -1 if all reached
     */
    public static int findNextWaypoint(PlatformState currentState, java.util.List<Waypoint> waypoints, int startIndex) {
        for (int i = startIndex; i < waypoints.size(); i++) {
            Waypoint waypoint = waypoints.get(i);
            if (!isWaypointReached(currentState, waypoint)) {
                return i;
            }
        }
        return -1;  // All waypoints reached
    }
}
