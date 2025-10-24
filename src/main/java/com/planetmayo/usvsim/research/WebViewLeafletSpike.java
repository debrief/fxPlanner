package com.planetmayo.usvsim.research;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

/**
 * Research Spike: WebView + Leaflet with 100vh height fix
 *
 * Based on Stack Overflow solution: https://stackoverflow.com/questions/55009403/missing-leaflet-map-tiles-when-using-react-leaflet
 *
 * KEY INSIGHT: Leaflet needs EXPLICIT height, not min-height
 * The map container must have height: 100vh (viewport height) for tiles to render
 *
 * This spike tests pure WebView with properly sized Leaflet container.
 */
public class WebViewLeafletSpike extends Application {

    @Override
    public void start(Stage stage) {
        System.out.println("Starting WebView Leaflet Spike with 100vh fix");

        // Create WebView
        WebView webView = new WebView();
        WebEngine webEngine = webView.getEngine();
        webEngine.setJavaScriptEnabled(true);

        // CRITICAL: Set explicit dimensions on WebView itself
        webView.setPrefHeight(600);
        webView.setPrefWidth(800);

        // Create the HTML with 100vh height fix
        String html = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8" />
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>WebView Leaflet - 100vh Fix</title>

                <!-- Leaflet CSS -->
                <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />

                <!-- Leaflet JS -->
                <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>

                <style>
                    /* CRITICAL FIX: Use explicit height units */
                    * {
                        margin: 0;
                        padding: 0;
                        box-sizing: border-box;
                    }

                    html, body {
                        /* CRITICAL: 100% height on html/body */
                        height: 100%;
                        width: 100%;
                        margin: 0;
                        padding: 0;
                        overflow: hidden;
                    }

                    #map {
                        /* CRITICAL: Use 100vh (viewport height) not 100% */
                        /* This ensures Leaflet gets actual pixel dimensions */
                        height: 100vh;
                        width: 100vw;
                        position: absolute;
                        top: 0;
                        left: 0;
                    }

                    #status {
                        position: absolute;
                        top: 10px;
                        right: 10px;
                        background: rgba(255, 255, 255, 0.95);
                        padding: 10px;
                        border-radius: 4px;
                        box-shadow: 0 2px 8px rgba(0,0,0,0.2);
                        z-index: 1000;
                        font-family: monospace;
                        font-size: 12px;
                        max-width: 300px;
                    }

                    .status-ok { color: green; }
                    .status-error { color: red; }
                    .status-info { color: blue; }
                </style>
            </head>
            <body>
                <!-- Map container with 100vh height -->
                <div id="map"></div>

                <!-- Status panel -->
                <div id="status">
                    <strong>100vh Height Fix Applied</strong><br/>
                    <span id="dimensions"></span><br/>
                    <span id="tile-status">Initializing...</span>
                </div>

                <script>
                    // Global variables for tile tracking
                    var tilesLoading = 0;
                    var tilesLoaded = 0;
                    var tilesFailed = 0;

                    function updateTileStatus() {
                        const status = document.getElementById('tile-status');
                        if (status) {
                            status.innerHTML =
                                '<span class="status-ok">Loaded: ' + tilesLoaded + '</span> | ' +
                                '<span class="status-info">Loading: ' + tilesLoading + '</span> | ' +
                                '<span class="status-error">Failed: ' + tilesFailed + '</span>';
                        }
                    }

                    // Log container dimensions immediately
                    const mapDiv = document.getElementById('map');
                    const actualHeight = mapDiv.offsetHeight;
                    const actualWidth = mapDiv.offsetWidth;
                    const computedStyle = window.getComputedStyle(mapDiv);

                    document.getElementById('dimensions').innerHTML =
                        'Container: ' + actualWidth + 'x' + actualHeight + 'px<br/>' +
                        'Computed height: ' + computedStyle.height;

                    console.log('Map container dimensions:', actualWidth + 'x' + actualHeight);
                    console.log('Computed style height:', computedStyle.height);

                    // Initialize map
                    try {
                        const map = L.map('map', {
                            center: [50.6, -2.4], // Portland Harbour
                            zoom: 14,
                            zoomControl: true,
                            preferCanvas: false // Use SVG renderer
                        });

                        // Try different tile server (CartoDB as fallback)
                        const tileUrls = {
                            osm: 'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',
                            cartodb: 'https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}.png'
                        };

                        // Add OpenStreetMap tiles with error handling
                        const tileLayer = L.tileLayer(tileUrls.osm, {
                            attribution: '© OpenStreetMap contributors',
                            maxZoom: 19,
                            tileSize: 256,
                            detectRetina: false, // Disable retina to simplify debugging
                            crossOrigin: true // Enable CORS
                        });

                        // Track tile loading events
                        tileLayer.on('tileloadstart', function(e) {
                            tilesLoading++;
                            updateTileStatus();
                        });

                        tileLayer.on('tileload', function(e) {
                            tilesLoading--;
                            tilesLoaded++;
                            updateTileStatus();
                            console.log('Tile loaded successfully');
                        });

                        tileLayer.on('tileerror', function(e) {
                            tilesLoading--;
                            tilesFailed++;
                            updateTileStatus();
                            console.error('Tile failed to load');
                            // Try CartoDB as fallback
                            if (tilesFailed === 1) {
                                console.log('Trying CartoDB tiles as fallback...');
                                map.removeLayer(tileLayer);
                                const cartoLayer = L.tileLayer(tileUrls.cartodb, {
                                    attribution: '© CartoDB',
                                    maxZoom: 19
                                }).addTo(map);
                            }
                        });

                        tileLayer.addTo(map);

                        // Get Leaflet's view of the container size
                        const size = map.getSize();
                        console.log('Leaflet container size:', size.x + 'x' + size.y);
                        document.getElementById('dimensions').innerHTML +=
                            '<br/>Leaflet sees: ' + size.x + 'x' + size.y + 'px';

                        // Add a test marker
                        L.marker([50.6, -2.4])
                            .addTo(map)
                            .bindPopup('Portland Harbour<br/>Research Location')
                            .openPopup();

                        // Add a polygon
                        L.polygon([
                            [50.65, -2.35],
                            [50.55, -2.45],
                            [50.60, -2.50],
                            [50.70, -2.40]
                        ], {
                            color: 'red',
                            weight: 2,
                            fillColor: 'red',
                            fillOpacity: 0.2
                        }).addTo(map);

                        // Force a resize after a delay (sometimes helps)
                        setTimeout(function() {
                            map.invalidateSize();
                            console.log('Map resized, new size:', map.getSize());
                        }, 500);

                        console.log('✓ Map initialized successfully');

                    } catch (e) {
                        console.error('Error initializing map:', e);
                        document.getElementById('tile-status').innerHTML =
                            '<span class="status-error">Error: ' + e.message + '</span>';
                    }
                </script>
            </body>
            </html>
            """;

        // Load the HTML
        webEngine.loadContent(html);

        // Monitor console output
        webEngine.setOnAlert(event -> System.out.println("JavaScript alert: " + event.getData()));

        // Add console.log bridge
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                System.out.println("WebView loaded successfully");

                // Inject console bridge
                webEngine.executeScript("""
                    if (typeof console.originalLog === 'undefined') {
                        console.originalLog = console.log;
                        console.log = function() {
                            console.originalLog.apply(console, arguments);
                            alert('CONSOLE: ' + Array.prototype.slice.call(arguments).join(' '));
                        };
                        console.error = function() {
                            console.originalLog.apply(console, arguments);
                            alert('ERROR: ' + Array.prototype.slice.call(arguments).join(' '));
                        };
                    }
                """);
            }
        });

        // Create root with explicit sizing
        BorderPane root = new BorderPane(webView);
        root.setPrefHeight(600);
        root.setPrefWidth(800);

        // Create scene
        Scene scene = new Scene(root, 800, 600);

        // Configure stage
        stage.setTitle("WebView Leaflet Spike - 100vh Fix");
        stage.setScene(scene);
        stage.setMinHeight(600);
        stage.setMinWidth(800);
        stage.show();

        System.out.println("WebView Leaflet Spike initialized");
        System.out.println("Key fixes applied:");
        System.out.println("1. HTML/body height: 100%");
        System.out.println("2. Map container height: 100vh (viewport height)");
        System.out.println("3. WebView explicit size: 800x600");
        System.out.println("4. Position: absolute on map container");
    }

    public static void main(String[] args) {
        launch(args);
    }
}