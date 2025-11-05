package com.planetmayo.usvsim.view;

import com.planetmayo.usvsim.model.geometry.Position;
import com.planetmayo.usvsim.model.geometry.Polygon;
import com.planetmayo.usvsim.model.geometry.Waypoint;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.util.ArrayList;
import java.util.List;

/**
 * Map display panel showing Portland Harbour with search areas and track patterns.
 *
 * Features:
 * - Leaflet.js map embedded in WebView (full Leaflet ecosystem support)
 * - Pan/zoom controls
 * - Polygon rendering (search areas)
 * - Track pattern display (parallel tracks, expanding squares)
 * - USV icon and track history (when simulating)
 * - Drawing support for polygon creation
 */
public class MapPanel extends BorderPane {
    private final WebView webView;
    private final WebEngine webEngine;
    private final HBox controlPanel;
    private List<Position> trackPoints;
    private static final int MAX_TRACK_POINTS = 1000;

    // Portland Harbour coordinates
    private static final double PORTLAND_LAT = 50.6;
    private static final double PORTLAND_LON = -2.4;
    private static final int DEFAULT_ZOOM = 14;

    private java.util.function.Consumer<Integer> vertexCountCallback;

    public MapPanel() {
        // Initialize WebView with Leaflet
        webView = new WebView();
        // CRITICAL FIX: Set EXPLICIT height/width (Stack Overflow solution)
        // Leaflet needs actual dimensions to calculate which tiles to fetch
        // Without explicit height, Leaflet sees height=0 and fetches no tiles
        webView.setPrefHeight(600);  // EXPLICIT height for tile calculation
        webView.setPrefWidth(800);   // EXPLICIT width for tile calculation
        webView.setMinHeight(400);  // Also set minimums as fallback
        webView.setMinWidth(600);
        webView.setStyle("-fx-font-smoothing-type: gray;");
        webEngine = webView.getEngine();
        webEngine.setJavaScriptEnabled(true);

        // Load Leaflet HTML
        loadLeafletMap();

        // CRITICAL: Use AnchorPane with zero anchors (java_leaflet pattern)
        // This forces WebView to fill ALL available space
        AnchorPane mapContainer = new AnchorPane(webView);
        AnchorPane.setLeftAnchor(webView, 0.0);
        AnchorPane.setRightAnchor(webView, 0.0);
        AnchorPane.setTopAnchor(webView, 0.0);
        AnchorPane.setBottomAnchor(webView, 0.0);
        mapContainer.setStyle("-fx-background-color: #e8e8e8;");

        // Control panel with pan/zoom buttons
        controlPanel = createControlPanel();

        // Initialize track history tracking
        trackPoints = new ArrayList<>();

        // Layout - set MapPanel to fill available space
        setCenter(mapContainer);
        setBottom(controlPanel);
        setPadding(new Insets(5));
        setStyle("-fx-border-color: transparent;");
    }

    /**
     * Load Leaflet map HTML/JavaScript into WebView
     */
    private void loadLeafletMap() {
        String html = String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8" />
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>USV Mission Planner Map</title>

                <!-- CRITICAL: Use Leaflet 1.8.0 for JavaFX WebView compatibility -->
                <link rel="stylesheet" href="https://unpkg.com/leaflet@1.8.0/dist/leaflet.css" />

                <!-- CRITICAL: Use Leaflet 1.8.0 for JavaFX WebView compatibility -->
                <script src="https://unpkg.com/leaflet@1.8.0/dist/leaflet.js"></script>

                <style>
                    * { box-sizing: border-box; }
                    html, body { margin: 0; padding: 0; height: 100%%; }
                    #map { position: absolute; top: 0; left: 0; bottom: 0; width: 100%%; z-index: 1; }

                    /* Ensure Leaflet controls are visible */
                    .leaflet-control { z-index: 999 !important; }
                    .leaflet-top { z-index: 999; }
                    .leaflet-bottom { z-index: 999; }
                </style>
            </head>
            <body>
                <div id="map"></div>
                <script>
                    // Bridge JavaScript console to Java console
                    var originalLog = console.log;
                    var originalError = console.error;
                    console.log = function(msg) {
                        originalLog(msg);
                        try {
                            var jsConsole = document.createElement('div');
                            jsConsole.id = 'js_log_' + Date.now();
                            jsConsole.style.display = 'none';
                            jsConsole.textContent = '[JS] ' + msg;
                            document.body.appendChild(jsConsole);
                        } catch(e) {}
                    };
                    console.error = function(msg) {
                        originalError(msg);
                        try {
                            var jsConsole = document.createElement('div');
                            jsConsole.id = 'js_error_' + Date.now();
                            jsConsole.style.display = 'none';
                            jsConsole.textContent = '[JS_ERR] ' + msg;
                            document.body.appendChild(jsConsole);
                        } catch(e) {}
                    };

                    // Delay map initialization for WebView (JavaFX compatibility fix)
                    function initMap() {
                        // Initialize map centered on Portland Harbour
                        var map = L.map('map').setView([%f, %f], %d);

                        // Add OpenStreetMap tiles
                        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                            attribution: '© OpenStreetMap contributors',
                            maxZoom: 19
                        }).addTo(map);

                        // Global tracking for polygons and features
                        window.drawnPolygons = [];
                        window.drawnPolylines = [];
                        window.markers = [];

                        // Make map accessible to Java
                        window.leafletMap = map;
                        window.drawnItems = new L.FeatureGroup();
                        window.leafletMap.addLayer(window.drawnItems);

                        return map;
                    }

                    // Helper to update status panel
                    function updateStatus(msg) {
                        var elem = document.getElementById('status-content');
                        if (elem) {
                            var existing = elem.innerHTML;
                            elem.innerHTML = existing + '<br/>' + msg;
                        }
                        console.log(msg);
                    }

                    // Simple click-to-draw using Leaflet's built-in features
                    function enableClickToDrawMode() {
                        updateStatus('FALLBACK: Click-to-draw enabled');
                        window.isDrawing = false;  // Expose globally for Java cleanup
                        var currentPolyline = null;
                        var firstVertexMarker = null;  // Track first vertex marker for polygon mode
                        var vertices = [];
                        var CLOSE_THRESHOLD = 5; // pixels

                        // Expose vertex count globally for Java to check
                        window.vertexCount = 0;

                        window.leafletMap.on('click', function(e) {
                            if (!window.isDrawing && window.currentDrawingMode !== 'polygon' && window.currentDrawingMode !== 'polyline') {
                                return;
                            }

                            // POLYLINE MODE: Check if re-clicking last point (within 5px) to finish
                            if (window.currentDrawingMode === 'polyline' && vertices.length >= 2) {
                                var lastVertex = vertices[vertices.length - 1];
                                var lastLatLng = L.latLng(lastVertex[0], lastVertex[1]);
                                var distPx = window.leafletMap.latLngToContainerPoint(lastLatLng).distanceTo(
                                    window.leafletMap.latLngToContainerPoint(e.latlng)
                                );

                                if (distPx < CLOSE_THRESHOLD) {
                                    updateStatus('Polyline finished by re-clicking last point');
                                    window.finishPolyline();
                                    return;
                                }
                            }

                            // POLYGON MODE: Check if clicking near first vertex (within 5px) to close polygon
                            if (window.currentDrawingMode === 'polygon' && vertices.length >= 3) {
                                var firstVertex = vertices[0];
                                var firstLatLng = L.latLng(firstVertex[0], firstVertex[1]);
                                var distPx = window.leafletMap.latLngToContainerPoint(firstLatLng).distanceTo(
                                    window.leafletMap.latLngToContainerPoint(e.latlng)
                                );

                                if (distPx < CLOSE_THRESHOLD) {
                                    updateStatus('Polygon closed by clicking near first vertex');
                                    window.finishDrawing();
                                    return;
                                }
                            }

                            vertices.push([e.latlng.lat, e.latlng.lng]);
                            window.vertexCount = vertices.length;

                            // For polygon mode: show blue marker on first click
                            if (window.currentDrawingMode === 'polygon' && vertices.length === 1) {
                                firstVertexMarker = L.circleMarker([e.latlng.lat, e.latlng.lng], {
                                    radius: 6,
                                    fillColor: 'blue',
                                    color: 'darkblue',
                                    weight: 2,
                                    opacity: 0.9,
                                    fillOpacity: 0.7
                                }).addTo(window.leafletMap);
                                updateStatus('Vertex 1 added (click to continue)');
                                return;  // Don't draw polyline yet
                            }

                            // Remove first vertex marker when drawing polyline (2+ vertices)
                            if (firstVertexMarker && vertices.length >= 2) {
                                window.leafletMap.removeLayer(firstVertexMarker);
                                firstVertexMarker = null;
                            }

                            if (currentPolyline) {
                                window.leafletMap.removeLayer(currentPolyline);
                            }

                            var latlngs = vertices.map(v => [v[0], v[1]]);
                            var color = window.currentDrawingMode === 'polyline' ? 'orange' : 'blue';
                            currentPolyline = L.polyline(latlngs, {color: color, weight: 2}).addTo(window.leafletMap);

                            updateStatus('Vertex ' + vertices.length + ' added');
                        });

                        window.finishDrawing = function() {
                            if (vertices.length >= 3) {
                                var polygon = L.polygon(vertices.map(v => [v[0], v[1]]), {color: 'red'}).addTo(window.leafletMap);
                                window.drawnItems.addLayer(polygon);
                                window.drawnCoordinates = vertices;

                                if (window.polygonReadyCallback) {
                                    window.polygonReadyCallback(window.drawnCoordinates);
                                }
                                updateStatus('✓ Polygon complete: ' + vertices.length + ' vertices');
                                vertices = [];
                                if (currentPolyline) {
                                    window.leafletMap.removeLayer(currentPolyline);
                                    currentPolyline = null;
                                }
                                if (firstVertexMarker) {
                                    window.leafletMap.removeLayer(firstVertexMarker);
                                    firstVertexMarker = null;
                                }
                                // Reset cursor to default (hand/grab)
                                document.getElementById('map').style.cursor = '';
                            }
                        };

                        window.startDrawing = function() {
                            window.isDrawing = true;
                            vertices = [];
                            // Clean up any existing first vertex marker
                            if (firstVertexMarker) {
                                window.leafletMap.removeLayer(firstVertexMarker);
                                firstVertexMarker = null;
                            }
                            // Change cursor to crosshair for drawing mode
                            document.getElementById('map').style.cursor = 'crosshair';
                            updateStatus('Click map to add vertices. Click near first vertex to close polygon.');
                        };

                        window.cancelDrawing = function() {
                            window.isDrawing = false;
                            vertices = [];
                            window.vertexCount = 0;
                            if (currentPolyline) {
                                window.leafletMap.removeLayer(currentPolyline);
                                currentPolyline = null;
                            }
                            if (firstVertexMarker) {
                                window.leafletMap.removeLayer(firstVertexMarker);
                                firstVertexMarker = null;
                            }
                            // Reset cursor to default (hand/grab)
                            document.getElementById('map').style.cursor = '';
                            updateStatus('Drawing cancelled');
                        };

                        // Polyline drawing mode (for waypoint transit)
                        window.startDrawingPolyline = function() {
                            window.currentDrawingMode = 'polyline';
                            window.isDrawing = true;
                            vertices = [];
                            window.vertexCount = 0;
                            if (currentPolyline) {
                                window.leafletMap.removeLayer(currentPolyline);
                                currentPolyline = null;
                            }
                            // Change cursor to crosshair for drawing mode
                            document.getElementById('map').style.cursor = 'crosshair';
                            updateStatus('Click map to add waypoints. Re-click last point to finish.');
                        };

                        window.finishPolyline = function() {
                            if (vertices.length >= 2) {
                                // Store coordinates for Java to retrieve
                                window.drawnPolylineCoordinates = vertices;

                                if (window.polylineReadyCallback) {
                                    window.polylineReadyCallback(window.drawnPolylineCoordinates);
                                }

                                updateStatus('✓ Polyline complete: ' + vertices.length + ' waypoints');

                                // Clean up drawing artifacts
                                vertices = [];
                                window.vertexCount = 0;
                                window.isDrawing = false;
                                window.currentDrawingMode = null;

                                // Remove temporary orange polyline
                                if (currentPolyline) {
                                    window.leafletMap.removeLayer(currentPolyline);
                                    currentPolyline = null;
                                }

                                // Reset cursor to default (hand/grab)
                                document.getElementById('map').style.cursor = '';
                            }
                        };
                    }

                    // Detect WebView environment
                    var isWebView = navigator.userAgent.indexOf('JavaFX') > -1;

                    // Initialize map immediately for WebView (Leaflet 1.8.0 + JavaFX fix)
                    if (isWebView) {
                        console.log('WebView detected, initializing map...');
                        initMap();
                        console.log('Leaflet 1.8.0 map initialized at Portland Harbour (WebView mode)');
                        enableClickToDrawMode();
                    } else {
                        // Browser can initialize immediately
                        initMap();
                        console.log('Leaflet 1.8.0 map initialized at Portland Harbour (Browser mode)');
                        enableClickToDrawMode();
                    }
                </script>
            </body>
            </html>
            """, PORTLAND_LAT, PORTLAND_LON, DEFAULT_ZOOM);

        webEngine.loadContent(html);
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
        zoomIn.setOnAction(event -> executeMapScript("window.leafletMap.zoomIn();"));

        Button zoomOut = new Button("-");
        zoomOut.setPrefWidth(50);
        zoomOut.setOnAction(event -> executeMapScript("window.leafletMap.zoomOut();"));

        Button resetView = new Button("Reset View");
        resetView.setPrefWidth(100);
        resetView.setOnAction(event -> handleResetView());

        box.getChildren().addAll(zoomIn, zoomOut, resetView);
        return box;
    }

    /**
     * Execute JavaScript on the Leaflet map
     */
    private void executeMapScript(String script) {
        try {
            webEngine.executeScript(script);
        } catch (Exception e) {
            System.err.println("Error executing map script: " + e.getMessage());
        }
    }

    /**
     * Render a polygon on the map (search area)
     */
    public void renderPolygon(Polygon polygon) {
        if (polygon == null) return;

        // Build Leaflet GeoJSON feature
        StringBuilder coordinates = new StringBuilder();
        var vertices = polygon.getVertices();
        for (int i = 0; i < vertices.size(); i++) {
            var v = vertices.get(i);
            coordinates.append("[").append(v.getLongitude()).append(", ").append(v.getLatitude()).append("]");
            if (i < vertices.size() - 1) coordinates.append(", ");
        }

        String script = String.format("""
            var polygon = L.polygon([%s], {
                color: 'green',
                weight: 2,
                opacity: 0.8
            }).addTo(window.leafletMap);
            window.drawnPolygons.push(polygon);
            console.log('Polygon rendered with %d vertices');
            """, coordinates.toString(), vertices.size());

        executeMapScript(script);
        System.out.println("Rendered polygon: " + vertices.size() + " vertices");
    }

    /**
     * Render track waypoints as polyline
     */
    public void renderTracks(List<Waypoint> waypoints) {
        renderTracks(waypoints, false);
    }

    /**
     * Render track waypoints as polyline with optional start marker
     */
    public void renderTracks(List<Waypoint> waypoints, boolean showStartMarker) {
        if (waypoints == null || waypoints.isEmpty()) return;

        // Build Leaflet polyline coordinates
        StringBuilder coordinates = new StringBuilder();
        for (int i = 0; i < waypoints.size(); i++) {
            var w = waypoints.get(i);
            coordinates.append("[").append(w.getPosition().getLatitude()).append(", ")
                    .append(w.getPosition().getLongitude()).append("]");
            if (i < waypoints.size() - 1) coordinates.append(", ");
        }

        String script;
        if (showStartMarker && !waypoints.isEmpty()) {
            // Include start marker
            var startPoint = waypoints.get(0).getPosition();
            script = String.format("""
                var polyline = L.polyline([%s], {
                    color: 'blue',
                    weight: 2,
                    opacity: 0.7
                }).addTo(window.leafletMap);
                window.drawnPolylines.push(polyline);

                // Add start marker
                var startMarker = L.circleMarker([%f, %f], {
                    radius: 6,
                    fillColor: '#00FF00',
                    color: '#008000',
                    weight: 2,
                    opacity: 1.0,
                    fillOpacity: 0.9
                }).addTo(window.leafletMap);
                startMarker.bindPopup('Start Point');
                window.markers.push(startMarker);

                console.log('Track polyline rendered with %d waypoints + start marker');
                """, coordinates.toString(),
                startPoint.getLatitude(), startPoint.getLongitude(),
                waypoints.size());
        } else {
            script = String.format("""
                var polyline = L.polyline([%s], {
                    color: 'blue',
                    weight: 1,
                    opacity: 0.6
                }).addTo(window.leafletMap);
                window.drawnPolylines.push(polyline);
                console.log('Track polyline rendered with %d waypoints');
                """, coordinates.toString(), waypoints.size());
        }

        executeMapScript(script);
        System.out.println("Rendered " + waypoints.size() + " waypoints" + (showStartMarker ? " with start marker" : ""));
    }

    /**
     * Update USV position marker with heading orientation and directional "stalk"
     */
    public void updatePlatformPosition(Position position, double heading) {
        if (position == null) return;

        // Calculate stalk endpoint (50 meters ahead in heading direction)
        double stalkLengthMeters = 50.0;
        Position stalkEnd = position.destination(stalkLengthMeters, heading);

        String script = String.format("""
            // Remove previous USV marker and stalk if exists
            if (window.usvMarker) {
                window.leafletMap.removeLayer(window.usvMarker);
            }
            if (window.usvStalk) {
                window.leafletMap.removeLayer(window.usvStalk);
            }

            // Draw direction "stalk" (line showing heading)
            window.usvStalk = L.polyline([
                [%f, %f],
                [%f, %f]
            ], {
                color: '#FF0000',
                weight: 3,
                opacity: 0.9,
                dashArray: '5, 5'
            }).addTo(window.leafletMap);

            // Draw USV position marker (circle at base of stalk)
            window.usvMarker = L.circleMarker([%f, %f], {
                radius: 8,
                fillColor: '#FF0000',
                color: '#8B0000',
                weight: 2,
                opacity: 1.0,
                fillOpacity: 0.9
            }).addTo(window.leafletMap);

            window.usvMarker.bindPopup('USV<br/>Heading: %.1f°<br/>Position: %.4f°N, %.4f°E');
            console.log('USV marker updated');
            """,
            position.getLatitude(), position.getLongitude(),
            stalkEnd.getLatitude(), stalkEnd.getLongitude(),
            position.getLatitude(), position.getLongitude(),
            heading, position.getLatitude(), position.getLongitude());

        executeMapScript(script);
    }

    /**
     * Add point to track history polyline
     */
    public void addTrackPoint(Position position) {
        if (position == null) return;

        trackPoints.add(position);

        // Limit track history size
        if (trackPoints.size() > MAX_TRACK_POINTS) {
            trackPoints.remove(0);
        }

        // Rebuild track polyline
        if (!trackPoints.isEmpty()) {
            StringBuilder coordinates = new StringBuilder();
            for (int i = 0; i < trackPoints.size(); i++) {
                var p = trackPoints.get(i);
                coordinates.append("[").append(p.getLatitude()).append(", ")
                        .append(p.getLongitude()).append("]");
                if (i < trackPoints.size() - 1) coordinates.append(", ");
            }

            String script = String.format("""
                // Remove previous track history polyline (but keep search patterns)
                if (window.trackHistoryPolyline) {
                    window.leafletMap.removeLayer(window.trackHistoryPolyline);
                }

                // Draw updated track history
                window.trackHistoryPolyline = L.polyline([%s], {
                    color: 'black',
                    weight: 2,
                    opacity: 0.8
                }).addTo(window.leafletMap);
                """, coordinates.toString());

            executeMapScript(script);
        }
    }

    /**
     * Show the initial platform start position marker (always visible)
     */
    public void showStartPosition(Position position) {
        if (position == null) return;

        String script = String.format("""
            // Remove previous start position marker if exists
            if (window.startPositionMarker) {
                window.leafletMap.removeLayer(window.startPositionMarker);
            }

            // Add start position marker
            window.startPositionMarker = L.circleMarker([%f, %f], {
                radius: 8,
                fillColor: '#00FF00',
                color: '#008000',
                weight: 3,
                opacity: 1.0,
                fillOpacity: 0.9
            }).addTo(window.leafletMap);
            window.startPositionMarker.bindPopup('Platform Start Position<br/>%.4f°N, %.4f°W');
            console.log('Start position marker displayed');
            """,
            position.getLatitude(), position.getLongitude(),
            position.getLatitude(), Math.abs(position.getLongitude()));

        executeMapScript(script);
        System.out.println("Start position marker shown at: " + position);
    }

    /**
     * Clear all overlays (polygons, tracks, markers)
     */
    public void clearOverlays() {
        String script = """
            // Clear drawnItems FeatureGroup (used by Leaflet.Draw and fallback drawing)
            if (typeof window.drawnItems !== 'undefined' && window.drawnItems) {
                window.drawnItems.clearLayers();
            }

            // Clear all polygons
            if (window.drawnPolygons) {
                window.drawnPolygons.forEach(p => window.leafletMap.removeLayer(p));
                window.drawnPolygons = [];
            }

            // Clear all polylines
            if (window.drawnPolylines) {
                window.drawnPolylines.forEach(p => window.leafletMap.removeLayer(p));
                window.drawnPolylines = [];
            }

            // Clear all markers (except start position marker)
            if (window.markers) {
                window.markers.forEach(m => window.leafletMap.removeLayer(m));
                window.markers = [];
            }

            console.log('Map overlays cleared (polygons/polylines/markers)');
            """;

        executeMapScript(script);
        trackPoints.clear();
        System.out.println("Clearing map overlays");
    }

    /**
     * Handle reset view to Portland Harbour
     */
    private void handleResetView() {
        executeMapScript(String.format(
            "window.leafletMap.setView([%f, %f], %d);",
            PORTLAND_LAT, PORTLAND_LON, DEFAULT_ZOOM
        ));
        clearOverlays();
        System.out.println("Reset view to Portland Harbour");
    }

    /**
     * Set callback to track vertex count changes during drawing
     */
    public void setVertexCountCallback(java.util.function.Consumer<Integer> callback) {
        this.vertexCountCallback = callback;
    }

    /**
     * Call from JavaScript when vertex count changes
     * (Makes this method visible to JavaScript via reflection)
     */
    public void onVertexCountChanged(int count) {
        if (vertexCountCallback != null) {
            javafx.application.Platform.runLater(() -> {
                vertexCountCallback.accept(count);
            });
        }
    }

    /**
     * Get WebEngine for advanced JavaScript integration (T037 polygon drawing)
     */
    public WebEngine getWebEngine() {
        return webEngine;
    }

    /**
     * Execute custom JavaScript (for testing and advanced features)
     */
    public void executeJavaScript(String script) {
        executeMapScript(script);
    }
}
