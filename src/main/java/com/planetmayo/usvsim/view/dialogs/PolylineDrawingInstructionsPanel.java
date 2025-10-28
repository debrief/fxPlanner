package com.planetmayo.usvsim.view.dialogs;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

/**
 * Inline panel showing instructions for polyline drawing on map (waypoint transit).
 */
public class PolylineDrawingInstructionsPanel extends VBox {
    private Button doneButton;
    private Runnable onDoneDrawing;
    private Runnable onCancel;

    public PolylineDrawingInstructionsPanel() {
        setSpacing(15);
        setPadding(new Insets(15));
        setStyle("-fx-background-color: white; -fx-border-color: #2196F3; -fx-border-width: 2;");
        setMaxWidth(500);

        // Title
        Label title = new Label("Draw Waypoint Route");
        title.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");

        // Subtitle
        Label subtitle = new Label("How to Draw Your Waypoint Route");
        subtitle.setFont(Font.font(null, FontWeight.BOLD, 12));

        // Instructions
        Label instructions = new Label(
            "Drawing a Polyline:\n" +
            "  • Click on the map to add each waypoint\n" +
            "  • Each click adds a waypoint (orange line shows route)\n" +
            "  • RE-CLICK THE LAST POINT (<5px) to finish the route\n" +
            "\n" +
            "Route Requirements:\n" +
            "  • Minimum 2 waypoints (start and end points)\n" +
            "  • Use \"Cancel\" button to cancel at any time\n" +
            "  • Click \"Done Drawing\" when route is complete"
        );
        instructions.setWrapText(true);
        instructions.setTextAlignment(TextAlignment.LEFT);
        instructions.setFont(Font.font(11));

        // Tip
        Label tip = new Label("💡 TIP: Re-clicking the LAST waypoint (not the first) finishes the route");
        tip.setStyle("-fx-text-fill: #ff6600; -fx-font-size: 10; -fx-font-weight: bold;");
        tip.setWrapText(true);

        // Buttons
        HBox buttons = createButtonBar();

        getChildren().addAll(title, subtitle, instructions, tip, buttons);
    }

    private HBox createButtonBar() {
        HBox box = new HBox(10);
        box.setPadding(new Insets(10, 0, 0, 0));

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setPrefWidth(100);
        cancelBtn.setOnAction(_ -> {
            if (onCancel != null) onCancel.run();
        });

        doneButton = new Button("Done Drawing (draw 2+ waypoints first)");
        doneButton.setPrefWidth(280);
        doneButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        doneButton.setDisable(true);
        doneButton.setOnAction(_ -> {
            if (onDoneDrawing != null) onDoneDrawing.run();
        });

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        box.getChildren().addAll(cancelBtn, spacer, doneButton);
        return box;
    }

    public Button getDoneButton() {
        return doneButton;
    }

    public void setOnDoneDrawing(Runnable handler) {
        this.onDoneDrawing = handler;
    }

    public void setOnCancel(Runnable handler) {
        this.onCancel = handler;
    }
}
