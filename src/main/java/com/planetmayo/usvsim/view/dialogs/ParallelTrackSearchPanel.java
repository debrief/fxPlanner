package com.planetmayo.usvsim.view.dialogs;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

/**
 * Inline embeddable panel for parallel track search configuration.
 * Includes Cancel button for drawing cancellation.
 */
public class ParallelTrackSearchPanel extends VBox {
    private TextField orientationField;
    private TextField spacingField;
    private TextField speedField;
    private Label errorLabel;

    private Consumer<ParallelTrackSearchParams> onComplete;
    private Runnable onCancel;

    public ParallelTrackSearchPanel() {
        setSpacing(15);
        setPadding(new Insets(15));
        setStyle("-fx-background-color: white; -fx-border-color: #2196F3; -fx-border-width: 2;");
        setMaxWidth(400);

        // Title
        Label title = new Label("Configure Parallel Track Search");
        title.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");

        // Input fields
        GridPane grid = createInputGrid();

        // Error label
        errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #F44336; -fx-font-size: 10;");

        // Buttons
        HBox buttons = createButtonBar();

        getChildren().addAll(title, grid, errorLabel, buttons);
    }

    private GridPane createInputGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        // Orientation
        Label orientLabel = new Label("Track Orientation (°):");
        orientationField = new TextField("0");
        orientationField.setPromptText("0-360");
        grid.add(orientLabel, 0, 0);
        grid.add(orientationField, 1, 0);

        // Spacing
        Label spacingLabel = new Label("Track Spacing (m):");
        spacingField = new TextField("100");
        spacingField.setPromptText("> 0");
        grid.add(spacingLabel, 0, 1);
        grid.add(spacingField, 1, 1);

        // Speed
        Label speedLabel = new Label("Platform Speed (knots):");
        speedField = new TextField("5");
        speedField.setPromptText("> 0");
        grid.add(speedLabel, 0, 2);
        grid.add(speedField, 1, 2);

        return grid;
    }

    private HBox createButtonBar() {
        HBox box = new HBox(10);
        box.setPadding(new Insets(10, 0, 0, 0));

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setPrefWidth(100);
        cancelBtn.setOnAction(e -> {
            if (onCancel != null) onCancel.run();
        });

        Button okBtn = new Button("OK");
        okBtn.setPrefWidth(100);
        okBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        okBtn.setOnAction(e -> handleOk());

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        box.getChildren().addAll(cancelBtn, spacer, okBtn);
        return box;
    }

    private void handleOk() {
        try {
            double orientation = Double.parseDouble(orientationField.getText());
            double spacing = Double.parseDouble(spacingField.getText());
            double speed = Double.parseDouble(speedField.getText());

            if (orientation < 0 || orientation > 360) {
                errorLabel.setText("Orientation must be 0-360°");
                return;
            }
            if (spacing <= 0) {
                errorLabel.setText("Spacing must be > 0");
                return;
            }
            if (speed <= 0) {
                errorLabel.setText("Speed must be > 0");
                return;
            }

            errorLabel.setText("");
            if (onComplete != null) {
                onComplete.accept(new ParallelTrackSearchParams(orientation, spacing, speed));
            }
        } catch (NumberFormatException e) {
            errorLabel.setText("Invalid number format");
        }
    }

    public void setOnComplete(Consumer<ParallelTrackSearchParams> handler) {
        this.onComplete = handler;
    }

    public void setOnCancel(Runnable handler) {
        this.onCancel = handler;
    }

    /**
     * Set initial values in the form (for editing existing behaviour)
     */
    public void setValues(double orientation, double spacing, double speed) {
        orientationField.setText(String.valueOf(orientation));
        spacingField.setText(String.valueOf(spacing));
        speedField.setText(String.valueOf(speed));
    }
}
