package com.planetmayo.usvsim.view;

import com.planetmayo.usvsim.model.geometry.Position;
import com.planetmayo.usvsim.model.geometry.Polygon;
import com.planetmayo.usvsim.model.geometry.Waypoint;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
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

    public MapPanel() {
        // Initialize WebView with Leaflet
        webView = new WebView();
        webView.setPrefWidth(USE_COMPUTED_SIZE);
        webView.setPrefHeight(USE_COMPUTED_SIZE);
        webView.setStyle("-fx-font-smoothing-type: gray;");
        webEngine = webView.getEngine();
        webEngine.setJavaScriptEnabled(true);

        // Load Leaflet HTML
        loadLeafletMap();

        // Control panel with pan/zoom buttons
        controlPanel = createControlPanel();

        // Initialize track history tracking
        trackPoints = new ArrayList<>();

        // Layout - set MapPanel to fill available space
        setCenter(webView);
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

                <!-- Leaflet CSS -->
                <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/leaflet.min.css" />

                <!-- Leaflet JS -->
                <script src="https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/leaflet.min.js"></script>

                <!-- Leaflet.Draw (for polygon drawing) - will be loaded dynamically -->
                <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/leaflet-draw/1.0.4/leaflet.draw.css" />

                <style>
                    * { box-sizing: border-box; }
                    html, body { margin: 0; padding: 0; height: 100%%; }
                    #map { position: absolute; top: 0; left: 0; bottom: 0; width: 100%%; z-index: 1; }

                    /* Ensure Leaflet controls are visible */
                    .leaflet-control { z-index: 999 !important; }
                    .leaflet-top { z-index: 999; }
                    .leaflet-bottom { z-index: 999; }

                    /* Leaflet.Draw toolbar styling - ensure visibility */
                    .leaflet-draw { z-index: 1000 !important; }
                    .leaflet-draw-toolbar {
                        background-color: white !important;
                        border-radius: 4px !important;
                        box-shadow: 0 2px 8px rgba(0,0,0,0.2) !important;
                        border: 1px solid #ddd !important;
                    }
                    .leaflet-draw-toolbar a {
                        background-color: white !important;
                        border: 1px solid #ccc !important;
                        color: #333 !important;
                    }
                    .leaflet-draw-toolbar a:hover {
                        background-color: #f5f5f5 !important;
                    }
                    .leaflet-draw-actions { background-color: white !important; }

                    /* Ensure draw icons are visible if CDN CSS loads */
                    .leaflet-draw-draw-polygon::before { content: "█" !important; }
                </style>
            </head>
            <body>
                <div id="map"></div>
                <div id="status-panel" style="position: absolute; top: 10px; right: 10px; background: white; padding: 10px; border: 1px solid #ccc; border-radius: 4px; font-size: 11px; z-index: 998; max-width: 200px; max-height: 150px; overflow-y: auto;">
                    <div style="font-weight: bold;">Map Status</div>
                    <div id="status-content">Loading...</div>
                </div>
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

                    // Helper to update status panel
                    function updateStatus(msg) {
                        var elem = document.getElementById('status-content');
                        if (elem) {
                            var existing = elem.innerHTML;
                            elem.innerHTML = existing + '<br/>' + msg;
                        }
                        console.log(msg);
                    }

                    // Initialize Leaflet.Draw controls
                    function initializeDrawing() {
                        updateStatus('=== DRAW INIT ===');
                        updateStatus('Step 1: Check L object');
                        updateStatus('  L: ' + typeof L);
                        updateStatus('  L.Control: ' + typeof L.Control);
                        updateStatus('  L.Control.Draw: ' + typeof L.Control.Draw);

                        if (typeof L.Control.Draw === 'undefined') {
                            updateStatus('Step 2: WAITING for L.Control.Draw...');
                            setTimeout(initializeDrawing, 200);
                            return;
                        }

                        updateStatus('Step 3: Creating drawControl');
                        try {
                            var drawControl = new L.Control.Draw({
                                draw: {
                                    polygon: true,
                                    polyline: false,
                                    circle: false,
                                    rectangle: false,
                                    circlemarker: false,
                                    marker: false
                                },
                                edit: {
                                    featureGroup: window.drawnItems
                                }
                            });

                            updateStatus('Step 4: drawControl created');

                            // Add control to map
                            window.leafletMap.addControl(drawControl);
                            window.drawControl = drawControl;
                            updateStatus('Step 5: drawControl ADDED');

                            // Verify it's on the map
                            var controls = document.querySelectorAll('.leaflet-draw');
                            updateStatus('Step 6: Found ' + controls.length + ' .leaflet-draw in DOM');

                            // Handle drawing completion
                            window.leafletMap.on('draw:created', function(e) {
                                var layer = e.layer;
                                if (layer instanceof L.Polygon) {
                                    var coords = layer.getLatLngs()[0];
                                    window.drawnCoordinates = coords.map(latlng => [latlng.lat, latlng.lng]);
                                    updateStatus('✓ Polygon: ' + coords.length + ' vertices');
                                    window.drawnItems.addLayer(layer);
                                    if (window.polygonReadyCallback) {
                                        window.polygonReadyCallback(window.drawnCoordinates);
                                    }
                                }
                            });

                            updateStatus('=== DRAW INIT COMPLETE ===');
                        } catch (error) {
                            updateStatus('ERROR: ' + error.message);
                        }
                    }

                    // Dynamically load Leaflet.Draw script from multiple CDNs
                    function loadLeafletDraw() {
                        updateStatus('Loading Leaflet.Draw library...');
                        var cdns = [
                            'https://cdn.jsdelivr.net/npm/leaflet-draw@1.0.4/dist/leaflet-draw.js',
                            'https://unpkg.com/leaflet-draw@1.0.4/dist/leaflet-draw.js',
                            'https://cdnjs.cloudflare.com/ajax/libs/leaflet-draw/1.0.4/leaflet.draw.js'
                        ];
                        var cdnIndex = 0;

                        function tryLoadFromCdn() {
                            if (cdnIndex >= cdns.length) {
                                updateStatus('ERROR: All CDN sources failed!');
                                // Fall back to built-in drawing
                                updateStatus('Using built-in click-to-draw');
                                enableClickToDrawMode();
                                return;
                            }

                            var cdnUrl = cdns[cdnIndex];
                            updateStatus('CDN #' + (cdnIndex + 1) + ': ' + cdnUrl.split('/')[2]);
                            var script = document.createElement('script');
                            script.src = cdnUrl;

                            script.onload = function() {
                                updateStatus('✓ Leaflet.Draw loaded successfully');
                                initializeDrawing();
                            };

                            script.onerror = function() {
                                cdnIndex++;
                                tryLoadFromCdn();
                            };

                            document.head.appendChild(script);
                        }

                        tryLoadFromCdn();
                    }

                    // Fallback: simple click-to-draw using Leaflet's built-in features
                    function enableClickToDrawMode() {
                        updateStatus('FALLBACK: Click-to-draw enabled');
                        var isDrawing = false;
                        var currentPolyline = null;
                        var vertices = [];

                        window.leafletMap.on('click', function(e) {
                            if (!isDrawing && window.currentDrawingMode !== 'polygon') {
                                return;
                            }

                            vertices.push([e.latlng.lat, e.latlng.lng]);

                            if (currentPolyline) {
                                window.leafletMap.removeLayer(currentPolyline);
                            }

                            var latlngs = vertices.map(v => [v[0], v[1]]);
                            currentPolyline = L.polyline(latlngs, {color: 'blue', weight: 2}).addTo(window.leafletMap);

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
                            }
                        };

                        window.startDrawing = function() {
                            isDrawing = true;
                            vertices = [];
                            updateStatus('Click to add vertices. Right-click to finish.');
                        };

                        window.cancelDrawing = function() {
                            isDrawing = false;
                            vertices = [];
                            if (currentPolyline) {
                                window.leafletMap.removeLayer(currentPolyline);
                                currentPolyline = null;
                            }
                            updateStatus('Drawing cancelled');
                        };
                    }

                    // Start loading Leaflet.Draw
                    loadLeafletDraw();

                    console.log('Leaflet map initialized at Portland Harbour');
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
        zoomIn.setOnAction(_ -> executeMapScript("window.leafletMap.zoomIn();"));

        Button zoomOut = new Button("-");
        zoomOut.setPrefWidth(50);
        zoomOut.setOnAction(_ -> executeMapScript("window.leafletMap.zoomOut();"));

        Button resetView = new Button("Reset View");
        resetView.setPrefWidth(100);
        resetView.setOnAction(_ -> handleResetView());

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
        if (waypoints == null || waypoints.isEmpty()) return;

        // Build Leaflet polyline coordinates
        StringBuilder coordinates = new StringBuilder();
        for (int i = 0; i < waypoints.size(); i++) {
            var w = waypoints.get(i);
            coordinates.append("[").append(w.getPosition().getLatitude()).append(", ")
                    .append(w.getPosition().getLongitude()).append("]");
            if (i < waypoints.size() - 1) coordinates.append(", ");
        }

        String script = String.format("""
            var polyline = L.polyline([%s], {
                color: 'blue',
                weight: 1,
                opacity: 0.6
            }).addTo(window.leafletMap);
            window.drawnPolylines.push(polyline);
            console.log('Track polyline rendered with %d waypoints');
            """, coordinates.toString(), waypoints.size());

        executeMapScript(script);
        System.out.println("Rendered " + waypoints.size() + " waypoints");
    }

    /**
     * Update USV position marker with heading orientation
     */
    public void updatePlatformPosition(Position position, double heading) {
        if (position == null) return;

        String script = String.format("""
            var marker = L.circleMarker([%f, %f], {
                radius: 8,
                fillColor: '#FF0000',
                color: '#8B0000',
                weight: 2,
                opacity: 0.9,
                fillOpacity: 0.8
            }).addTo(window.leafletMap);
            marker.bindPopup('USV - Heading: %.1f°');
            window.markers.push(marker);
            console.log('USV marker placed');
            """, position.getLatitude(), position.getLongitude(), heading);

        executeMapScript(script);
        System.out.println("Updated position: " + position + ", heading: " + heading);
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
                // Clear previous track polylines
                window.drawnPolylines.forEach(p => window.leafletMap.removeLayer(p));

                // Draw updated track
                var trackLine = L.polyline([%s], {
                    color: 'cyan',
                    weight: 2,
                    opacity: 0.7
                }).addTo(window.leafletMap);
                window.drawnPolylines = [trackLine];
                console.log('Track updated with %d points');
                """, coordinates.toString(), trackPoints.size());

            executeMapScript(script);
        }

        System.out.println("Added track point: " + position + " (total: " + trackPoints.size() + ")");
    }

    /**
     * Clear all overlays (polygons, tracks, markers)
     */
    public void clearOverlays() {
        String script = """
            // Clear all polygons
            window.drawnPolygons.forEach(p => window.leafletMap.removeLayer(p));
            window.drawnPolygons = [];

            // Clear all polylines
            window.drawnPolylines.forEach(p => window.leafletMap.removeLayer(p));
            window.drawnPolylines = [];

            // Clear all markers
            window.markers.forEach(m => window.leafletMap.removeLayer(m));
            window.markers = [];

            console.log('Map overlays cleared');
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
