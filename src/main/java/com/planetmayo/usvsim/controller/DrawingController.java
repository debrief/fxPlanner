package com.planetmayo.usvsim.controller;

import com.planetmayo.usvsim.model.geometry.Polygon;
import com.planetmayo.usvsim.model.geometry.Position;
import com.planetmayo.usvsim.view.MapPanel;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Handles polygon drawing on the map.
 *
 * Drawing modes:
 * - DISABLED: No drawing
 * - POLYGON: Collecting vertices for search area polygon
 * - WAYPOINT: Collecting waypoints for waypoint transit
 *
 * Usage:
 * 1. Create DrawingController with MapPanel
 * 2. Call startDrawingPolygon() or startDrawingWaypoints()
 * 3. User clicks to add vertices
 * 4. Call finishDrawing() to complete
 */
public class DrawingController {
    public enum DrawingMode {
        DISABLED, POLYGON, WAYPOINT
    }

    private final MapPanel mapPanel;
    private DrawingMode mode = DrawingMode.DISABLED;
    private List<Position> currentVertices = new ArrayList<>();
    private Consumer<Polygon> onPolygonComplete;
    private Consumer<List<Position>> onWaypointsComplete;
    private EventHandler<MouseEvent> mapClickHandler;

    public DrawingController(MapPanel mapPanel) {
        this.mapPanel = mapPanel;
        setupMapClickHandler();
    }

    /**
     * Setup the click handler on the map container
     */
    private void setupMapClickHandler() {
        mapClickHandler = event -> {
            if (mode == DrawingMode.DISABLED) {
                return;
            }

            // Convert mouse coordinates to geographic position
            // TODO: Integrate with java_leaflet to get actual lat/lon
            // For now, using screen coordinates as placeholder
            Position position = Position.of(
                50.6 + (event.getX() / 100000),  // Mock latitude
                -2.4 + (event.getY() / 100000)   // Mock longitude
            );

            addVertex(position);
            event.consume();
        };

        mapPanel.getMapContainer().setOnMouseClicked(mapClickHandler);
    }

    /**
     * Start drawing a polygon (search area)
     */
    public void startDrawingPolygon(Consumer<Polygon> onComplete) {
        mode = DrawingMode.POLYGON;
        currentVertices.clear();
        onPolygonComplete = onComplete;
        onWaypointsComplete = null;
        System.out.println("Starting polygon drawing mode");
    }

    /**
     * Start drawing waypoints for waypoint transit
     */
    public void startDrawingWaypoints(Consumer<List<Position>> onComplete) {
        mode = DrawingMode.WAYPOINT;
        currentVertices.clear();
        onWaypointsComplete = onComplete;
        onPolygonComplete = null;
        System.out.println("Starting waypoint drawing mode");
    }

    /**
     * Finish drawing and return the result
     */
    public void finishDrawing() {
        if (mode == DrawingMode.DISABLED || currentVertices.isEmpty()) {
            mode = DrawingMode.DISABLED;
            return;
        }

        try {
            if (mode == DrawingMode.POLYGON) {
                if (currentVertices.size() < 3) {
                    System.err.println("Polygon requires at least 3 vertices");
                    return;
                }
                Polygon polygon = new Polygon(currentVertices);
                if (onPolygonComplete != null) {
                    onPolygonComplete.accept(polygon);
                }
                System.out.println("Polygon drawing complete: " + currentVertices.size() + " vertices");
            } else if (mode == DrawingMode.WAYPOINT) {
                if (onWaypointsComplete != null) {
                    onWaypointsComplete.accept(new ArrayList<>(currentVertices));
                }
                System.out.println("Waypoint drawing complete: " + currentVertices.size() + " waypoints");
            }
        } finally {
            cancelDrawing();
        }
    }

    /**
     * Cancel the current drawing operation
     */
    public void cancelDrawing() {
        mode = DrawingMode.DISABLED;
        currentVertices.clear();
        onPolygonComplete = null;
        onWaypointsComplete = null;
        mapPanel.clearOverlays();
        System.out.println("Drawing cancelled");
    }

    /**
     * Add a vertex to the current drawing
     */
    private void addVertex(Position position) {
        currentVertices.add(position);
        System.out.println("Vertex added: " + position + " (total: " + currentVertices.size() + ")");

        // TODO: Visualize the drawing in progress on the map
        // - Show vertices as markers
        // - Show polyline connecting vertices
        // - Update preview in real-time
    }

    /**
     * Undo the last vertex
     */
    public void undoLastVertex() {
        if (!currentVertices.isEmpty()) {
            currentVertices.remove(currentVertices.size() - 1);
            System.out.println("Undid last vertex (remaining: " + currentVertices.size() + ")");
        }
    }

    /**
     * Get the current drawing mode
     */
    public DrawingMode getMode() {
        return mode;
    }

    /**
     * Get the vertices collected so far
     */
    public List<Position> getCurrentVertices() {
        return new ArrayList<>(currentVertices);
    }
}
