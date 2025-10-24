package com.planetmayo.usvsim.controller;

import com.planetmayo.usvsim.model.geometry.Polygon;
import com.planetmayo.usvsim.model.geometry.Position;
import com.planetmayo.usvsim.view.MapPanel;
import javafx.application.Platform;
import javafx.scene.web.WebEngine;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Handles polygon drawing on the map using Leaflet.Draw.
 *
 * Drawing modes:
 * - DISABLED: No drawing
 * - POLYGON: Drawing search area polygons
 * - WAYPOINT: Marking waypoints
 *
 * Usage:
 * 1. Create DrawingController with MapPanel
 * 2. Call startDrawingPolygon() to enable polygon drawing
 * 3. User draws on map with Leaflet.Draw
 * 4. DrawingController captures completed shapes and converts to Polygon model
 */
public class DrawingController {
    public enum DrawingMode {
        DISABLED, POLYGON, WAYPOINT
    }

    private final MapPanel mapPanel;
    private final WebEngine webEngine;
    private DrawingMode mode = DrawingMode.DISABLED;
    private Consumer<Polygon> onPolygonComplete;
    private Consumer<List<Position>> onWaypointsComplete;
    private List<Position> currentVertices = new ArrayList<>();

    public DrawingController(MapPanel mapPanel) {
        this.mapPanel = mapPanel;
        this.webEngine = mapPanel.getWebEngine();
        // Drawing controls are initialized in MapPanel HTML when page loads
        System.out.println("DrawingController initialized - Leaflet.Draw controls auto-initialized on map");
    }

    /**
     * Setup additional event handlers (drawing controls are initialized in MapPanel HTML)
     */
    private void setupDrawingIntegration() {
        // Leaflet.Draw is initialized in the HTML page itself
        // This method is kept for any additional setup from Java side
        String eventHandlerSetup = """
            console.log('Additional drawing event handlers configured');
            """;

        try {
            webEngine.executeScript(eventHandlerSetup);
        } catch (Exception e) {
            System.err.println("Error in drawing integration: " + e.getMessage());
        }
    }

    /**
     * Start drawing a polygon (search area)
     */
    public void startDrawingPolygon(Consumer<Polygon> onComplete) {
        mode = DrawingMode.POLYGON;
        currentVertices.clear();
        onPolygonComplete = onComplete;
        onWaypointsComplete = null;

        // Setup JavaScript callback
        String callbackSetup = """
            window.currentDrawingMode = 'polygon';
            window.polygonReadyCallback = function(coordinates) {
                console.log('Polygon callback received with ' + coordinates.length + ' vertices');
            };

            // Start drawing (works with both Leaflet.Draw and fallback)
            if (typeof window.startDrawing === 'function') {
                window.startDrawing();
            } else {
                console.log('Drawing not initialized yet');
            }
            """;

        try {
            webEngine.executeScript(callbackSetup);
            System.out.println("Polygon drawing mode started - click on map to add vertices");
        } catch (Exception e) {
            System.err.println("Error starting polygon drawing: " + e.getMessage());
        }
    }

    /**
     * Start drawing waypoints
     */
    public void startDrawingWaypoints(Consumer<List<Position>> onComplete) {
        mode = DrawingMode.WAYPOINT;
        currentVertices.clear();
        onWaypointsComplete = onComplete;
        onPolygonComplete = null;
        System.out.println("Starting waypoint drawing mode - click to add waypoints");
    }

    /**
     * Finish drawing and return the collected geometry
     */
    public void finishDrawing() {
        if (mode == DrawingMode.DISABLED) {
            System.out.println("Drawing is not active");
            return;
        }

        try {
            // Call JavaScript finishDrawing function (works with both Leaflet.Draw and fallback)
            String finishScript = """
                if (typeof window.finishDrawing === 'function') {
                    window.finishDrawing();
                }
                window.drawnCoordinates;
                """;

            Object coordsObj = webEngine.executeScript(finishScript);

            if (coordsObj != null && mode == DrawingMode.POLYGON) {
                System.out.println("Coordinates received: " + coordsObj);

                // Parse the coordinates and create polygon
                try {
                    // coordsObj should be a JavaScript array of [lat, lng] pairs
                    // Convert to Position objects
                    List<Position> vertices = parseCoordinates(coordsObj);

                    if (!vertices.isEmpty() && vertices.size() >= 3) {
                        Polygon polygon = new Polygon(vertices);
                        mode = DrawingMode.DISABLED;

                        if (onPolygonComplete != null) {
                            onPolygonComplete.accept(polygon);
                        }
                        System.out.println("✓ Polygon complete: " + polygon.getVertices().size() + " vertices");
                    } else {
                        System.out.println("Polygon needs at least 3 vertices");
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing coordinates: " + e.getMessage());
                }
            } else if (mode == DrawingMode.WAYPOINT) {
                // Handle waypoint completion
                mode = DrawingMode.DISABLED;
                System.out.println("Waypoint drawing complete");
            }
        } catch (Exception e) {
            System.err.println("Error finishing drawing: " + e.getMessage());
        }

        mode = DrawingMode.DISABLED;
    }

    /**
     * Parse JavaScript coordinate array into Position objects
     */
    private List<Position> parseCoordinates(Object coordsObj) {
        List<Position> positions = new ArrayList<>();

        if (coordsObj == null) {
            return positions;
        }

        // coordsObj is from JavaScript, should be an array
        String coordStr = coordsObj.toString();
        System.out.println("Raw coords: " + coordStr);

        // Simple parsing: [lat,lng],[lat,lng]... pattern
        String[] pairs = coordStr.replaceAll("[\\[\\]]", "").split(",");
        for (int i = 0; i < pairs.length; i += 2) {
            if (i + 1 < pairs.length) {
                try {
                    double lat = Double.parseDouble(pairs[i].trim());
                    double lng = Double.parseDouble(pairs[i + 1].trim());
                    positions.add(Position.of(lat, lng));
                } catch (NumberFormatException e) {
                    System.err.println("Could not parse coordinate: " + pairs[i] + ", " + pairs[i + 1]);
                }
            }
        }

        return positions;
    }

    /**
     * Add a vertex to the current drawing
     */
    private void addVertex(Position position) {
        if (mode == DrawingMode.DISABLED) {
            return;
        }

        currentVertices.add(position);
        System.out.println("Added vertex: " + position + " (total: " + currentVertices.size() + ")");

        // Update map visualization
        if (mode == DrawingMode.WAYPOINT) {
            String markerScript = String.format("""
                var marker = L.circleMarker([%f, %f], {
                    radius: 5,
                    fillColor: '#FFA500',
                    color: '#FF8C00',
                    weight: 1,
                    opacity: 0.8,
                    fillOpacity: 0.8
                }).addTo(window.leafletMap);
                window.markers.push(marker);
                console.log('Waypoint marker added');
                """, position.getLatitude(), position.getLongitude());

            webEngine.executeScript(markerScript);
        }
    }

    /**
     * Cancel drawing and reset
     */
    public void cancelDrawing() {
        mode = DrawingMode.DISABLED;
        currentVertices.clear();
        onPolygonComplete = null;
        onWaypointsComplete = null;
        System.out.println("Drawing cancelled");
    }

    /**
     * Get current drawing mode
     */
    public DrawingMode getMode() {
        return mode;
    }

    /**
     * Check if currently drawing
     */
    public boolean isDrawing() {
        return mode != DrawingMode.DISABLED;
    }

    /**
     * Get current vertices (useful for live preview)
     */
    public List<Position> getCurrentVertices() {
        return new ArrayList<>(currentVertices);
    }
}
