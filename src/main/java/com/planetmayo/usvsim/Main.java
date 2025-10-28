package com.planetmayo.usvsim;

import com.planetmayo.usvsim.controller.MissionController;
import com.planetmayo.usvsim.model.geometry.Position;
import com.planetmayo.usvsim.model.mission.Mission;
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

        // Create mission model (default: start at Portland Harbour)
        // Start position: 50°34'16.3"N 2°27'09.1"W = 50.5712°N, -2.4525°W
        Mission mission = Mission.createDefault(Position.of(50.5712, -2.4525));

        // Create controller to wire UI to model (constructor has side effects - wires everything up)
        new MissionController(mission, mainView);

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
