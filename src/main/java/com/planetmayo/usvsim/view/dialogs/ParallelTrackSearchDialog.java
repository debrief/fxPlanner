package com.planetmayo.usvsim.view.dialogs;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Dialog for configuring parallel track search parameters.
 *
 * Inputs:
 * - Track Orientation (0-360°)
 * - Track Spacing (metres, > 0)
 * - Platform Speed (knots, > 0)
 *
 * Features:
 * - Real-time input validation with error messages
 * - Preview of estimated track count and distance
 * - OK/Cancel buttons
 */
public class ParallelTrackSearchDialog extends Dialog<ParallelTrackSearchParams> {
    private final TextField orientationField;
    private final TextField spacingField;
    private final TextField speedField;
    private final Label previewLabel;
    private final Label errorLabel;

    public ParallelTrackSearchDialog() {
        setTitle("Add Parallel Track Search");
        setHeaderText("Configure search pattern parameters");

        // Create content
        VBox content = createContent();
        getDialogPane().setContent(content);

        // Buttons
        getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);

        // Result conversion
        setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return new ParallelTrackSearchParams(
                    Double.parseDouble(orientationField.getText()),
                    Double.parseDouble(spacingField.getText()),
                    Double.parseDouble(speedField.getText())
                );
            }
            return null;
        });
    }

    private VBox createContent() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(15));

        // Input fields
        GridPane grid = createInputGrid();
        root.getChildren().add(grid);

        // Error label
        errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #F44336; -fx-font-size: 10;");
        root.getChildren().add(errorLabel);

        // Preview box
        VBox previewBox = createPreviewBox();
        root.getChildren().add(previewBox);

        return root;
    }

    private GridPane createInputGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        // Orientation
        Label orientLabel = new Label("Track Orientation (°):");
        orientLabel.setStyle("-fx-font-weight: bold;");
        orientationField = new TextField("45");
        orientationField.setPrefWidth(200);
        orientationField.setOnKeyReleased(e -> validateInputs());
        grid.add(orientLabel, 0, 0);
        grid.add(orientationField, 1, 0);

        // Spacing
        Label spacingLabel = new Label("Track Spacing (metres):");
        spacingLabel.setStyle("-fx-font-weight: bold;");
        spacingField = new TextField("500");
        spacingField.setPrefWidth(200);
        spacingField.setOnKeyReleased(e -> validateInputs());
        grid.add(spacingLabel, 0, 1);
        grid.add(spacingField, 1, 1);

        // Speed
        Label speedLabel = new Label("Platform Speed (knots):");
        speedLabel.setStyle("-fx-font-weight: bold;");
        speedField = new TextField("6.0");
        speedField.setPrefWidth(200);
        speedField.setOnKeyReleased(e -> validateInputs());
        grid.add(speedLabel, 0, 2);
        grid.add(speedField, 1, 2);

        return grid;
    }

    private VBox createPreviewBox() {
        VBox box = new VBox(5);
        box.setStyle("-fx-border-color: #2196F3; -fx-border-width: 1; -fx-padding: 10; -fx-border-radius: 5;");

        Label title = new Label("Preview:");
        title.setStyle("-fx-font-weight: bold;");

        previewLabel = new Label("5 tracks, ~12.3 nm total distance");
        previewLabel.setStyle("-fx-font-size: 11;");

        box.getChildren().addAll(title, previewLabel);
        return box;
    }

    private void validateInputs() {
        errorLabel.setText("");
        boolean valid = true;

        try {
            double orientation = Double.parseDouble(orientationField.getText());
            if (orientation < 0 || orientation >= 360) {
                errorLabel.setText("Orientation must be 0-359°");
                valid = false;
            }
        } catch (NumberFormatException e) {
            errorLabel.setText("Orientation must be a number");
            valid = false;
        }

        try {
            double spacing = Double.parseDouble(spacingField.getText());
            if (spacing <= 0) {
                errorLabel.setText("Track spacing must be > 0 metres");
                valid = false;
            }
        } catch (NumberFormatException e) {
            errorLabel.setText("Track spacing must be a number");
            valid = false;
        }

        try {
            double speed = Double.parseDouble(speedField.getText());
            if (speed <= 0) {
                errorLabel.setText("Platform speed must be > 0 knots");
                valid = false;
            }
        } catch (NumberFormatException e) {
            errorLabel.setText("Platform speed must be a number");
            valid = false;
        }

        // Update OK button
        Button okButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
        okButton.setDisable(!valid);

        // Update preview if valid
        if (valid) {
            updatePreview();
        }
    }

    private void updatePreview() {
        try {
            double spacing = Double.parseDouble(spacingField.getText());
            // Rough estimate: assuming ~2km x 2km area
            int estimatedTracks = (int) (2000 / spacing) + 1;
            double estimatedDistance = estimatedTracks * 2; // ~2nm per track
            previewLabel.setText(String.format("%d tracks, ~%.1f nm total distance",
                estimatedTracks, estimatedDistance));
        } catch (NumberFormatException e) {
            // Preview unchanged
        }
    }

    /**
     * Result class for dialog output
     */
    public static class ParallelTrackSearchParams {
        public final double orientation;
        public final double spacing;
        public final double speed;

        public ParallelTrackSearchParams(double orientation, double spacing, double speed) {
            this.orientation = orientation;
            this.spacing = spacing;
            this.speed = speed;
        }
    }
}
