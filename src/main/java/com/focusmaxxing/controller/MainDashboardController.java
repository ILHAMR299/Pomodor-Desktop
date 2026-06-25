package com.focusmaxxing.controller;

import com.focusmaxxing.util.multimedia.MediaEventHandler;
import com.focusmaxxing.util.multimedia.MultimediaEvent;
import com.focusmaxxing.util.multimedia.MultimediaEventBus;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class MainDashboardController {

    @FXML private BorderPane mainContainer;
    @FXML private StackPane  contentArea;
    @FXML private Label      userGreetingLabel;
    @FXML private Button     btnTimer;
    @FXML private Button     btnTasks;
    @FXML private Button     btnStats;

    private Node               pomodoroView;
    private Node               taskView;
    private PomodoroController pomodoroController;
    private TaskController taskController;
    private StatisticsController statisticsController;

    /**
     * Created once; kept as a field so subscriptions survive for the lifetime
     * of this controller. Never recreate this object.
     */
    private MediaEventHandler mediaEventHandler;

    @FXML
    public void initialize() {
        if (com.focusmaxxing.util.SessionManager.getInstance().getCurrentUser() != null) {
            userGreetingLabel.setText(
                    "Halo, " + com.focusmaxxing.util.SessionManager.getInstance()
                            .getCurrentUser().getUsername());
        }

        // Create the ONE handler for the entire dashboard lifetime
        mediaEventHandler = new MediaEventHandler();
        mediaEventHandler.setLogoutConfirmedCallback(confirmed -> {
            if (confirmed) performLogout();
        });

        showTimer();
    }

    @FXML
    public void showTimer() {
        if (pomodoroView == null) {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/com/focusmaxxing/view/PomodoroView.fxml"));
                pomodoroView       = loader.load();
                pomodoroController = loader.getController();

                // Inject the shared handler — this is what prevents double popups
                pomodoroController.setMediaEventHandler(mediaEventHandler);

                pomodoroController.setOnSessionSaved(() -> {
                    if (statisticsController != null)
                        javafx.application.Platform.runLater(() -> statisticsController.refreshData());
                });
                pomodoroController.setOnOpenTasksRequested(this::showTasks);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        contentArea.getChildren().setAll(pomodoroView);
        updateNavButtons(btnTimer);
    }

    @FXML
    public void showTasks() {
        if (taskView == null) {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/com/focusmaxxing/view/TaskView.fxml"));
                taskView = loader.load();
                taskController = loader.getController();
                taskController.setOnTaskDataChanged(() -> javafx.application.Platform.runLater(() -> {
                    if (pomodoroController != null) pomodoroController.refreshTasks();
                    if (statisticsController != null) statisticsController.refreshData();
                }));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        contentArea.getChildren().setAll(taskView);
        updateNavButtons(btnTasks);
    }

    @FXML
    public void showStats() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/focusmaxxing/view/StatisticsView.fxml"));
            Node view = loader.load();
            statisticsController = loader.getController();
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
        updateNavButtons(btnStats);
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        MultimediaEventBus.getInstance().publish(MultimediaEvent.LOGOUT_CONFIRM);
    }

    private void performLogout() {
        com.focusmaxxing.util.SessionManager.getInstance().logout();
        try {
            javafx.scene.Parent root = FXMLLoader.load(
                    getClass().getResource("/com/focusmaxxing/view/LoginView.fxml"));
            javafx.stage.Stage stage =
                    (javafx.stage.Stage) mainContainer.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root, 400, 500));
            stage.setResizable(false);
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateNavButtons(Button active) {
        if (btnTimer == null || btnTasks == null || btnStats == null) return;
        btnTimer.getStyleClass().setAll("button", "nav-button");
        btnTasks.getStyleClass().setAll("button", "nav-button");
        btnStats.getStyleClass().setAll("button", "nav-button");
        active.getStyleClass().setAll("button", "nav-button-active");
    }

    private void loadView(String path) {
        try {
            Node view = new FXMLLoader(getClass().getResource(path)).load();
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
