package com.planetmayo.usvsim.model.mission;

/**
 * Enumeration of mission execution states.
 */
public enum MissionState {
    /** Mission being created/edited - simulation not running */
    PLANNING,

    /** Simulation currently executing */
    EXECUTING,

    /** Simulation paused - can resume */
    PAUSED,

    /** All behaviours completed */
    COMPLETE
}
