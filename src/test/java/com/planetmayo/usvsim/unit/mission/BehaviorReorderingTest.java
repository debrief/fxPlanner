package com.planetmayo.usvsim.unit.mission;

import com.planetmayo.usvsim.model.behaviour.Behaviour;
import com.planetmayo.usvsim.model.behaviour.ParallelTrackSearch;
import com.planetmayo.usvsim.model.behaviour.ExpandingSquareSearch;
import com.planetmayo.usvsim.model.behaviour.WaypointTransit;
import com.planetmayo.usvsim.model.geometry.Polygon;
import com.planetmayo.usvsim.model.geometry.Position;
import com.planetmayo.usvsim.model.mission.CompositeBehaviour;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for behavior reordering in CompositeBehaviour.
 *
 * Verifies that:
 * - Behaviors can be reordered using reorderBehaviour()
 * - Order is maintained correctly after multiple reorders
 * - Invalid reorder operations are handled safely
 */
class BehaviorReorderingTest {

    private CompositeBehaviour missionPlan;
    private Behaviour behaviour1;
    private Behaviour behaviour2;
    private Behaviour behaviour3;

    @BeforeEach
    void setUp() {
        missionPlan = new CompositeBehaviour();

        // Create 3 distinct behaviors
        Polygon area1 = new Polygon(createSquare(50.0, -2.0));
        behaviour1 = new ParallelTrackSearch(area1, 0.0, 100.0, 5.0);

        Polygon area2 = new Polygon(createSquare(50.1, -2.0));
        behaviour2 = new ExpandingSquareSearch(area2, 90.0, 500.0, 5.0);

        List<Position> waypoints = List.of(
            Position.of(50.2, -2.0),
            Position.of(50.3, -2.0)
        );
        behaviour3 = new WaypointTransit(waypoints, 6.0);

        // Add in order: 1, 2, 3
        missionPlan.addBehaviour(behaviour1);
        missionPlan.addBehaviour(behaviour2);
        missionPlan.addBehaviour(behaviour3);
    }

    @Test
    void testInitialOrder() {
        List<Behaviour> behaviors = missionPlan.getBehaviours();
        assertEquals(3, behaviors.size());
        assertSame(behaviour1, behaviors.get(0));
        assertSame(behaviour2, behaviors.get(1));
        assertSame(behaviour3, behaviors.get(2));
    }

    @Test
    void testMoveFirstToSecond() {
        // Move behavior at index 0 to index 1
        // Before: [1, 2, 3]
        // After:  [2, 1, 3]
        missionPlan.reorderBehaviour(0, 1);

        List<Behaviour> behaviors = missionPlan.getBehaviours();
        assertEquals(3, behaviors.size());
        assertSame(behaviour2, behaviors.get(0), "Second behavior should now be first");
        assertSame(behaviour1, behaviors.get(1), "First behavior should now be second");
        assertSame(behaviour3, behaviors.get(2), "Third behavior should remain third");
    }

    @Test
    void testMoveLastToFirst() {
        // Move behavior at index 2 to index 0
        // Before: [1, 2, 3]
        // After:  [3, 1, 2]
        missionPlan.reorderBehaviour(2, 0);

        List<Behaviour> behaviors = missionPlan.getBehaviours();
        assertEquals(3, behaviors.size());
        assertSame(behaviour3, behaviors.get(0), "Third behavior should now be first");
        assertSame(behaviour1, behaviors.get(1), "First behavior should now be second");
        assertSame(behaviour2, behaviors.get(2), "Second behavior should now be third");
    }

    @Test
    void testMoveMiddleUp() {
        // Simulate UI "move up" button on middle item
        // Before: [1, 2, 3]
        // After:  [2, 1, 3]
        missionPlan.reorderBehaviour(1, 0);

        List<Behaviour> behaviors = missionPlan.getBehaviours();
        assertSame(behaviour2, behaviors.get(0));
        assertSame(behaviour1, behaviors.get(1));
        assertSame(behaviour3, behaviors.get(2));
    }

    @Test
    void testMoveMiddleDown() {
        // Simulate UI "move down" button on middle item
        // Before: [1, 2, 3]
        // After:  [1, 3, 2]
        missionPlan.reorderBehaviour(1, 2);

        List<Behaviour> behaviors = missionPlan.getBehaviours();
        assertSame(behaviour1, behaviors.get(0));
        assertSame(behaviour3, behaviors.get(1));
        assertSame(behaviour2, behaviors.get(2));
    }

    @Test
    void testMultipleReorders() {
        // Perform multiple reorders
        // Start: [1, 2, 3]
        missionPlan.reorderBehaviour(0, 2); // [2, 3, 1]
        missionPlan.reorderBehaviour(1, 0); // [3, 2, 1]

        List<Behaviour> behaviors = missionPlan.getBehaviours();
        assertSame(behaviour3, behaviors.get(0));
        assertSame(behaviour2, behaviors.get(1));
        assertSame(behaviour1, behaviors.get(2));
    }

    @Test
    void testReorderSamePosition() {
        // Reorder to same position - should be no-op
        missionPlan.reorderBehaviour(1, 1);

        List<Behaviour> behaviors = missionPlan.getBehaviours();
        assertSame(behaviour1, behaviors.get(0));
        assertSame(behaviour2, behaviors.get(1));
        assertSame(behaviour3, behaviors.get(2));
    }

    @Test
    void testReorderInvalidIndices() {
        // Try invalid reorders - should not crash or modify list
        missionPlan.reorderBehaviour(-1, 1);
        missionPlan.reorderBehaviour(0, 5);
        missionPlan.reorderBehaviour(5, 0);

        // Order should remain unchanged
        List<Behaviour> behaviors = missionPlan.getBehaviours();
        assertEquals(3, behaviors.size());
        assertSame(behaviour1, behaviors.get(0));
        assertSame(behaviour2, behaviors.get(1));
        assertSame(behaviour3, behaviors.get(2));
    }

    @Test
    void testGetBehavioursReturnsDefensiveCopy() {
        // Verify that getBehaviours() returns a defensive copy
        List<Behaviour> behaviors1 = missionPlan.getBehaviours();
        List<Behaviour> behaviors2 = missionPlan.getBehaviours();

        assertNotSame(behaviors1, behaviors2,
            "Each call should return a new list instance");
        assertEquals(behaviors1, behaviors2,
            "But the contents should be equal");

        // Modifying returned list should not affect mission plan
        behaviors1.clear();
        assertEquals(3, missionPlan.getBehaviours().size(),
            "Mission plan should still have 3 behaviors");
    }

    // Helper to create square polygons
    private List<Position> createSquare(double centerLat, double centerLon) {
        return List.of(
            Position.of(centerLat - 0.01, centerLon - 0.01),
            Position.of(centerLat + 0.01, centerLon - 0.01),
            Position.of(centerLat + 0.01, centerLon + 0.01),
            Position.of(centerLat - 0.01, centerLon + 0.01)
        );
    }
}
