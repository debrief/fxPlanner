package com.planetmayo.usvsim.view;

import com.planetmayo.usvsim.model.geometry.Position;
import com.planetmayo.usvsim.model.geometry.Polygon;
import com.planetmayo.usvsim.model.geometry.Waypoint;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polyline;
import javafx.scene.transform.Rotate;
import javafx.collections.FXCollections;

import java.util.ArrayList;
import java.util.List;

/**
 * Map display panel showing Portland Harbour with search areas and track patterns.
 *
 * Features:
 * - Offline map tiles (java_leaflet MapView)
 * - Pan/zoom controls
 * - Polygon rendering (search areas)
 * - Track pattern display (parallel tracks, expanding squares)
 * - USV icon and track history (when simulating)
 */
public class MapPanel extends BorderPane {
    private final Pane mapContainer;
    private final HBox controlPanel;
    private Circle usvMarker;
    private Polyline trackHistory;
    private List<Position> trackPoints;
    private static final int MAX_TRACK_POINTS = 1000;

    public MapPanel() {
        // Main map container - placeholder for java_leaflet MapView
        mapContainer = new Pane();
        mapContainer.setStyle("-fx-background-color: #E8F4F8; -fx-border-color: #999;");
        mapContainer.setPrefSize(800, 600);

        // Control panel with pan/zoom buttons
        controlPanel = createControlPanel();

        // Initialize track history tracking
        trackPoints = new ArrayList<>();
        trackHistory = new Polyline();
        trackHistory.setStroke(Color.BLUE);
        trackHistory.setStrokeWidth(1);
        trackHistory.setOpacity(0.6);
        mapContainer.getChildren().add(trackHistory);

        // Initialize USV marker
        usvMarker = new Circle(8);
        usvMarker.setFill(Color.RED);
        usvMarker.setStroke(Color.DARKRED);
        usvMarker.setStrokeWidth(2);
        mapContainer.getChildren().add(usvMarker);

        // Layout
        setCenter(mapContainer);
        setBottom(controlPanel);
        setPadding(new Insets(5));

        // Placeholder message
        javafx.scene.control.Label placeholder = new javafx.scene.control.Label(
            "Map View: Portland Harbour\n(java_leaflet MapView will be integrated here)"
        );
        placeholder.setStyle("-fx-font-size: 12; -fx-text-fill: #666;");
        mapContainer.getChildren().add(placeholder);
    }

    /**
     * Create pan/zoom control buttons
     */
    private HBox createControlPanel() {
        HBox box = new HBox(10);
        box.setStyle("-fx-background-color: #F5F5F5; -fx-padding: 8;");
        box.setPrefHeight(50);

        Button zoomIn = new Button("+");
        zoomIn.setPrefWidth(50);
        zoomIn.setOnAction(e -> handleZoomIn());

        Button zoomOut = new Button("-");
        zoomOut.setPrefWidth(50);
        zoomOut.setOnAction(e -> handleZoomOut());

        Button resetView = new Button("Reset View");
        resetView.setPrefWidth(100);
        resetView.setOnAction(e -> handleResetView());

        box.getChildren().addAll(zoomIn, zoomOut, resetView);
        return box;
    }

    /**
     * Render a polygon on the map (search area)
     */
    public void renderPolygon(Polygon polygon) {
        // TODO: Implement polygon rendering with java_leaflet
        System.out.println("Rendering polygon: " + polygon);
    }

    /**
     * Render track waypoints as polyline
     */
    public void renderTracks(List<Waypoint> waypoints) {
        // TODO: Implement track rendering with java_leaflet
        System.out.println("Rendering " + waypoints.size() + " waypoints");
    }

    /**
     * Update USV position marker with heading orientation.
     * Position is scaled to fit within the map container (pixel coordinates).
     */
    public void updatePlatformPosition(Position position, double heading) {
        if (position == null) return;

        // Scale geographic position to pixel coordinates within map container
        // Portland Harbour approx: 50.6°N, 2.4°W
        // For this mock implementation, use simple linear mapping
        double mapWidth = mapContainer.getWidth() > 0 ? mapContainer.getWidth() : 600;
        double mapHeight = mapContainer.getHeight() > 0 ? mapContainer.getHeight() : 400;

        // Simple linear mapping: (50.6°N±0.01) maps to height, (2.4°W±0.01) maps to width
        double centerLat = 50.6;
        double centerLon = -2.4;
        double latSpan = 0.02; // ±0.01 degrees
        double lonSpan = 0.02; // ±0.01 degrees

        double pixelX = mapWidth * 0.5 + (position.getLongitude() - centerLon) / lonSpan * (mapWidth * 0.4);
        double pixelY = mapHeight * 0.5 - (position.getLatitude() - centerLat) / latSpan * (mapHeight * 0.4);

        // Constrain to container
        pixelX = Math.max(8, Math.min(mapWidth - 8, pixelX));
        pixelY = Math.max(8, Math.min(mapHeight - 8, pixelY));

        // Update marker position and rotation
        usvMarker.setCenterX(pixelX);
        usvMarker.setCenterY(pixelY);

        // Apply heading rotation (0° = North, 90° = East)
        usvMarker.getTransforms().clear();
        usvMarker.getTransforms().add(new Rotate(heading, pixelX, pixelY));

        System.out.println("Updating position: " + position + ", heading: " + heading);
    }

    /**
     * Add point to track history polyline.
     * Maintains a maximum of 1000 points to prevent memory growth.
     */
    public void addTrackPoint(Position position) {
        if (position == null) return;

        trackPoints.add(position);

        // Limit track history size
        if (trackPoints.size() > MAX_TRACK_POINTS) {
            trackPoints.remove(0);
        }

        // Rebuild polyline points array from tracked positions
        double mapWidth = mapContainer.getWidth() > 0 ? mapContainer.getWidth() : 600;
        double mapHeight = mapContainer.getHeight() > 0 ? mapContainer.getHeight() : 400;

        double centerLat = 50.6;
        double centerLon = -2.4;
        double latSpan = 0.02;
        double lonSpan = 0.02;

        List<Double> polylinePoints = new ArrayList<>();
        for (Position p : trackPoints) {
            double px = mapWidth * 0.5 + (p.getLongitude() - centerLon) / lonSpan * (mapWidth * 0.4);
            double py = mapHeight * 0.5 - (p.getLatitude() - centerLat) / latSpan * (mapHeight * 0.4);
            polylinePoints.add(px);
            polylinePoints.add(py);
        }

        trackHistory.getPoints().setAll(polylinePoints);
        System.out.println("Adding track point: " + position + " (total: " + trackPoints.size() + ")");
    }

    /**
     * Clear all overlays (polygons, tracks, position)
     */
    public void clearOverlays() {
        trackPoints.clear();
        trackHistory.getPoints().clear();
        usvMarker.setCenterX(-100);
        usvMarker.setCenterY(-100);
        System.out.println("Clearing map overlays");
    }

    private void handleZoomIn() {
        System.out.println("Zoom in");
    }

    private void handleZoomOut() {
        System.out.println("Zoom out");
    }

    private void handleResetView() {
        System.out.println("Resetting view to Portland Harbour");
    }

    /**
     * Get the underlying map container for java_leaflet integration
     */
    public Pane getMapContainer() {
        return mapContainer;
    }
}
