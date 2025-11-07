package com.planetmayo.usvsim.model.mission;

import com.planetmayo.usvsim.model.behaviour.Behaviour;
import com.planetmayo.usvsim.model.behaviour.BehaviourExecutionState;
import com.planetmayo.usvsim.model.behaviour.BehaviourState;
import com.planetmayo.usvsim.model.geometry.Waypoint;
import com.planetmayo.usvsim.model.platform.PlatformDemand;
import com.planetmayo.usvsim.model.platform.PlatformState;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sequences multiple behaviours for multi-phase missions.
 *
 * Implements Behaviour interface to be usable in composite structures.
 * Manages sequential execution and automatic advancement to next behaviour.
 */
public class CompositeBehaviour implements Behaviour {
    private final ObservableList<Behaviour> behaviours;
    private int currentIndex = 0;
    // Track execution state for each child behavior (for stateless delegation)
    private final Map<Integer, BehaviourExecutionState> childStates = new HashMap<>();

    public CompositeBehaviour() {
        this.behaviours = FXCollections.observableArrayList();
    }

    public void addBehaviour(Behaviour behaviour) {
        behaviours.add(behaviour);
    }

    public void removeBehaviour(int index) {
        if (index >= 0 && index < behaviours.size()) {
            behaviours.remove(index);
            if (currentIndex >= behaviours.size() && currentIndex > 0) {
                currentIndex--;
            }
        }
    }

    /**
     * Replace a behaviour at a specific index.
     * Used for editing behaviours without changing mission structure.
     */
    public void setBehaviour(int index, Behaviour behaviour) {
        if (index >= 0 && index < behaviours.size()) {
            behaviours.set(index, behaviour);
        }
    }

    public void reorderBehaviour(int from, int to) {
        if (from >= 0 && from < behaviours.size() && to >= 0 && to < behaviours.size()) {
            Behaviour behaviour = behaviours.remove(from);
            behaviours.add(to, behaviour);
        }
    }

    public Behaviour getCurrentBehaviour() {
        if (currentIndex < behaviours.size()) {
            return behaviours.get(currentIndex);
        }
        return null;
    }

    public List<Behaviour> getBehaviours() {
        return new ArrayList<>(behaviours);
    }

    public ObservableList<Behaviour> getBehavioursObservable() {
        return behaviours;
    }

    @Override
    public String getName() {
        return "Mission Plan";
    }

    @Override
    public String getDescription() {
        return String.format("%d behaviours", behaviours.size());
    }

    @Override
    public BehaviourState getState() {
        if (behaviours.isEmpty()) return BehaviourState.COMPLETE;
        if (currentIndex >= behaviours.size()) return BehaviourState.COMPLETE;
        return behaviours.get(currentIndex).getState();
    }

    @Override
    public double getProgress() {
        if (behaviours.isEmpty()) return 1.0;
        double totalProgress = 0;
        for (Behaviour b : behaviours) {
            totalProgress += b.getProgress();
        }
        return totalProgress / behaviours.size();
    }

    @Override
    public PlatformDemand getDemandedState(PlatformState currentState) {
        if (currentIndex < behaviours.size()) {
            Behaviour currentBehaviour = behaviours.get(currentIndex);

            // Get or create execution state for current child
            BehaviourExecutionState childState = childStates.computeIfAbsent(
                currentIndex,
                idx -> BehaviourExecutionState.initial("composite-child-" + idx)
            );

            // Delegate to stateless method
            return currentBehaviour.calculateDemand(childState, currentState);
        }
        return null;
    }

    @Override
    public void updateProgress(PlatformState currentState) {
        if (currentIndex < behaviours.size()) {
            Behaviour currentBehaviour = behaviours.get(currentIndex);

            // Get or create execution state for current child
            BehaviourExecutionState childState = childStates.computeIfAbsent(
                currentIndex,
                idx -> BehaviourExecutionState.initial("composite-child-" + idx)
            );

            // Call stateless updateProgress and store returned state
            BehaviourExecutionState newChildState = currentBehaviour.updateProgress(childState, currentState);
            childStates.put(currentIndex, newChildState);

            // Advance to next behaviour if current is complete
            if (currentBehaviour.isComplete(newChildState)) {
                currentIndex++;
            }
        }
    }

    @Override
    public boolean isComplete() {
        if (behaviours.isEmpty()) return true;
        // Complete when currentIndex has advanced past all behaviours
        return currentIndex >= behaviours.size();
    }

    @Override
    public List<Waypoint> getWaypoints() {
        List<Waypoint> allWaypoints = new ArrayList<>();
        for (Behaviour b : behaviours) {
            allWaypoints.addAll(b.getWaypoints());
        }
        return allWaypoints;
    }

    @Override
    public Color getDisplayColor() {
        if (getCurrentBehaviour() != null) {
            return getCurrentBehaviour().getDisplayColor();
        }
        return Color.BLACK;
    }

    // ===================================================================
    // STATELESS INTERFACE IMPLEMENTATION (for REST API / Web Frontend)
    // ===================================================================

    @Override
    public PlatformDemand calculateDemand(com.planetmayo.usvsim.model.behaviour.BehaviourExecutionState executionState, PlatformState platformState) {
        // CompositeBehaviour stateless implementation not yet fully supported
        // For now, delegate to current behavior based on stateful index
        if (getCurrentBehaviour() != null) {
            return getCurrentBehaviour().calculateDemand(executionState, platformState);
        }
        return new PlatformDemand(platformState.getHeading(), 0.0, 0.0,
            com.planetmayo.usvsim.model.platform.TurnDirection.SHORTEST);
    }

    @Override
    public com.planetmayo.usvsim.model.behaviour.BehaviourExecutionState updateProgress(
            com.planetmayo.usvsim.model.behaviour.BehaviourExecutionState executionState,
            PlatformState platformState) {
        // CompositeBehaviour stateless implementation not yet fully supported
        // For now, delegate to current behavior
        if (getCurrentBehaviour() != null) {
            return getCurrentBehaviour().updateProgress(executionState, platformState);
        }
        return executionState.withState(com.planetmayo.usvsim.model.behaviour.BehaviourState.COMPLETE);
    }

    @Override
    public boolean isComplete(com.planetmayo.usvsim.model.behaviour.BehaviourExecutionState executionState) {
        // Composite is complete if all behaviors are complete
        return currentIndex >= behaviours.size();
    }
}
