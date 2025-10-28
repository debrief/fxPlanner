package com.planetmayo.usvsim.e2e;

import com.planetmayo.usvsim.controller.MissionController;
import com.planetmayo.usvsim.model.geometry.Polygon;
import com.planetmayo.usvsim.model.geometry.Position;
import com.planetmayo.usvsim.model.mission.Mission;
import com.planetmayo.usvsim.view.MainView;
import com.planetmayo.usvsim.view.dialogs.ParallelTrackSearchParams;
import com.planetmayo.usvsim.view.dialogs.ExpandingSquareSearchParams;
import com.planetmayo.usvsim.view.dialogs.WaypointTransitParams;
import com.planetmayo.usvsim.view.dialogs.ReturnToBaseParams;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * E2E tests specifically designed to capture screenshots for documentation.
 * 
 * This test class runs scenarios and captures interesting screenshots
 * that showcase the application's features.
 */
@ExtendWith(ApplicationExtension.class)
public class ScreenshotCaptureE2ETest {

    // Screenshot configuration
    private static final String SCREENSHOT_DIR = "screenshots/";
    private static final int UI_RENDER_WAIT_MS = 1000;
    private static final int SIMULATION_RUN_WAIT_MS = 2000;
    private static final int OPERATION_WAIT_MS = 500;
    private static final int SCREENSHOT_TIMEOUT_SECONDS = 5;

    private MainView mainView;
    private MissionController controller;
    private Mission mission;
    private Stage stage;

    @Start
    private void start(Stage stage) {
        this.stage = stage;
        
        // Create main view
        mainView = new MainView(stage);

        // Create mission model - Portland Harbour start position
        mission = Mission.createDefault(Position.of(50.5712, -2.4525));

        // Create controller
        controller = new MissionController(mission, mainView);

        // Show the application
        mainView.show();
    }

    /**
     * Captures a screenshot of the current stage.
     * 
     * @param filename The filename to save the screenshot to
     * @throws IOException If screenshot cannot be saved
     */
    private void captureScreenshot(String filename) throws IOException {
        CountDownLatch latch = new CountDownLatch(1);
        final WritableImage[] imageHolder = new WritableImage[1];
        
        Platform.runLater(() -> {
            try {
                Scene scene = stage.getScene();
                if (scene != null) {
                    WritableImage image = scene.snapshot(null);
                    imageHolder[0] = image;
                }
            } finally {
                latch.countDown();
            }
        });

        try {
            latch.await(SCREENSHOT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (imageHolder[0] != null) {
                File outputFile = new File(SCREENSHOT_DIR + filename);
                outputFile.getParentFile().mkdirs();
                ImageIO.write(SwingFXUtils.fromFXImage(imageHolder[0], null), "png", outputFile);
                System.out.println("Screenshot saved: " + outputFile.getAbsolutePath());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Screenshot capture interrupted", e);
        }
    }

    @Test
    void captureApplicationStartup(FxRobot robot) throws Exception {
        // Wait for UI to fully render
        robot.sleep(UI_RENDER_WAIT_MS);
        
        // Capture initial application state
        captureScreenshot("01-application-startup.png");
    }

    @Test
    void captureParallelTrackSearch(FxRobot robot) throws Exception {
        // Define search area - 1km square in Portland Harbour
        List<Position> vertices = createTestSquare(50.58, -2.42, 0.01);
        Polygon searchArea = new Polygon(vertices);

        CountDownLatch behaviorAdded = new CountDownLatch(1);

        Platform.runLater(() -> {
            controller.addParallelTrackSearch(
                searchArea,
                new ParallelTrackSearchParams(45.0, 150.0, 6.0)
            );
            behaviorAdded.countDown();
        });

        behaviorAdded.await(3, TimeUnit.SECONDS);
        robot.sleep(UI_RENDER_WAIT_MS);
        
        // Capture parallel track search pattern
        captureScreenshot("02-parallel-track-search.png");
    }

    @Test
    void captureExpandingSquareSearch(FxRobot robot) throws Exception {
        // Define search area
        List<Position> vertices = createTestSquare(50.59, -2.41, 0.015);
        Polygon searchArea = new Polygon(vertices);

        CountDownLatch behaviorAdded = new CountDownLatch(1);

        Platform.runLater(() -> {
            controller.addExpandingSquareSearch(
                searchArea,
                new ExpandingSquareSearchParams(90.0, 200.0, 5.0)
            );
            behaviorAdded.countDown();
        });

        behaviorAdded.await(3, TimeUnit.SECONDS);
        robot.sleep(UI_RENDER_WAIT_MS);
        
        // Capture expanding square search pattern
        captureScreenshot("03-expanding-square-search.png");
    }

    @Test
    void captureMultipleBehaviorMission(FxRobot robot) throws Exception {
        CountDownLatch allBehaviorsAdded = new CountDownLatch(4);

        Platform.runLater(() -> {
            // 1. Waypoint transit to search area
            List<Position> transitWaypoints = List.of(
                Position.of(50.57, -2.45),
                Position.of(50.58, -2.43)
            );
            controller.addWaypointTransit(new WaypointTransitParams(transitWaypoints, 8.0));
            allBehaviorsAdded.countDown();

            // 2. Parallel track search
            Polygon searchArea1 = new Polygon(createTestSquare(50.58, -2.43, 0.008));
            controller.addParallelTrackSearch(
                searchArea1,
                new ParallelTrackSearchParams(0.0, 100.0, 5.0)
            );
            allBehaviorsAdded.countDown();

            // 3. Expanding square search
            Polygon searchArea2 = new Polygon(createTestSquare(50.59, -2.42, 0.012));
            controller.addExpandingSquareSearch(
                searchArea2,
                new ExpandingSquareSearchParams(45.0, 200.0, 4.0)
            );
            allBehaviorsAdded.countDown();

            // 4. Return to base
            controller.addReturnToBase(
                new ReturnToBaseParams(Position.of(50.5712, -2.4525), 8.0)
            );
            allBehaviorsAdded.countDown();
        });

        allBehaviorsAdded.await(5, TimeUnit.SECONDS);
        robot.sleep(UI_RENDER_WAIT_MS);
        
        // Capture complete multi-behavior mission
        captureScreenshot("04-multi-behavior-mission.png");
    }

    @Test
    void captureSimulationExecution(FxRobot robot) throws Exception {
        // Create a simple mission
        List<Position> vertices = createTestSquare(50.58, -2.42, 0.01);
        Polygon searchArea = new Polygon(vertices);

        CountDownLatch behaviorAdded = new CountDownLatch(1);

        Platform.runLater(() -> {
            controller.addParallelTrackSearch(
                searchArea,
                new ParallelTrackSearchParams(0.0, 100.0, 6.0)
            );
            behaviorAdded.countDown();
        });

        behaviorAdded.await(3, TimeUnit.SECONDS);
        robot.sleep(OPERATION_WAIT_MS);
        
        // Start simulation
        Platform.runLater(() -> controller.startSimulation());
        robot.sleep(SIMULATION_RUN_WAIT_MS); // Let simulation run
        
        // Capture during simulation
        captureScreenshot("05-simulation-executing.png");
        
        // Stop simulation
        Platform.runLater(() -> controller.stopSimulation());
        robot.sleep(OPERATION_WAIT_MS);
    }

    // Helper method to create test squares
    private List<Position> createTestSquare(double centerLat, double centerLon, double size) {
        double halfSize = size / 2;
        return List.of(
            Position.of(centerLat - halfSize, centerLon - halfSize),
            Position.of(centerLat + halfSize, centerLon - halfSize),
            Position.of(centerLat + halfSize, centerLon + halfSize),
            Position.of(centerLat - halfSize, centerLon + halfSize)
        );
    }
}
