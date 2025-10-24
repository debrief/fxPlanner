package com.planetmayo.usvsim.view;

import com.planetmayo.usvsim.model.platform.PlatformState;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

/**
 * Real-time platform state display panel.
 *
 * Shows:
 * - Platform ID
 * - Current position (lat/lon)
 * - Heading (degrees)
 * - Speed (knots)
 * - Current behavior status
 * - Progress indicator
 * - Update frequency (Hz)
 */
public class StatePanel extends GridPane {
    private final Label positionLabel;
    private final Label headingLabel;
    private final Label speedLabel;
    private final Label statusLabel;
    private final Label progressLabel;
    private final Label updateFreqLabel;

    public StatePanel() {
        setStyle("-fx-border-color: #DDD; -fx-padding: 10; -fx-hgap: 10; -fx-vgap: 5;");
        setPrefHeight(150);

        // Title
        Label title = new Label("State Panel");
        title.setStyle("-fx-font-size: 12; -fx-font-weight: bold;");
        add(title, 0, 0, 2, 1);

        // Position
        Label posLabel = new Label("Position:");
        posLabel.setStyle("-fx-font-weight: bold;");
        positionLabel = new Label("50.60°N, 2.40°W");
        add(posLabel, 0, 1);
        add(positionLabel, 1, 1);

        // Heading
        Label hdgLabel = new Label("Heading:");
        hdgLabel.setStyle("-fx-font-weight: bold;");
        headingLabel = new Label("0°");
        add(hdgLabel, 0, 2);
        add(headingLabel, 1, 2);

        // Speed
        Label spdLabel = new Label("Speed:");
        spdLabel.setStyle("-fx-font-weight: bold;");
        speedLabel = new Label("0 knots");
        add(spdLabel, 0, 3);
        add(speedLabel, 1, 3);

        // Status
        Label statLabel = new Label("Status:");
        statLabel.setStyle("-fx-font-weight: bold;");
        statusLabel = new Label("Ready");
        add(statLabel, 0, 4);
        add(statusLabel, 1, 4);

        // Progress
        Label progLabel = new Label("Progress:");
        progLabel.setStyle("-fx-font-weight: bold;");
        progressLabel = new Label("0%");
        add(progLabel, 0, 5);
        add(progressLabel, 1, 5);

        // Update frequency
        Label freqLabel = new Label("Update Freq:");
        freqLabel.setStyle("-fx-font-weight: bold;");
        updateFreqLabel = new Label("0 Hz");
        add(freqLabel, 0, 6);
        add(updateFreqLabel, 1, 6);

        setHgap(10);
        setVgap(5);
        setPadding(new Insets(10));
    }

    /**
     * Update state display from PlatformState
     */
    public void updateState(PlatformState state) {
        if (state == null) return;

        positionLabel.setText(String.format("%.2f°N, %.2f°W",
            state.getPosition().getLatitude(),
            state.getPosition().getLongitude()));

        headingLabel.setText(String.format("%.0f°", state.getHeading()));
        speedLabel.setText(String.format("%.1f knots", state.getSpeed()));
    }

    /**
     * Update behavior status
     */
    public void setStatus(String status) {
        statusLabel.setText(status);
    }

    /**
     * Update progress percentage
     */
    public void setProgress(double progressPercent) {
        progressLabel.setText(String.format("%.1f%%", progressPercent * 100));
    }

    /**
     * Update update frequency display
     */
    public void setUpdateFrequency(double hz) {
        updateFreqLabel.setText(String.format("%.1f Hz", hz));
    }

    /**
     * Reset to default state
     */
    public void reset() {
        positionLabel.setText("50.60°N, 2.40°W");
        headingLabel.setText("0°");
        speedLabel.setText("0 knots");
        statusLabel.setText("Ready");
        progressLabel.setText("0%");
        updateFreqLabel.setText("0 Hz");
    }
}
