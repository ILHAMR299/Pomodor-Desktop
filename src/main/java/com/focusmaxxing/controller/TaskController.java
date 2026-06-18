package com.focusmaxxing.controller;

import com.focusmaxxing.model.Priority;
import com.focusmaxxing.model.Task;
import com.focusmaxxing.service.TaskService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

public class TaskController {
    @FXML private TextField taskTitleField;
    @FXML private ComboBox<Priority> priorityComboBox;
    @FXML private TableView<Task> taskTable;
    @FXML private TableColumn<Task, Void> actionColumn;

    private TaskService taskService;
    private ObservableList<Task> masterData;

    @FXML
    public void initialize() {
        taskService = new TaskService();
        priorityComboBox.setItems(FXCollections.observableArrayList(Priority.values()));
        priorityComboBox.setValue(Priority.MEDIUM);

        loadTasks();
        setupActionColumn();
    }

    private void loadTasks() {
        try {
            masterData = FXCollections.observableArrayList(taskService.getMyTasks());
            taskTable.setItems(masterData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleAddTask() {
        String title = taskTitleField.getText();
        Priority priority = priorityComboBox.getValue();

        if (title == null || title.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Task title cannot be empty.");
            return;
        }

        try {
            Task newTask = taskService.addTask(title, "", priority);
            masterData.add(newTask);
            taskTitleField.clear();
            priorityComboBox.setValue(Priority.MEDIUM);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    private void setupActionColumn() {
        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button completeBtn = new Button("Done");
            private final Button deleteBtn = new Button("Delete");
            private final HBox pane = new HBox(10, completeBtn, deleteBtn);

            {
                completeBtn.setStyle("-fx-background-color: #78a890; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 5;");
                deleteBtn.setStyle("-fx-background-color: #e57373; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 5;");

                completeBtn.setOnAction(event -> {
                    Task task = getTableView().getItems().get(getIndex());
                    if (!task.isCompleted()) {
                        try {
                            taskService.completeTask(task.getId());
                            task.setCompleted(true);
                            taskTable.refresh();
                        } catch (Exception e) {
                            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
                        }
                    }
                });

                deleteBtn.setOnAction(event -> {
                    Task task = getTableView().getItems().get(getIndex());
                    try {
                        taskService.deleteTask(task.getId());
                        masterData.remove(task);
                    } catch (Exception e) {
                        showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Task task = getTableView().getItems().get(getIndex());
                    completeBtn.setDisable(task.isCompleted());
                    setGraphic(pane);
                }
            }
        });
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
