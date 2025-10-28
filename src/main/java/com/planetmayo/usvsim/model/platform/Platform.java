package com.planetmayo.usvsim.model.platform;

import com.planetmayo.usvsim.model.geometry.Position;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

/**
 * USV platform representing the autonomous vehicle being controlled.
 *
 * Manages current state and track history for mission execution and replay.
 */
public class Platform {
    private final String id;
    private final PlatformCapabilities capabilities;
    private PlatformState currentState;
    private final ObservableList<PlatformState> trackHistory;

    /**
     * Creates a new Platform.
     *
     * @param id platform identifier
     * @param capabilities platform characteristics
     * @param initialState starting position, heading, speed
     */
    public Platform(String id, PlatformCapabilities capabilities, PlatformState initialState) {
        this.id = id;
        this.capabilities = capabilities;
        this.currentState = initialState;
        this.trackHistory = FXCollections.observableArrayList();
        this.trackHistory.add(initialState);
    }

    /**
     * Updates platform state and records in history.
     *
     * @param newState new state
     */
    public void setState(PlatformState newState) {
        currentState = newState;
        trackHistory.add(newState);
    }

    /**
     * Clears track history (called on simulation reset).
     */
    public void clearTrackHistory() {
        trackHistory.clear();
        if (currentState != null) {
            trackHistory.add(currentState);
        }
    }

    public String getId() { return id; }
    public PlatformCapabilities getCapabilities() { return capabilities; }
    public PlatformState getState() { return currentState; }
    public ObservableList<PlatformState> getTrackHistory() { return trackHistory; }
    public List<PlatformState> getTrackHistoryList() { return List.copyOf(trackHistory); }

    /**
     * Gets track as list of positions for visualization.
     */
    public List<Position> getTrackPositions() {
        return trackHistory.stream().map(PlatformState::getPosition).toList();
    }

    @Override
    public String toString() {
        return String.format("Platform(%s, %s)", id, currentState);
    }
}
