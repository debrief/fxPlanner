package com.planetmayo.usvsim;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Main entry point for USV Mission Planning & Simulation System.
 *
 * Extends JavaFX Application to provide desktop UI.
 */
public class Main extends Application {

    /**
     * Start method called when JavaFX application launches.
     *
     * @param stage the primary stage for this application
     * @throws Exception if application fails to initialize
     */
    @Override
    public void start(Stage stage) throws Exception {
        // Create root layout
        BorderPane root = new BorderPane();
        root.setCenter(new Label("USV Mission Planner - Loading..."));

        // Create scene
        Scene scene = new Scene(root, 1200, 800);

        // Configure stage
        stage.setTitle("USV Mission Planner");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Application entry point.
     *
     * @param args command line arguments (unused)
     */
    public static void main(String[] args) {
        launch(args);
    }
}
