package com.planetmayo.usvsim;

import com.planetmayo.usvsim.view.MainView;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Main entry point for USV Mission Planning & Simulation System.
 *
 * Extends JavaFX Application to provide desktop UI.
 * Launches MainView with 70/30 layout (map/controls).
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
        // Create main application view
        MainView mainView = new MainView(stage);

        // Display the application
        mainView.show();
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
