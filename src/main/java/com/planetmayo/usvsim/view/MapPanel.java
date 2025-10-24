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

    public MapPanel() {
        // Main map container - placeholder for java_leaflet MapView
        mapContainer = new Pane();
        mapContainer.setStyle("-fx-background-color: #E8F4F8; -fx-border-color: #999;");
        mapContainer.setPrefSize(800, 600);

        // Control panel with pan/zoom buttons
        controlPanel = createControlPanel();

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
     * Update USV position marker
     */
    public void updatePlatformPosition(Position position, double heading) {
        // TODO: Implement position marker with heading-oriented icon
        System.out.println("Updating position: " + position + ", heading: " + heading);
    }

    /**
     * Add point to track history
     */
    public void addTrackPoint(Position position) {
        // TODO: Implement track history polyline
        System.out.println("Adding track point: " + position);
    }

    /**
     * Clear all overlays (polygons, tracks, position)
     */
    public void clearOverlays() {
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
