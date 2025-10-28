package com.planetmayo.usvsim.view.dialogs;

import com.planetmayo.usvsim.model.geometry.Position;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

/**
 * Inline embeddable panel for return-to-base configuration.
 * No Cancel button needed (no drawing involved).
 */
public class ReturnToBasePanel extends VBox {
    private RadioButton useCurrentRadio;
    private RadioButton useCustomRadio;
    private TextField latField;
    private TextField lonField;
    private Spinner<Double> speedSpinner;
    private Label errorLabel;
    private Position currentPosition;

    private Consumer<ReturnToBaseParams> onComplete;
    private Runnable onCancel;

    public ReturnToBasePanel(Position currentPosition) {
        this.currentPosition = currentPosition;

        setSpacing(15);
        setPadding(new Insets(15));
        setStyle("-fx-background-color: white; -fx-border-color: #2196F3; -fx-border-width: 2;");
        setMaxWidth(400);

        // Title
        Label title = new Label("Configure Return to Base");
        title.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");

        // Content grid
        GridPane grid = createGrid();

        // Error label
        errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #F44336; -fx-font-size: 10;");

        // Buttons (no Cancel button - non-drawing dialog)
        HBox buttons = createButtonBar();

        getChildren().addAll(title, grid, errorLabel, buttons);
    }

    private GridPane createGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(5));

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
        speedSpinner.setEditable(true);
        grid.add(speedSpinner, 1, 7);

        return grid;
    }

    private HBox createButtonBar() {
        HBox box = new HBox(10);
        box.setPadding(new Insets(10, 0, 0, 0));

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setPrefWidth(100);
        cancelBtn.setOnAction(_ -> {
            if (onCancel != null) onCancel.run();
        });

        Button okBtn = new Button("OK");
        okBtn.setPrefWidth(100);
        okBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        okBtn.setOnAction(_ -> handleOk());

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        box.getChildren().addAll(cancelBtn, spacer, okBtn);
        return box;
    }

    private void handleOk() {
        if (useCurrentRadio.isSelected()) {
            errorLabel.setText("");
            if (onComplete != null) {
                onComplete.accept(new ReturnToBaseParams(currentPosition, speedSpinner.getValue()));
            }
        } else {
            try {
                double lat = Double.parseDouble(latField.getText());
                double lon = Double.parseDouble(lonField.getText());

                if (lat < -90 || lat > 90) {
                    errorLabel.setText("Latitude must be between -90 and 90 degrees.");
                    return;
                }
                if (lon < -180 || lon > 180) {
                    errorLabel.setText("Longitude must be between -180 and 180 degrees.");
                    return;
                }

                Position basePos = Position.of(lat, lon);
                errorLabel.setText("");
                if (onComplete != null) {
                    onComplete.accept(new ReturnToBaseParams(basePos, speedSpinner.getValue()));
                }
            } catch (NumberFormatException e) {
                errorLabel.setText("Invalid coordinates. Please enter valid numbers.");
            }
        }
    }

    public void setOnComplete(Consumer<ReturnToBaseParams> handler) {
        this.onComplete = handler;
    }

    public void setOnCancel(Runnable handler) {
        this.onCancel = handler;
    }

    /**
     * Set initial values in the form (for editing existing behaviour)
     */
    public void setValues(Position baseLocation, double speed) {
        // Check if base location matches current position (within small tolerance)
        double tolerance = 0.0001;  // ~10m
        boolean isCurrentPos = Math.abs(baseLocation.getLatitude() - currentPosition.getLatitude()) < tolerance &&
                              Math.abs(baseLocation.getLongitude() - currentPosition.getLongitude()) < tolerance;

        if (isCurrentPos) {
            useCurrentRadio.setSelected(true);
        } else {
            useCustomRadio.setSelected(true);
            latField.setText(String.valueOf(baseLocation.getLatitude()));
            lonField.setText(String.valueOf(baseLocation.getLongitude()));
        }

        speedSpinner.getValueFactory().setValue(speed);
    }
}
