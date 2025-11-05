package com.planetmayo.usvsim.view.dialogs;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

/**
 * Inline embeddable panel for expanding square search configuration.
 * Includes Cancel button for drawing cancellation.
 */
public class ExpandingSquareSearchPanel extends VBox {
    private Spinner<Double> directionSpinner;
    private Spinner<Double> legIncrementSpinner;
    private Spinner<Double> speedSpinner;

    private Consumer<ExpandingSquareSearchParams> onComplete;
    private Runnable onCancel;

    public ExpandingSquareSearchPanel() {
        setSpacing(15);
        setPadding(new Insets(15));
        setStyle("-fx-background-color: white; -fx-border-color: #2196F3; -fx-border-width: 2;");
        setMaxWidth(400);

        // Title
        Label title = new Label("Configure Expanding Square Search");
        title.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");

        // Content grid
        GridPane grid = createGrid();

        // Buttons
        HBox buttons = createButtonBar();

        getChildren().addAll(title, grid, buttons);
    }

    private GridPane createGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(5));

        // Initial Direction (bearing)
        Label directionLabel = new Label("Initial Direction (bearing):");
        directionSpinner = new Spinner<>(0.0, 360.0, 0.0, 5.0);
        directionSpinner.setPrefWidth(120);
        directionSpinner.setEditable(true);
        grid.add(directionLabel, 0, 0);
        grid.add(directionSpinner, 1, 0);

        Label directionHint = new Label("0°=North, 90°=East, 180°=South, 270°=West");
        directionHint.setStyle("-fx-font-size: 10; -fx-text-fill: #666;");
        grid.add(directionHint, 1, 1);

        // Leg Increment
        Label legLabel = new Label("Leg Increment (metres):");
        legIncrementSpinner = new Spinner<>(100.0, 5000.0, 500.0, 50.0);
        legIncrementSpinner.setPrefWidth(120);
        legIncrementSpinner.setEditable(true);
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
        speedSpinner.setEditable(true);
        grid.add(speedLabel, 0, 4);
        grid.add(speedSpinner, 1, 4);

        return grid;
    }

    private HBox createButtonBar() {
        HBox box = new HBox(10);
        box.setPadding(new Insets(10, 0, 0, 0));

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setPrefWidth(100);
        cancelBtn.setOnAction(event -> {
            if (onCancel != null) onCancel.run();
        });

        Button okBtn = new Button("OK");
        okBtn.setPrefWidth(100);
        okBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        okBtn.setOnAction(event -> handleOk());

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        box.getChildren().addAll(cancelBtn, spacer, okBtn);
        return box;
    }

    private void handleOk() {
        if (onComplete != null) {
            onComplete.accept(new ExpandingSquareSearchParams(
                directionSpinner.getValue(),
                legIncrementSpinner.getValue(),
                speedSpinner.getValue()
            ));
        }
    }

    public void setOnComplete(Consumer<ExpandingSquareSearchParams> handler) {
        this.onComplete = handler;
    }

    public void setOnCancel(Runnable handler) {
        this.onCancel = handler;
    }

    /**
     * Set initial values in the form (for editing existing behaviour)
     */
    public void setValues(double initialDirection, double legIncrement, double speed) {
        directionSpinner.getValueFactory().setValue(initialDirection);
        legIncrementSpinner.getValueFactory().setValue(legIncrement);
        speedSpinner.getValueFactory().setValue(speed);
    }
}
