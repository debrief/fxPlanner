package com.planetmayo.fxplanner;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Simple Hello World JavaFX application for fxPlanner.
 * This is Phase 0: Foundation - a placeholder app for testing the build system.
 */
public class HelloWorldApp extends Application {

    private int clickCount = 0;

    @Override
    public void start(Stage primaryStage) {
        // Create the main label
        Label helloLabel = new Label("Hello World from fxPlanner!");
        helloLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // Create a subtitle label
        Label subtitleLabel = new Label("Sample JavaFX UXV Planning/Monitoring Tool");
        subtitleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");

        // Create a counter label
        Label counterLabel = new Label("Button clicks: 0");
        counterLabel.setStyle("-fx-font-size: 16px; -fx-padding: 20px 0 0 0;");

        // Create a button
        Button helloButton = new Button("Click Me!");
        helloButton.setStyle("-fx-font-size: 14px; -fx-padding: 10px 20px;");
        helloButton.setOnAction(event -> {
            clickCount++;
            counterLabel.setText("Button clicks: " + clickCount);
            if (clickCount == 1) {
                helloLabel.setText("Thanks for clicking!");
            } else if (clickCount >= 5) {
                helloLabel.setText("You really like clicking! 🎉");
            }
        });

        // Create layout
        VBox root = new VBox(15);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(50));
        root.getChildren().addAll(helloLabel, subtitleLabel, helloButton, counterLabel);

        // Apply some styling to the root
        root.setStyle("-fx-background-color: #f5f5f5;");

        // Create and configure the scene
        Scene scene = new Scene(root, 600, 400);

        // Configure the stage
        primaryStage.setTitle("fxPlanner - Hello World");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Main entry point for the application.
     * @param args command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
