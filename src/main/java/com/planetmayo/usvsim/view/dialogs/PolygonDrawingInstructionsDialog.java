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
 * Modal dialog showing instructions for polygon drawing on map.
 *
 * Tells user to:
 * 1. Look for drawing toolbar on map (Leaflet.Draw controls or click-to-draw)
 * 2. Click polygon tool to start (or click to add vertices)
 * 3. Click vertices on map
 * 4. Complete polygon and click Done
 */
public class PolygonDrawingInstructionsDialog extends Dialog<Void> {

    public PolygonDrawingInstructionsDialog() {
        setTitle("Draw Search Area");
        setHeaderText("Define Your Search Area");

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
        Label titleLabel = new Label("How to Draw Your Search Area");
        titleLabel.setFont(Font.font(null, FontWeight.BOLD, 14));
        root.getChildren().add(titleLabel);

        // Instructions
        Label instructions = new Label(
            "Method 1 - Drawing Toolbar:\n" +
            "  • Look for the toolbar in the TOP-LEFT corner of the map\n" +
            "  • Click the POLYGON ICON to start drawing\n" +
            "  • Click vertices on the map to define the search area\n" +
            "\n" +
            "Method 2 - Click-to-Draw (if toolbar unavailable):\n" +
            "  • Simply click on the map to add vertices\n" +
            "  • Each click adds a vertex (blue line shows progress)\n" +
            "\n" +
            "Requirements:\n" +
            "  • Minimum 3 vertices (triangle shape)\n" +
            "  • Click \"Done Drawing\" when finished"
        );
        instructions.setWrapText(true);
        instructions.setTextAlignment(TextAlignment.LEFT);
        instructions.setFont(Font.font(11));
        root.getChildren().add(instructions);

        // Minimum vertices reminder
        Label reminder = new Label("💡 Draw a triangle or larger polygon to define your search area");
        reminder.setStyle("-fx-text-fill: #0066cc; -fx-font-size: 10;");
        reminder.setWrapText(true);
        root.getChildren().add(reminder);

        return root;
    }
}
