package com.planetmayo.usvsim.view.dialogs;

import com.planetmayo.usvsim.model.geometry.Position;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

/**
 * Dialog for configuring return-to-base behaviour.
 *
 * Options:
 * - Use current platform position as base
 * - Specify custom base coordinates (lat/lon)
 * - Set transit speed
 */
public class ReturnToBaseDialog extends Dialog<ReturnToBaseParams> {
    private RadioButton useCurrentRadio;
    private RadioButton useCustomRadio;
    private TextField latField;
    private TextField lonField;
    private Spinner<Double> speedSpinner;

    public ReturnToBaseDialog(Position currentPosition) {
        setTitle("Return to Base Configuration");
        setHeaderText("Configure return-to-base behaviour");

        // Create content
        GridPane grid = createGrid(currentPosition);
        getDialogPane().setContent(grid);

        // Add buttons
        ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(okButton, cancelButton);

        // Result converter
        setResultConverter(dialogButton -> {
            if (dialogButton == okButton) {
                if (useCurrentRadio.isSelected()) {
                    return new ReturnToBaseParams(currentPosition, speedSpinner.getValue());
                } else {
                    try {
                        double lat = Double.parseDouble(latField.getText());
                        double lon = Double.parseDouble(lonField.getText());
                        Position basePos = Position.of(lat, lon);
                        return new ReturnToBaseParams(basePos, speedSpinner.getValue());
                    } catch (NumberFormatException e) {
                        showError("Invalid coordinates. Please enter valid numbers.");
                        return null;
                    }
                }
            }
            return null;
        });
    }

    private GridPane createGrid(Position currentPosition) {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        // Base location selection
        Label baseLabel = new Label("Base Location:");
        baseLabel.setStyle("-fx-font-weight: bold;");
        grid.add(baseLabel, 0, 0, 2, 1);

        useCurrentRadio = new RadioButton("Use current position");
        useCurrentRadio.setSelected(true);
        grid.add(useCurrentRadio, 0, 1, 2, 1);

        useCustomRadio = new RadioButton("Specify coordinates:");
        grid.add(useCustomRadio, 0, 2, 2, 1);

        ToggleGroup group = new ToggleGroup();
        useCurrentRadio.setToggleGroup(group);
        useCustomRadio.setToggleGroup(group);

        // Coordinate inputs (initially disabled)
        Label latLabel = new Label("Latitude:");
        latField = new TextField();
        latField.setPromptText("50.600");
        latField.setDisable(true);
        latField.setPrefWidth(150);
        grid.add(latLabel, 0, 3);
        grid.add(latField, 1, 3);

        Label lonLabel = new Label("Longitude:");
        lonField = new TextField();
        lonField.setPromptText("-2.400");
        lonField.setDisable(true);
        lonField.setPrefWidth(150);
        grid.add(lonLabel, 0, 4);
        grid.add(lonField, 1, 4);

        // Display current position
        Label currentLabel = new Label(String.format("Current: %.3f°N, %.3f°E",
            currentPosition.getLatitude(),
            currentPosition.getLongitude()));
        currentLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 10;");
        grid.add(currentLabel, 0, 5, 2, 1);

        // Toggle listener
        useCustomRadio.selectedProperty().addListener((_, _, newVal) -> {
            latField.setDisable(!newVal);
            lonField.setDisable(!newVal);
        });

        // Speed selection
        Label speedLabel = new Label("Transit Speed (knots):");
        speedLabel.setStyle("-fx-font-weight: bold;");
        grid.add(speedLabel, 0, 6, 2, 1);

        speedSpinner = new Spinner<>(1.0, 12.0, 5.0, 0.5);
        speedSpinner.setPrefWidth(150);
        grid.add(speedSpinner, 1, 7);

        return grid;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Invalid Input");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
