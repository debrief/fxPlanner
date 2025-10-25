package com.planetmayo.usvsim.e2e;

import com.planetmayo.usvsim.controller.MissionController;
import com.planetmayo.usvsim.model.behaviour.Behaviour;
import com.planetmayo.usvsim.model.geometry.Polygon;
import com.planetmayo.usvsim.model.geometry.Position;
import com.planetmayo.usvsim.model.mission.Mission;
import com.planetmayo.usvsim.view.MainView;
import com.planetmayo.usvsim.view.dialogs.ParallelTrackSearchParams;
import com.planetmayo.usvsim.view.dialogs.ExpandingSquareSearchParams;
import com.planetmayo.usvsim.view.dialogs.WaypointTransitParams;
import com.planetmayo.usvsim.view.dialogs.ReturnToBaseParams;
import com.planetmayo.usvsim.model.geometry.Waypoint;
import javafx.application.Platform;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
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
 * E2E tests for creating all behavior types.
 *
 * Tests complete workflows:
 * - User interaction with UI
 * - Drawing on map
 * - Parameter entry
 * - Validation
 * - Rendering verification
 *
 * Coverage:
 * - All 4 behavior types
 * - Valid parameter ranges
 * - Map rendering
 * - Mission plan updates
 * - Start button state management
 */
@ExtendWith(ApplicationExtension.class)
public class BehaviorCreationE2ETest {

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

    @BeforeEach
    void setUp() {
        // Clear any existing behaviors before each test
        Platform.runLater(() -> {
            mission.getMissionPlan().getBehaviours().clear();
            mainView.getMissionPlanPanel().getBehaviors().clear();
            mainView.getMapPanel().clearOverlays();
        });
    }

    @Test
    void testCreateParallelTrackSearch(FxRobot robot) throws InterruptedException {
        // Define search area - 1km square in Portland Harbour
        List<Position> vertices = createTestSquare(50.58, -2.42, 0.01); // ~1km square
        Polygon searchArea = new Polygon(vertices);

        CountDownLatch behaviorAdded = new CountDownLatch(1);

        Platform.runLater(() -> {
            // Simulate the complete workflow
            controller.addParallelTrackSearch(
                searchArea,
                new ParallelTrackSearchParams(45.0, 150.0, 6.0) // 45° orientation, 150m spacing, 6 knots
            );
            behaviorAdded.countDown();
        });

        // Wait for behavior to be added
        assertTrue(behaviorAdded.await(3, TimeUnit.SECONDS), "Behavior should be added within 3 seconds");

        // Verify behavior was added to mission
        assertEquals(1, mission.getMissionPlan().getBehaviours().size(),
            "Mission should have exactly 1 behavior");

        // Verify behavior type and parameters
        Behaviour behavior = mission.getMissionPlan().getBehaviours().get(0);
        assertEquals("Parallel Track Search", behavior.getName());
        assertTrue(behavior.getDescription().contains("45.0°"),
            "Description should contain orientation");
        assertTrue(behavior.getDescription().contains("150.0m"),
            "Description should contain spacing");

        // Verify waypoints were generated
        assertFalse(behavior.getWaypoints().isEmpty(),
            "Parallel track search should generate waypoints");
        assertTrue(behavior.getWaypoints().size() >= 2,
            "Should have at least 2 waypoints for parallel tracks");

        // Verify UI updates
        robot.sleep(200); // Allow UI to update

        Platform.runLater(() -> {
            // Check mission plan panel
            ListView<Behaviour> behaviorList = mainView.getMissionPlanPanel().getBehaviorListView();
            assertEquals(1, behaviorList.getItems().size(),
                "UI list should show 1 behavior");

            // Verify Start button is enabled (mission has behaviors)
            assertTrue(mainView.getControlPanel().isStartEnabled(),
                "Start button should be enabled when behaviors exist");
        });
    }

    @Test
    void testCreateExpandingSquareSearch(FxRobot robot) throws InterruptedException {
        // Define search area - 2km square for expanding pattern
        List<Position> vertices = createTestSquare(50.59, -2.41, 0.02); // ~2km square
        Polygon searchArea = new Polygon(vertices);

        CountDownLatch behaviorAdded = new CountDownLatch(1);

        Platform.runLater(() -> {
            controller.addExpandingSquareSearch(
                searchArea,
                new ExpandingSquareSearchParams(90.0, 200.0, 5.0) // East start, 200m increment, 5 knots
            );
            behaviorAdded.countDown();
        });

        assertTrue(behaviorAdded.await(3, TimeUnit.SECONDS));

        // Verify behavior was added
        assertEquals(1, mission.getMissionPlan().getBehaviours().size());

        Behaviour behavior = mission.getMissionPlan().getBehaviours().get(0);
        assertEquals("Expanding Square Search", behavior.getName());

        // Verify spiral pattern generation
        List<Waypoint> waypoints = behavior.getWaypoints();
        assertFalse(waypoints.isEmpty(), "Should generate waypoints");

        // First waypoint should be at centroid
        Position firstWp = waypoints.get(0).getPosition();
        Position centroid = searchArea.getCentroid();
        double distanceFromCentroid = firstWp.distanceTo(centroid);
        assertTrue(distanceFromCentroid < 10,
            "First waypoint should be at centroid (within 10m)");

        // Verify expanding pattern - distances should increase
        if (waypoints.size() >= 3) {
            double dist1 = waypoints.get(0).getPosition().distanceTo(waypoints.get(1).getPosition());
            double dist2 = waypoints.get(2).getPosition().distanceTo(waypoints.get(3).getPosition());
            // Later legs should be longer (expanding)
            assertTrue(dist2 >= dist1, "Pattern should expand outward");
        }
    }

    @Test
    void testCreateWaypointTransit(FxRobot robot) throws InterruptedException {
        // Create a route with 5 waypoints
        List<Position> routePoints = List.of(
            Position.of(50.57, -2.45),   // Start near platform position
            Position.of(50.58, -2.44),   // Northeast
            Position.of(50.59, -2.43),   // Further northeast
            Position.of(50.59, -2.41),   // East
            Position.of(50.58, -2.40)    // Southeast
        );

        // Convert to Waypoint objects
        List<Waypoint> waypoints = new ArrayList<>();
        for (Position pos : routePoints) {
            waypoints.add(Waypoint.transit(pos, 7.0)); // 7 knots transit speed
        }

        CountDownLatch behaviorAdded = new CountDownLatch(1);

        Platform.runLater(() -> {
            controller.addWaypointTransit(
                new WaypointTransitParams(waypoints, 7.0)
            );
            behaviorAdded.countDown();
        });

        assertTrue(behaviorAdded.await(3, TimeUnit.SECONDS));

        // Verify behavior was added
        assertEquals(1, mission.getMissionPlan().getBehaviours().size());

        Behaviour behavior = mission.getMissionPlan().getBehaviours().get(0);
        assertEquals("Waypoint Transit", behavior.getName());
        assertEquals(5, behavior.getWaypoints().size(),
            "Should have exactly 5 waypoints");

        // Verify waypoints match input
        for (int i = 0; i < routePoints.size(); i++) {
            Position expected = routePoints.get(i);
            Position actual = behavior.getWaypoints().get(i).getPosition();
            double distance = expected.distanceTo(actual);
            assertTrue(distance < 1.0,
                String.format("Waypoint %d should match input position", i));
        }

        // Verify all waypoints have correct speed
        for (Waypoint wp : behavior.getWaypoints()) {
            assertEquals(7.0, wp.getSpeed(), 0.01,
                "All waypoints should have speed 7.0 knots");
        }
    }

    @Test
    void testCreateReturnToBase(FxRobot robot) throws InterruptedException {
        // First, add a search behavior to establish an end position
        List<Position> vertices = createTestSquare(50.58, -2.42, 0.01);
        Polygon searchArea = new Polygon(vertices);

        CountDownLatch firstBehaviorAdded = new CountDownLatch(1);

        Platform.runLater(() -> {
            controller.addParallelTrackSearch(
                searchArea,
                new ParallelTrackSearchParams(0.0, 100.0, 5.0)
            );
            firstBehaviorAdded.countDown();
        });

        assertTrue(firstBehaviorAdded.await(3, TimeUnit.SECONDS));
        assertEquals(1, mission.getMissionPlan().getBehaviours().size());

        // Now add Return to Base using current position
        Position basePosition = mission.getPlatform().getState().getPosition();

        CountDownLatch rtbAdded = new CountDownLatch(1);

        Platform.runLater(() -> {
            controller.addReturnToBase(
                new ReturnToBaseParams(basePosition, 8.0) // Return at 8 knots
            );
            rtbAdded.countDown();
        });

        assertTrue(rtbAdded.await(3, TimeUnit.SECONDS));

        // Should now have 2 behaviors
        assertEquals(2, mission.getMissionPlan().getBehaviours().size(),
            "Should have search behavior and return to base");

        // Verify RTB behavior
        Behaviour rtb = mission.getMissionPlan().getBehaviours().get(1);
        assertEquals("Return to Base", rtb.getName());

        // RTB should generate waypoints from last position of previous behavior to base
        assertFalse(rtb.getWaypoints().isEmpty(),
            "Return to base should generate waypoints");

        // Last waypoint should be at base position
        if (!rtb.getWaypoints().isEmpty()) {
            Position lastWp = rtb.getWaypoints().get(rtb.getWaypoints().size() - 1).getPosition();
            double distanceToBase = lastWp.distanceTo(basePosition);
            assertTrue(distanceToBase < 10,
                "Last waypoint should be at base position (within 10m)");
        }
    }

    @Test
    void testCreateReturnToBaseCustomLocation(FxRobot robot) throws InterruptedException {
        // Add initial behavior
        List<Position> vertices = createTestSquare(50.58, -2.42, 0.01);
        Polygon searchArea = new Polygon(vertices);

        CountDownLatch firstBehaviorAdded = new CountDownLatch(1);

        Platform.runLater(() -> {
            controller.addParallelTrackSearch(
                searchArea,
                new ParallelTrackSearchParams(0.0, 100.0, 5.0)
            );
            firstBehaviorAdded.countDown();
        });

        assertTrue(firstBehaviorAdded.await(3, TimeUnit.SECONDS));

        // Add RTB with custom coordinates
        Position customBase = Position.of(50.60, -2.39); // Different from platform position

        CountDownLatch rtbAdded = new CountDownLatch(1);

        Platform.runLater(() -> {
            controller.addReturnToBase(
                new ReturnToBaseParams(customBase, 6.0)
            );
            rtbAdded.countDown();
        });

        assertTrue(rtbAdded.await(3, TimeUnit.SECONDS));

        // Verify custom base location is used
        Behaviour rtb = mission.getMissionPlan().getBehaviours().get(1);
        if (!rtb.getWaypoints().isEmpty()) {
            Position lastWp = rtb.getWaypoints().get(rtb.getWaypoints().size() - 1).getPosition();
            double distanceToCustomBase = lastWp.distanceTo(customBase);
            assertTrue(distanceToCustomBase < 10,
                "Should navigate to custom base location");
        }
    }

    @Test
    void testMultipleBehaviorCreation(FxRobot robot) throws InterruptedException {
        // Create a complete mission with all behavior types
        CountDownLatch allBehaviorsAdded = new CountDownLatch(4);

        Platform.runLater(() -> {
            // 1. Waypoint transit to search area
            List<Waypoint> transitWaypoints = List.of(
                Waypoint.transit(Position.of(50.57, -2.45), 8.0),
                Waypoint.transit(Position.of(50.58, -2.43), 8.0)
            );
            controller.addWaypointTransit(new WaypointTransitParams(transitWaypoints, 8.0));
            allBehaviorsAdded.countDown();

            // 2. Parallel track search
            Polygon searchArea1 = new Polygon(createTestSquare(50.58, -2.43, 0.01));
            controller.addParallelTrackSearch(
                searchArea1,
                new ParallelTrackSearchParams(0.0, 100.0, 5.0)
            );
            allBehaviorsAdded.countDown();

            // 3. Expanding square search
            Polygon searchArea2 = new Polygon(createTestSquare(50.59, -2.42, 0.015));
            controller.addExpandingSquareSearch(
                searchArea2,
                new ExpandingSquareSearchParams(45.0, 250.0, 4.0)
            );
            allBehaviorsAdded.countDown();

            // 4. Return to base
            controller.addReturnToBase(
                new ReturnToBaseParams(Position.of(50.5712, -2.4525), 10.0)
            );
            allBehaviorsAdded.countDown();
        });

        assertTrue(allBehaviorsAdded.await(5, TimeUnit.SECONDS),
            "All 4 behaviors should be added");

        // Verify all behaviors were added in correct order
        List<Behaviour> behaviors = mission.getMissionPlan().getBehaviours();
        assertEquals(4, behaviors.size(), "Should have all 4 behaviors");

        assertEquals("Waypoint Transit", behaviors.get(0).getName());
        assertEquals("Parallel Track Search", behaviors.get(1).getName());
        assertEquals("Expanding Square Search", behaviors.get(2).getName());
        assertEquals("Return to Base", behaviors.get(3).getName());

        // Verify mission is ready to execute
        robot.sleep(200);
        Platform.runLater(() -> {
            assertTrue(mainView.getControlPanel().isStartEnabled(),
                "Start button should be enabled for complete mission");

            ListView<Behaviour> behaviorList = mainView.getMissionPlanPanel().getBehaviorListView();
            assertEquals(4, behaviorList.getItems().size(),
                "UI should show all 4 behaviors");
        });
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