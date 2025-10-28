package com.planetmayo.usvsim.e2e;

import com.planetmayo.usvsim.model.behaviour.Behaviour;
import com.planetmayo.usvsim.model.behaviour.ParallelTrackSearch;
import com.planetmayo.usvsim.model.behaviour.ExpandingSquareSearch;
import com.planetmayo.usvsim.model.behaviour.ReturnToBase;
import com.planetmayo.usvsim.model.geometry.Polygon;
import com.planetmayo.usvsim.model.geometry.Position;
import com.planetmayo.usvsim.model.mission.CompositeBehaviour;
import com.planetmayo.usvsim.util.MissionSerializer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * E2E test for mission save/load functionality (US7).
 *
 * Tests complete workflow: create mission → save to file → load from file → verify.
 */
class MissionPersistenceE2ETest {

    @TempDir
    Path tempDir;

    @Test
    void saveMission_thenLoadMission_restoresAllBehaviours() throws Exception {
        // Given: Mission with multiple behaviours
        CompositeBehaviour originalMission = new CompositeBehaviour();

        Polygon searchArea = new Polygon(Arrays.asList(
            Position.of(50.6, -2.4),
            Position.of(50.61, -2.4),
            Position.of(50.61, -2.39),
            Position.of(50.6, -2.39)
        ));

        ParallelTrackSearch pts = new ParallelTrackSearch(searchArea, 45.0, 100.0, 5.0);
        ExpandingSquareSearch ess = new ExpandingSquareSearch(searchArea, 90.0, 50.0, 5.0);
        ReturnToBase rtb = new ReturnToBase(Position.of(50.6, -2.4), 8.0);

        originalMission.addBehaviour(pts);
        originalMission.addBehaviour(ess);
        originalMission.addBehaviour(rtb);

        // When: Save mission to file
        File missionFile = tempDir.resolve("test-mission.geojson").toFile();
        String geoJson = MissionSerializer.serializeToGeoJSON(originalMission);
        Files.writeString(missionFile.toPath(), geoJson);

        // Then: File exists and is not empty
        assertTrue(missionFile.exists(), "Mission file should be created");
        assertTrue(missionFile.length() > 0, "Mission file should not be empty");

        // When: Load mission from file
        String loadedGeoJson = Files.readString(missionFile.toPath());
        CompositeBehaviour loadedMission = MissionSerializer.deserializeFromGeoJSON(loadedGeoJson);

        // Then: All behaviours restored correctly
        assertEquals(3, loadedMission.getBehaviours().size(), "Should load all 3 behaviours");

        // Verify Parallel Track Search
        Behaviour behaviour1 = loadedMission.getBehaviours().get(0);
        assertTrue(behaviour1 instanceof ParallelTrackSearch, "First behaviour should be ParallelTrackSearch");
        ParallelTrackSearch loadedPts = (ParallelTrackSearch) behaviour1;
        assertEquals(45.0, loadedPts.getTrackOrientation(), 0.01);
        assertEquals(100.0, loadedPts.getTrackSpacing(), 0.01);
        assertEquals(5.0, loadedPts.getPlatformSpeed(), 0.01);

        // Verify Expanding Square Search
        Behaviour behaviour2 = loadedMission.getBehaviours().get(1);
        assertTrue(behaviour2 instanceof ExpandingSquareSearch, "Second behaviour should be ExpandingSquareSearch");
        ExpandingSquareSearch loadedEss = (ExpandingSquareSearch) behaviour2;
        assertEquals(90.0, loadedEss.getInitialDirection(), 0.01);
        assertEquals(50.0, loadedEss.getLegIncrement(), 0.01);
        assertEquals(5.0, loadedEss.getPlatformSpeed(), 0.01);

        // Verify Return to Base
        Behaviour behaviour3 = loadedMission.getBehaviours().get(2);
        assertTrue(behaviour3 instanceof ReturnToBase, "Third behaviour should be ReturnToBase");
        ReturnToBase loadedRtb = (ReturnToBase) behaviour3;
        assertEquals(8.0, loadedRtb.getPlatformSpeed(), 0.01);
    }

    @Test
    void saveEmptyMission_shouldProduceValidGeoJSON() throws Exception {
        // Given: Empty mission
        CompositeBehaviour emptyMission = new CompositeBehaviour();

        // When: Save to file
        File missionFile = tempDir.resolve("empty-mission.geojson").toFile();
        String geoJson = MissionSerializer.serializeToGeoJSON(emptyMission);
        Files.writeString(missionFile.toPath(), geoJson);

        // Then: File contains valid empty FeatureCollection
        assertTrue(missionFile.exists());
        String content = Files.readString(missionFile.toPath());
        assertTrue(content.contains("\"type\": \"FeatureCollection\""));
        assertTrue(content.contains("\"features\""));

        // When: Load empty mission
        CompositeBehaviour loadedMission = MissionSerializer.deserializeFromGeoJSON(content);

        // Then: No behaviours loaded
        assertEquals(0, loadedMission.getBehaviours().size());
    }

    @Test
    void loadInvalidJSON_throwsException() {
        // Given: Invalid JSON content
        String invalidJson = "{ this is not valid json }";

        // When/Then: Loading should throw exception
        assertThrows(Exception.class, () -> {
            MissionSerializer.deserializeFromGeoJSON(invalidJson);
        });
    }

    @Test
    void loadMissingBehaviourType_throwsException() {
        // Given: GeoJSON without behaviourType
        String geoJsonWithoutType = """
            {
                "type": "FeatureCollection",
                "features": [
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
                ]
            }
            """;

        // When/Then: Loading should throw exception
        assertThrows(Exception.class, () -> {
            MissionSerializer.deserializeFromGeoJSON(geoJsonWithoutType);
        });
    }

    @Test
    void roundTripPersistence_preservesAllData() throws Exception {
        // Given: Complex mission with all behaviour types
        CompositeBehaviour originalMission = new CompositeBehaviour();

        Polygon searchArea1 = new Polygon(Arrays.asList(
            Position.of(50.6, -2.4),
            Position.of(50.61, -2.4),
            Position.of(50.61, -2.39),
            Position.of(50.6, -2.39)
        ));

        Polygon searchArea2 = new Polygon(Arrays.asList(
            Position.of(50.62, -2.38),
            Position.of(50.63, -2.38),
            Position.of(50.63, -2.37),
            Position.of(50.62, -2.37)
        ));

        originalMission.addBehaviour(new ParallelTrackSearch(searchArea1, 0.0, 50.0, 4.0));
        originalMission.addBehaviour(new ExpandingSquareSearch(searchArea2, 180.0, 75.0, 6.0));
        originalMission.addBehaviour(new ParallelTrackSearch(searchArea1, 90.0, 200.0, 7.0));
        originalMission.addBehaviour(new ReturnToBase(Position.of(50.6, -2.4), 8.0));

        // When: Save and reload multiple times
        File missionFile = tempDir.resolve("roundtrip-mission.geojson").toFile();

        // First save
        String geoJson1 = MissionSerializer.serializeToGeoJSON(originalMission);
        Files.writeString(missionFile.toPath(), geoJson1);

        // First load
        String loadedJson1 = Files.readString(missionFile.toPath());
        CompositeBehaviour loadedMission1 = MissionSerializer.deserializeFromGeoJSON(loadedJson1);

        // Second save (from loaded mission)
        String geoJson2 = MissionSerializer.serializeToGeoJSON(loadedMission1);
        Files.writeString(missionFile.toPath(), geoJson2);

        // Second load
        String loadedJson2 = Files.readString(missionFile.toPath());
        CompositeBehaviour loadedMission2 = MissionSerializer.deserializeFromGeoJSON(loadedJson2);

        // Then: All data preserved through multiple round trips
        assertEquals(originalMission.getBehaviours().size(), loadedMission1.getBehaviours().size());
        assertEquals(originalMission.getBehaviours().size(), loadedMission2.getBehaviours().size());

        // Verify behaviour types match
        for (int i = 0; i < originalMission.getBehaviours().size(); i++) {
            String originalType = originalMission.getBehaviours().get(i).getClass().getSimpleName();
            String loadedType1 = loadedMission1.getBehaviours().get(i).getClass().getSimpleName();
            String loadedType2 = loadedMission2.getBehaviours().get(i).getClass().getSimpleName();

            assertEquals(originalType, loadedType1, "First load should match original type at index " + i);
            assertEquals(originalType, loadedType2, "Second load should match original type at index " + i);
        }
    }

    @Test
    void loadedMission_hasCorrectWaypoints() throws Exception {
        // Given: Mission with parallel track search
        CompositeBehaviour originalMission = new CompositeBehaviour();

        Polygon searchArea = new Polygon(Arrays.asList(
            Position.of(50.6, -2.4),
            Position.of(50.61, -2.4),
            Position.of(50.61, -2.39),
            Position.of(50.6, -2.39)
        ));

        ParallelTrackSearch original = new ParallelTrackSearch(searchArea, 45.0, 100.0, 5.0);
        int originalWaypointCount = original.getWaypoints().size();
        originalMission.addBehaviour(original);

        // When: Save and load
        File missionFile = tempDir.resolve("waypoint-test.geojson").toFile();
        String geoJson = MissionSerializer.serializeToGeoJSON(originalMission);
        Files.writeString(missionFile.toPath(), geoJson);

        String loadedJson = Files.readString(missionFile.toPath());
        CompositeBehaviour loadedMission = MissionSerializer.deserializeFromGeoJSON(loadedJson);

        // Then: Waypoints are recalculated and match original count
        ParallelTrackSearch loaded = (ParallelTrackSearch) loadedMission.getBehaviours().get(0);
        assertEquals(originalWaypointCount, loaded.getWaypoints().size(),
            "Loaded behaviour should have same number of waypoints (recalculated from polygon)");
        assertTrue(loaded.getWaypoints().size() > 0, "Loaded behaviour should have waypoints");
    }
}
