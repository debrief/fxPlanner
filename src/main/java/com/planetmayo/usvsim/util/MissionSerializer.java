package com.planetmayo.usvsim.util;

import com.google.gson.*;
import com.planetmayo.usvsim.model.behaviour.*;
import com.planetmayo.usvsim.model.geometry.Polygon;
import com.planetmayo.usvsim.model.geometry.Position;
import com.planetmayo.usvsim.model.mission.CompositeBehaviour;

import java.util.ArrayList;
import java.util.List;

/**
 * Serializes and deserializes Mission objects to/from GeoJSON format.
 *
 * <p>GeoJSON Structure:
 * - Mission → FeatureCollection
 * - Each Behaviour → Feature
 * - Polygon-based behaviours: geometry = Polygon, properties = params
 * - Waypoint-based behaviours: geometry = LineString/Point, properties = params
 *
 * <p>Routes are recalculated on load, not persisted.
 */
public class MissionSerializer {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Serializes a CompositeBehaviour (mission plan) to GeoJSON FeatureCollection.
     *
     * @param mission the mission plan to serialize
     * @return GeoJSON string
     */
    public static String serializeToGeoJSON(CompositeBehaviour mission) {
        JsonObject featureCollection = new JsonObject();
        featureCollection.addProperty("type", "FeatureCollection");

        JsonArray features = new JsonArray();
        for (var behaviour : mission.getBehaviours()) {
            String featureJson = serializeBehaviourToGeoJSON(behaviour);
            features.add(JsonParser.parseString(featureJson));
        }
        featureCollection.add("features", features);

        return gson.toJson(featureCollection);
    }

    /**
     * Serializes a single Behaviour to GeoJSON Feature.
     *
     * @param behaviour the behaviour to serialize
     * @return GeoJSON Feature string
     */
    public static String serializeBehaviourToGeoJSON(Behaviour behaviour) {
        JsonObject feature = new JsonObject();
        feature.addProperty("type", "Feature");

        // Determine behaviour type and serialize accordingly
        if (behaviour instanceof ParallelTrackSearch pts) {
            feature.add("geometry", serializePolygon(pts.getSearchArea()));
            feature.add("properties", createParallelTrackSearchProperties(pts));

        } else if (behaviour instanceof ExpandingSquareSearch ess) {
            feature.add("geometry", serializePolygon(ess.getSearchArea()));
            feature.add("properties", createExpandingSquareSearchProperties(ess));

        } else if (behaviour instanceof WaypointTransit wt) {
            feature.add("geometry", serializeWaypoints(wt.getUserWaypoints()));
            feature.add("properties", createWaypointTransitProperties(wt));

        } else if (behaviour instanceof ReturnToBase rtb) {
            feature.add("geometry", serializePoint(rtb.getBaseLocation()));
            feature.add("properties", createReturnToBaseProperties(rtb));

        } else {
            throw new IllegalArgumentException("Unsupported behaviour type: " + behaviour.getClass().getName());
        }

        return gson.toJson(feature);
    }

    /**
     * Deserializes a GeoJSON FeatureCollection to CompositeBehaviour.
     *
     * @param geoJson GeoJSON FeatureCollection string
     * @return CompositeBehaviour with all behaviours restored
     */
    public static CompositeBehaviour deserializeFromGeoJSON(String geoJson) {
        CompositeBehaviour mission = new CompositeBehaviour();

        JsonObject root = JsonParser.parseString(geoJson).getAsJsonObject();
        if (!root.has("type") || !root.get("type").getAsString().equals("FeatureCollection")) {
            throw new IllegalArgumentException("Expected FeatureCollection, got: " + root.get("type"));
        }

        JsonArray features = root.getAsJsonArray("features");
        for (JsonElement featureElement : features) {
            Behaviour behaviour = deserializeBehaviourFromGeoJSON(featureElement.toString());
            mission.addBehaviour(behaviour);
        }

        return mission;
    }

    /**
     * Deserializes a single GeoJSON Feature to Behaviour.
     *
     * @param featureJson GeoJSON Feature string
     * @return reconstructed Behaviour
     */
    public static Behaviour deserializeBehaviourFromGeoJSON(String featureJson) {
        JsonObject feature = JsonParser.parseString(featureJson).getAsJsonObject();

        if (!feature.has("properties") || !feature.getAsJsonObject("properties").has("behaviourType")) {
            throw new IllegalArgumentException("Missing behaviourType in properties");
        }

        String behaviourType = feature.getAsJsonObject("properties").get("behaviourType").getAsString();
        JsonObject geometry = feature.getAsJsonObject("geometry");
        JsonObject properties = feature.getAsJsonObject("properties");

        return switch (behaviourType) {
            case "ParallelTrackSearch" -> deserializeParallelTrackSearch(geometry, properties);
            case "ExpandingSquareSearch" -> deserializeExpandingSquareSearch(geometry, properties);
            case "WaypointTransit" -> deserializeWaypointTransit(geometry, properties);
            case "ReturnToBase" -> deserializeReturnToBase(geometry, properties);
            default -> throw new IllegalArgumentException("Unknown behaviour type: " + behaviourType);
        };
    }

    // ========== Serialization Helpers ==========

    private static JsonObject serializePolygon(Polygon polygon) {
        JsonObject geometry = new JsonObject();
        geometry.addProperty("type", "Polygon");

        JsonArray coordinates = new JsonArray();
        JsonArray ring = new JsonArray();

        for (Position vertex : polygon.getVertices()) {
            JsonArray coord = new JsonArray();
            coord.add(vertex.getLongitude());
            coord.add(vertex.getLatitude());
            ring.add(coord);
        }

        // Close the ring
        Position first = polygon.getVertices().get(0);
        JsonArray closingCoord = new JsonArray();
        closingCoord.add(first.getLongitude());
        closingCoord.add(first.getLatitude());
        ring.add(closingCoord);

        coordinates.add(ring);
        geometry.add("coordinates", coordinates);

        return geometry;
    }

    private static JsonObject serializeWaypoints(List<Position> waypoints) {
        JsonObject geometry = new JsonObject();
        geometry.addProperty("type", "LineString");

        JsonArray coordinates = new JsonArray();
        for (Position pos : waypoints) {
            JsonArray coord = new JsonArray();
            coord.add(pos.getLongitude());
            coord.add(pos.getLatitude());
            coordinates.add(coord);
        }

        geometry.add("coordinates", coordinates);
        return geometry;
    }

    private static JsonObject serializePoint(Position position) {
        JsonObject geometry = new JsonObject();
        geometry.addProperty("type", "Point");

        JsonArray coordinates = new JsonArray();
        coordinates.add(position.getLongitude());
        coordinates.add(position.getLatitude());

        geometry.add("coordinates", coordinates);
        return geometry;
    }

    private static JsonObject createParallelTrackSearchProperties(ParallelTrackSearch pts) {
        JsonObject props = new JsonObject();
        props.addProperty("behaviourType", "ParallelTrackSearch");
        props.addProperty("trackOrientation", pts.getTrackOrientation());
        props.addProperty("trackSpacing", pts.getTrackSpacing());
        props.addProperty("platformSpeed", pts.getPlatformSpeed());
        return props;
    }

    private static JsonObject createExpandingSquareSearchProperties(ExpandingSquareSearch ess) {
        JsonObject props = new JsonObject();
        props.addProperty("behaviourType", "ExpandingSquareSearch");
        props.addProperty("initialDirection", ess.getInitialDirection());
        props.addProperty("legIncrement", ess.getLegIncrement());
        props.addProperty("platformSpeed", ess.getPlatformSpeed());
        return props;
    }

    private static JsonObject createWaypointTransitProperties(WaypointTransit wt) {
        JsonObject props = new JsonObject();
        props.addProperty("behaviourType", "WaypointTransit");
        props.addProperty("platformSpeed", wt.getPlatformSpeed());
        return props;
    }

    private static JsonObject createReturnToBaseProperties(ReturnToBase rtb) {
        JsonObject props = new JsonObject();
        props.addProperty("behaviourType", "ReturnToBase");
        props.addProperty("platformSpeed", rtb.getPlatformSpeed());
        return props;
    }

    // ========== Deserialization Helpers ==========

    private static Polygon deserializePolygon(JsonObject geometry) {
        JsonArray coordinates = geometry.getAsJsonArray("coordinates").get(0).getAsJsonArray();
        List<Position> vertices = new ArrayList<>();

        // Skip the last coordinate (closing point)
        for (int i = 0; i < coordinates.size() - 1; i++) {
            JsonArray coord = coordinates.get(i).getAsJsonArray();
            double lon = coord.get(0).getAsDouble();
            double lat = coord.get(1).getAsDouble();
            vertices.add(Position.of(lat, lon));
        }

        return new Polygon(vertices);
    }

    private static List<Position> deserializeLineString(JsonObject geometry) {
        JsonArray coordinates = geometry.getAsJsonArray("coordinates");
        List<Position> waypoints = new ArrayList<>();

        for (JsonElement coordElement : coordinates) {
            JsonArray coord = coordElement.getAsJsonArray();
            double lon = coord.get(0).getAsDouble();
            double lat = coord.get(1).getAsDouble();
            waypoints.add(Position.of(lat, lon));
        }

        return waypoints;
    }

    private static Position deserializePoint(JsonObject geometry) {
        JsonArray coordinates = geometry.getAsJsonArray("coordinates");
        double lon = coordinates.get(0).getAsDouble();
        double lat = coordinates.get(1).getAsDouble();
        return Position.of(lat, lon);
    }

    private static ParallelTrackSearch deserializeParallelTrackSearch(JsonObject geometry, JsonObject properties) {
        Polygon searchArea = deserializePolygon(geometry);
        double trackOrientation = properties.get("trackOrientation").getAsDouble();
        double trackSpacing = properties.get("trackSpacing").getAsDouble();
        double platformSpeed = properties.get("platformSpeed").getAsDouble();

        return new ParallelTrackSearch(searchArea, trackOrientation, trackSpacing, platformSpeed);
    }

    private static ExpandingSquareSearch deserializeExpandingSquareSearch(JsonObject geometry, JsonObject properties) {
        Polygon searchArea = deserializePolygon(geometry);
        double initialDirection = properties.get("initialDirection").getAsDouble();
        double legIncrement = properties.get("legIncrement").getAsDouble();
        double platformSpeed = properties.get("platformSpeed").getAsDouble();

        return new ExpandingSquareSearch(searchArea, initialDirection, legIncrement, platformSpeed);
    }

    private static WaypointTransit deserializeWaypointTransit(JsonObject geometry, JsonObject properties) {
        List<Position> waypoints = deserializeLineString(geometry);
        double platformSpeed = properties.get("platformSpeed").getAsDouble();

        return new WaypointTransit(waypoints, platformSpeed);
    }

    private static ReturnToBase deserializeReturnToBase(JsonObject geometry, JsonObject properties) {
        Position basePosition = deserializePoint(geometry);
        double platformSpeed = properties.get("platformSpeed").getAsDouble();

        return new ReturnToBase(basePosition, platformSpeed);
    }
}
