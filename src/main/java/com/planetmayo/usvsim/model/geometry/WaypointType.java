package com.planetmayo.usvsim.model.geometry;

/**
 * Enumeration of waypoint types for classification and visualization.
 */
public enum WaypointType {
    /** Simple point-to-point navigation waypoint */
    TRANSIT,

    /** Part of search pattern (parallel track, expanding square) */
    SEARCH,

    /** Return-to-base destination */
    BASE,

    /** Intermediate turn waypoint */
    TURN
}
