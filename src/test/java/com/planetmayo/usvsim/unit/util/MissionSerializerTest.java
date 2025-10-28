package com.planetmayo.usvsim.unit.util;

import com.planetmayo.usvsim.model.behaviour.ParallelTrackSearch;
import com.planetmayo.usvsim.model.behaviour.ExpandingSquareSearch;
import com.planetmayo.usvsim.model.behaviour.WaypointTransit;
import com.planetmayo.usvsim.model.behaviour.ReturnToBase;
import com.planetmayo.usvsim.model.geometry.Polygon;
import com.planetmayo.usvsim.model.geometry.Position;
import com.planetmayo.usvsim.model.mission.CompositeBehaviour;
import com.planetmayo.usvsim.util.MissionSerializer;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MissionSerializer.
 *
 * Tests GeoJSON serialization/deserialization for all behaviour types.
 */
class MissionSerializerTest {

    @Test
    void serializeParallelTrackSearch_producesValidGeoJSON() {
        // Given: Parallel track search behaviour
        Polygon searchArea = new Polygon(Arrays.asList(
            Position.of(50.6, -2.4),
            Position.of(50.61, -2.4),
            Position.of(50.61, -2.39),
            Position.of(50.6, -2.39)
        ));
        ParallelTrackSearch behaviour = new ParallelTrackSearch(searchArea, 45.0, 100.0, 5.0);

        // When: Serialize to GeoJSON
        String geoJson = MissionSerializer.serializeBehaviourToGeoJSON(behaviour);

        // Debug output
        System.out.println("ParallelTrackSearch JSON:");
        System.out.println(geoJson);

        // Then: Valid JSON structure
        assertNotNull(geoJson);
        assertTrue(geoJson.contains("\"type\": \"Feature\"") || geoJson.contains("\"type\":\"Feature\""));
        assertTrue(geoJson.contains("\"type\": \"Polygon\"") || geoJson.contains("\"type\":\"Polygon\""));
        assertTrue(geoJson.contains("\"behaviourType\": \"ParallelTrackSearch\"") || geoJson.contains("\"behaviourType\":\"ParallelTrackSearch\""));
        assertTrue(geoJson.contains("\"trackOrientation\": 45") || geoJson.contains("\"trackOrientation\":45"));
        assertTrue(geoJson.contains("\"trackSpacing\": 100") || geoJson.contains("\"trackSpacing\":100"));
        assertTrue(geoJson.contains("\"platformSpeed\": 5") || geoJson.contains("\"platformSpeed\":5"));
    }

    @Test
    void serializeExpandingSquareSearch_producesValidGeoJSON() {
        // Given: Expanding square search behaviour
        Polygon searchArea = new Polygon(Arrays.asList(
            Position.of(50.6, -2.4),
            Position.of(50.61, -2.4),
            Position.of(50.61, -2.39),
            Position.of(50.6, -2.39)
        ));
        ExpandingSquareSearch behaviour = new ExpandingSquareSearch(searchArea, 90.0, 50.0, 5.0);

        // When: Serialize to GeoJSON
        String geoJson = MissionSerializer.serializeBehaviourToGeoJSON(behaviour);

        // Then: Valid JSON structure
        assertNotNull(geoJson);
        assertTrue(geoJson.contains("\"type\": \"Feature\"") || geoJson.contains("\"type\":\"Feature\""));
        assertTrue(geoJson.contains("\"type\": \"Polygon\""));
        assertTrue(geoJson.contains("\"behaviourType\": \"ExpandingSquareSearch\""));
        assertTrue(geoJson.contains("\"initialDirection\": 90") || geoJson.contains("\"initialDirection\":90"));
        assertTrue(geoJson.contains("\"legIncrement\": 50") || geoJson.contains("\"legIncrement\":50"));
        assertTrue(geoJson.contains("\"platformSpeed\": 5") || geoJson.contains("\"platformSpeed\":5"));
    }

    @Test
    void serializeWaypointTransit_producesValidGeoJSON() {
        // Given: Waypoint transit behaviour
        List<Position> waypoints = Arrays.asList(
            Position.of(50.6, -2.4),
            Position.of(50.61, -2.39),
            Position.of(50.62, -2.38)
        );
        WaypointTransit behaviour = new WaypointTransit(waypoints, 6.0);

        // When: Serialize to GeoJSON
        String geoJson = MissionSerializer.serializeBehaviourToGeoJSON(behaviour);

        // Then: Valid JSON structure
        assertNotNull(geoJson);
        assertTrue(geoJson.contains("\"type\": \"Feature\"") || geoJson.contains("\"type\":\"Feature\""));
        assertTrue(geoJson.contains("\"type\": \"MultiPoint\"") || geoJson.contains("\"type\": \"LineString\""));
        assertTrue(geoJson.contains("\"behaviourType\": \"WaypointTransit\""));
        assertTrue(geoJson.contains("\"platformSpeed\": 6") || geoJson.contains("\"platformSpeed\":6"));
    }

    @Test
    void serializeReturnToBase_producesValidGeoJSON() {
        // Given: Return to base behaviour
        Position basePosition = Position.of(50.6, -2.4);
        ReturnToBase behaviour = new ReturnToBase(basePosition, 8.0);

        // When: Serialize to GeoJSON
        String geoJson = MissionSerializer.serializeBehaviourToGeoJSON(behaviour);

        // Then: Valid JSON structure
        assertNotNull(geoJson);
        assertTrue(geoJson.contains("\"type\": \"Feature\"") || geoJson.contains("\"type\":\"Feature\""));
        assertTrue(geoJson.contains("\"type\": \"Point\""));
        assertTrue(geoJson.contains("\"behaviourType\": \"ReturnToBase\""));
        assertTrue(geoJson.contains("\"platformSpeed\": 8") || geoJson.contains("\"platformSpeed\":8"));
    }

    @Test
    void serializeMission_producesFeatureCollection() {
        // Given: Mission with multiple behaviours
        CompositeBehaviour mission = new CompositeBehaviour();

        Polygon searchArea = new Polygon(Arrays.asList(
            Position.of(50.6, -2.4),
            Position.of(50.61, -2.4),
            Position.of(50.61, -2.39),
            Position.of(50.6, -2.39)
        ));
        mission.addBehaviour(new ParallelTrackSearch(searchArea, 45.0, 100.0, 5.0));
        mission.addBehaviour(new ReturnToBase(Position.of(50.6, -2.4), 8.0));

        // When: Serialize to GeoJSON FeatureCollection
        String geoJson = MissionSerializer.serializeToGeoJSON(mission);

        // Then: Valid FeatureCollection structure
        assertNotNull(geoJson);
        assertTrue(geoJson.contains("\"type\": \"FeatureCollection\""));
        assertTrue(geoJson.contains("\"features\"") || geoJson.contains("\"features\":"));
        assertTrue(geoJson.contains("\"behaviourType\": \"ParallelTrackSearch\""));
        assertTrue(geoJson.contains("\"behaviourType\": \"ReturnToBase\""));
    }

    @Test
    void deserializeParallelTrackSearch_reconstructsBehaviour() {
        // Given: GeoJSON for parallel track search
        String geoJson = """
            {
                "type": "Feature",
                "geometry": {
                    "type": "Polygon",
                    "coordinates": [[
                        [-2.4, 50.6],
                        [-2.4, 50.61],
                        [-2.39, 50.61],
                        [-2.39, 50.6],
                        [-2.4, 50.6]
                    ]]
                },
                "properties": {
                    "behaviourType": "ParallelTrackSearch",
                    "trackOrientation": 45.0,
                    "trackSpacing": 100.0,
                    "platformSpeed": 5.0
                }
            }
            """;

        // When: Deserialize
        ParallelTrackSearch behaviour = (ParallelTrackSearch) MissionSerializer.deserializeBehaviourFromGeoJSON(geoJson);

        // Then: Behaviour reconstructed correctly
        assertNotNull(behaviour);
        assertEquals("Parallel Track Search", behaviour.getName());
        assertTrue(behaviour.getDescription().contains("45°"));
        assertTrue(behaviour.getDescription().contains("100m"));
    }

    @Test
    void deserializeExpandingSquareSearch_reconstructsBehaviour() {
        // Given: GeoJSON for expanding square search
        String geoJson = """
            {
                "type": "Feature",
                "geometry": {
                    "type": "Polygon",
                    "coordinates": [[
                        [-2.4, 50.6],
                        [-2.4, 50.61],
                        [-2.39, 50.61],
                        [-2.39, 50.6],
                        [-2.4, 50.6]
                    ]]
                },
                "properties": {
                    "behaviourType": "ExpandingSquareSearch",
                    "initialDirection": 90.0,
                    "legIncrement": 50.0,
                    "platformSpeed": 5.0
                }
            }
            """;

        // When: Deserialize
        ExpandingSquareSearch behaviour = (ExpandingSquareSearch) MissionSerializer.deserializeBehaviourFromGeoJSON(geoJson);

        // Then: Behaviour reconstructed correctly
        assertNotNull(behaviour);
        assertEquals("Expanding Square Search", behaviour.getName());
        assertTrue(behaviour.getDescription().contains("90°"));
        assertTrue(behaviour.getDescription().contains("50m"));
    }

    @Test
    void deserializeWaypointTransit_reconstructsBehaviour() {
        // Given: GeoJSON for waypoint transit
        String geoJson = """
            {
                "type": "Feature",
                "geometry": {
                    "type": "LineString",
                    "coordinates": [
                        [-2.4, 50.6],
                        [-2.39, 50.61],
                        [-2.38, 50.62]
                    ]
                },
                "properties": {
                    "behaviourType": "WaypointTransit",
                    "platformSpeed": 6.0
                }
            }
            """;

        // When: Deserialize
        WaypointTransit behaviour = (WaypointTransit) MissionSerializer.deserializeBehaviourFromGeoJSON(geoJson);

        // Then: Behaviour reconstructed correctly
        assertNotNull(behaviour);
        assertEquals("Waypoint Transit", behaviour.getName());
        assertTrue(behaviour.getDescription().contains("3 waypoints"));
    }

    @Test
    void deserializeReturnToBase_reconstructsBehaviour() {
        // Given: GeoJSON for return to base
        String geoJson = """
            {
                "type": "Feature",
                "geometry": {
                    "type": "Point",
                    "coordinates": [-2.4, 50.6]
                },
                "properties": {
                    "behaviourType": "ReturnToBase",
                    "platformSpeed": 8.0
                }
            }
            """;

        // When: Deserialize
        ReturnToBase behaviour = (ReturnToBase) MissionSerializer.deserializeBehaviourFromGeoJSON(geoJson);

        // Then: Behaviour reconstructed correctly
        assertNotNull(behaviour);
        assertEquals("Return to Base", behaviour.getName());
    }

    @Test
    void deserializeMission_reconstructsAllBehaviours() {
        // Given: GeoJSON FeatureCollection with multiple behaviours
        String geoJson = """
            {
                "type": "FeatureCollection",
                "features": [
                    {
                        "type": "Feature",
                        "geometry": {
                            "type": "Polygon",
                            "coordinates": [[
                                [-2.4, 50.6],
                                [-2.4, 50.61],
                                [-2.39, 50.61],
                                [-2.39, 50.6],
                                [-2.4, 50.6]
                            ]]
                        },
                        "properties": {
                            "behaviourType": "ParallelTrackSearch",
                            "trackOrientation": 45.0,
                            "trackSpacing": 100.0,
                            "platformSpeed": 5.0
                        }
                    },
                    {
                        "type": "Feature",
                        "geometry": {
                            "type": "Point",
                            "coordinates": [-2.4, 50.6]
                        },
                        "properties": {
                            "behaviourType": "ReturnToBase",
                            "platformSpeed": 8.0
                        }
                    }
                ]
            }
            """;

        // When: Deserialize
        CompositeBehaviour mission = MissionSerializer.deserializeFromGeoJSON(geoJson);

        // Then: All behaviours reconstructed in correct order
        assertNotNull(mission);
        assertEquals(2, mission.getBehaviours().size());
        assertEquals("Parallel Track Search", mission.getBehaviours().get(0).getName());
        assertEquals("Return to Base", mission.getBehaviours().get(1).getName());
    }

    @Test
    void deserializeInvalidJSON_throwsException() {
        // Given: Invalid JSON
        String invalidJson = "{ this is not valid json }";

        // When/Then: Exception thrown
        assertThrows(Exception.class, () -> {
            MissionSerializer.deserializeFromGeoJSON(invalidJson);
        });
    }

    @Test
    void deserializeMissingBehaviourType_throwsException() {
        // Given: GeoJSON without behaviourType property
        String geoJson = """
            {
                "type": "Feature",
                "geometry": {
                    "type": "Point",
                    "coordinates": [-2.4, 50.6]
                },
                "properties": {
                    "platformSpeed": 8.0
                }
            }
            """;

        // When/Then: Exception thrown
        assertThrows(Exception.class, () -> {
            MissionSerializer.deserializeBehaviourFromGeoJSON(geoJson);
        });
    }

    @Test
    void roundTripSerialization_preservesData() {
        // Given: Mission with multiple behaviours
        CompositeBehaviour original = new CompositeBehaviour();

        Polygon searchArea = new Polygon(Arrays.asList(
            Position.of(50.6, -2.4),
            Position.of(50.61, -2.4),
            Position.of(50.61, -2.39),
            Position.of(50.6, -2.39)
        ));
        original.addBehaviour(new ParallelTrackSearch(searchArea, 45.0, 100.0, 5.0));
        original.addBehaviour(new ExpandingSquareSearch(searchArea, 90.0, 50.0, 5.0));
        original.addBehaviour(new WaypointTransit(Arrays.asList(
            Position.of(50.6, -2.4),
            Position.of(50.61, -2.39)
        ), 6.0));
        original.addBehaviour(new ReturnToBase(Position.of(50.6, -2.4), 8.0));

        // When: Serialize and deserialize
        String geoJson = MissionSerializer.serializeToGeoJSON(original);
        CompositeBehaviour restored = MissionSerializer.deserializeFromGeoJSON(geoJson);

        // Then: Behaviours match
        assertEquals(original.getBehaviours().size(), restored.getBehaviours().size());
        for (int i = 0; i < original.getBehaviours().size(); i++) {
            assertEquals(
                original.getBehaviours().get(i).getName(),
                restored.getBehaviours().get(i).getName()
            );
        }
    }
}
