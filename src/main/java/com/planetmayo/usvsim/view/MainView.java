package com.planetmayo.usvsim.view;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Main application window layout.
 *
 * Layout (70/30 split):
 * - Left (70%): Map panel with drawing overlay
 * - Right (30%): Vertical stack
 *   - Simulation Control (buttons + speed slider)
 *   - Mission Plan (behavior list)
 *   - State Panel (platform state display)
 */
public class MainView extends BorderPane {
    private final Stage stage;
    private final MapPanel mapPanel;
    private final ControlPanel controlPanel;
    private final MissionPlanPanel missionPlanPanel;
    private final StatePanel statePanel;
    private MissionControllerCallback missionController;
    private ComboBox<String> addBehaviorDropdown;

    public MainView(Stage stage) {
        this.stage = stage;

        // Create panels
        mapPanel = new MapPanel();
        controlPanel = new ControlPanel();
        missionPlanPanel = new MissionPlanPanel();
        statePanel = new StatePanel();

        // Layout
        setupLayout();

        // Wire event handlers
        wireEventHandlers();
    }

    private void setupLayout() {
        // Left side: Map (70%)
        mapPanel.setPrefWidth(700);
        setCenter(mapPanel);

        // Right side: Control + Mission Plan + State (30%)
        VBox rightPanel = createRightPanel();
        rightPanel.setPrefWidth(300);
        setRight(rightPanel);

        setPadding(new Insets(5));
    }

    private VBox createRightPanel() {
        VBox rightPanel = new VBox(5);
        rightPanel.setStyle("-fx-border-color: #DDD; -fx-border-width: 0 0 0 1;");
        rightPanel.setPadding(new Insets(5));

        // Control panel (top)
        rightPanel.getChildren().add(controlPanel);

        // Mission plan panel (middle)
        VBox.setVgrow(missionPlanPanel, Priority.ALWAYS);
        rightPanel.getChildren().add(missionPlanPanel);

        // Add Behaviour dropdown + Configure
        HBox behaviorButtonBar = createBehaviorButtonBar();
        rightPanel.getChildren().add(behaviorButtonBar);

        // State panel (bottom)
        rightPanel.getChildren().add(statePanel);

        return rightPanel;
    }

    private HBox createBehaviorButtonBar() {
        HBox box = new HBox(10);
        box.setStyle("-fx-padding: 5; -fx-border-color: #EEE; -fx-border-width: 1 0 0 0;");

        // Add Behaviour dropdown
        addBehaviorDropdown = new ComboBox<>();
        addBehaviorDropdown.setPromptText("+ Add Behaviour");
        addBehaviorDropdown.getItems().addAll(
            "Parallel Track Search",
            "Expanding Square Search",
            "Waypoint Transit",
            "Return to Base"
        );
        addBehaviorDropdown.setPrefWidth(180);
        addBehaviorDropdown.setOnAction(_ -> {
            String selected = addBehaviorDropdown.getValue();
            handleAddBehavior(selected);
            // Reset selection to allow re-selecting same behavior type
            javafx.application.Platform.runLater(() -> addBehaviorDropdown.setValue(null));
        });

        // Configure button
        Button configureBtn = new Button("⚙ Configure");
        configureBtn.setPrefWidth(100);
        configureBtn.setOnAction(_ -> handleConfigure());

        box.getChildren().addAll(addBehaviorDropdown, configureBtn);
        return box;
    }

    private void wireEventHandlers() {
        // Control panel handlers (wired in MissionController)
    }

    private void handleAddBehavior(String behaviorType) {
        if (behaviorType == null) return;

        switch (behaviorType) {
            case "Parallel Track Search" -> showParallelTrackSearchDialog();
            case "Expanding Square Search" -> showExpandingSquareDialog();
            case "Waypoint Transit" -> showWaypointTransitDialog();
            case "Return to Base" -> showReturnToBaseDialog();
        }
    }

    private void handleConfigure() {
        showPlatformConfigDialog();
    }

    private void showParallelTrackSearchDialog() {
        if (missionController != null) {
            missionController.onParallelTrackSearchRequested();
        }
    }

    private void showExpandingSquareDialog() {
        if (missionController != null) {
            missionController.onExpandingSquareSearchRequested();
        }
    }

    private void showWaypointTransitDialog() {
        if (missionController != null) {
            missionController.onWaypointTransitRequested();
        }
    }

    private void showReturnToBaseDialog() {
        if (missionController != null) {
            missionController.onReturnToBaseRequested();
        }
    }

    private void showPlatformConfigDialog() {
        if (missionController != null) {
            missionController.onPlatformConfigRequested();
        }
    }

    public MapPanel getMapPanel() {
        return mapPanel;
    }

    public ControlPanel getControlPanel() {
        return controlPanel;
    }

    public MissionPlanPanel getMissionPlanPanel() {
        return missionPlanPanel;
    }

    public StatePanel getStatePanel() {
        return statePanel;
    }

    public void show() {
        Scene scene = new Scene(this, 1000, 900);
        stage.setScene(scene);
        stage.setTitle("USV Mission Planner");
        stage.show();
    }

    /**
     * Set the mission controller callback for behaviour dialog requests
     */
    public void setMissionControllerCallback(MissionControllerCallback controller) {
        this.missionController = controller;
    }

    /**
     * Update Start button enabled state based on mission contents
     */
    public void updateStartButtonState(boolean hasBehaviours) {
        controlPanel.setStartButtonEnabled(hasBehaviours);
    }

    /**
     * Callback interface for MainView to request actions from MissionController
     */
    public interface MissionControllerCallback {
        void onParallelTrackSearchRequested();
        void onExpandingSquareSearchRequested();
        void onWaypointTransitRequested();
        void onReturnToBaseRequested();
        void onPlatformConfigRequested();
    }
}
