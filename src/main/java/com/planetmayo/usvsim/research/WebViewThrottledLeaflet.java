package com.planetmayo.usvsim.research;

import javafx.application.Application;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

/**
 * Research Spike: WebView with THROTTLED tile loading
 *
 * Since standalone HTML works perfectly in browser but has missing tiles in WebView,
 * the issue is WebView-specific. Likely causes:
 * 1. Concurrent HTTP request limits in WebView
 * 2. Memory/cache constraints
 * 3. JavaScript execution timing
 *
 * Solution: Throttle tile loading to work within WebView's constraints
 */
public class WebViewThrottledLeaflet extends Application {

    @Override
    public void start(Stage stage) {
        System.out.println("Starting WebView with Throttled Tile Loading");

        // Create WebView
        WebView webView = new WebView();
        WebEngine webEngine = webView.getEngine();
        webEngine.setJavaScriptEnabled(true);

        // Set explicit dimensions
        webView.setPrefHeight(600);
        webView.setPrefWidth(800);

        // Create HTML with THROTTLED tile loading
        String html = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8" />
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>WebView Throttled Leaflet</title>

                <!-- Leaflet CSS -->
                <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />

                <!-- Leaflet JS -->
                <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>

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
                        padding: 10px;
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
                    .status-warning { color: orange; }
                </style>
            </head>
            <body>
                <div id="map"></div>

                <div id="status">
                    <strong>WebView Throttled Loading</strong><br/>
                    <span id="init-status">Waiting for page load...</span><br/>
                    <span id="tile-status">Tiles not started</span><br/>
                    <span id="throttle-info"></span>
                </div>

                <script>
                    // Detect WebView environment
                    const isWebView = navigator.userAgent.indexOf('JavaFX') > -1;
                    console.log('Environment: ' + (isWebView ? 'JavaFX WebView' : 'Standard Browser'));

                    // Global tile tracking
                    var tilesLoading = 0;
                    var tilesLoaded = 0;
                    var tilesFailed = 0;
                    var map = null;

                    function updateStatus() {
                        const status = document.getElementById('tile-status');
                        if (status) {
                            status.innerHTML =
                                '<span class="status-ok">Loaded: ' + tilesLoaded + '</span> | ' +
                                '<span class="status-info">Loading: ' + tilesLoading + '</span> | ' +
                                '<span class="status-error">Failed: ' + tilesFailed + '</span>';
                        }
                    }

                    function initializeMap() {
                        document.getElementById('init-status').innerHTML =
                            '<span class="status-info">Initializing map...</span>';

                        try {
                            // CRITICAL: WebView-specific configuration
                            const mapOptions = {
                                center: [50.6, -2.4], // Portland Harbour
                                zoom: 14,
                                zoomControl: true,
                                // WebView optimizations
                                preferCanvas: true,     // Canvas renderer (less memory)
                                fadeAnimation: false,   // Disable fade (reduces redraws)
                                markerZoomAnimation: false, // Disable marker animations
                                zoomAnimation: false    // Disable zoom animations
                            };

                            map = L.map('map', mapOptions);

                            // THROTTLED TILE LAYER for WebView
                            const tileOptions = {
                                attribution: '© OpenStreetMap',
                                maxZoom: 19,
                                tileSize: 256,
                                // WebView-specific throttling
                                updateWhenIdle: true,     // Only update when map stops moving
                                updateInterval: 200,      // Throttle updates
                                keepBuffer: 1,           // Reduce tile buffer (default is 2)
                                detectRetina: false,     // Disable retina tiles
                                crossOrigin: null        // Remove CORS (WebView issue?)
                            };

                            // For WebView, add extra throttling
                            if (isWebView) {
                                tileOptions.className = 'webview-tiles';
                                // Reduce concurrent loads by using subdomains sequentially
                                tileOptions.subdomains = 'a';  // Use only one subdomain

                                document.getElementById('throttle-info').innerHTML =
                                    '<span class="status-warning">WebView mode: Throttling enabled</span>';
                            }

                            const tileLayer = L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', tileOptions);

                            // Track tile events
                            let lastTileTime = Date.now();

                            tileLayer.on('tileloadstart', function(e) {
                                tilesLoading++;
                                updateStatus();

                                // In WebView, throttle if too many concurrent
                                if (isWebView && tilesLoading > 2) {
                                    console.log('Throttling: ' + tilesLoading + ' tiles loading');
                                    // Pause tile loading briefly
                                    setTimeout(() => {
                                        map.invalidateSize();
                                    }, 100);
                                }
                            });

                            tileLayer.on('tileload', function(e) {
                                tilesLoading--;
                                tilesLoaded++;
                                const loadTime = Date.now() - lastTileTime;
                                lastTileTime = Date.now();
                                updateStatus();
                                console.log('Tile loaded (', loadTime, 'ms)');
                            });

                            tileLayer.on('tileerror', function(e) {
                                tilesLoading--;
                                tilesFailed++;
                                updateStatus();
                                console.error('Tile failed');

                                // In WebView, retry failed tiles after delay
                                if (isWebView && tilesFailed < 10) {
                                    setTimeout(() => {
                                        console.log('Retrying failed tile...');
                                        e.tile.src = e.tile.src + '?retry=' + Date.now();
                                    }, 1000);
                                }
                            });

                            tileLayer.addTo(map);

                            // Add test markers
                            L.marker([50.6, -2.4])
                                .addTo(map)
                                .bindPopup('Portland Harbour');

                            L.polygon([
                                [50.65, -2.35],
                                [50.55, -2.45],
                                [50.60, -2.50],
                                [50.70, -2.40]
                            ], {
                                color: 'red',
                                weight: 2,
                                fillOpacity: 0.2
                            }).addTo(map);

                            document.getElementById('init-status').innerHTML =
                                '<span class="status-ok">Map initialized</span>';

                            // For WebView, force periodic revalidation
                            if (isWebView) {
                                let revalidateCount = 0;
                                const revalidateInterval = setInterval(() => {
                                    if (revalidateCount++ < 5) {
                                        console.log('Revalidating map size...');
                                        map.invalidateSize();
                                    } else {
                                        clearInterval(revalidateInterval);
                                    }
                                }, 1000);
                            }

                        } catch (e) {
                            console.error('Map init error:', e);
                            document.getElementById('init-status').innerHTML =
                                '<span class="status-error">Error: ' + e.message + '</span>';
                        }
                    }

                    // CRITICAL: Delay initialization for WebView
                    if (isWebView) {
                        console.log('WebView detected, delaying initialization...');
                        // Give WebView time to fully initialize
                        setTimeout(initializeMap, 500);
                    } else {
                        // Browser can initialize immediately
                        initializeMap();
                    }
                </script>
            </body>
            </html>
            """;

        // Load HTML content
        webEngine.loadContent(html);

        // Monitor loading
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            System.out.println("WebView state: " + newState);
            if (newState == Worker.State.SUCCEEDED) {
                System.out.println("WebView loaded successfully");

                // Add console bridge
                webEngine.executeScript("""
                    console.originalLog = console.log;
                    console.originalError = console.error;
                    console.log = function() {
                        console.originalLog.apply(console, arguments);
                    };
                    console.error = function() {
                        console.originalError.apply(console, arguments);
                    };
                """);
            }
        });

        // Create scene
        BorderPane root = new BorderPane(webView);
        Scene scene = new Scene(root, 800, 600);

        stage.setTitle("WebView Throttled Leaflet - Research");
        stage.setScene(scene);
        stage.show();

        System.out.println("=== THROTTLING STRATEGIES APPLIED ===");
        System.out.println("1. Single subdomain (reduces concurrent connections)");
        System.out.println("2. Canvas renderer (less memory than SVG)");
        System.out.println("3. Disabled animations (reduces redraws)");
        System.out.println("4. Reduced tile buffer (keepBuffer: 1)");
        System.out.println("5. UpdateWhenIdle (tiles load after pan/zoom stops)");
        System.out.println("6. Delayed initialization (500ms for WebView)");
        System.out.println("7. Periodic revalidation (forces missed tiles to load)");
    }

    public static void main(String[] args) {
        launch(args);
    }
}