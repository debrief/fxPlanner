package com.planetmayo.usvsim.model.platform;

import com.planetmayo.usvsim.model.geometry.Position;
import javafx.beans.property.*;

import java.time.Instant;

/**
 * Current dynamic state of the USV (mutable).
 * Uses JavaFX properties for UI binding.
 */
public class PlatformState {
    private final StringProperty id;
    private final ObjectProperty<Position> position;
    private final DoubleProperty heading;
    private final DoubleProperty speed;
    private final DoubleProperty depth;
    private final ObjectProperty<Instant> timestamp;

    public PlatformState(String id, Position position, double heading, double speed, double depth, Instant timestamp) {
        this.id = new SimpleStringProperty(id);
        this.position = new SimpleObjectProperty<>(position);
        this.heading = new SimpleDoubleProperty(heading);
        this.speed = new SimpleDoubleProperty(speed);
        this.depth = new SimpleDoubleProperty(depth);
        this.timestamp = new SimpleObjectProperty<>(timestamp);
    }

    // Getters
    public String getId() { return id.get(); }
    public Position getPosition() { return position.get(); }
    public double getHeading() { return heading.get(); }
    public double getSpeed() { return speed.get(); }
    public double getDepth() { return depth.get(); }
    public Instant getTimestamp() { return timestamp.get(); }

    // Properties for binding
    public StringProperty idProperty() { return id; }
    public ObjectProperty<Position> positionProperty() { return position; }
    public DoubleProperty headingProperty() { return heading; }
    public DoubleProperty speedProperty() { return speed; }
    public DoubleProperty depthProperty() { return depth; }
    public ObjectProperty<Instant> timestampProperty() { return timestamp; }

    // Setters
    public void setPosition(Position value) { position.set(value); }
    public void setHeading(double value) { heading.set(value); }
    public void setSpeed(double value) { speed.set(value); }
    public void setDepth(double value) { depth.set(value); }
    public void setTimestamp(Instant value) { timestamp.set(value); }

    @Override
    public String toString() {
        return String.format("PlatformState(pos=%.4f/%.4f, hdg=%.0f°, spd=%.1f knots)",
            position.get().getLatitude(), position.get().getLongitude(), heading.get(), speed.get());
    }
}
