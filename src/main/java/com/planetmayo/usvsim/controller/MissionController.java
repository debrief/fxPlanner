package com.planetmayo.usvsim.controller;

import com.planetmayo.usvsim.model.behaviour.ParallelTrackSearch;
import com.planetmayo.usvsim.model.behaviour.WaypointTransit;
import com.planetmayo.usvsim.model.behaviour.ReturnToBase;
import com.planetmayo.usvsim.model.behaviour.ExpandingSquareSearch;
import com.planetmayo.usvsim.model.geometry.Polygon;
import com.planetmayo.usvsim.model.geometry.Position;
import com.planetmayo.usvsim.model.mission.Mission;
import com.planetmayo.usvsim.view.ControlPanel;
import com.planetmayo.usvsim.view.MainView;
import com.planetmayo.usvsim.view.MapPanel;
import com.planetmayo.usvsim.view.MissionPlanPanel;
import com.planetmayo.usvsim.view.StatePanel;
import com.planetmayo.usvsim.view.dialogs.ParallelTrackSearchDialog;
import com.planetmayo.usvsim.view.dialogs.ParallelTrackSearchParams;
import com.planetmayo.usvsim.view.dialogs.WaypointTransitDialog;
import com.planetmayo.usvsim.view.dialogs.WaypointTransitParams;
import com.planetmayo.usvsim.view.dialogs.ReturnToBaseDialog;
import com.planetmayo.usvsim.view.dialogs.ReturnToBaseParams;
import com.planetmayo.usvsim.view.dialogs.ExpandingSquareSearchDialog;
import com.planetmayo.usvsim.view.dialogs.ExpandingSquareSearchParams;

import java.util.List;

/**
 * Main controller wiring UI events to mission model operations.
 *
 * Responsibilities:
 * - Handle "Add Behaviour" dropdown selections
 * - Coordinate DrawingController for polygon/waypoint drawing
 * - Wire dialog callbacks to Mission.addBehaviour()
 * - Update MapPanel with generated patterns
 * - Manage mission state (start, pause, stop)
 */
public class MissionController implements MainView.MissionControllerCallback {
    private final Mission mission;
    private final MainView mainView;
    private final MapPanel mapPanel;
    private final MissionPlanPanel missionPlanPanel;
    private final ControlPanel controlPanel;
    private final StatePanel statePanel;
    private final DrawingController drawingController;
    private final SimulationEngine simulationEngine;
    private long simulationStartTimeMs = 0;
    private long finalSimulationTimeMs = 0;  // Frozen time when simulation completes

    public MissionController(Mission mission, MainView mainView) {
        this.mission = mission;
        this.mainView = mainView;
        this.mapPanel = mainView.getMapPanel();
        this.missionPlanPanel = mainView.getMissionPlanPanel();
        this.controlPanel = mainView.getControlPanel();
        this.statePanel = mainView.getStatePanel();
        this.drawingController = new DrawingController(mapPanel);
        this.simulationEngine = new SimulationEngine(mission);

        // Wire UI event handlers
        wireUIHandlers();

        // Show initial platform start position on map after it loads
        initializeStartPositionMarker();
    }

    /**
     * Initialize start position marker after map is fully loaded.
     * Uses polling to check if Leaflet map is ready before showing marker.
     */
    private void initializeStartPositionMarker() {
        // Poll for map readiness (check if window.leafletMap exists)
        javafx.animation.Timeline mapReadyPoller = new javafx.animation.Timeline();
        javafx.animation.KeyFrame checkFrame = new javafx.animation.KeyFrame(
            javafx.util.Duration.millis(200),
            _ -> {
                try {
                    Object mapReady = mapPanel.getWebEngine().executeScript(
                        "typeof window.leafletMap !== 'undefined' && window.leafletMap !== null"
                    );

                    if (mapReady != null && Boolean.parseBoolean(mapReady.toString())) {
                        System.out.println("Map ready - showing start position marker");
                        javafx.application.Platform.runLater(() -> {
                            mapPanel.showStartPosition(mission.getPlatform().getState().getPosition());
                        });
                        // Stop polling once marker is shown
                        mapReadyPoller.stop();
                    } else {
                        System.out.println("Waiting for map to initialize...");
                    }
                } catch (Exception e) {
                    System.err.println("Error checking map readiness: " + e.getMessage());
                }
            }
        );
        mapReadyPoller.getKeyFrames().add(checkFrame);
        mapReadyPoller.setCycleCount(javafx.animation.Animation.INDEFINITE);
        mapReadyPoller.play();
    }

    /**
     * Handle "Add Behaviour" dropdown selection
     */
    public void handleAddBehavior(String behaviorType) {
        if (behaviorType == null || behaviorType.isEmpty()) {
            return;
        }

        switch (behaviorType) {
            case "Parallel Track Search":
                startParallelTrackSearchDialog();
                break;
            case "Expanding Square Search":
                System.out.println("TODO: Expanding square search");
                break;
            case "Waypoint Transit":
                startWaypointTransitDialog();
                break;
            case "Return to Base":
                startReturnToBaseDialog();
                break;
            default:
                System.err.println("Unknown behavior type: " + behaviorType);
        }
    }

    /**
     * Start the Parallel Track Search workflow:
     * 1. Show drawing instructions dialog
     * 2. Prompt user to draw polygon on map
     * 3. Show parameter dialog (orientation, spacing)
     * 4. Create ParallelTrackSearch behaviour
     * 5. Add to mission
     * 6. Update map display
     */
    private void startParallelTrackSearchDialog() {
        System.out.println("Starting Parallel Track Search workflow");

        // Step 1: Start polygon drawing on map (setup callback first)
        drawingController.startDrawingPolygon(polygon -> {
            // This callback is triggered when the polygon is drawn and confirmed
            System.out.println("✓ Polygon received with " + polygon.getVertices().size() + " vertices");

            // Step 2: Show parameter dialog for search pattern
            ParallelTrackSearchDialog dialog = new ParallelTrackSearchDialog();
            dialog.showAndWait().ifPresent(params -> {
                addParallelTrackSearch(polygon, params);
            });
        });

        // Step 0: Show instructions dialog (NON-MODAL so user can click on map)
        com.planetmayo.usvsim.view.dialogs.PolygonDrawingInstructionsDialog instructionsDialog =
            new com.planetmayo.usvsim.view.dialogs.PolygonDrawingInstructionsDialog();
        // CRITICAL: Make dialog non-modal so map remains clickable
        instructionsDialog.initModality(javafx.stage.Modality.NONE);

        // Wire the "Done Drawing" button directly (don't use onCloseRequest which fires on Escape/X too)
        javafx.scene.control.Button doneButton = (javafx.scene.control.Button) instructionsDialog.getDialogPane().lookupButton(javafx.scene.control.ButtonType.OK);
        if (doneButton != null) {
            // CRITICAL: Disable button initially to prevent premature clicks
            doneButton.setDisable(true);
            doneButton.setText("Done Drawing (draw 1+ vertices first)");

            // Setup button click handler
            doneButton.setOnAction(event -> {
                System.out.println("User clicked Done Drawing - finishing polygon");
                drawingController.finishDrawing();
                instructionsDialog.close();
            });

            // Setup polling to enable button when vertices are drawn
            javafx.animation.Timeline enableButtonPoller = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.millis(100), event -> {
                    Object vertexCountObj = mapPanel.getWebEngine().executeScript("window.vertexCount || 0");
                    try {
                        final int vertexCount = vertexCountObj != null ? Integer.parseInt(vertexCountObj.toString()) : 0;
                        if (vertexCount > 0 && doneButton.isDisable()) {
                            javafx.application.Platform.runLater(() -> {
                                doneButton.setDisable(false);
                                doneButton.setText("Done Drawing");
                                System.out.println("Done Drawing button enabled - " + vertexCount + " vertices drawn");
                            });
                        }
                    } catch (Exception e) {
                        // Ignore parse errors
                    }
                })
            );
            enableButtonPoller.setCycleCount(javafx.animation.Animation.INDEFINITE);
            enableButtonPoller.play();

            // Stop polling when dialog closes
            instructionsDialog.setOnCloseRequest(event -> {
                enableButtonPoller.stop();
            });
        }

        instructionsDialog.show();
    }

    /**
     * Add a ParallelTrackSearch behaviour to the mission
     */
    public void addParallelTrackSearch(Polygon searchArea, ParallelTrackSearchParams params) {
        try {
            // Create the behaviour
            ParallelTrackSearch behavior = new ParallelTrackSearch(
                searchArea,
                params.orientation,
                params.spacing,
                params.speed
            );

            // Add to mission
            mission.getMissionPlan().addBehaviour(behavior);
            System.out.println("Added parallel track search: " + params.orientation + "°, " +
                             params.spacing + "m spacing");

            // Update UI
            missionPlanPanel.addBehavior(behavior);
            mapPanel.renderPolygon(searchArea);
            mapPanel.renderTracks(behavior.getWaypoints());

        } catch (IllegalArgumentException e) {
            System.err.println("Failed to create parallel track search: " + e.getMessage());
        }
    }

    /**
     * Start the Waypoint Transit workflow:
     * 1. Start polyline drawing on map
     * 2. User draws polyline by clicking waypoints
     * 3. Re-clicking last point finishes drawing
     * 4. Show parameter dialog with drawn waypoints
     * 5. Create WaypointTransit behaviour
     * 6. Add to mission
     * 7. Update map display
     */
    private void startWaypointTransitDialog() {
        System.out.println("Starting Waypoint Transit workflow");

        // Step 1: Start polyline drawing on map (setup callback first)
        drawingController.startDrawingWaypoints(waypoints -> {
            // This callback is triggered when the polyline is drawn and completed
            System.out.println("✓ Polyline received with " + waypoints.size() + " waypoints");

            // Step 2: Show parameter dialog with the drawn waypoints
            WaypointTransitDialog dialog = new WaypointTransitDialog(waypoints);
            dialog.showAndWait().ifPresent(params -> {
                addWaypointTransit(params);
            });
        });

        // Step 0: Show instructions dialog (NON-MODAL so user can click on map)
        com.planetmayo.usvsim.view.dialogs.PolylineDrawingInstructionsDialog instructionsDialog =
            new com.planetmayo.usvsim.view.dialogs.PolylineDrawingInstructionsDialog();
        // CRITICAL: Make dialog non-modal so map remains clickable
        instructionsDialog.initModality(javafx.stage.Modality.NONE);

        // Wire the "Done Drawing" button directly
        javafx.scene.control.Button doneButton = (javafx.scene.control.Button) instructionsDialog.getDialogPane().lookupButton(javafx.scene.control.ButtonType.OK);
        if (doneButton != null) {
            // CRITICAL: Disable button initially to prevent premature clicks
            doneButton.setDisable(true);
            doneButton.setText("Done Drawing (draw 2+ waypoints first)");

            // Setup button click handler
            doneButton.setOnAction(_ -> {
                System.out.println("User clicked Done Drawing - finishing polyline");
                drawingController.finishDrawing();
                instructionsDialog.close();
            });

            // Setup polling to enable button when waypoints are drawn
            javafx.animation.Timeline enableButtonPoller = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.millis(100), _ -> {
                    Object vertexCountObj = mapPanel.getWebEngine().executeScript("window.vertexCount || 0");
                    try {
                        final int vertexCount = vertexCountObj != null ? Integer.parseInt(vertexCountObj.toString()) : 0;
                        if (vertexCount >= 2 && doneButton.isDisable()) {
                            javafx.application.Platform.runLater(() -> {
                                doneButton.setDisable(false);
                                doneButton.setText("Done Drawing");
                                System.out.println("Done Drawing button enabled - " + vertexCount + " waypoints drawn");
                            });
                        }
                    } catch (Exception e) {
                        // Ignore parse errors
                    }
                })
            );
            enableButtonPoller.setCycleCount(javafx.animation.Animation.INDEFINITE);
            enableButtonPoller.play();

            // Stop polling when dialog closes
            instructionsDialog.setOnCloseRequest(_ -> {
                enableButtonPoller.stop();
            });
        }

        instructionsDialog.show();
    }

    /**
     * Add a WaypointTransit behaviour to the mission
     */
    public void addWaypointTransit(WaypointTransitParams params) {
        try {
            // Create the behaviour
            WaypointTransit behavior = new WaypointTransit(params.waypoints, params.speed);

            // Add to mission
            mission.getMissionPlan().addBehaviour(behavior);
            System.out.println("Added waypoint transit: " + params.waypoints.size() +
                             " waypoints at " + params.speed + " knots");

            // Update UI
            missionPlanPanel.addBehavior(behavior);
            mapPanel.renderTracks(behavior.getWaypoints(), true);  // Show start marker for waypoint transit

        } catch (IllegalArgumentException e) {
            System.err.println("Failed to create waypoint transit: " + e.getMessage());
        }
    }

    /**
     * Start the Return to Base workflow:
     * 1. Show return-to-base configuration dialog
     * 2. Create ReturnToBase behaviour
     * 3. Add to mission
     * 4. Update map display
     */
    private void startReturnToBaseDialog() {
        System.out.println("Starting Return to Base workflow");

        // Get current platform position as default
        Position currentPos = mission.getPlatform().getState().getPosition();

        ReturnToBaseDialog dialog = new ReturnToBaseDialog(currentPos);
        dialog.showAndWait().ifPresent(params -> {
            addReturnToBase(params);
        });
    }

    /**
     * Add a ReturnToBase behaviour to the mission
     */
    public void addReturnToBase(ReturnToBaseParams params) {
        try {
            // Determine start position for rendering BEFORE adding new behaviour
            Position startPos = getLastWaypointPosition();

            // Create the behaviour
            ReturnToBase behavior = new ReturnToBase(params.baseLocation, params.speed);

            // Add to mission
            mission.getMissionPlan().addBehaviour(behavior);
            System.out.println("Added return to base: " + String.format("(%.3f°N, %.3f°E)",
                             params.baseLocation.getLatitude(),
                             params.baseLocation.getLongitude()) +
                             " at " + params.speed + " knots");

            // Update UI
            missionPlanPanel.addBehavior(behavior);

            // Render path from start to base
            renderReturnToBasePath(startPos, params.baseLocation, params.speed);

        } catch (IllegalArgumentException e) {
            System.err.println("Failed to create return to base: " + e.getMessage());
        }
    }

    /**
     * Get the last waypoint position from the previous behaviour, or current platform position
     */
    private Position getLastWaypointPosition() {
        var behaviours = mission.getMissionPlan().getBehaviours();
        System.out.println("Getting last waypoint position - " + behaviours.size() + " behaviours total");

        if (behaviours.size() >= 1) {
            // Get last waypoint from most recent behaviour
            var lastBehaviour = behaviours.get(behaviours.size() - 1);
            var waypoints = lastBehaviour.getWaypoints();
            System.out.println("  Last behaviour: " + lastBehaviour.getName() +
                             " with " + waypoints.size() + " waypoints");
            if (!waypoints.isEmpty()) {
                Position pos = waypoints.get(waypoints.size() - 1).getPosition();
                System.out.println("  Using last waypoint from last behaviour");
                return pos;
            }
        }
        // Default to current platform position
        Position pos = mission.getPlatform().getState().getPosition();
        System.out.println("  Using current platform position (no behaviours yet)");
        return pos;
    }

    /**
     * Render return to base path from start position to base location
     */
    private void renderReturnToBasePath(Position startPos, Position basePos, double speed) {
        System.out.println("Rendering return to base path:");
        System.out.println("  Start: " + String.format("%.4f°N, %.4f°W",
            startPos.getLatitude(), Math.abs(startPos.getLongitude())));
        System.out.println("  Base:  " + String.format("%.4f°N, %.4f°W",
            basePos.getLatitude(), Math.abs(basePos.getLongitude())));
        System.out.println("  Speed: " + speed + " knots");

        // Create waypoints for rendering the path
        var pathWaypoints = new java.util.ArrayList<com.planetmayo.usvsim.model.geometry.Waypoint>();
        pathWaypoints.add(new com.planetmayo.usvsim.model.geometry.Waypoint(
            startPos, speed, 50, com.planetmayo.usvsim.model.geometry.WaypointType.TRANSIT));
        pathWaypoints.add(new com.planetmayo.usvsim.model.geometry.Waypoint(
            basePos, speed, 50, com.planetmayo.usvsim.model.geometry.WaypointType.BASE));

        System.out.println("  Created " + pathWaypoints.size() + " waypoints for rendering");

        // Render with start marker
        mapPanel.renderTracks(pathWaypoints, true);
        System.out.println("  Called renderTracks with start marker");
    }

    /**
     * Finish the current drawing operation (for keyboard/UI button)
     */
    public void finishDrawing() {
        drawingController.finishDrawing();
    }

    /**
     * Cancel the current drawing operation
     */
    public void cancelDrawing() {
        drawingController.cancelDrawing();
    }

    /**
     * Start the simulation
     */
    public void startSimulation() {
        if (mission.getMissionPlan().getBehaviours().isEmpty()) {
            System.err.println("Cannot start: mission plan is empty");
            return;
        }
        simulationStartTimeMs = System.currentTimeMillis();
        finalSimulationTimeMs = 0;  // Reset frozen time
        simulationEngine.start();
    }

    /**
     * Pause the simulation
     */
    public void pauseSimulation() {
        simulationEngine.pause();
    }

    /**
     * Resume after pause
     */
    public void resumeSimulation() {
        simulationEngine.resume();
    }

    /**
     * Stop the simulation and reset
     */
    public void stopSimulation() {
        simulationEngine.stop();
        simulationStartTimeMs = 0;
        finalSimulationTimeMs = 0;  // Reset frozen time
        statePanel.reset();
    }

    /**
     * Set time acceleration factor
     */
    public void setTimeAcceleration(double factor) {
        simulationEngine.setTimeAcceleration(factor);
    }

    /**
     * Get the mission model
     */
    public Mission getMission() {
        return mission;
    }

    /**
     * Get the drawing controller
     */
    public DrawingController getDrawingController() {
        return drawingController;
    }

    /**
     * Wire UI event handlers to controller methods
     */
    private void wireUIHandlers() {
        // Wire control panel handlers (T053, T054)
        controlPanel.setOnStart(this::startSimulation);
        controlPanel.setOnPause(this::pauseSimulation);
        controlPanel.setOnStop(this::stopSimulation);
        controlPanel.setOnSpeedChange(this::setTimeAcceleration);

        // Wire state panel updates from simulation engine (T055)
        // Post updates to JavaFX thread to avoid cross-thread UI access
        simulationEngine.setOnStateChanged(() -> {
            javafx.application.Platform.runLater(() -> {
                var state = mission.getPlatform().getState();
                statePanel.updateState(state);

                // Update simulation time (elapsed since start)
                if (finalSimulationTimeMs > 0) {
                    // Simulation complete - show frozen final time
                    statePanel.setTimestamp(finalSimulationTimeMs);
                    controlPanel.updateSimulationTime(finalSimulationTimeMs);
                } else if (simulationStartTimeMs > 0) {
                    // Simulation running - show live elapsed time
                    long elapsedMs = System.currentTimeMillis() - simulationStartTimeMs;
                    statePanel.setTimestamp(elapsedMs);
                    controlPanel.updateSimulationTime(elapsedMs);

                    // Debug: Log every 100th update
                    if (elapsedMs % 10000 < 200) {
                        System.out.println("Simulation time: " + (elapsedMs / 1000) + "s");
                    }
                }

                // Update map with current position and heading
                mapPanel.updatePlatformPosition(state.getPosition(), state.getHeading());

                // Add to track history
                mapPanel.addTrackPoint(state.getPosition());

                // Refresh mission plan to show updated behaviour status
                missionPlanPanel.refresh();
            });
        });

        // Wire simulation completion callback
        simulationEngine.setOnSimulationComplete(() -> {
            javafx.application.Platform.runLater(() -> {
                // Capture final elapsed time
                if (simulationStartTimeMs > 0) {
                    finalSimulationTimeMs = System.currentTimeMillis() - simulationStartTimeMs;
                    statePanel.setTimestamp(finalSimulationTimeMs);
                    System.out.println("Mission complete - final time: " +
                        String.format("%02d:%02d:%02d",
                            finalSimulationTimeMs / 3600000,
                            (finalSimulationTimeMs % 3600000) / 60000,
                            (finalSimulationTimeMs % 60000) / 1000));
                }
            });
        });

        // Wire double-click handler for editing behaviours in mission plan
        missionPlanPanel.setOnBehaviourDoubleClick(behaviour -> {
            System.out.println("Double-clicked on behaviour: " + behaviour.getName());
            // TODO: Open appropriate edit dialog based on behaviour type
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.setTitle("Edit Behaviour");
            alert.setHeaderText(behaviour.getName());
            alert.setContentText("Edit functionality coming soon.\n\nBehaviour: " + behaviour.getDescription());
            alert.show();
        });

        // Register this controller as the callback for MainView (T068)
        mainView.setMissionControllerCallback(this);
    }

    // ========== MainView.MissionControllerCallback Implementation ==========

    @Override
    public void onParallelTrackSearchRequested() {
        startParallelTrackSearchDialog();
    }

    @Override
    public void onExpandingSquareSearchRequested() {
        startExpandingSquareSearchDialog();
    }

    @Override
    public void onWaypointTransitRequested() {
        startWaypointTransitDialog();
    }

    @Override
    public void onReturnToBaseRequested() {
        startReturnToBaseDialog();
    }

    @Override
    public void onPlatformConfigRequested() {
        // TODO: Implement platform configuration dialog
        System.out.println("Platform configuration requested");
    }

    /**
     * Start the Expanding Square Search workflow
     */
    private void startExpandingSquareSearchDialog() {
        System.out.println("Starting Expanding Square Search workflow");

        // Step 1: Start polygon drawing on map (setup callback first)
        drawingController.startDrawingPolygon(polygon -> {
            System.out.println("✓ Polygon received with " + polygon.getVertices().size() + " vertices");

            // Step 2: Show parameter dialog for search pattern
            ExpandingSquareSearchDialog dialog = new ExpandingSquareSearchDialog();
            dialog.showAndWait().ifPresent(params -> {
                addExpandingSquareSearch(polygon, params);
            });
        });

        // Step 0: Show instructions dialog (NON-MODAL so user can click on map)
        com.planetmayo.usvsim.view.dialogs.PolygonDrawingInstructionsDialog instructionsDialog =
            new com.planetmayo.usvsim.view.dialogs.PolygonDrawingInstructionsDialog();
        // CRITICAL: Make dialog non-modal so map remains clickable
        instructionsDialog.initModality(javafx.stage.Modality.NONE);

        // Wire the "Done Drawing" button directly (don't use onCloseRequest which fires on Escape/X too)
        javafx.scene.control.Button doneButton = (javafx.scene.control.Button) instructionsDialog.getDialogPane().lookupButton(javafx.scene.control.ButtonType.OK);
        if (doneButton != null) {
            // CRITICAL: Disable button initially to prevent premature clicks
            doneButton.setDisable(true);
            doneButton.setText("Done Drawing (draw 1+ vertices first)");

            // Setup button click handler
            doneButton.setOnAction(event -> {
                System.out.println("User clicked Done Drawing - finishing polygon");
                drawingController.finishDrawing();
                instructionsDialog.close();
            });

            // Setup polling to enable button when vertices are drawn
            javafx.animation.Timeline enableButtonPoller = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.millis(100), event -> {
                    Object vertexCountObj = mapPanel.getWebEngine().executeScript("window.vertexCount || 0");
                    try {
                        final int vertexCount = vertexCountObj != null ? Integer.parseInt(vertexCountObj.toString()) : 0;
                        if (vertexCount > 0 && doneButton.isDisable()) {
                            javafx.application.Platform.runLater(() -> {
                                doneButton.setDisable(false);
                                doneButton.setText("Done Drawing");
                                System.out.println("Done Drawing button enabled - " + vertexCount + " vertices drawn");
                            });
                        }
                    } catch (Exception e) {
                        // Ignore parse errors
                    }
                })
            );
            enableButtonPoller.setCycleCount(javafx.animation.Animation.INDEFINITE);
            enableButtonPoller.play();

            // Stop polling when dialog closes
            instructionsDialog.setOnCloseRequest(event -> {
                enableButtonPoller.stop();
            });
        }

        instructionsDialog.show();
    }

    /**
     * Add an ExpandingSquareSearch behaviour to the mission
     */
    public void addExpandingSquareSearch(Polygon searchArea, ExpandingSquareSearchParams params) {
        try {
            // Create the behaviour
            ExpandingSquareSearch behavior = new ExpandingSquareSearch(
                searchArea,
                params.initialDirection,
                params.legIncrement,
                params.speed
            );

            // Add to mission
            mission.getMissionPlan().addBehaviour(behavior);
            System.out.println("Added expanding square search: " + params.initialDirection + "°, " +
                             params.legIncrement + "m increment");

            // Update UI
            missionPlanPanel.addBehavior(behavior);
            mapPanel.renderPolygon(searchArea);
            mapPanel.renderTracks(behavior.getWaypoints());

        } catch (IllegalArgumentException e) {
            System.err.println("Failed to create expanding square search: " + e.getMessage());
        }
    }
}
