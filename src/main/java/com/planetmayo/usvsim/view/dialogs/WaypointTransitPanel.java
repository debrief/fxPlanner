package com.planetmayo.usvsim.view.dialogs;

import com.planetmayo.usvsim.model.geometry.Position;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Inline embeddable panel for waypoint transit configuration.
 * Includes Cancel button for drawing cancellation.
 */
public class WaypointTransitPanel extends VBox {
    private TextField latField;
    private TextField lonField;
    private Spinner<Double> speedSpinner;
    private ListView<String> waypointList;
    private List<Position> waypoints;
    private Label errorLabel;

    private Consumer<WaypointTransitParams> onComplete;
    private Runnable onCancel;

    public WaypointTransitPanel() {
        this(new ArrayList<>());
    }

    /**
     * Constructor with pre-drawn waypoints from map
     */
    public WaypointTransitPanel(List<Position> drawnWaypoints) {
        waypoints = new ArrayList<>(drawnWaypoints);

        setSpacing(15);
        setPadding(new Insets(15));
        setStyle("-fx-background-color: white; -fx-border-color: #2196F3; -fx-border-width: 2;");
        setMaxWidth(500);

        // Title
        Label title = new Label("Configure Waypoint Transit");
        title.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");

        // Speed section
        HBox speedBox = createSpeedBox();

        // Waypoint input section
        GridPane inputGrid = createInputGrid();

        // Waypoint list section
        VBox listBox = createListBox();

        // Error label
        errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #F44336; -fx-font-size: 10;");

        // Buttons
        HBox buttons = createButtonBar();

        getChildren().addAll(title, speedBox, new Separator(), inputGrid, new Separator(), listBox, errorLabel, buttons);

        // Populate list with pre-drawn waypoints
        if (!waypoints.isEmpty()) {
            updateWaypointList();
        }
    }

    private HBox createSpeedBox() {
        HBox speedBox = new HBox(10);
        speedBox.setPadding(new Insets(5));
        Label speedLabel = new Label("Transit Speed (knots):");
        speedLabel.setStyle("-fx-font-weight: bold;");
        speedSpinner = new Spinner<>(1.0, 12.0, 5.0, 0.5);
        speedSpinner.setPrefWidth(100);
        speedSpinner.setEditable(true);
        speedBox.getChildren().addAll(speedLabel, speedSpinner);
        return speedBox;
    }

    private GridPane createInputGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(5));

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
        addButton.setOnAction(e -> addWaypoint());
        grid.add(addButton, 2, 1, 1, 2);

        return grid;
    }

    private VBox createListBox() {
        VBox listBox = new VBox(10);
        listBox.setPadding(new Insets(5));

        Label listLabel = new Label("Waypoints:");
        listLabel.setStyle("-fx-font-weight: bold;");

        waypointList = new ListView<>();
        waypointList.setPrefHeight(150);
        waypointList.setStyle("-fx-control-inner-background: #f5f5f5;");

        HBox buttonBox = new HBox(10);
        Button removeButton = new Button("Remove Selected");
        removeButton.setPrefWidth(120);
        removeButton.setOnAction(e -> removeWaypoint());
        Button clearButton = new Button("Clear All");
        clearButton.setPrefWidth(120);
        clearButton.setOnAction(e -> clearWaypoints());
        buttonBox.getChildren().addAll(removeButton, clearButton);

        listBox.getChildren().addAll(listLabel, waypointList, buttonBox);
        return listBox;
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

    private void addWaypoint() {
        try {
            double lat = Double.parseDouble(latField.getText());
            double lon = Double.parseDouble(lonField.getText());

            // Validate coordinates
            if (lat < -90 || lat > 90) {
                errorLabel.setText("Latitude must be between -90 and 90 degrees.");
                return;
            }
            if (lon < -180 || lon > 180) {
                errorLabel.setText("Longitude must be between -180 and 180 degrees.");
                return;
            }

            Position pos = Position.of(lat, lon);
            waypoints.add(pos);
            updateWaypointList();

            errorLabel.setText("");
            latField.clear();
            lonField.clear();
            latField.requestFocus();
        } catch (NumberFormatException e) {
            errorLabel.setText("Please enter valid numeric coordinates.");
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

    private void handleOk() {
        if (waypoints.isEmpty()) {
            errorLabel.setText("Please add at least one waypoint.");
            return;
        }

        errorLabel.setText("");
        if (onComplete != null) {
            onComplete.accept(new WaypointTransitParams(new ArrayList<>(waypoints), speedSpinner.getValue()));
        }
    }

    public void setOnComplete(Consumer<WaypointTransitParams> handler) {
        this.onComplete = handler;
    }

    public void setOnCancel(Runnable handler) {
        this.onCancel = handler;
    }
}
