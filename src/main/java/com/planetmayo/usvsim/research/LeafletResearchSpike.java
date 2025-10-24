package com.planetmayo.usvsim.research;

import io.github.makbn.jlmap.JLProperties;
import io.github.makbn.jlmap.fx.JLMapView;
import io.github.makbn.jlmap.listener.JLAction;
import io.github.makbn.jlmap.listener.event.ClickEvent;
import io.github.makbn.jlmap.listener.event.MapEvent;
import io.github.makbn.jlmap.listener.event.MoveEvent;
import io.github.makbn.jlmap.listener.event.ZoomEvent;
import io.github.makbn.jlmap.map.JLMapProvider;
import io.github.makbn.jlmap.model.JLLatLng;
import io.github.makbn.jlmap.model.JLOptions;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * Research Spike: Demonstrates java_leaflet library approach for Leaflet integration
 *
 * COMPARISON WITH OUR WebView APPROACH:
 *
 * java_leaflet (JLMapView):
 * - Wraps Leaflet.js in a managed JavaFX component
 * - Built-in event system (MapEvent, ClickEvent, MoveEvent, ZoomEvent)
 * - Fluent builder pattern for configuration
 * - Explicit min-size constraints (600x800px default)
 * - UI layers, vector layers, GeoJSON layers with styling
 * - Marker popups and context menus
 *
 * Our WebView Approach:
 * - Raw JavaScript integration via WebEngine
 * - Manual event handling via window callbacks
 * - Full control over HTML/CSS/JS
 * - Lighter weight (no library wrapper)
 * - Click-to-draw fallback when CDNs unavailable
 *
 * KEY INSIGHT (The Critical Pattern):
 * Both approaches MUST use explicit sizing:
 *   root.setMinHeight(600)
 *   root.setMinWidth(800)
 *   stage.setMinHeight(600)
 *   stage.setMinWidth(800)
 *
 * This prevents 0x0 initialization of WebView/JLMapView by breaking the
 * USE_COMPUTED_SIZE dependency chain. Without this, Leaflet initializes
 * with no viewport and tiles never render.
 */
public class LeafletResearchSpike extends Application {

    @Override
    public void start(Stage stage) {
        System.out.println("Starting LeafletResearchSpike with JLMapView");

        // Build map using JLMapView builder pattern (java_leaflet library)
        // Using OpenStreetMap Mapnik (free, no API key needed)
        final JLMapView map = JLMapView.builder()
                .jlMapProvider(JLMapProvider.OSM_MAPNIK.build())
                .startCoordinate(new JLLatLng(50.6, -2.4))  // Portland Harbour
                .showZoomController(true)
                .build();

        // ===== CRITICAL FIX: Explicit HEIGHT (not just min-height) =====
        // Stack Overflow solution: Leaflet needs ACTUAL height, not minimum
        // Without explicit height, Leaflet calculates 0 height → fetches no tiles
        map.setPrefHeight(600);  // EXPLICIT height for tile calculation
        map.setPrefWidth(800);   // EXPLICIT width for tile calculation
        map.setMinHeight(400);   // Also set minimums
        map.setMinWidth(600);

        // Create root container with AnchorPane (pattern from LeafletTestJFX)
        AnchorPane root = new AnchorPane(map);
        root.setBackground(Background.EMPTY);

        // Also set explicit dimensions on root
        root.setPrefHeight(JLProperties.INIT_MIN_HEIGHT_STAGE);  // 600px
        root.setPrefWidth(JLProperties.INIT_MIN_WIDTH_STAGE);    // 800px
        root.setMinHeight(JLProperties.INIT_MIN_HEIGHT_STAGE);
        root.setMinWidth(JLProperties.INIT_MIN_WIDTH_STAGE);

        // Create scene
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);

        // Configure stage with SAME constraints (also critical)
        stage.setMinHeight(JLProperties.INIT_MIN_HEIGHT_STAGE);
        stage.setMinWidth(JLProperties.INIT_MIN_WIDTH_STAGE);
        stage.setTitle("Research Spike: java_leaflet JLMapView");
        stage.setScene(scene);
        stage.show();

        // Center on screen (java_leaflet example pattern)
        Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
        stage.setX((primScreenBounds.getWidth() - stage.getWidth()) / 2);
        stage.setY(100);

        // Set up event listeners (demonstrates java_leaflet event system)
        map.setOnActionListener((_, event) -> {
            System.out.println("onActionReceived: " + event);

            if (event instanceof MoveEvent moveEvent) {
                System.out.println("move event: center=" + moveEvent.center() + ", zoom=" + moveEvent.zoomLevel());

            } else if (event instanceof ClickEvent clickEvent) {
                System.out.println("click event: " + clickEvent.center());
                // Add popup at clicked location
                map.getUiLayer().addPopup(clickEvent.center(),
                    "Clicked at " + clickEvent.center(),
                    JLOptions.builder().closeButton(false).autoClose(true).build());

            } else if (event instanceof ZoomEvent zoomEvent) {
                System.out.println("zoom event: level=" + zoomEvent.zoomLevel());

            } else if (event instanceof MapEvent mapEvent &&
                       mapEvent.action() == JLAction.MAP_LOADED) {
                System.out.println("Map loaded successfully");
                addDemoElements(map);
            }
        });

        System.out.println("=== RESEARCH SPIKE INITIALIZED ===");
        System.out.println("Min height: " + JLProperties.INIT_MIN_HEIGHT_STAGE + " px");
        System.out.println("Min width: " + JLProperties.INIT_MIN_WIDTH_STAGE + " px");
        System.out.println("CRITICAL PATTERN: Explicit min-size prevents 0x0 initialization");
        System.out.println("This allows Leaflet.js to render tiles immediately on map load");
    }

    /**
     * Add demo markers and shapes to the map
     */
    private void addDemoElements(JLMapView map) {
        try {
            // Add marker at Portland Harbour
            map.getUiLayer().addMarker(
                new JLLatLng(50.6, -2.4),
                "Portland Harbour",
                true
            );

            // Add circle marker
            map.getVectorLayer().addCircleMarker(new JLLatLng(50.6, -2.4));

            System.out.println("Demo elements added: marker + circle marker");
        } catch (Exception e) {
            System.err.println("Error adding demo elements: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
