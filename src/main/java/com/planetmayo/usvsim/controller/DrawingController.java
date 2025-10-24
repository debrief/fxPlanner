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
     * Start drawing waypoints (polyline mode)
     */
    public void startDrawingWaypoints(Consumer<List<Position>> onComplete) {
        mode = DrawingMode.WAYPOINT;
        currentVertices.clear();
        onWaypointsComplete = onComplete;
        onPolygonComplete = null;

        // Setup JavaScript callback for polyline
        String callbackSetup = """
            window.currentDrawingMode = 'polyline';
            window.polylineReadyCallback = function(coordinates) {
                console.log('Polyline callback received with ' + coordinates.length + ' waypoints');
            };

            // Start polyline drawing
            if (typeof window.startDrawingPolyline === 'function') {
                window.startDrawingPolyline();
            } else {
                console.log('Polyline drawing not initialized yet');
            }
            """;

        try {
            webEngine.executeScript(callbackSetup);
            System.out.println("Polyline drawing mode started - click map to add waypoints, re-click last point to finish");
        } catch (Exception e) {
            System.err.println("Error starting polyline drawing: " + e.getMessage());
        }
    }

    /**
     * Finish drawing and return the collected geometry
     */
    public void finishDrawing() {
        System.out.println("finishDrawing() called, mode = " + mode);

        if (mode == DrawingMode.DISABLED) {
            System.out.println("Drawing is not active");
            return;
        }

        try {
            // Call JavaScript finishDrawing function (works with both Leaflet.Draw and fallback)
            String finishScript = """
                window.updateStatus('Finalizing polygon...');
                if (typeof window.finishDrawing === 'function') {
                    window.finishDrawing();
                    window.updateStatus('Finished drawing function called');
                } else {
                    window.updateStatus('finishDrawing function not found!');
                }
                window.drawnCoordinates;
                """;

            System.out.println("Executing finish script...");
            Object coordsObj = webEngine.executeScript(finishScript);
            System.out.println("Finish script returned: " + coordsObj);

            if (coordsObj != null && mode == DrawingMode.POLYGON) {
                System.out.println("✓ Coordinates received: " + coordsObj);

                // Parse the coordinates and create polygon
                try {
                    // coordsObj should be a JavaScript array of [lat, lng] pairs
                    // Convert to Position objects
                    List<Position> vertices = parseCoordinates(coordsObj);
                    System.out.println("Parsed " + vertices.size() + " vertices from coordinates");

                    if (!vertices.isEmpty() && vertices.size() >= 3) {
                        // Auto-close polygon if not already closed
                        Position first = vertices.get(0);
                        Position last = vertices.get(vertices.size() - 1);
                        double distance = first.distanceTo(last);

                        if (distance > 1.0) {  // More than 1m apart - not closed
                            vertices.add(first);
                            System.out.println("Auto-closed polygon by adding first point at end (gap was " +
                                String.format("%.1fm", distance) + ")");
                        }

                        Polygon polygon = new Polygon(vertices);
                        mode = DrawingMode.DISABLED;

                        if (onPolygonComplete != null) {
                            System.out.println("Calling onPolygonComplete callback with " + polygon.getVertices().size() + " vertices");
                            onPolygonComplete.accept(polygon);
                        } else {
                            System.out.println("ERROR: onPolygonComplete callback is NULL!");
                        }
                        System.out.println("✓ Polygon complete: " + polygon.getVertices().size() + " vertices");
                    } else {
                        System.out.println("ERROR: Polygon needs at least 3 vertices, got: " + vertices.size());
                        showInvalidPolygonDialog(vertices.size());
                        // Do NOT set mode to DISABLED - keep drawing active
                        return;
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing coordinates: " + e.getMessage());
                    e.printStackTrace();
                }
            } else if (mode == DrawingMode.WAYPOINT) {
                // Handle waypoint/polyline completion
                System.out.println("✓ Retrieving polyline coordinates");

                // Get polyline coordinates from JavaScript
                String polylineScript = """
                    if (typeof window.finishPolyline === 'function') {
                        window.finishPolyline();
                    }
                    window.drawnPolylineCoordinates;
                    """;

                Object polylineObj = webEngine.executeScript(polylineScript);
                System.out.println("Polyline script returned: " + polylineObj);

                if (polylineObj != null) {
                    try {
                        List<Position> waypoints = parseCoordinates(polylineObj);
                        System.out.println("Parsed " + waypoints.size() + " waypoints from polyline");

                        if (!waypoints.isEmpty() && waypoints.size() >= 2) {
                            mode = DrawingMode.DISABLED;

                            if (onWaypointsComplete != null) {
                                System.out.println("Calling onWaypointsComplete callback with " + waypoints.size() + " waypoints");
                                onWaypointsComplete.accept(waypoints);
                            } else {
                                System.out.println("ERROR: onWaypointsComplete callback is NULL!");
                            }
                            System.out.println("✓ Polyline complete: " + waypoints.size() + " waypoints");
                            } else {
                            System.out.println("ERROR: Polyline needs at least 2 waypoints, got: " + waypoints.size());
                            showErrorDialog("Polyline must have at least 2 waypoints. Please add more points.");
                            return;
                        }
                    } catch (Exception e) {
                        System.err.println("Error parsing polyline coordinates: " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("ERROR: polylineObj is null");
                    showErrorDialog("Failed to capture polyline. Please try again.");
                    mode = DrawingMode.DISABLED;
                }
            } else {
                System.out.println("ERROR: coordsObj is null or mode is not POLYGON. coordsObj=" + coordsObj + ", mode=" + mode);
                showErrorDialog("Failed to capture polygon. Please try again.");
            }
        } catch (Exception e) {
            System.err.println("Error finishing drawing: " + e.getMessage());
            e.printStackTrace();
            showErrorDialog("Error during drawing: " + e.getMessage());
        }

        // Only set to DISABLED if we successfully completed
        if (mode == DrawingMode.POLYGON) {
            mode = DrawingMode.DISABLED;
        }
    }

    /**
     * Show error when polygon has too few vertices
     */
    private void showInvalidPolygonDialog(int vertexCount) {
        javafx.application.Platform.runLater(() -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
            alert.setTitle("Invalid Polygon");
            alert.setHeaderText("Polygon must have at least 3 vertices");
            alert.setContentText("You have drawn " + vertexCount + " vertices. Please click at least 3 points on the map, then click near the first vertex to close the polygon.");
            alert.showAndWait();
            System.out.println("User shown invalid polygon warning");
        });
    }

    /**
     * Show generic error dialog
     */
    private void showErrorDialog(String message) {
        javafx.application.Platform.runLater(() -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Drawing Error");
            alert.setHeaderText("An error occurred while drawing");
            alert.setContentText(message);
            alert.showAndWait();
        });
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
