package com.planetmayo.usvsim.controller;

import com.planetmayo.usvsim.model.behaviour.Behaviour;
import com.planetmayo.usvsim.model.behaviour.BehaviourState;
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
import com.planetmayo.usvsim.view.dialogs.ParallelTrackSearchParams;
import com.planetmayo.usvsim.view.dialogs.WaypointTransitParams;
import com.planetmayo.usvsim.view.dialogs.ReturnToBaseParams;
import com.planetmayo.usvsim.view.dialogs.ExpandingSquareSearchParams;
import com.planetmayo.usvsim.util.MissionSerializer;

import javafx.stage.FileChooser;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
    private long finalSimulationTimeMs = 0;  // Frozen simulation time when complete
    private final Map<Behaviour, BehaviourState> previousBehaviourStates = new HashMap<>();

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
            event -> {
                try {
                    Object mapReady = mapPanel.getWebEngine().executeScript(
                        "typeof window.leafletMap !== 'undefined' && window.leafletMap !== null"
                    );

                    if (mapReady != null && Boolean.parseBoolean(mapReady.toString())) {
                        javafx.application.Platform.runLater(() -> {
                            Position startPos = mission.getPlatform().getState().getPosition();
                            mapPanel.showStartPosition(startPos);

                            // Zoom out 2 levels and center on start point
                            String zoomCenterScript = String.format("""
                                var currentZoom = window.leafletMap.getZoom();
                                window.leafletMap.setView([%f, %f], currentZoom - 2);
                                console.log('Map centered on start position and zoomed out 2 levels');
                                """, startPos.getLatitude(), startPos.getLongitude());
                            mapPanel.getWebEngine().executeScript(zoomCenterScript);
                        });
                        // Stop polling once marker is shown
                        mapReadyPoller.stop();
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
                startExpandingSquareSearchDialog();
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
        // Step 1: Start polygon drawing on map (setup callback first)
        drawingController.startDrawingPolygon(polygon -> {
            // This callback is triggered when the polygon is drawn and confirmed

            // Hide instructions panel, show parameter panel
            statePanel.hideDialog();

            // Step 2: Show parameter panel for search pattern
            com.planetmayo.usvsim.view.dialogs.ParallelTrackSearchPanel panel =
                new com.planetmayo.usvsim.view.dialogs.ParallelTrackSearchPanel();

            panel.setOnComplete(params -> {
                statePanel.hideDialog();
                addParallelTrackSearch(polygon, params);
            });

            panel.setOnCancel(() -> {
                statePanel.hideDialog();
                drawingController.cancelDrawing();
            });

            statePanel.showDialog(panel);
        });

        // Step 0: Show instructions panel on StatePanel
        com.planetmayo.usvsim.view.dialogs.PolygonDrawingInstructionsPanel instructionsPanel =
            new com.planetmayo.usvsim.view.dialogs.PolygonDrawingInstructionsPanel();

        // Wire the "Done Drawing" button
        javafx.scene.control.Button doneButton = instructionsPanel.getDoneButton();

        instructionsPanel.setOnDoneDrawing(() -> {
            System.out.println("User clicked Done Drawing - finishing polygon");
            drawingController.finishDrawing();
            // Note: callback will hide instructions and show config panel
        });

        instructionsPanel.setOnCancel(() -> {
            statePanel.hideDialog();
            drawingController.cancelDrawing();
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

        statePanel.showDialog(instructionsPanel);
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

            // Update UI
            missionPlanPanel.addBehavior(behavior);
            mapPanel.renderPolygon(searchArea);
            mapPanel.renderTracks(behavior.getWaypoints());

            // Update Start button state
            updateStartButtonState();

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
        // Step 1: Start polyline drawing on map (setup callback first)
        drawingController.startDrawingWaypoints(waypoints -> {
            // This callback is triggered when the polyline is drawn and completed

            // Hide instructions panel, show parameter panel
            statePanel.hideDialog();

            // Step 2: Show parameter panel with the drawn waypoints
            com.planetmayo.usvsim.view.dialogs.WaypointTransitPanel panel =
                new com.planetmayo.usvsim.view.dialogs.WaypointTransitPanel(waypoints);

            panel.setOnComplete(params -> {
                statePanel.hideDialog();
                addWaypointTransit(params);
            });

            panel.setOnCancel(() -> {
                statePanel.hideDialog();
                drawingController.cancelDrawing();
            });

            statePanel.showDialog(panel);
        });

        // Step 0: Show instructions panel on StatePanel
        com.planetmayo.usvsim.view.dialogs.PolylineDrawingInstructionsPanel instructionsPanel =
            new com.planetmayo.usvsim.view.dialogs.PolylineDrawingInstructionsPanel();

        // Wire the "Done Drawing" button
        javafx.scene.control.Button doneButton = instructionsPanel.getDoneButton();

        instructionsPanel.setOnDoneDrawing(() -> {
            drawingController.finishDrawing();
            // Note: callback will hide instructions and show config panel
        });

        instructionsPanel.setOnCancel(() -> {
            statePanel.hideDialog();
            drawingController.cancelDrawing();
        });

        // Setup polling to enable button when waypoints are drawn
        javafx.animation.Timeline enableButtonPoller = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(javafx.util.Duration.millis(100), event -> {
                Object vertexCountObj = mapPanel.getWebEngine().executeScript("window.vertexCount || 0");
                try {
                    final int vertexCount = vertexCountObj != null ? Integer.parseInt(vertexCountObj.toString()) : 0;
                    if (vertexCount >= 2 && doneButton.isDisable()) {
                        javafx.application.Platform.runLater(() -> {
                            doneButton.setDisable(false);
                            doneButton.setText("Done Drawing");
                        });
                    }
                } catch (Exception e) {
                    // Ignore parse errors
                }
            })
        );
        enableButtonPoller.setCycleCount(javafx.animation.Animation.INDEFINITE);
        enableButtonPoller.play();

        statePanel.showDialog(instructionsPanel);
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

            // Update UI
            missionPlanPanel.addBehavior(behavior);
            mapPanel.renderTracks(behavior.getWaypoints(), true);  // Show start marker for waypoint transit

            // Update Start button state
            updateStartButtonState();

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
        // Get current platform position as default
        Position currentPos = mission.getPlatform().getState().getPosition();

        com.planetmayo.usvsim.view.dialogs.ReturnToBasePanel panel =
            new com.planetmayo.usvsim.view.dialogs.ReturnToBasePanel(currentPos);

        panel.setOnComplete(params -> {
            statePanel.hideDialog();
            addReturnToBase(params);
        });

        panel.setOnCancel(() -> {
            statePanel.hideDialog();
        });

        statePanel.showDialog(panel);
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

            // Update UI
            missionPlanPanel.addBehavior(behavior);

            // Render path from start to base
            renderReturnToBasePath(startPos, params.baseLocation, params.speed);

            // Update Start button state
            updateStartButtonState();

        } catch (IllegalArgumentException e) {
            System.err.println("Failed to create return to base: " + e.getMessage());
        }
    }

    /**
     * Get the last waypoint position from the previous behaviour, or current platform position
     */
    private Position getLastWaypointPosition() {
        var behaviours = mission.getMissionPlan().getBehaviours();

        if (behaviours.size() >= 1) {
            // Get last waypoint from most recent behaviour
            var lastBehaviour = behaviours.get(behaviours.size() - 1);
            var waypoints = lastBehaviour.getWaypoints();
            if (!waypoints.isEmpty()) {
                return waypoints.get(waypoints.size() - 1).getPosition();
            }
        }
        // Default to current platform position
        return mission.getPlatform().getState().getPosition();
    }

    /**
     * Render return to base path from start position to base location
     */
    private void renderReturnToBasePath(Position startPos, Position basePos, double speed) {
        // Create waypoints for rendering the path
        var pathWaypoints = new java.util.ArrayList<com.planetmayo.usvsim.model.geometry.Waypoint>();
        pathWaypoints.add(new com.planetmayo.usvsim.model.geometry.Waypoint(
            startPos, speed, 50, com.planetmayo.usvsim.model.geometry.WaypointType.TRANSIT));
        pathWaypoints.add(new com.planetmayo.usvsim.model.geometry.Waypoint(
            basePos, speed, 50, com.planetmayo.usvsim.model.geometry.WaypointType.BASE));

        // Render with start marker
        mapPanel.renderTracks(pathWaypoints, true);
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
     * Stop the simulation and reset everything to initial state
     */
    public void stopSimulation() {
        simulationEngine.stop();
        finalSimulationTimeMs = 0;  // Reset frozen time

        // Clear track history (done via clearOverlays)
        mapPanel.clearOverlays();

        // Reset platform to initial state
        Position initialPosition = Position.of(50.5712, -2.4525);  // Portland Harbour start
        mission.getPlatform().setState(new com.planetmayo.usvsim.model.platform.PlatformState(
            "USV-1",
            initialPosition,
            0.0,  // heading
            0.0,  // speed
            0.0,  // depth
            java.time.Instant.now()
        ));

        // Reset all behaviours to PENDING state
        for (com.planetmayo.usvsim.model.behaviour.Behaviour ignored : mission.getMissionPlan().getBehaviours()) {
            // Note: Behaviours don't have a reset method, they would need to be recreated
            // For now, we can clear the mission plan and let user recreate behaviours
        }

        // Update UI
        statePanel.reset();
        controlPanel.reset();
        mapPanel.updatePlatformPosition(initialPosition, 0.0);
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
        controlPanel.setOnResume(this::resumeSimulation);
        controlPanel.setOnStop(this::stopSimulation);
        controlPanel.setOnSpeedChange(this::setTimeAcceleration);

        // Initialize Start button to disabled (no behaviours yet)
        updateStartButtonState();

        // Wire state panel updates from simulation engine (T055)
        // Post updates to JavaFX thread to avoid cross-thread UI access
        simulationEngine.setOnStateChanged(() -> {
            javafx.application.Platform.runLater(() -> {
                var state = mission.getPlatform().getState();
                statePanel.updateState(state);

                // Update simulation time (accounts for time acceleration)
                if (finalSimulationTimeMs > 0) {
                    // Simulation complete - show frozen final time
                    statePanel.setTimestamp(finalSimulationTimeMs);
                    controlPanel.updateSimulationTime(finalSimulationTimeMs);
                } else {
                    // Simulation running - show live simulation time
                    long simTimeMs = simulationEngine.getSimulationTimeMs();
                    statePanel.setTimestamp(simTimeMs);
                    controlPanel.updateSimulationTime(simTimeMs);
                }

                // Update map with current position and heading
                mapPanel.updatePlatformPosition(state.getPosition(), state.getHeading());

                // Add to track history
                mapPanel.addTrackPoint(state.getPosition());

                // Refresh mission plan only if any behaviour state has changed
                if (hasBehaviourStateChanged()) {
                    missionPlanPanel.refresh();
                }
            });
        });

        // Wire simulation completion callback
        simulationEngine.setOnSimulationComplete(() -> {
            javafx.application.Platform.runLater(() -> {
                // Capture final simulation time
                finalSimulationTimeMs = simulationEngine.getSimulationTimeMs();
                statePanel.setTimestamp(finalSimulationTimeMs);
                controlPanel.updateSimulationTime(finalSimulationTimeMs);

                // Disable pause button when simulation completes
                controlPanel.setSimulationComplete();
            });
        });

        // Wire double-click handler for editing behaviours in mission plan
        missionPlanPanel.setOnBehaviourDoubleClick(this::handleEditBehaviour);

        // Wire delete button handler for removing behaviours
        missionPlanPanel.setOnBehaviourDelete(this::handleDeleteBehaviour);

        // Wire reorder handler for moving behaviours up/down
        missionPlanPanel.setOnBehaviourReorder(this::handleReorderBehaviour);

        // Wire save/load mission handlers (T116, T117)
        missionPlanPanel.setOnSaveMission(this::handleSaveMission);
        missionPlanPanel.setOnLoadMission(this::handleLoadMission);

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
        // Get current platform capabilities
        var currentCapabilities = mission.getPlatform().getCapabilities();

        // Show configuration dialog
        var dialog = new com.planetmayo.usvsim.view.dialogs.PlatformConfigDialog(currentCapabilities);
        dialog.showAndWait();
        // Note: New capabilities will be loaded on next mission creation
        // Current running mission is not affected
    }

    /**
     * Start the Expanding Square Search workflow
     */
    private void startExpandingSquareSearchDialog() {
        // Step 1: Start polygon drawing on map (setup callback first)
        drawingController.startDrawingPolygon(polygon -> {

            // Hide instructions panel, show parameter panel
            statePanel.hideDialog();

            // Step 2: Show parameter panel for search pattern
            com.planetmayo.usvsim.view.dialogs.ExpandingSquareSearchPanel panel =
                new com.planetmayo.usvsim.view.dialogs.ExpandingSquareSearchPanel();

            panel.setOnComplete(params -> {
                statePanel.hideDialog();
                addExpandingSquareSearch(polygon, params);
            });

            panel.setOnCancel(() -> {
                statePanel.hideDialog();
                drawingController.cancelDrawing();
            });

            statePanel.showDialog(panel);
        });

        // Step 0: Show instructions panel on StatePanel
        com.planetmayo.usvsim.view.dialogs.PolygonDrawingInstructionsPanel instructionsPanel =
            new com.planetmayo.usvsim.view.dialogs.PolygonDrawingInstructionsPanel();

        // Wire the "Done Drawing" button
        javafx.scene.control.Button doneButton = instructionsPanel.getDoneButton();

        instructionsPanel.setOnDoneDrawing(() -> {
            drawingController.finishDrawing();
            // Note: callback will hide instructions and show config panel
        });

        instructionsPanel.setOnCancel(() -> {
            statePanel.hideDialog();
            drawingController.cancelDrawing();
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
                        });
                    }
                } catch (Exception e) {
                    // Ignore parse errors
                }
            })
        );
        enableButtonPoller.setCycleCount(javafx.animation.Animation.INDEFINITE);
        enableButtonPoller.play();

        statePanel.showDialog(instructionsPanel);
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

            // Update UI
            missionPlanPanel.addBehavior(behavior);
            mapPanel.renderPolygon(searchArea);
            mapPanel.renderTracks(behavior.getWaypoints());

            // Update Start button state
            updateStartButtonState();

        } catch (IllegalArgumentException e) {
            System.err.println("Failed to create expanding square search: " + e.getMessage());
        }
    }

    /**
     * Handle deletion of a behaviour from the mission plan.
     * Removes from mission model, UI, clears and re-renders map.
     */
    private void handleDeleteBehaviour(Behaviour behaviour) {
        // Get index before removal
        int index = mission.getMissionPlan().getBehaviours().indexOf(behaviour);

        // Remove from mission model
        mission.getMissionPlan().removeBehaviour(index);

        // Remove from UI
        missionPlanPanel.removeBehavior(behaviour);

        // Clear map and re-render remaining behaviours
        mapPanel.clearOverlays();
        rerenderAllBehaviours();

        // Update Start button state
        updateStartButtonState();
    }

    /**
     * Handle reordering of behaviours in the mission plan.
     * Synchronizes the mission model with the UI list order.
     */
    private void handleReorderBehaviour(int fromIndex, int toIndex) {
        // Update mission model to match UI order
        mission.getMissionPlan().reorderBehaviour(fromIndex, toIndex);

        // Re-render map to show updated order (if needed for visual feedback)
        mapPanel.clearOverlays();
        rerenderAllBehaviours();
    }

    /**
     * Handle editing of a behaviour from the mission plan.
     * Opens the appropriate dialog based on behaviour type and replaces behaviour if user saves.
     */
    private void handleEditBehaviour(Behaviour behaviour) {
        // Get the index from UI list (more reliable than mission plan since UI may have stale references)
        int index = missionPlanPanel.getBehaviors().indexOf(behaviour);
        if (index < 0) {
            System.err.println("Behaviour not found in UI list: " + behaviour);
            // Try mission plan as fallback
            index = mission.getMissionPlan().getBehaviours().indexOf(behaviour);
            if (index < 0) {
                System.err.println("Behaviour not found in mission plan either - cannot edit");
                return;
            }
        }

        // Open appropriate dialog based on behaviour type
        if (behaviour instanceof ParallelTrackSearch) {
            editParallelTrackSearch((ParallelTrackSearch) behaviour, index);
        } else if (behaviour instanceof WaypointTransit) {
            editWaypointTransit((WaypointTransit) behaviour, index);
        } else if (behaviour instanceof ReturnToBase) {
            editReturnToBase((ReturnToBase) behaviour, index);
        } else if (behaviour instanceof ExpandingSquareSearch) {
            editExpandingSquareSearch((ExpandingSquareSearch) behaviour, index);
        } else {
            System.err.println("Unknown behaviour type: " + behaviour.getClass().getName());
        }
    }

    private void editParallelTrackSearch(ParallelTrackSearch behaviour, int index) {
        com.planetmayo.usvsim.view.dialogs.ParallelTrackSearchPanel panel =
            new com.planetmayo.usvsim.view.dialogs.ParallelTrackSearchPanel();

        // Populate panel with current values
        panel.setValues(
            behaviour.getTrackOrientation(),
            behaviour.getTrackSpacing(),
            behaviour.getPlatformSpeed()
        );

        panel.setOnComplete(params -> {
            statePanel.hideDialog();

            // Create new behaviour with updated params
            ParallelTrackSearch newBehaviour = new ParallelTrackSearch(
                behaviour.getSearchArea(),
                params.orientation,
                params.spacing,
                params.speed
            );

            // Replace at same index (instead of remove+add to maintain references)
            mission.getMissionPlan().setBehaviour(index, newBehaviour);
            missionPlanPanel.getBehaviors().set(index, newBehaviour);

            // Refresh mission plan UI
            missionPlanPanel.refresh();

            // CRITICAL: Update selection to new behavior to prevent stale reference
            javafx.application.Platform.runLater(() -> {
                missionPlanPanel.getBehaviorListView().getSelectionModel().clearAndSelect(index);
            });

            // Clear old overlays and re-render all behaviours
            mapPanel.clearOverlays();
            rerenderAllBehaviours();

            // Update Start button state
            updateStartButtonState();
        });

        panel.setOnCancel(() -> {
            statePanel.hideDialog();
        });

        statePanel.showDialog(panel);
    }

    private void editWaypointTransit(WaypointTransit behaviour, int index) {
        // For waypoint transit editing, show info that user needs to delete and recreate
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Edit Waypoint Transit");
        alert.setHeaderText("Waypoint Transit Editing");
        alert.setContentText("To edit waypoint transit, please:\n" +
            "1. Delete this behaviour\n" +
            "2. Add new Waypoint Transit with updated route\n\n" +
            "Current: " + behaviour.getDescription());
        alert.showAndWait();
    }

    private void editReturnToBase(ReturnToBase behaviour, int index) {
        com.planetmayo.usvsim.view.dialogs.ReturnToBasePanel panel =
            new com.planetmayo.usvsim.view.dialogs.ReturnToBasePanel(mission.getPlatform().getState().getPosition());

        // Populate panel with current values
        panel.setValues(
            behaviour.getBaseLocation(),
            behaviour.getPlatformSpeed()
        );

        panel.setOnComplete(params -> {
            statePanel.hideDialog();

            // Create new behaviour with updated params
            ReturnToBase newBehaviour = new ReturnToBase(params.baseLocation, params.speed);

            // Replace at same index (instead of remove+add to maintain references)
            mission.getMissionPlan().setBehaviour(index, newBehaviour);
            missionPlanPanel.getBehaviors().set(index, newBehaviour);

            // Refresh mission plan UI
            missionPlanPanel.refresh();

            // CRITICAL: Update selection to new behavior to prevent stale reference
            javafx.application.Platform.runLater(() -> {
                missionPlanPanel.getBehaviorListView().getSelectionModel().clearAndSelect(index);
            });

            // Clear old overlays and re-render all behaviours
            mapPanel.clearOverlays();
            rerenderAllBehaviours();

            // Update Start button state
            updateStartButtonState();
        });

        panel.setOnCancel(() -> {
            statePanel.hideDialog();
        });

        statePanel.showDialog(panel);
    }

    private void editExpandingSquareSearch(ExpandingSquareSearch behaviour, int index) {
        com.planetmayo.usvsim.view.dialogs.ExpandingSquareSearchPanel panel =
            new com.planetmayo.usvsim.view.dialogs.ExpandingSquareSearchPanel();

        // Populate panel with current values
        panel.setValues(
            behaviour.getInitialDirection(),
            behaviour.getLegIncrement(),
            behaviour.getPlatformSpeed()
        );

        panel.setOnComplete(params -> {
            statePanel.hideDialog();

            // Create new behaviour with updated params
            ExpandingSquareSearch newBehaviour = new ExpandingSquareSearch(
                behaviour.getSearchArea(),
                params.initialDirection,
                params.legIncrement,
                params.speed
            );

            // Replace at same index (instead of remove+add to maintain references)
            mission.getMissionPlan().setBehaviour(index, newBehaviour);
            missionPlanPanel.getBehaviors().set(index, newBehaviour);

            // Refresh mission plan UI
            missionPlanPanel.refresh();

            // CRITICAL: Update selection to new behavior to prevent stale reference
            javafx.application.Platform.runLater(() -> {
                missionPlanPanel.getBehaviorListView().getSelectionModel().clearAndSelect(index);
            });

            // Clear old overlays and re-render all behaviours
            mapPanel.clearOverlays();
            rerenderAllBehaviours();

            // Update Start button state
            updateStartButtonState();
        });

        panel.setOnCancel(() -> {
            statePanel.hideDialog();
        });

        statePanel.showDialog(panel);
    }

    /**
     * Re-render all behaviours on the map.
     * Used after editing a behaviour to show updated pattern.
     */
    private void rerenderAllBehaviours() {
        for (Behaviour behaviour : mission.getMissionPlan().getBehaviours()) {
            if (behaviour instanceof ParallelTrackSearch pts) {
                mapPanel.renderPolygon(pts.getSearchArea());
                mapPanel.renderTracks(pts.getWaypoints());
            } else if (behaviour instanceof ExpandingSquareSearch ess) {
                mapPanel.renderPolygon(ess.getSearchArea());
                mapPanel.renderTracks(ess.getWaypoints());
            } else if (behaviour instanceof WaypointTransit wt) {
                mapPanel.renderTracks(wt.getWaypoints(), true);
            } else if (behaviour instanceof ReturnToBase rtb) {
                // Find start position for RTB (last waypoint of previous behaviour)
                int rtbIndex = mission.getMissionPlan().getBehaviours().indexOf(rtb);
                Position startPos = getLastWaypointPosition(rtbIndex);
                renderReturnToBasePath(startPos, rtb.getBaseLocation(), rtb.getPlatformSpeed());
            }
        }
    }

    /**
     * Get last waypoint position before a given behaviour index.
     */
    private Position getLastWaypointPosition(int beforeIndex) {
        var behaviours = mission.getMissionPlan().getBehaviours();
        for (int i = beforeIndex - 1; i >= 0; i--) {
            Behaviour prev = behaviours.get(i);
            var waypoints = prev.getWaypoints();
            if (!waypoints.isEmpty()) {
                return waypoints.get(waypoints.size() - 1).getPosition();
            }
        }
        // No previous waypoints - return platform start position
        return mission.getPlatform().getState().getPosition();
    }

    /**
     * Update Start button enabled state based on mission contents.
     * Start button should only be enabled when mission contains 1+ behaviours.
     */
    private void updateStartButtonState() {
        boolean hasBehaviours = !mission.getMissionPlan().getBehaviours().isEmpty();
        mainView.updateStartButtonState(hasBehaviours);
    }

    /**
     * Check if any behaviour state has changed since last check.
     * Updates the tracked state and returns true if any change detected.
     */
    private boolean hasBehaviourStateChanged() {
        boolean changed = false;
        for (Behaviour behaviour : mission.getMissionPlan().getBehaviours()) {
            BehaviourState currentState = behaviour.getState();
            BehaviourState previousState = previousBehaviourStates.get(behaviour);

            if (previousState != currentState) {
                previousBehaviourStates.put(behaviour, currentState);
                changed = true;
            }
        }
        return changed;
    }

    // ========== Mission Persistence (T116, T117) ==========

    /**
     * Handle Save Mission button click.
     * Shows file chooser and serializes mission to GeoJSON.
     */
    private void handleSaveMission() {
        // Check if mission has behaviours
        if (mission.getMissionPlan().getBehaviours().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Save Mission");
            alert.setHeaderText("Mission is empty");
            alert.setContentText("Cannot save an empty mission. Please add at least one behaviour.");
            alert.showAndWait();
            return;
        }

        // Show file chooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Mission");
        fileChooser.setInitialFileName("mission.geojson");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("GeoJSON Files", "*.geojson")
        );

        File file = fileChooser.showSaveDialog(mainView.getScene().getWindow());
        if (file == null) {
            return;
        }

        try {
            // Serialize mission to GeoJSON
            String geoJson = MissionSerializer.serializeToGeoJSON(mission.getMissionPlan());

            // Write to file
            Files.writeString(file.toPath(), geoJson);

            // Clear dirty flag
            mission.clearDirty();

            // Show success message
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Save Mission");
            alert.setHeaderText("Mission saved successfully");
            alert.setContentText("Saved to: " + file.getAbsolutePath());
            alert.showAndWait();

        } catch (IOException e) {
            // Show error dialog
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Save Mission Failed");
            alert.setHeaderText("Could not save mission");
            alert.setContentText("Error: " + e.getMessage());
            alert.showAndWait();

            System.err.println("Failed to save mission: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle Load Mission button click.
     * Shows file chooser, deserializes mission from GeoJSON, and updates UI.
     */
    private void handleLoadMission() {
        // Warn if current mission has unsaved changes
        if (mission.isDirty()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Load Mission");
            alert.setHeaderText("Unsaved changes");
            alert.setContentText("Current mission has unsaved changes. Continue loading?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) {
                return;
            }
        }

        // Show file chooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Mission");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("GeoJSON Files", "*.geojson")
        );

        File file = fileChooser.showOpenDialog(mainView.getScene().getWindow());
        if (file == null) {
            return;
        }

        try {
            // Read file
            String geoJson = Files.readString(file.toPath());

            // Deserialize mission
            var loadedMissionPlan = MissionSerializer.deserializeFromGeoJSON(geoJson);

            // Clear current mission
            var currentBehaviours = new java.util.ArrayList<>(mission.getMissionPlan().getBehaviours());
            for (int i = currentBehaviours.size() - 1; i >= 0; i--) {
                mission.getMissionPlan().removeBehaviour(i);
            }
            missionPlanPanel.getBehaviors().clear();

            // Add loaded behaviours to mission
            for (Behaviour behaviour : loadedMissionPlan.getBehaviours()) {
                mission.getMissionPlan().addBehaviour(behaviour);
                missionPlanPanel.addBehavior(behaviour);
            }

            // Update map
            mapPanel.clearOverlays();
            rerenderAllBehaviours();

            // Update Start button state
            updateStartButtonState();

            // Clear dirty flag
            mission.clearDirty();

            // Show success message
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Load Mission");
            alert.setHeaderText("Mission loaded successfully");
            alert.setContentText("Loaded " + loadedMissionPlan.getBehaviours().size() +
                " behaviours from: " + file.getName());
            alert.showAndWait();

        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Load Mission Failed");
            alert.setHeaderText("Could not read file");
            alert.setContentText("Error: " + e.getMessage());
            alert.showAndWait();

            System.err.println("Failed to load mission: " + e.getMessage());
            e.printStackTrace();

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Load Mission Failed");
            alert.setHeaderText("Invalid mission file");
            alert.setContentText("Error: " + e.getMessage());
            alert.showAndWait();

            System.err.println("Failed to deserialize mission: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
