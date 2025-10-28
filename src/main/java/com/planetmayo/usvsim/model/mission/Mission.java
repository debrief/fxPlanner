package com.planetmayo.usvsim.model.mission;

import com.planetmayo.usvsim.model.platform.Platform;
import com.planetmayo.usvsim.model.platform.PlatformCapabilities;
import com.planetmayo.usvsim.model.platform.PlatformState;
import com.planetmayo.usvsim.model.geometry.Position;
import javafx.beans.property.*;

import java.time.Instant;

/**
 * Top-level container for a complete mission plan.
 *
 * Manages mission state, behaviours (via CompositeBehaviour), and platform.
 */
public class Mission {
    private final StringProperty name;
    private final ObjectProperty<MissionState> state;
    private final CompositeBehaviour missionPlan;
    private final Platform platform;
    private final ObjectProperty<Instant> startTime;
    private boolean isDirty = false;

    public Mission(String missionName, Platform platform) {
        this.name = new SimpleStringProperty(missionName);
        this.state = new SimpleObjectProperty<>(MissionState.PLANNING);
        this.missionPlan = new CompositeBehaviour();
        this.platform = platform;
        this.startTime = new SimpleObjectProperty<>();
    }

    public static Mission createDefault(Position startPosition) {
        PlatformCapabilities caps = PlatformCapabilities.defaultUSV();
        PlatformState initialState = new PlatformState(
            "USV-1",
            startPosition,
            0.0,  // heading
            0.0,  // speed
            0.0,  // depth
            Instant.now()
        );
        Platform platform = new Platform("USV-1", caps, initialState);
        return new Mission("New Mission", platform);
    }

    // Mission control
    public void start() {
        state.set(MissionState.EXECUTING);
        startTime.set(Instant.now());
    }

    public void pause() {
        if (state.get() == MissionState.EXECUTING) {
            state.set(MissionState.PAUSED);
        }
    }

    public void resume() {
        if (state.get() == MissionState.PAUSED) {
            state.set(MissionState.EXECUTING);
        }
    }

    public void stop() {
        state.set(MissionState.PLANNING);
        startTime.set(null);
        platform.clearTrackHistory();
    }

    public boolean isComplete() {
        return missionPlan.isComplete();
    }

    public double getProgress() {
        return missionPlan.getProgress();
    }

    // Accessors
    public String getName() { return name.get(); }
    public void setName(String value) { name.set(value); }
    public StringProperty nameProperty() { return name; }

    public MissionState getState() { return state.get(); }
    public ObjectProperty<MissionState> stateProperty() { return state; }

    public CompositeBehaviour getMissionPlan() { return missionPlan; }
    public Platform getPlatform() { return platform; }

    public Instant getStartTime() { return startTime.get(); }
    public ObjectProperty<Instant> startTimeProperty() { return startTime; }

    // Dirty state tracking for unsaved changes
    public boolean isDirty() { return isDirty; }
    public void setDirty(boolean dirty) { this.isDirty = dirty; }
    public void markDirty() { this.isDirty = true; }
    public void clearDirty() { this.isDirty = false; }

    @Override
    public String toString() {
        return String.format("Mission(%s, %s, %d behaviours)",
            name.get(), state.get(), missionPlan.getBehaviours().size());
    }
}
