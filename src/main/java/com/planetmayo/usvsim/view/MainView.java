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
        ComboBox<String> addBehaviorDropdown = new ComboBox<>();
        addBehaviorDropdown.setPromptText("+ Add Behaviour");
        addBehaviorDropdown.getItems().addAll(
            "Parallel Track Search",
            "Expanding Square Search",
            "Waypoint Transit",
            "Return to Base"
        );
        addBehaviorDropdown.setPrefWidth(180);
        addBehaviorDropdown.setOnAction(event -> handleAddBehavior(addBehaviorDropdown.getValue()));

        // Configure button
        Button configureBtn = new Button("⚙ Configure");
        configureBtn.setPrefWidth(100);
        configureBtn.setOnAction(event -> handleConfigure());

        box.getChildren().addAll(addBehaviorDropdown, configureBtn);
        return box;
    }

    private void wireEventHandlers() {
        // Control panel handlers (wired in MissionController)
        controlPanel.setOnStart(() -> System.out.println("Start simulation"));
        controlPanel.setOnPause(() -> System.out.println("Pause simulation"));
        controlPanel.setOnStop(() -> System.out.println("Stop simulation"));
        controlPanel.setOnSpeedChange(speed -> System.out.println("Speed: " + speed + "×"));
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
        // TODO: Implement dialog
        System.out.println("Showing Parallel Track Search dialog");
    }

    private void showExpandingSquareDialog() {
        // TODO: Implement dialog
        System.out.println("Showing Expanding Square dialog");
    }

    private void showWaypointTransitDialog() {
        // TODO: Implement dialog
        System.out.println("Showing Waypoint Transit dialog");
    }

    private void showReturnToBaseDialog() {
        // TODO: Implement dialog
        System.out.println("Showing Return to Base dialog");
    }

    private void showPlatformConfigDialog() {
        // TODO: Implement dialog
        System.out.println("Showing Platform Configuration dialog");
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
        Scene scene = new Scene(this, 1000, 700);
        stage.setScene(scene);
        stage.setTitle("USV Mission Planner");
        stage.show();
    }
}
