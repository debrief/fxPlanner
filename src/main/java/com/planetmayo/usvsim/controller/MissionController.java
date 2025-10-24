package com.planetmayo.usvsim.controller;

import com.planetmayo.usvsim.model.behaviour.ParallelTrackSearch;
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
public class MissionController {
    private final Mission mission;
    private final MapPanel mapPanel;
    private final MissionPlanPanel missionPlanPanel;
    private final ControlPanel controlPanel;
    private final StatePanel statePanel;
    private final DrawingController drawingController;
    private final SimulationEngine simulationEngine;

    public MissionController(Mission mission, MainView mainView) {
        this.mission = mission;
        this.mapPanel = mainView.getMapPanel();
        this.missionPlanPanel = mainView.getMissionPlanPanel();
        this.controlPanel = mainView.getControlPanel();
        this.statePanel = mainView.getStatePanel();
        this.drawingController = new DrawingController(mapPanel);
        this.simulationEngine = new SimulationEngine(mission);

        // Wire UI event handlers
        wireUIHandlers();
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
                System.out.println("TODO: Waypoint transit");
                break;
            case "Return to Base":
                System.out.println("TODO: Return to base");
                break;
            default:
                System.err.println("Unknown behavior type: " + behaviorType);
        }
    }

    /**
     * Start the Parallel Track Search workflow:
     * 1. Prompt user to draw polygon on map
     * 2. Show parameter dialog (orientation, spacing)
     * 3. Create ParallelTrackSearch behaviour
     * 4. Add to mission
     * 5. Update map display
     */
    private void startParallelTrackSearchDialog() {
        System.out.println("Starting Parallel Track Search workflow");

        // Step 1: Start polygon drawing
        drawingController.startDrawingPolygon(polygon -> {
            System.out.println("Polygon drawn with " + polygon.getVertices().size() + " vertices");

            // Step 2: Show parameter dialog
            ParallelTrackSearchDialog dialog = new ParallelTrackSearchDialog();
            dialog.showAndWait().ifPresent(params -> {
                addParallelTrackSearch(polygon, params);
            });
        });

        // Prompt user
        System.out.println("Please draw a polygon on the map. Click to add vertices. Press Enter or click 'Done' when finished.");
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
                statePanel.updateState(mission.getPlatform().getState());
            });
        });
    }
}
