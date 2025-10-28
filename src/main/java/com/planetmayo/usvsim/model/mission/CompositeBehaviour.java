package com.planetmayo.usvsim.model.mission;

import com.planetmayo.usvsim.model.behaviour.Behaviour;
import com.planetmayo.usvsim.model.behaviour.BehaviourState;
import com.planetmayo.usvsim.model.geometry.Waypoint;
import com.planetmayo.usvsim.model.platform.PlatformDemand;
import com.planetmayo.usvsim.model.platform.PlatformState;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

/**
 * Sequences multiple behaviours for multi-phase missions.
 *
 * Implements Behaviour interface to be usable in composite structures.
 * Manages sequential execution and automatic advancement to next behaviour.
 */
public class CompositeBehaviour implements Behaviour {
    private final ObservableList<Behaviour> behaviours;
    private int currentIndex = 0;

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
            return behaviours.get(currentIndex).getDemandedState(currentState);
        }
        return null;
    }

    @Override
    public void updateProgress(PlatformState currentState) {
        if (currentIndex < behaviours.size()) {
            Behaviour current = behaviours.get(currentIndex);
            current.updateProgress(currentState);

            // Advance to next behaviour if current is complete
            if (current.isComplete()) {
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
}
