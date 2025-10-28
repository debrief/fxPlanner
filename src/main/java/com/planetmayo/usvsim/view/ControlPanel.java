package com.planetmayo.usvsim.view;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Simulation control panel with start/pause/stop and time acceleration slider.
 *
 * Features:
 * - Start/Pause/Stop buttons
 * - Time acceleration slider (1× to 10×)
 * - Visual feedback for active state
 */
public class ControlPanel extends VBox {
    private Button startBtn;
    private Button pauseResumeBtn;
    private Button stopBtn;
    private Slider speedSlider;
    private Label speedLabel;
    private Label timeLabel;
    private boolean isPaused = false;

    private Runnable onStart;
    private Runnable onPause;
    private Runnable onResume;
    private Runnable onStop;
    private java.util.function.Consumer<Double> onSpeedChange;

    public ControlPanel() {
        setStyle("-fx-border-color: #DDD; -fx-padding: 10; -fx-spacing: 10;");
        setPrefHeight(150);

        // Banner
        Label banner = new Label("═══ Simulation Control ═══");
        banner.setStyle("-fx-font-weight: bold; -fx-font-size: 12; -fx-text-alignment: center;");

        // Button row
        HBox buttonRow = createButtonRow();

        // Speed control row
        HBox speedRow = createSpeedRow();

        // Time display row
        HBox timeRow = createTimeRow();

        getChildren().addAll(banner, buttonRow, speedRow, timeRow);
    }

    private HBox createButtonRow() {
        HBox box = new HBox(10);
        box.setStyle("-fx-padding: 5;");

        startBtn = new Button("Start");
        startBtn.setPrefWidth(80);
        startBtn.setStyle("-fx-font-size: 12;");
        startBtn.setOnAction(_ -> {
            if (onStart != null) onStart.run();
            isPaused = false;
            pauseResumeBtn.setText("Pause");
            updateButtonStates(true, false);
        });

        pauseResumeBtn = new Button("Pause");
        pauseResumeBtn.setPrefWidth(80);
        pauseResumeBtn.setStyle("-fx-font-size: 12;");
        pauseResumeBtn.setDisable(true);
        pauseResumeBtn.setOnAction(_ -> {
            if (!isPaused) {
                // Currently running - pause it
                if (onPause != null) onPause.run();
                isPaused = true;
                pauseResumeBtn.setText("Resume");
            } else {
                // Currently paused - resume it
                if (onResume != null) onResume.run();
                isPaused = false;
                pauseResumeBtn.setText("Pause");
            }
        });

        stopBtn = new Button("Stop");
        stopBtn.setPrefWidth(80);
        stopBtn.setStyle("-fx-font-size: 12;");
        stopBtn.setDisable(true);
        stopBtn.setOnAction(_ -> {
            if (onStop != null) onStop.run();
            isPaused = false;
            pauseResumeBtn.setText("Pause");
            updateButtonStates(false, false);
        });

        box.getChildren().addAll(startBtn, pauseResumeBtn, stopBtn);
        return box;
    }

    private HBox createSpeedRow() {
        HBox box = new HBox(10);
        box.setStyle("-fx-padding: 5;");

        Label minLabel = new Label("Speed: 1×");
        minLabel.setPrefWidth(80);

        speedSlider = new Slider(1, 500, 1);
        speedSlider.setShowTickLabels(true);
        speedSlider.setShowTickMarks(true);
        speedSlider.setMajorTickUnit(50);
        speedSlider.setMinorTickCount(4);
        speedSlider.setSnapToTicks(false);  // Allow smooth adjustment
        speedSlider.setPrefWidth(200);
        speedSlider.setOnMouseReleased(_ -> {
            double value = speedSlider.getValue();
            speedLabel.setText(String.format("%.0f×", value));  // Show integer for clarity
            if (onSpeedChange != null) onSpeedChange.accept(value);
        });

        speedLabel = new Label("1×");
        speedLabel.setPrefWidth(50);
        speedLabel.setStyle("-fx-font-weight: bold;");

        Label maxLabel = new Label("500×");
        maxLabel.setPrefWidth(30);

        box.getChildren().addAll(minLabel, speedSlider, speedLabel, maxLabel);
        return box;
    }

    private HBox createTimeRow() {
        HBox box = new HBox(10);
        box.setStyle("-fx-padding: 5;");

        Label timeDisplayLabel = new Label("Simulation Time:");
        timeDisplayLabel.setStyle("-fx-font-weight: bold;");
        timeDisplayLabel.setPrefWidth(120);

        timeLabel = new Label("00:00:00");
        timeLabel.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: black;");
        timeLabel.setPrefWidth(100);

        box.getChildren().addAll(timeDisplayLabel, timeLabel);
        return box;
    }

    /**
     * Set handler for Start button
     */
    public void setOnStart(Runnable handler) {
        this.onStart = handler;
    }

    /**
     * Set handler for Pause button
     */
    public void setOnPause(Runnable handler) {
        this.onPause = handler;
    }

    /**
     * Set handler for Resume button
     */
    public void setOnResume(Runnable handler) {
        this.onResume = handler;
    }

    /**
     * Set handler for Stop button
     */
    public void setOnStop(Runnable handler) {
        this.onStop = handler;
    }

    /**
     * Set handler for speed change
     */
    public void setOnSpeedChange(java.util.function.Consumer<Double> handler) {
        this.onSpeedChange = handler;
    }

    /**
     * Get current speed multiplier from slider
     */
    public double getSpeedMultiplier() {
        return speedSlider.getValue();
    }

    /**
     * Update button enabled states based on simulation state
     */
    private void updateButtonStates(boolean isRunning, boolean isPaused) {
        startBtn.setDisable(isRunning);
        pauseResumeBtn.setDisable(!isRunning);
        stopBtn.setDisable(!isRunning);
    }

    /**
     * Disable controls when simulation completes naturally
     */
    public void setSimulationComplete() {
        startBtn.setDisable(false);
        pauseResumeBtn.setDisable(true);
        stopBtn.setDisable(true);
        isPaused = false;
        pauseResumeBtn.setText("Pause");
    }

    /**
     * Enable or disable Start button based on mission state
     */
    public void setStartButtonEnabled(boolean enabled) {
        startBtn.setDisable(!enabled);
    }

    /**
     * Reset to initial state
     */
    public void reset() {
        speedSlider.setValue(1.0);
        speedLabel.setText("1.0×");
        isPaused = false;
        pauseResumeBtn.setText("Pause");
        updateButtonStates(false, false);
        updateSimulationTime(0);
    }

    /**
     * Update simulation time display (in milliseconds)
     */
    public void updateSimulationTime(long elapsedMillis) {
        long seconds = elapsedMillis / 1000;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        timeLabel.setText(String.format("%02d:%02d:%02d", hours, minutes, secs));
    }
}
