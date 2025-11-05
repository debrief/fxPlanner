package com.planetmayo.usvsim.view.dialogs;

import com.planetmayo.usvsim.model.geometry.Position;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

/**
 * Dialog for configuring waypoint transit behaviour.
 *
 * Options:
 * - Specify waypoints via lat/lon input
 * - Add/remove waypoints interactively
 * - Set transit speed
 * - Preview waypoint list
 */
public class WaypointTransitDialog extends Dialog<WaypointTransitParams> {
    private TextField latField;
    private TextField lonField;
    private Spinner<Double> speedSpinner;
    private ListView<String> waypointList;
    private List<Position> waypoints;

    public WaypointTransitDialog() {
        this(new ArrayList<>());
    }

    /**
     * Constructor with pre-drawn waypoints from map
     */
    public WaypointTransitDialog(List<Position> drawnWaypoints) {
        setTitle("Waypoint Transit Configuration");
        setHeaderText("Configure waypoint-based navigation");

        waypoints = new ArrayList<>(drawnWaypoints);

        // Create content
        VBox content = createContent();
        getDialogPane().setContent(content);

        // Populate list with pre-drawn waypoints
        if (!waypoints.isEmpty()) {
            updateWaypointList();
        }

        // Add buttons
        ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(okButton, cancelButton);

        // Result converter
        setResultConverter(dialogButton -> {
            if (dialogButton == okButton) {
                if (waypoints.isEmpty()) {
                    showError("Please add at least one waypoint.");
                    return null;
                }
                return new WaypointTransitParams(new ArrayList<>(waypoints), speedSpinner.getValue());
            }
            return null;
        });
    }

    private VBox createContent() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(20));

        // Speed section
        HBox speedBox = createSpeedBox();
        vbox.getChildren().add(speedBox);

        // Waypoint input section
        GridPane inputGrid = createInputGrid();
        vbox.getChildren().add(new Separator());
        vbox.getChildren().add(inputGrid);

        // Waypoint list section
        VBox listBox = createListBox();
        vbox.getChildren().add(new Separator());
        vbox.getChildren().add(listBox);

        return vbox;
    }

    private HBox createSpeedBox() {
        HBox speedBox = new HBox(10);
        Label speedLabel = new Label("Transit Speed (knots):");
        speedLabel.setStyle("-fx-font-weight: bold;");
        speedSpinner = new Spinner<>(1.0, 12.0, 5.0, 0.5);
        speedSpinner.setPrefWidth(100);
        speedBox.getChildren().addAll(speedLabel, speedSpinner);
        return speedBox;
    }

    private GridPane createInputGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        Label coordLabel = new Label("Add Waypoint:");
        coordLabel.setStyle("-fx-font-weight: bold;");
        grid.add(coordLabel, 0, 0, 3, 1);

        Label latLabel = new Label("Latitude:");
        latField = new TextField();
        latField.setPromptText("50.600");
        latField.setPrefWidth(120);
        grid.add(latLabel, 0, 1);
        grid.add(latField, 1, 1);

        Label lonLabel = new Label("Longitude:");
        lonField = new TextField();
        lonField.setPromptText("-2.400");
        lonField.setPrefWidth(120);
        grid.add(lonLabel, 0, 2);
        grid.add(lonField, 1, 2);

        Button addButton = new Button("Add Waypoint");
        addButton.setPrefWidth(120);
        addButton.setOnAction(event -> addWaypoint());
        grid.add(addButton, 2, 1, 1, 2);

        return grid;
    }

    private VBox createListBox() {
        VBox listBox = new VBox(10);

        Label listLabel = new Label("Waypoints:");
        listLabel.setStyle("-fx-font-weight: bold;");

        waypointList = new ListView<>();
        waypointList.setPrefHeight(150);
        waypointList.setStyle("-fx-control-inner-background: #f5f5f5;");

        HBox buttonBox = new HBox(10);
        Button removeButton = new Button("Remove Selected");
        removeButton.setPrefWidth(120);
        removeButton.setOnAction(event -> removeWaypoint());
        Button clearButton = new Button("Clear All");
        clearButton.setPrefWidth(120);
        clearButton.setOnAction(event -> clearWaypoints());
        buttonBox.getChildren().addAll(removeButton, clearButton);

        listBox.getChildren().addAll(listLabel, waypointList, buttonBox);
        return listBox;
    }

    private void addWaypoint() {
        try {
            double lat = Double.parseDouble(latField.getText());
            double lon = Double.parseDouble(lonField.getText());

            // Validate coordinates
            if (lat < -90 || lat > 90) {
                showError("Latitude must be between -90 and 90 degrees.");
                return;
            }
            if (lon < -180 || lon > 180) {
                showError("Longitude must be between -180 and 180 degrees.");
                return;
            }

            Position pos = Position.of(lat, lon);
            waypoints.add(pos);
            updateWaypointList();

            latField.clear();
            lonField.clear();
            latField.requestFocus();
        } catch (NumberFormatException e) {
            showError("Please enter valid numeric coordinates.");
        }
    }

    private void removeWaypoint() {
        int selectedIndex = waypointList.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < waypoints.size()) {
            waypoints.remove(selectedIndex);
            updateWaypointList();
        }
    }

    private void clearWaypoints() {
        waypoints.clear();
        updateWaypointList();
    }

    private void updateWaypointList() {
        waypointList.getItems().clear();
        for (int i = 0; i < waypoints.size(); i++) {
            Position pos = waypoints.get(i);
            String item = String.format("%d. (%.4f°N, %.4f°E)",
                i + 1,
                pos.getLatitude(),
                pos.getLongitude());
            waypointList.getItems().add(item);
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Invalid Input");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
