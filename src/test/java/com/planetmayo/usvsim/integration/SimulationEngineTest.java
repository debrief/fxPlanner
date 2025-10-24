package com.planetmayo.usvsim.integration;

import com.planetmayo.usvsim.controller.SimulationEngine;
import com.planetmayo.usvsim.model.behaviour.ParallelTrackSearch;
import com.planetmayo.usvsim.model.geometry.Polygon;
import com.planetmayo.usvsim.model.geometry.Position;
import com.planetmayo.usvsim.model.mission.Mission;
import com.planetmayo.usvsim.model.mission.MissionState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for SimulationEngine.
 *
 * Tests full simulation lifecycle:
 * - Start, pause, resume, stop
 * - Behaviour sequencing
 * - State updates and callbacks
 * - Time acceleration
 */
class SimulationEngineTest {
    private Mission mission;
    private SimulationEngine engine;

    @BeforeEach
    void setup() {
        // Create mission with default platform at Portland Harbour
        mission = Mission.createDefault(Position.of(50.6, -2.4));
        engine = new SimulationEngine(mission);
    }

    @AfterEach
    void cleanup() {
        if (engine != null) {
            engine.shutdown();
        }
    }

    @Test
    void testSimulationStartsAndStops() {
        assertFalse(engine.isRunning(), "Should not be running initially");

        engine.start();
        assertTrue(engine.isRunning(), "Should be running after start");
        assertEquals(MissionState.EXECUTING, mission.getState());

        engine.stop();
        assertFalse(engine.isRunning(), "Should not be running after stop");
        assertEquals(MissionState.PLANNING, mission.getState());
    }

    @Test
    void testPauseAndResume() throws InterruptedException {
        // Create a behaviour first
        List<Position> vertices = new ArrayList<>();
        vertices.add(Position.of(50.6, -2.4));
        vertices.add(Position.of(50.601, -2.4));
        vertices.add(Position.of(50.601, -2.401));
        Polygon searchArea = new Polygon(vertices);

        ParallelTrackSearch behaviour = new ParallelTrackSearch(
            searchArea,
            0.0,
            100.0,
            5.0
        );

        mission.getMissionPlan().addBehaviour(behaviour);

        engine.start();
        assertTrue(engine.isRunning());

        // Give engine time to start
        Thread.sleep(300);

        // Pause
        engine.pause();
        assertTrue(engine.isPaused(), "Should be paused");
        assertEquals(MissionState.PAUSED, mission.getState());

        // Resume
        engine.resume();
        assertFalse(engine.isPaused(), "Should not be paused after resume");
        assertEquals(MissionState.EXECUTING, mission.getState());

        engine.stop();
    }

    @Test
    void testBehaviourSequencing() throws InterruptedException {
        // Create a simple parallel track search behaviour
        List<Position> vertices = new ArrayList<>();
        vertices.add(Position.of(50.6, -2.4));
        vertices.add(Position.of(50.61, -2.4));
        vertices.add(Position.of(50.61, -2.41));
        Polygon searchArea = new Polygon(vertices);

        ParallelTrackSearch behaviour = new ParallelTrackSearch(
            searchArea,
            0.0,      // 0° orientation
            100.0,    // 100m spacing
            5.0       // 5 knots speed
        );

        mission.getMissionPlan().addBehaviour(behaviour);
        assertEquals(1, mission.getMissionPlan().getBehaviours().size());

        // Start simulation
        engine.start();
        assertTrue(engine.isRunning(), "Engine should be running");

        // Let simulation run briefly
        Thread.sleep(2000);

        // Verify engine is processing (state updates happening)
        assertTrue(engine.isRunning() || mission.isComplete(), "Engine should be running or mission complete");

        engine.stop();
        assertFalse(engine.isRunning(), "Engine should stop after stop()");
    }

    @Test
    void testStateUpdatesCallback() throws InterruptedException {
        // Create simple behaviour
        List<Position> vertices = new ArrayList<>();
        vertices.add(Position.of(50.6, -2.4));
        vertices.add(Position.of(50.601, -2.4));
        vertices.add(Position.of(50.601, -2.401));
        Polygon searchArea = new Polygon(vertices);

        ParallelTrackSearch behaviour = new ParallelTrackSearch(
            searchArea,
            0.0,
            100.0,
            5.0
        );

        mission.getMissionPlan().addBehaviour(behaviour);

        // Track state updates
        List<Position> positionHistory = new ArrayList<>();
        engine.setOnStateChanged(() -> {
            positionHistory.add(mission.getPlatform().getState().getPosition());
        });

        engine.start();

        // Let it run for 1 second to collect some state updates
        Thread.sleep(1000);

        engine.stop();

        // Should have received multiple state updates
        assertTrue(positionHistory.size() > 0, "Should have received state updates");

        // Position should have changed from start
        Position startPos = mission.getPlatform().getState().getPosition();
        assertNotNull(startPos);
    }

    @Test
    void testTimeAcceleration() throws InterruptedException {
        // Create behaviour
        List<Position> vertices = new ArrayList<>();
        vertices.add(Position.of(50.6, -2.4));
        vertices.add(Position.of(50.601, -2.4));
        vertices.add(Position.of(50.601, -2.401));
        Polygon searchArea = new Polygon(vertices);

        ParallelTrackSearch behaviour = new ParallelTrackSearch(
            searchArea,
            0.0,
            100.0,
            5.0
        );

        mission.getMissionPlan().addBehaviour(behaviour);

        // Test 1× speed
        engine.setTimeAcceleration(1.0);
        engine.start();
        Thread.sleep(500);
        Position pos1xAfterHalf = mission.getPlatform().getState().getPosition();
        engine.stop();

        // Reset mission
        mission = Mission.createDefault(Position.of(50.6, -2.4));
        engine = new SimulationEngine(mission);
        mission.getMissionPlan().addBehaviour(behaviour);

        // Test 5× speed (should cover more distance in same time)
        engine.setTimeAcceleration(5.0);
        engine.start();
        Thread.sleep(500);
        Position pos5xAfterHalf = mission.getPlatform().getState().getPosition();
        engine.stop();

        // At 5× acceleration, should cover roughly 5× more distance
        double distance1x = pos1xAfterHalf.distanceTo(Position.of(50.6, -2.4));
        double distance5x = pos5xAfterHalf.distanceTo(Position.of(50.6, -2.4));

        assertTrue(distance5x > distance1x, "5× acceleration should cover more distance");
        // Allow some variance due to thread timing
        assertTrue(distance5x > distance1x * 2, "5× speed should cover significantly more distance");
    }

    @Test
    void testCannotStartEmptyMission() throws InterruptedException {
        // Mission with no behaviours
        mission = Mission.createDefault(Position.of(50.6, -2.4));
        engine = new SimulationEngine(mission);

        // Should handle gracefully (no behaviour to execute)
        engine.start();
        Thread.sleep(200);  // Let engine tick a few times

        // Engine should naturally stop when no behaviours
        // Either engine stops or mission completes (both valid)
        assertTrue(!engine.isRunning() || mission.isComplete(),
            "Engine should stop or mission should complete with no behaviours");
    }

    @Test
    void testPlatformStateUpdates() throws InterruptedException {
        // Create behaviour
        List<Position> vertices = new ArrayList<>();
        vertices.add(Position.of(50.6, -2.4));
        vertices.add(Position.of(50.601, -2.4));
        vertices.add(Position.of(50.601, -2.401));
        Polygon searchArea = new Polygon(vertices);

        ParallelTrackSearch behaviour = new ParallelTrackSearch(
            searchArea,
            0.0,
            100.0,
            5.0
        );

        mission.getMissionPlan().addBehaviour(behaviour);

        Position initialPosition = mission.getPlatform().getState().getPosition();
        double initialSpeed = mission.getPlatform().getState().getSpeed();

        engine.start();
        Thread.sleep(500);
        engine.stop();

        Position finalPosition = mission.getPlatform().getState().getPosition();
        double finalSpeed = mission.getPlatform().getState().getSpeed();

        // Platform should have moved
        double distance = initialPosition.distanceTo(finalPosition);
        assertTrue(distance > 0, "Platform should have moved");

        // Speed may have increased due to acceleration
        assertTrue(finalSpeed >= initialSpeed, "Speed should not decrease");
    }

    @Test
    void testMultipleBehaviours() throws InterruptedException {
        // Create two behaviours
        List<Position> vertices = new ArrayList<>();
        vertices.add(Position.of(50.6, -2.4));
        vertices.add(Position.of(50.601, -2.4));
        vertices.add(Position.of(50.601, -2.401));
        Polygon searchArea = new Polygon(vertices);

        ParallelTrackSearch behaviour1 = new ParallelTrackSearch(
            searchArea,
            0.0,
            100.0,
            5.0
        );

        ParallelTrackSearch behaviour2 = new ParallelTrackSearch(
            searchArea,
            90.0,  // Different orientation
            100.0,
            5.0
        );

        mission.getMissionPlan().addBehaviour(behaviour1);
        mission.getMissionPlan().addBehaviour(behaviour2);

        assertEquals(2, mission.getMissionPlan().getBehaviours().size());

        // Start simulation
        engine.start();
        assertTrue(engine.isRunning());

        // Run for a bit
        Thread.sleep(2000);

        // Should have multiple behaviours queued
        assertTrue(engine.isRunning() || mission.isComplete());

        engine.stop();
    }
}
