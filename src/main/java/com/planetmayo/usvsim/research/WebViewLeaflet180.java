package com.planetmayo.usvsim.research;

import javafx.application.Application;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

/**
 * Research Spike: WebView with Leaflet 1.8.0
 *
 * SOLUTION from Stack Overflow:
 * JavaFX 21+ has known issues with Leaflet 1.9.x
 * Downgrading to Leaflet 1.8.0 fixes the missing tiles issue
 *
 * Reference: https://stackoverflow.com/questions/77764181/webview-in-javafx-not-showing-leaflet-map-correct
 */
public class WebViewLeaflet180 extends Application {

    @Override
    public void start(Stage stage) {
        System.out.println("Starting WebView with Leaflet 1.8.0 (known working version)");

        // Create WebView
        WebView webView = new WebView();
        WebEngine webEngine = webView.getEngine();
        webEngine.setJavaScriptEnabled(true);

        // Set explicit dimensions
        webView.setPrefHeight(600);
        webView.setPrefWidth(800);

        // HTML with Leaflet 1.8.0 (the WORKING version for JavaFX)
        String html = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8" />
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Leaflet 1.8.0 - JavaFX Compatible Version</title>

                <!-- CRITICAL: Use Leaflet 1.8.0 (NOT 1.9.x) for JavaFX compatibility -->
                <link rel="stylesheet" href="https://unpkg.com/leaflet@1.8.0/dist/leaflet.css" />
                <script src="https://unpkg.com/leaflet@1.8.0/dist/leaflet.js"></script>

                <style>
                    * {
                        margin: 0;
                        padding: 0;
                        box-sizing: border-box;
                    }

                    html, body {
                        height: 100%;
                        width: 100%;
                        overflow: hidden;
                    }

                    #map {
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
                        padding: 15px;
                        border-radius: 4px;
                        box-shadow: 0 2px 8px rgba(0,0,0,0.2);
                        z-index: 1000;
                        font-family: monospace;
                        font-size: 12px;
                        max-width: 350px;
                    }

                    .status-ok { color: green; font-weight: bold; }
                    .status-error { color: red; }
                    .status-info { color: blue; }
                    .version-info {
                        background: #e8f5e9;
                        padding: 5px;
                        margin-top: 5px;
                        border-radius: 3px;
                    }
                </style>
            </head>
            <body>
                <div id="map"></div>

                <div id="status">
                    <strong>Leaflet 1.8.0 - JavaFX Fix</strong><br/>
                    <div class="version-info">
                        <strong>Leaflet Version:</strong> <span id="leaflet-version"></span><br/>
                        <strong>Environment:</strong> <span id="environment"></span>
                    </div>
                    <br/>
                    <span id="tile-status">Initializing...</span><br/>
                    <span id="map-info"></span>
                </div>

                <script>
                    // Show Leaflet version
                    document.getElementById('leaflet-version').innerHTML = L.version;

                    // Detect environment
                    const isWebView = navigator.userAgent.indexOf('JavaFX') > -1;
                    document.getElementById('environment').innerHTML =
                        isWebView ? 'JavaFX WebView' : 'Standard Browser';

                    // Tile tracking
                    var tilesLoading = 0;
                    var tilesLoaded = 0;
                    var tilesFailed = 0;

                    function updateStatus() {
                        const status = document.getElementById('tile-status');
                        status.innerHTML =
                            '<span class="status-ok">Loaded: ' + tilesLoaded + '</span> | ' +
                            '<span class="status-info">Loading: ' + tilesLoading + '</span> | ' +
                            '<span class="status-error">Failed: ' + tilesFailed + '</span>';
                    }

                    // Delay initialization for WebView
                    setTimeout(function() {
                        console.log('Initializing Leaflet 1.8.0...');

                        try {
                            // Create map
                            const map = L.map('map', {
                                center: [50.6, -2.4], // Portland Harbour
                                zoom: 14,
                                zoomControl: true
                            });

                            // Add tile layer
                            const tileLayer = L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                                attribution: '© OpenStreetMap contributors',
                                maxZoom: 19,
                                tileSize: 256,
                                detectRetina: false
                            });

                            // Track tile events
                            tileLayer.on('tileloadstart', function() {
                                tilesLoading++;
                                updateStatus();
                            });

                            tileLayer.on('tileload', function() {
                                tilesLoading--;
                                tilesLoaded++;
                                updateStatus();
                            });

                            tileLayer.on('tileerror', function() {
                                tilesLoading--;
                                tilesFailed++;
                                updateStatus();
                                console.error('Tile failed');
                            });

                            tileLayer.addTo(map);

                            // Add test elements
                            L.marker([50.6, -2.4])
                                .addTo(map)
                                .bindPopup('Portland Harbour<br/>Leaflet 1.8.0 Test');

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

                            // Get map size
                            const size = map.getSize();
                            document.getElementById('map-info').innerHTML =
                                'Map size: ' + size.x + 'x' + size.y + 'px';

                            // Periodic revalidation (helps with WebView)
                            if (isWebView) {
                                let count = 0;
                                const interval = setInterval(function() {
                                    if (count++ < 5) {
                                        map.invalidateSize();
                                        console.log('Revalidated map size');
                                    } else {
                                        clearInterval(interval);
                                    }
                                }, 1000);
                            }

                            console.log('✓ Leaflet 1.8.0 initialized successfully');

                        } catch (e) {
                            console.error('Error:', e);
                            document.getElementById('tile-status').innerHTML =
                                '<span class="status-error">Error: ' + e.message + '</span>';
                        }
                    }, 500); // Delay for WebView
                </script>
            </body>
            </html>
            """;

        // Load HTML
        webEngine.loadContent(html);

        // Monitor loading
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                System.out.println("✓ WebView loaded - Using Leaflet 1.8.0");
            }
        });

        // Create scene
        BorderPane root = new BorderPane(webView);
        Scene scene = new Scene(root, 800, 600);

        stage.setTitle("Leaflet 1.8.0 - JavaFX Compatible Version");
        stage.setScene(scene);
        stage.show();

        System.out.println("=== LEAFLET 1.8.0 FIX APPLIED ===");
        System.out.println("This version is confirmed to work with JavaFX 21+");
        System.out.println("Reference: stackoverflow.com/questions/77764181");
    }

    public static void main(String[] args) {
        launch(args);
    }
}