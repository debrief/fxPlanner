package com.planetmayo.usvsim.view.dialogs;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

/**
 * Dialog for configuring expanding square search pattern.
 *
 * Options:
 * - Initial direction (bearing 0-360°)
 * - Leg increment (distance between spiral rings)
 * - Transit speed
 */
public class ExpandingSquareSearchDialog extends Dialog<ExpandingSquareSearchParams> {
    private Spinner<Double> directionSpinner;
    private Spinner<Double> legIncrementSpinner;
    private Spinner<Double> speedSpinner;

    public ExpandingSquareSearchDialog() {
        setTitle("Expanding Square Search Configuration");
        setHeaderText("Configure expanding square spiral pattern");

        // Create content
        GridPane grid = createGrid();
        getDialogPane().setContent(grid);

        // Add buttons
        ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(okButton, cancelButton);

        // Result converter
        setResultConverter(dialogButton -> {
            if (dialogButton == okButton) {
                return new ExpandingSquareSearchParams(
                    directionSpinner.getValue(),
                    legIncrementSpinner.getValue(),
                    speedSpinner.getValue()
                );
            }
            return null;
        });
    }

    private GridPane createGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        // Initial Direction (bearing)
        Label directionLabel = new Label("Initial Direction (bearing):");
        directionSpinner = new Spinner<>(0.0, 360.0, 0.0, 5.0);
        directionSpinner.setPrefWidth(120);
        grid.add(directionLabel, 0, 0);
        grid.add(directionSpinner, 1, 0);

        Label directionHint = new Label("0°=North, 90°=East, 180°=South, 270°=West");
        directionHint.setStyle("-fx-font-size: 10; -fx-text-fill: #666;");
        grid.add(directionHint, 1, 1);

        // Leg Increment
        Label legLabel = new Label("Leg Increment (metres):");
        legIncrementSpinner = new Spinner<>(100.0, 5000.0, 500.0, 50.0);
        legIncrementSpinner.setPrefWidth(120);
        grid.add(legLabel, 0, 2);
        grid.add(legIncrementSpinner, 1, 2);

        Label legHint = new Label("Distance added per spiral ring (larger = fewer waypoints)");
        legHint.setStyle("-fx-font-size: 10; -fx-text-fill: #666;");
        grid.add(legHint, 1, 3);

        // Speed
        Label speedLabel = new Label("Transit Speed (knots):");
        speedLabel.setStyle("-fx-font-weight: bold;");
        speedSpinner = new Spinner<>(1.0, 12.0, 5.0, 0.5);
        speedSpinner.setPrefWidth(120);
        grid.add(speedLabel, 0, 4);
        grid.add(speedSpinner, 1, 4);

        return grid;
    }
}
