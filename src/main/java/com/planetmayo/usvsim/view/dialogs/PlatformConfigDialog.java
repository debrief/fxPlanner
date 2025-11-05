package com.planetmayo.usvsim.view.dialogs;

import com.planetmayo.usvsim.model.platform.PlatformCapabilities;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Dialog for configuring platform dynamics (turn radius, speed, acceleration).
 *
 * Allows users to customize USV performance characteristics.
 * Settings are saved to ~/.usv-planner/platform.properties
 */
public class PlatformConfigDialog {
    private final Stage dialog;
    private final TextField turnRadiusField;
    private final TextField maxSpeedField;
    private final TextField accelerationField;
    private final TextField decelerationField;

    private PlatformCapabilities result = null;

    public PlatformConfigDialog(PlatformCapabilities current) {
        dialog = new Stage();
        dialog.setTitle("Platform Configuration");
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setResizable(false);

        // Create form grid
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        // Title label
        Label titleLabel = new Label("Configure USV Platform Dynamics");
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        GridPane.setColumnSpan(titleLabel, 2);
        grid.add(titleLabel, 0, 0);

        // Separator
        Separator sep1 = new Separator();
        GridPane.setColumnSpan(sep1, 2);
        grid.add(sep1, 0, 1);

        // Turn Radius field
        Label turnRadiusLabel = new Label("Turn Radius (m):");
        turnRadiusField = new TextField(String.valueOf(current.getTurnRadius()));
        turnRadiusField.setPrefWidth(150);
        grid.add(turnRadiusLabel, 0, 2);
        grid.add(turnRadiusField, 1, 2);

        // Max Speed field
        Label maxSpeedLabel = new Label("Maximum Speed (knots):");
        maxSpeedField = new TextField(String.valueOf(current.getMaxSpeed()));
        maxSpeedField.setPrefWidth(150);
        grid.add(maxSpeedLabel, 0, 3);
        grid.add(maxSpeedField, 1, 3);

        // Acceleration field
        Label accelerationLabel = new Label("Acceleration (m/s²):");
        accelerationField = new TextField(String.valueOf(current.getAcceleration()));
        accelerationField.setPrefWidth(150);
        grid.add(accelerationLabel, 0, 4);
        grid.add(accelerationField, 1, 4);

        // Deceleration field
        Label decelerationLabel = new Label("Deceleration (m/s²):");
        decelerationField = new TextField(String.valueOf(current.getDeceleration()));
        decelerationField.setPrefWidth(150);
        grid.add(decelerationLabel, 0, 5);
        grid.add(decelerationField, 1, 5);

        // Separator
        Separator sep2 = new Separator();
        GridPane.setColumnSpan(sep2, 2);
        grid.add(sep2, 0, 6);

        // Buttons
        Button saveButton = new Button("Save");
        saveButton.setPrefWidth(80);
        saveButton.setDefaultButton(true);
        saveButton.setOnAction(event -> handleSave());

        Button cancelButton = new Button("Cancel");
        cancelButton.setPrefWidth(80);
        cancelButton.setCancelButton(true);
        cancelButton.setOnAction(event -> dialog.close());

        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(saveButton, cancelButton);
        buttonBox.setStyle("-fx-alignment: center;");
        GridPane.setColumnSpan(buttonBox, 2);
        grid.add(buttonBox, 0, 7);

        // Add validation
        addValidation();

        Scene scene = new Scene(grid);
        dialog.setScene(scene);
    }

    private void addValidation() {
        // Turn radius: 50-500m
        turnRadiusField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateNumericField(turnRadiusField, 50.0, 500.0);
        });

        // Max speed: 1-15 knots
        maxSpeedField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateNumericField(maxSpeedField, 1.0, 15.0);
        });

        // Acceleration: 0.1-2.0 m/s²
        accelerationField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateNumericField(accelerationField, 0.1, 2.0);
        });

        // Deceleration: 0.5-3.0 m/s²
        decelerationField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateNumericField(decelerationField, 0.5, 3.0);
        });
    }

    private void validateNumericField(TextField field, double min, double max) {
        try {
            double value = Double.parseDouble(field.getText());
            if (value < min || value > max) {
                field.setStyle("-fx-border-color: red;");
            } else {
                field.setStyle(null);
            }
        } catch (NumberFormatException e) {
            field.setStyle("-fx-border-color: red;");
        }
    }

    private void handleSave() {
        try {
            // Parse and validate all fields
            double turnRadius = Double.parseDouble(turnRadiusField.getText());
            double maxSpeed = Double.parseDouble(maxSpeedField.getText());
            double acceleration = Double.parseDouble(accelerationField.getText());
            double deceleration = Double.parseDouble(decelerationField.getText());

            // Validate ranges
            if (turnRadius < 50 || turnRadius > 500) {
                showError("Turn Radius must be between 50 and 500 metres");
                return;
            }
            if (maxSpeed < 1 || maxSpeed > 15) {
                showError("Maximum Speed must be between 1 and 15 knots");
                return;
            }
            if (acceleration < 0.1 || acceleration > 2.0) {
                showError("Acceleration must be between 0.1 and 2.0 m/s²");
                return;
            }
            if (deceleration < 0.5 || deceleration > 3.0) {
                showError("Deceleration must be between 0.5 and 3.0 m/s²");
                return;
            }

            // Create new capabilities
            result = new PlatformCapabilities("USV", maxSpeed, 0.0, 0.0,
                                             turnRadius, acceleration, deceleration);

            // Save to file
            result.save();

            // Show success message
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Platform Configuration");
            alert.setHeaderText("Configuration Saved");
            alert.setContentText("Platform settings have been saved to:\n" +
                               PlatformCapabilities.getPropertiesPath());
            alert.showAndWait();

            dialog.close();

        } catch (NumberFormatException e) {
            showError("All fields must contain valid numbers");
        } catch (IOException e) {
            showError("Failed to save configuration: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Invalid Input");
        alert.setHeaderText("Validation Error");
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Show the dialog and wait for user input.
     *
     * @return new PlatformCapabilities if saved, null if cancelled
     */
    public PlatformCapabilities showAndWait() {
        dialog.showAndWait();
        return result;
    }
}
