package com.planetmayo.usvsim.e2e;

import com.planetmayo.usvsim.controller.MissionController;
import com.planetmayo.usvsim.model.geometry.Polygon;
import com.planetmayo.usvsim.model.geometry.Position;
import com.planetmayo.usvsim.model.mission.Mission;
import com.planetmayo.usvsim.view.MainView;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end tests for mission simulation execution (E2E).
 *
 * Tests complete workflow:
 * - Create mission with parallel track search
 * - Start/pause/stop simulation
 * - Verify controller methods work
 *
 * Uses TestFX for application context.
 */
@ExtendWith(ApplicationExtension.class)
class SimulationExecutionTest {
    private MainView mainView;
    private MissionController controller;
    private Mission mission;

    @Start
    private void start(Stage stage) {
        // Create main view
        mainView = new MainView(stage);

        // Create mission model
        mission = Mission.createDefault(Position.of(50.6, -2.4));

        // Create controller
        controller = new MissionController(mission, mainView);

        // Show the application
        mainView.show();
    }

    @Test
    void testMissionCanBeCreatedAndStarted(FxRobot robot) throws InterruptedException {
        // Create a mission with a behaviour
        List<Position> vertices = new ArrayList<>();
        vertices.add(Position.of(50.6, -2.4));
        vertices.add(Position.of(50.601, -2.4));
        vertices.add(Position.of(50.601, -2.401));
        Polygon searchArea = new Polygon(vertices);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            controller.addParallelTrackSearch(
                searchArea,
                new com.planetmayo.usvsim.view.dialogs.ParallelTrackSearchParams(0.0, 100.0, 5.0)
            );
            latch.countDown();
        });

        // Verify behaviour was added
        assertTrue(latch.await(2, TimeUnit.SECONDS));
        assertEquals(1, mission.getMissionPlan().getBehaviours().size());

        // Start simulation
        Platform.runLater(() -> controller.startSimulation());
        robot.sleep(300);

        // Verify mission is in executing state
        assertEquals(com.planetmayo.usvsim.model.mission.MissionState.EXECUTING,
            mission.getState(), "Mission should be executing");
    }

    @Test
    void testSimulationCanBePausedAndResumed(FxRobot robot) throws InterruptedException {
        // Create mission with behaviour
        List<Position> vertices = new ArrayList<>();
        vertices.add(Position.of(50.6, -2.4));
        vertices.add(Position.of(50.601, -2.4));
        vertices.add(Position.of(50.601, -2.401));
        Polygon searchArea = new Polygon(vertices);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            controller.addParallelTrackSearch(
                searchArea,
                new com.planetmayo.usvsim.view.dialogs.ParallelTrackSearchParams(0.0, 100.0, 5.0)
            );
            latch.countDown();
        });

        assertTrue(latch.await(2, TimeUnit.SECONDS));

        // Start simulation
        Platform.runLater(() -> controller.startSimulation());
        robot.sleep(300);

        // Pause
        Platform.runLater(() -> controller.pauseSimulation());
        robot.sleep(200);

        assertEquals(com.planetmayo.usvsim.model.mission.MissionState.PAUSED,
            mission.getState(), "Mission should be paused");

        // Resume
        Platform.runLater(() -> controller.resumeSimulation());
        robot.sleep(200);

        assertEquals(com.planetmayo.usvsim.model.mission.MissionState.EXECUTING,
            mission.getState(), "Mission should be executing after resume");
    }

    @Test
    void testSimulationCanBeStopped(FxRobot robot) throws InterruptedException {
        // Create mission
        List<Position> vertices = new ArrayList<>();
        vertices.add(Position.of(50.6, -2.4));
        vertices.add(Position.of(50.601, -2.4));
        vertices.add(Position.of(50.601, -2.401));
        Polygon searchArea = new Polygon(vertices);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            controller.addParallelTrackSearch(
                searchArea,
                new com.planetmayo.usvsim.view.dialogs.ParallelTrackSearchParams(0.0, 100.0, 5.0)
            );
            latch.countDown();
        });

        assertTrue(latch.await(2, TimeUnit.SECONDS));

        // Start simulation
        Platform.runLater(() -> controller.startSimulation());
        robot.sleep(500);

        // Stop
        Platform.runLater(() -> controller.stopSimulation());
        robot.sleep(200);

        // Verify state is back to PLANNING
        assertEquals(com.planetmayo.usvsim.model.mission.MissionState.PLANNING,
            mission.getState(), "Mission should be in PLANNING state after stop");
    }

    @Test
    void testTimeAccelerationCanBeSet(FxRobot robot) throws InterruptedException {
        // Create mission
        List<Position> vertices = new ArrayList<>();
        vertices.add(Position.of(50.6, -2.4));
        vertices.add(Position.of(50.601, -2.4));
        vertices.add(Position.of(50.601, -2.401));
        Polygon searchArea = new Polygon(vertices);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            controller.addParallelTrackSearch(
                searchArea,
                new com.planetmayo.usvsim.view.dialogs.ParallelTrackSearchParams(0.0, 100.0, 5.0)
            );
            latch.countDown();
        });

        assertTrue(latch.await(2, TimeUnit.SECONDS));

        // Set time acceleration - should not throw
        Platform.runLater(() -> {
            controller.setTimeAcceleration(5.0);
            controller.startSimulation();
        });

        robot.sleep(300);

        assertEquals(com.planetmayo.usvsim.model.mission.MissionState.EXECUTING,
            mission.getState(), "Mission should be executing with time acceleration");

        Platform.runLater(() -> controller.stopSimulation());
    }

    @Test
    void testDrawingControllerIsAvailable(FxRobot robot) {
        // Verify drawing controller exists
        assertNotNull(controller.getDrawingController(),
            "DrawingController should be available from MissionController");
    }

    @Test
    void testMissionWithoutBehavioursCannotStart(FxRobot robot) throws InterruptedException {
        // Create new mission without behaviours
        Mission emptyMission = Mission.createDefault(Position.of(50.6, -2.4));
        MissionController emptyController = new MissionController(emptyMission, mainView);

        // Try to start
        Platform.runLater(() -> {
            emptyController.startSimulation();
        });

        robot.sleep(300);

        // Mission should handle gracefully
        assertTrue(emptyMission.isComplete() ||
                   emptyMission.getState() == com.planetmayo.usvsim.model.mission.MissionState.PLANNING,
            "Mission without behaviours should complete or stay in PLANNING");
    }
}
