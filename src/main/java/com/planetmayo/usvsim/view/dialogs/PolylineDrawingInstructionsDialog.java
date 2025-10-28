package com.planetmayo.usvsim.view.dialogs;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

/**
 * Modal dialog showing instructions for polyline drawing on map (waypoint transit).
 *
 * Tells user to:
 * 1. Click to add waypoints
 * 2. Re-click the last point to finish the polyline
 * 3. Click Done when finished
 */
public class PolylineDrawingInstructionsDialog extends Dialog<Void> {

    public PolylineDrawingInstructionsDialog() {
        setTitle("Draw Waypoint Route");
        setHeaderText("Define Your Waypoint Route");

        // Create content
        VBox content = createContent();
        getDialogPane().setContent(content);

        // Done button - user clicks when finished drawing
        getDialogPane().getButtonTypes().add(ButtonType.OK);
        Button doneButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
        doneButton.setText("Done Drawing");
        getDialogPane().setMinWidth(420);
    }

    private VBox createContent() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-border-color: #f0f0f0; -fx-border-width: 1;");

        // Title
        Label titleLabel = new Label("How to Draw Your Waypoint Route");
        titleLabel.setFont(Font.font(null, FontWeight.BOLD, 14));
        root.getChildren().add(titleLabel);

        // Instructions
        Label instructions = new Label(
            "Drawing a Polyline:\n" +
            "  • Click on the map to add each waypoint\n" +
            "  • Each click adds a waypoint (orange line shows route)\n" +
            "  • RE-CLICK THE LAST POINT (<5px) to finish the route\n" +
            "\n" +
            "Route Requirements:\n" +
            "  • Minimum 2 waypoints (start and end points)\n" +
            "  • Use \"Cancel Drawing\" button to cancel at any time\n" +
            "  • Click \"Done Drawing\" when route is complete"
        );
        instructions.setWrapText(true);
        instructions.setTextAlignment(TextAlignment.LEFT);
        instructions.setFont(Font.font(11));
        root.getChildren().add(instructions);

        // Tip
        Label tip = new Label("💡 TIP: Re-clicking the LAST waypoint (not the first) finishes the route");
        tip.setStyle("-fx-text-fill: #ff6600; -fx-font-size: 10; -fx-font-weight: bold;");
        tip.setWrapText(true);
        root.getChildren().add(tip);

        return root;
    }
}
