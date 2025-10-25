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
import com.planetmayo.usvsim.view.dialogs.ParallelTrackSearchDialog;
import com.planetmayo.usvsim.view.dialogs.ParallelTrackSearchParams;
import com.planetmayo.usvsim.view.dialogs.WaypointTransitDialog;
import com.planetmayo.usvsim.view.dialogs.WaypointTransitParams;
import com.planetmayo.usvsim.view.dialogs.ReturnToBaseDialog;
import com.planetmayo.usvsim.view.dialogs.ReturnToBaseParams;
import com.planetmayo.usvsim.view.dialogs.ExpandingSquareSearchDialog;
import com.planetmayo.usvsim.view.dialogs.ExpandingSquareSearchParams;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            _ -> {
                try {
                    Object mapReady = mapPanel.getWebEngine().executeScript(
                        "typeof window.leafletMap !== 'undefined' && window.leafletMap !== null"
                    );

                    if (mapReady != null && Boolean.parseBoolean(mapReady.toString())) {
                        System.out.println("Map ready - showing start position marker");
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
        System.out.println("Starting Parallel Track Search workflow");

        // Step 1: Start polygon drawing on map (setup callback first)
        drawingController.startDrawingPolygon(polygon -> {
            // This callback is triggered when the polygon is drawn and confirmed
            System.out.println("✓ Polygon received with " + polygon.getVertices().size() + " vertices");

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
            new javafx.animation.KeyFrame(javafx.util.Duration.millis(100), _ -> {
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
            System.out.println("Added parallel track search: " + params.orientation + "°, " +
                             params.spacing + "m spacing");

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
        System.out.println("Starting Waypoint Transit workflow");

        // Step 1: Start polyline drawing on map (setup callback first)
        drawingController.startDrawingWaypoints(waypoints -> {
            // This callback is triggered when the polyline is drawn and completed
            System.out.println("✓ Polyline received with " + waypoints.size() + " waypoints");

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
            System.out.println("User clicked Done Drawing - finishing polyline");
            drawingController.finishDrawing();
            // Note: callback will hide instructions and show config panel
        });

        instructionsPanel.setOnCancel(() -> {
            statePanel.hideDialog();
            drawingController.cancelDrawing();
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
            System.out.println("Added waypoint transit: " + params.waypoints.size() +
                             " waypoints at " + params.speed + " knots");

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
        System.out.println("Starting Return to Base workflow");

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
            System.out.println("Added return to base: " + String.format("(%.3f°N, %.3f°E)",
                             params.baseLocation.getLatitude(),
                             params.baseLocation.getLongitude()) +
                             " at " + params.speed + " knots");

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
        for (com.planetmayo.usvsim.model.behaviour.Behaviour behaviour : mission.getMissionPlan().getBehaviours()) {
            // Note: Behaviours don't have a reset method, they would need to be recreated
            // For now, we can clear the mission plan and let user recreate behaviours
        }

        // Update UI
        statePanel.reset();
        controlPanel.reset();
        mapPanel.updatePlatformPosition(initialPosition, 0.0);

        System.out.println("Simulation stopped - all state reset to initial conditions");
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

                System.out.println("Mission complete - final simulation time: " +
                    String.format("%02d:%02d:%02d",
                        finalSimulationTimeMs / 3600000,
                        (finalSimulationTimeMs % 3600000) / 60000,
                        (finalSimulationTimeMs % 60000) / 1000));
            });
        });

        // Wire double-click handler for editing behaviours in mission plan
        missionPlanPanel.setOnBehaviourDoubleClick(this::handleEditBehaviour);

        // Wire delete button handler for removing behaviours
        missionPlanPanel.setOnBehaviourDelete(this::handleDeleteBehaviour);

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
            new javafx.animation.KeyFrame(javafx.util.Duration.millis(100), _ -> {
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
        System.out.println("=== Deleting Behaviour ===");
        System.out.println("Behaviour: " + behaviour.getName());
        System.out.println("Before delete - Mission behaviours: " + mission.getMissionPlan().getBehaviours().size());
        System.out.println("Before delete - UI behaviours: " + missionPlanPanel.getBehaviors().size());

        // Get index before removal
        int index = mission.getMissionPlan().getBehaviours().indexOf(behaviour);

        // Remove from mission model
        mission.getMissionPlan().removeBehaviour(index);

        // Remove from UI
        missionPlanPanel.removeBehavior(behaviour);

        System.out.println("After delete - Mission behaviours: " + mission.getMissionPlan().getBehaviours().size());
        System.out.println("After delete - UI behaviours: " + missionPlanPanel.getBehaviors().size());

        // Clear map and re-render remaining behaviours
        mapPanel.clearOverlays();
        rerenderAllBehaviours();

        // Update Start button state
        updateStartButtonState();

        System.out.println("Deleted behaviour: " + behaviour.getName());
    }

    /**
     * Handle editing of a behaviour from the mission plan.
     * Opens the appropriate dialog based on behaviour type and replaces behaviour if user saves.
     */
    private void handleEditBehaviour(Behaviour behaviour) {
        System.out.println("Editing behaviour: " + behaviour.getName());

        // Get the index of this behaviour in the mission plan
        int index = mission.getMissionPlan().getBehaviours().indexOf(behaviour);
        if (index < 0) {
            System.err.println("Behaviour not found in mission plan");
            return;
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

        panel.setOnComplete(params -> {
            statePanel.hideDialog();

            System.out.println("=== Editing Parallel Track Search ===");
            System.out.println("Before edit - Mission behaviours: " + mission.getMissionPlan().getBehaviours().size());
            System.out.println("Before edit - UI behaviours: " + missionPlanPanel.getBehaviors().size());

            // Remove old behaviour
            mission.getMissionPlan().removeBehaviour(index);
            missionPlanPanel.removeBehavior(behaviour);

            System.out.println("After remove - Mission behaviours: " + mission.getMissionPlan().getBehaviours().size());
            System.out.println("After remove - UI behaviours: " + missionPlanPanel.getBehaviors().size());

            // Create new behaviour with updated params
            ParallelTrackSearch newBehaviour = new ParallelTrackSearch(
                behaviour.getSearchArea(),
                params.orientation,
                params.spacing,
                params.speed
            );

            // Add at same index
            mission.getMissionPlan().getBehaviours().add(index, newBehaviour);
            missionPlanPanel.getBehaviors().add(index, newBehaviour);

            System.out.println("After add - Mission behaviours: " + mission.getMissionPlan().getBehaviours().size());
            System.out.println("After add - UI behaviours: " + missionPlanPanel.getBehaviors().size());
            System.out.println("New behaviour waypoints: " + newBehaviour.getWaypoints().size());

            // Refresh mission plan UI
            missionPlanPanel.refresh();

            // Clear old overlays and re-render all behaviours
            mapPanel.clearOverlays();
            System.out.println("Starting rerender of all behaviours...");
            rerenderAllBehaviours();
            System.out.println("Rerender complete");

            // Update Start button state
            updateStartButtonState();

            System.out.println("Updated parallel track search at index " + index);
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

        panel.setOnComplete(params -> {
            statePanel.hideDialog();

            // Remove old behaviour
            mission.getMissionPlan().removeBehaviour(index);
            missionPlanPanel.removeBehavior(behaviour);

            // Create new behaviour
            ReturnToBase newBehaviour = new ReturnToBase(params.baseLocation, params.speed);

            // Add at same index
            mission.getMissionPlan().getBehaviours().add(index, newBehaviour);
            missionPlanPanel.getBehaviors().add(index, newBehaviour);

            // Refresh mission plan UI
            missionPlanPanel.refresh();

            // Clear old overlays and re-render all behaviours
            mapPanel.clearOverlays();
            rerenderAllBehaviours();

            // Update Start button state
            updateStartButtonState();

            System.out.println("Updated return to base at index " + index);
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

            System.out.println("=== Editing Expanding Square Search ===");
            System.out.println("Before edit - Mission behaviours: " + mission.getMissionPlan().getBehaviours().size());
            System.out.println("Before edit - UI behaviours: " + missionPlanPanel.getBehaviors().size());

            // Create new behaviour with updated params
            ExpandingSquareSearch newBehaviour = new ExpandingSquareSearch(
                behaviour.getSearchArea(),
                params.initialDirection,
                params.legIncrement,
                params.speed
            );

            // Replace at same index (instead of remove+add to maintain references)
            mission.getMissionPlan().getBehaviours().set(index, newBehaviour);
            missionPlanPanel.getBehaviors().set(index, newBehaviour);

            System.out.println("After edit - Mission behaviours: " + mission.getMissionPlan().getBehaviours().size());
            System.out.println("After edit - UI behaviours: " + missionPlanPanel.getBehaviors().size());
            System.out.println("New behaviour waypoints: " + newBehaviour.getWaypoints().size());

            // Refresh mission plan UI
            missionPlanPanel.refresh();

            // Clear old overlays and re-render all behaviours
            mapPanel.clearOverlays();
            System.out.println("Starting rerender of all behaviours...");
            rerenderAllBehaviours();
            System.out.println("Rerender complete");

            // Update Start button state
            updateStartButtonState();

            System.out.println("Updated expanding square search at index " + index);
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
        System.out.println("=== Rerendering " + mission.getMissionPlan().getBehaviours().size() + " behaviours ===");
        for (Behaviour behaviour : mission.getMissionPlan().getBehaviours()) {
            if (behaviour instanceof ParallelTrackSearch pts) {
                System.out.println("Rendering ParallelTrackSearch: " + pts.getWaypoints().size() + " waypoints");
                mapPanel.renderPolygon(pts.getSearchArea());
                mapPanel.renderTracks(pts.getWaypoints());
            } else if (behaviour instanceof ExpandingSquareSearch ess) {
                System.out.println("Rendering ExpandingSquareSearch: " + ess.getWaypoints().size() + " waypoints");
                mapPanel.renderPolygon(ess.getSearchArea());
                mapPanel.renderTracks(ess.getWaypoints());
            } else if (behaviour instanceof WaypointTransit wt) {
                System.out.println("Rendering WaypointTransit: " + wt.getWaypoints().size() + " waypoints");
                mapPanel.renderTracks(wt.getWaypoints(), true);
            } else if (behaviour instanceof ReturnToBase rtb) {
                System.out.println("Rendering ReturnToBase");
                // Find start position for RTB (last waypoint of previous behaviour)
                int rtbIndex = mission.getMissionPlan().getBehaviours().indexOf(rtb);
                Position startPos = getLastWaypointPosition(rtbIndex);
                renderReturnToBasePath(startPos, rtb.getBaseLocation(), rtb.getPlatformSpeed());
            }
        }
        System.out.println("=== Rerender complete ===");
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
}
