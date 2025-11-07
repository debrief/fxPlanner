package com.planetmayo.usvsim.view;

import com.planetmayo.usvsim.model.behaviour.Behaviour;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Mission plan panel showing list of added behaviors with edit/delete/reorder controls.
 *
 * Features:
 * - List of behaviors in mission
 * - Status indicator per behavior
 * - Reorder buttons (up/down)
 * - Delete button
 * - Add behavior dropdown (integrated with parent)
 */
public class MissionPlanPanel extends VBox {
    private final ListView<Behaviour> behaviorList;
    private final Label emptyLabel;
    private final HBox buttonBar;
    private java.util.function.Consumer<Behaviour> onBehaviourDelete;
    private ReorderHandler onBehaviourReorder;
    private Runnable onSaveMission;
    private Runnable onLoadMission;

    /**
     * Callback interface for behavior reordering
     */
    @FunctionalInterface
    public interface ReorderHandler {
        void onReorder(int fromIndex, int toIndex);
    }

    public MissionPlanPanel() {
        setStyle("-fx-border-color: #DDD; -fx-padding: 8; -fx-spacing: 8;");
        setPrefHeight(200);

        // Title
        Label title = new Label("Mission Plan");
        title.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");

        // Behavior list
        behaviorList = new ListView<>();
        behaviorList.setCellFactory(param -> new BehaviourCell());
        behaviorList.setPrefHeight(150);
        VBox.setVgrow(behaviorList, Priority.ALWAYS);

        // Empty state label
        emptyLabel = new Label("[No behaviors added]");
        emptyLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #999;");
        behaviorList.setPlaceholder(emptyLabel);

        // Control buttons (reorder, delete)
        buttonBar = createButtonBar();

        getChildren().addAll(title, behaviorList, buttonBar);
    }

    private HBox createButtonBar() {
        HBox box = new HBox(5);
        box.setStyle("-fx-padding: 5;");

        Button upBtn = new Button("↑");
        upBtn.setPrefWidth(40);
        upBtn.setOnAction(event -> handleMoveUp());

        Button downBtn = new Button("↓");
        downBtn.setPrefWidth(40);
        downBtn.setOnAction(event -> handleMoveDown());

        Button deleteBtn = new Button("✕");
        deleteBtn.setPrefWidth(40);
        deleteBtn.setStyle("-fx-text-fill: #F44336;");
        deleteBtn.setOnAction(event -> handleDelete());

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Save mission button with icon
        Button saveBtn = new Button("⬇");
        saveBtn.setPrefWidth(40);
        saveBtn.setTooltip(new javafx.scene.control.Tooltip("Save Mission"));
        saveBtn.setStyle("-fx-font-size: 16px;");
        saveBtn.setOnAction(event -> handleSaveMission());

        // Load mission button with icon
        Button loadBtn = new Button("⬆");
        loadBtn.setPrefWidth(40);
        loadBtn.setTooltip(new javafx.scene.control.Tooltip("Load Mission"));
        loadBtn.setStyle("-fx-font-size: 16px;");
        loadBtn.setOnAction(event -> handleLoadMission());

        box.getChildren().addAll(upBtn, downBtn, deleteBtn, spacer, saveBtn, loadBtn);
        return box;
    }

    /**
     * Add a behavior to the mission plan
     */
    public void addBehavior(Behaviour behaviour) {
        behaviorList.getItems().add(behaviour);
    }

    /**
     * Get selected behavior
     */
    public Behaviour getSelectedBehavior() {
        return behaviorList.getSelectionModel().getSelectedItem();
    }

    /**
     * Remove selected behavior
     */
    public void removeBehavior(Behaviour behaviour) {
        behaviorList.getItems().remove(behaviour);
    }

    /**
     * Get all behaviors in mission
     */
    public java.util.List<Behaviour> getBehaviors() {
        return behaviorList.getItems();
    }

    /**
     * Get the underlying ListView (for selection management)
     */
    public ListView<Behaviour> getBehaviorListView() {
        return behaviorList;
    }

    /**
     * Refresh the ListView to show updated behaviour states
     */
    public void refresh() {
        behaviorList.refresh();
    }

    /**
     * Set double-click handler for editing behaviours
     */
    public void setOnBehaviourDoubleClick(java.util.function.Consumer<Behaviour> handler) {
        behaviorList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Behaviour selected = behaviorList.getSelectionModel().getSelectedItem();
                if (selected != null && handler != null) {
                    handler.accept(selected);
                }
            }
        });
    }

    /**
     * Set delete button handler for removing behaviours
     */
    public void setOnBehaviourDelete(java.util.function.Consumer<Behaviour> handler) {
        this.onBehaviourDelete = handler;
    }

    /**
     * Set reorder handler for moving behaviours up/down
     */
    public void setOnBehaviourReorder(ReorderHandler handler) {
        this.onBehaviourReorder = handler;
    }

    /**
     * Set save mission handler
     */
    public void setOnSaveMission(Runnable handler) {
        this.onSaveMission = handler;
    }

    /**
     * Set load mission handler
     */
    public void setOnLoadMission(Runnable handler) {
        this.onLoadMission = handler;
    }

    private void handleMoveUp() {
        Behaviour selected = getSelectedBehavior();
        if (selected == null) return;
        int index = behaviorList.getItems().indexOf(selected);
        if (index > 0) {
            // Update UI list
            behaviorList.getItems().remove(index);
            behaviorList.getItems().add(index - 1, selected);
            behaviorList.getSelectionModel().select(index - 1);

            // Notify controller to update mission model
            if (onBehaviourReorder != null) {
                onBehaviourReorder.onReorder(index, index - 1);
            }
        }
    }

    private void handleMoveDown() {
        Behaviour selected = getSelectedBehavior();
        if (selected == null) return;
        int index = behaviorList.getItems().indexOf(selected);
        if (index < behaviorList.getItems().size() - 1) {
            // Update UI list
            behaviorList.getItems().remove(index);
            behaviorList.getItems().add(index + 1, selected);
            behaviorList.getSelectionModel().select(index + 1);

            // Notify controller to update mission model
            if (onBehaviourReorder != null) {
                onBehaviourReorder.onReorder(index, index + 1);
            }
        }
    }

    private void handleDelete() {
        Behaviour selected = getSelectedBehavior();
        if (selected != null) {
            if (onBehaviourDelete != null) {
                onBehaviourDelete.accept(selected);
            } else {
                // Fallback to direct removal if no handler set
                removeBehavior(selected);
            }
        }
    }

    private void handleSaveMission() {
        if (onSaveMission != null) {
            onSaveMission.run();
        }
    }

    private void handleLoadMission() {
        if (onLoadMission != null) {
            onLoadMission.run();
        }
    }

    /**
     * Custom cell rendering for behaviors
     */
    private static class BehaviourCell extends ListCell<Behaviour> {
        @Override
        protected void updateItem(Behaviour item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                // Display behavior name and status
                VBox cellContent = new VBox(2);
                cellContent.setPadding(new Insets(5));
                cellContent.setStyle("-fx-border-color: #EEE; -fx-border-width: 0 0 1 0;");

                Label name = new Label(item.getName());
                name.setStyle("-fx-font-weight: bold;");

                Label description = new Label(item.getDescription());
                description.setStyle("-fx-font-size: 10; -fx-text-fill: #666;");

                Label status = new Label("Status: " + item.getState());
                status.setStyle("-fx-font-size: 10; -fx-text-fill: " +
                    (item.isComplete() ? "#4CAF50" : "#2196F3") + ";");

                cellContent.getChildren().addAll(name, description, status);
                setGraphic(cellContent);
            }
        }
    }
}
