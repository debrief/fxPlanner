package com.planetmayo.usvsim.api.controller;

import com.planetmayo.usvsim.api.service.MissionSerializationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for mission serialization (save/load).
 *
 * Endpoints:
 * - POST /api/mission/serialize - Convert mission to GeoJSON
 * - POST /api/mission/deserialize - Convert GeoJSON to mission
 */
@RestController
@RequestMapping("/api/mission")
public class MissionController {

    private final MissionSerializationService missionSerializationService;

    @Autowired
    public MissionController(MissionSerializationService missionSerializationService) {
        this.missionSerializationService = missionSerializationService;
    }

    /**
     * Serialize mission to GeoJSON format.
     *
     * @param mission mission object to serialize
     * @return GeoJSON string
     */
    @PostMapping(value = "/serialize", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> serialize(@RequestBody Object mission) {
        String geoJSON = missionSerializationService.serializeToGeoJSON(mission);
        return ResponseEntity.ok(geoJSON);
    }

    /**
     * Deserialize mission from GeoJSON format.
     *
     * @param geoJSON GeoJSON string to deserialize
     * @return mission object
     */
    @PostMapping(value = "/deserialize", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> deserialize(@RequestBody String geoJSON) {
        Object mission = missionSerializationService.deserializeFromGeoJSON(geoJSON);
        return ResponseEntity.ok(mission);
    }
}
