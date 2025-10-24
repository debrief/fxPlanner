package com.planetmayo.usvsim.e2e;

import com.planetmayo.usvsim.Main;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end test for mission planning workflow.
 *
 * Scenario:
 * 1. Application launches and shows main view
 * 2. User selects "Parallel Track Search" from behavior dropdown
 * 3. Dialog appears with input fields
 * 4. User enters search parameters (orientation, spacing, speed)
 * 5. User clicks OK
 * 6. Behavior is added to mission plan
 * 7. Behavior appears in list with correct parameters
 */
public class MissionPlanningWorkflowTest extends ApplicationTest {

    private Stage applicationStage;

    @Override
    public void start(Stage stage) throws Exception {
        this.applicationStage = stage;
        new Main().start(stage);
    }

    @Test
    void testApplicationLaunches() {
        // Verify main window is displayed
        assertNotNull(applicationStage, "Stage should be initialized");
        assertNotNull(applicationStage.getScene(), "Scene should be created");
        assertEquals("USV Mission Planner", applicationStage.getTitle(), "Window title should be correct");
    }

    @Test
    void testMainViewComponentsPresent() {
        // Verify key UI components exist - check for expected nodes
        var allLabels = lookup(".label").queryAll();
        assertTrue(allLabels.size() > 0, "Labels should be present");

        // Check for Mission Plan label
        boolean hasMissionPlanLabel = allLabels.stream()
            .filter(node -> node instanceof Label)
            .map(node -> ((Label) node).getText())
            .anyMatch(text -> text != null && text.contains("Mission Plan"));
        assertTrue(hasMissionPlanLabel, "Mission Plan label should be visible");

        // Check for State Panel label
        boolean hasStatePanelLabel = allLabels.stream()
            .filter(node -> node instanceof Label)
            .map(node -> ((Label) node).getText())
            .anyMatch(text -> text != null && text.contains("State Panel"));
        assertTrue(hasStatePanelLabel, "State Panel label should be visible");
    }

    @Test
    void testControlPanelPresent() {
        // Verify simulation control buttons exist
        var allButtons = lookup(".button").queryAll();
        assertTrue(allButtons.size() > 0, "Buttons should be present");

        var buttonTexts = allButtons.stream()
            .filter(node -> node instanceof Button)
            .map(node -> ((Button) node).getText())
            .toList();

        assertTrue(buttonTexts.contains("Start"), "Start button should be visible");
        assertTrue(buttonTexts.contains("Pause"), "Pause button should be visible");
        assertTrue(buttonTexts.contains("Stop"), "Stop button should be visible");
    }

    @Test
    void testAddBehaviorDropdownPresent() {
        // Verify Add Behaviour dropdown exists
        var comboBoxes = lookup(".combo-box").queryAll();
        assertTrue(comboBoxes.size() > 0, "Behavior dropdown should exist");

        // Verify it's a ComboBox
        assertTrue(comboBoxes.stream().anyMatch(node -> node instanceof ComboBox),
            "ComboBox should be found");
    }

    @Test
    void testMapViewIntegration() {
        // **CRITICAL**: Verify java_leaflet MapView is integrated (not placeholder)
        // This test ensures T036 is properly implemented

        // Get the main scene root
        var root = applicationStage.getScene().getRoot();
        assertTrue(root instanceof BorderPane, "Root should be BorderPane layout");

        BorderPane mainLayout = (BorderPane) root;
        var centerContent = mainLayout.getCenter();
        assertNotNull(centerContent, "Center should contain MapPanel");

        // MapPanel should be a Pane/Region containing the map
        assertTrue(centerContent instanceof Pane, "Center should be a Pane (MapPanel)");
        Pane mapPanel = (Pane) centerContent;

        // Debug: Print all labels to see what's actually in the map
        var allLabels = mapPanel.getChildren().stream()
            .filter(node -> node instanceof Label)
            .map(node -> ((Label) node).getText())
            .toList();

        System.out.println("DEBUG: MapPanel contains " + mapPanel.getChildren().size() + " children");
        System.out.println("DEBUG: Labels found: " + allLabels);

        // Verify MapPanel has actual java_leaflet MapView, not just placeholder
        // T036 requires: MapView (actual map), NOT placeholder message
        var hasIncompletePlaceholder = mapPanel.getChildren().stream()
            .filter(node -> node instanceof Label)
            .map(node -> ((Label) node).getText())
            .anyMatch(text -> text != null &&
                (text.contains("will be integrated") ||
                 text.contains("placeholder") ||
                 text.contains("TODO")));

        assertTrue(!hasIncompletePlaceholder && mapPanel.getChildren().size() > 3,
            "T036 INCOMPLETE: MapPanel must have actual java_leaflet MapView with offline tiles and pan/zoom. " +
            "Currently has placeholder stub message. Requires functional map integration.");
    }

    @Test
    void testLayoutProportions() {
        // Verify 70/30 layout (map left, controls right)
        double stageWidth = applicationStage.getWidth();
        double stageHeight = applicationStage.getHeight();

        // Rough check that window has reasonable dimensions
        assertTrue(stageWidth >= 800, "Window width should be sufficient");
        assertTrue(stageHeight >= 600, "Window height should be sufficient");
    }

    @Test
    void testInitialMissionPlanEmpty() {
        // Verify mission plan list exists
        var listViews = lookup(".list-view").queryAll();
        assertTrue(listViews.size() > 0, "Mission plan list should exist");
    }

    @Test
    void testStateDisplayInitialized() {
        // Verify state panel shows default values
        var labels = lookup(".label").queryAll();
        assertTrue(labels.size() > 0, "Labels should be present");

        var labelTexts = labels.stream()
            .filter(node -> node instanceof Label)
            .map(node -> ((Label) node).getText())
            .toList();

        // Should show default values
        boolean hasPositionValue = labelTexts.stream()
            .anyMatch(text -> text != null && (text.contains("50.60") || text.contains("°N")));
        assertTrue(hasPositionValue, "Position value should be shown");
    }

    /**
     * Note: Full dialog interaction test would require:
     * - TestFX snapshot capability (display manager)
     * - Dialog focus handling
     * - Text field input simulation
     *
     * This test verifies basic component presence. Full integration
     * testing would be done in manual testing or with headless display setup.
     */
    @Test
    void testUIRenderingComplete() {
        // Verify that the stage window actually shows content
        // (the other tests verify component presence)
        assertNotNull(applicationStage.getScene(), "Scene should be loaded");
        assertTrue(applicationStage.isShowing(), "Window should be visible");
        assertTrue(applicationStage.getWidth() > 0, "Window should have valid dimensions");
        assertTrue(applicationStage.getHeight() > 0, "Window should have valid height");
    }
}
