package com.planetmayo.usvsim.model.behaviour;

/**
 * Enumeration of possible states for a behaviour during mission execution.
 */
public enum BehaviourState {
    /** Behaviour created but not yet started */
    PENDING,

    /** Behaviour is currently executing */
    EXECUTING,

    /** Behaviour has completed all waypoints */
    COMPLETE
}
