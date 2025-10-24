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
 * Inline panel showing instructions for polygon drawing on map.
 */
public class PolygonDrawingInstructionsPanel extends VBox {
    private Button doneButton;
    private Runnable onDoneDrawing;
    private Runnable onCancel;

    public PolygonDrawingInstructionsPanel() {
        setSpacing(15);
        setPadding(new Insets(15));
        setStyle("-fx-background-color: white; -fx-border-color: #2196F3; -fx-border-width: 2;");
        setMaxWidth(500);

        // Title
        Label title = new Label("Draw Search Area");
        title.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");

        // Subtitle
        Label subtitle = new Label("How to Draw Your Search Area");
        subtitle.setFont(Font.font(null, FontWeight.BOLD, 12));

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
            "  • Click NEAR THE FIRST VERTEX (<5px) to close polygon\n" +
            "\n" +
            "Requirements:\n" +
            "  • Minimum 3 vertices (triangle shape)\n" +
            "  • Use \"Cancel\" button to cancel at any time"
        );
        instructions.setWrapText(true);
        instructions.setTextAlignment(TextAlignment.LEFT);
        instructions.setFont(Font.font(11));

        // Reminder
        Label reminder = new Label("💡 Draw a triangle or larger polygon to define your search area");
        reminder.setStyle("-fx-text-fill: #0066cc; -fx-font-size: 10;");
        reminder.setWrapText(true);

        // Buttons
        HBox buttons = createButtonBar();

        getChildren().addAll(title, subtitle, instructions, reminder, buttons);
    }

    private HBox createButtonBar() {
        HBox box = new HBox(10);
        box.setPadding(new Insets(10, 0, 0, 0));

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setPrefWidth(100);
        cancelBtn.setOnAction(e -> {
            if (onCancel != null) onCancel.run();
        });

        doneButton = new Button("Done Drawing (draw 1+ vertices first)");
        doneButton.setPrefWidth(280);
        doneButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        doneButton.setDisable(true);
        doneButton.setOnAction(e -> {
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
