package com.focusmaxxing.controller;

import com.focusmaxxing.model.SessionStatus;
import com.focusmaxxing.model.SessionType;
import com.focusmaxxing.model.Task;
import com.focusmaxxing.service.PomodoroService;
import com.focusmaxxing.service.TaskService;
import com.focusmaxxing.util.component.ProgressGifComponent;
import com.focusmaxxing.util.multimedia.MediaEventHandler;
import com.focusmaxxing.util.multimedia.MultimediaEvent;
import com.focusmaxxing.util.multimedia.MultimediaEventBus;
import com.focusmaxxing.util.multimedia.TimerState;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.time.LocalDateTime;

public class PomodoroController {

    @FXML private Label      timerLabel;
    @FXML private Label      sessionTypeLabel;
    @FXML private Button     startPauseButton;
    @FXML private ComboBox<Task> taskComboBox;
    @FXML private HBox       buttonRow;
    @FXML private StackPane  timerPane;
    @FXML private HBox       timerAreaHBox;

    private PomodoroService pomodoroService;
    private TaskService     taskService;

    private Timeline      timeline;
    private int           secondsRemaining;
    private SessionType   currentType  = SessionType.FOCUS;
    private boolean       isRunning    = false;
    private LocalDateTime startedAt;

    private static final int FOCUS_MINUTES       = 25;
    private static final int SHORT_BREAK_MINUTES = 5;
    private static final int LONG_BREAK_MINUTES  = 15;
    private int sessionCount = 0;

    private MediaEventHandler    mediaEventHandler;
    private ProgressGifComponent progressGifComponent;

    private Runnable onSessionSaved;

    public void setOnSessionSaved(Runnable cb) { this.onSessionSaved = cb; }

    private void notifySessionSaved() {
        if (onSessionSaved != null) onSessionSaved.run();
    }

    /**
     * Injects the shared {@link MediaEventHandler}.
     * Must be called by {@link MainDashboardController} immediately after loading
     * this controller via FXMLLoader.
     */
    public void setMediaEventHandler(MediaEventHandler handler) {
        this.mediaEventHandler = handler;

        progressGifComponent = new ProgressGifComponent();
        handler.setProgressGifComponent(progressGifComponent);

        if (timerAreaHBox != null) {
            timerAreaHBox.getChildren().add(progressGifComponent.getRoot());
        }
    }

    @FXML
    public void initialize() {
        pomodoroService = new PomodoroService();
        taskService     = new TaskService();

        setupTimer(FOCUS_MINUTES);
        setupTaskComboBox();
    }

    private void setupTaskComboBox() {
        if (taskComboBox == null) return;
        taskComboBox.setConverter(new javafx.util.StringConverter<Task>() {
            @Override public String toString(Task t)     { return t == null ? null : t.getTitle() + " (" + t.getPriority() + ")"; }
            @Override public Task   fromString(String s) { return null; }
        });
        taskComboBox.setOnShowing(e -> refreshTasks());
        refreshTasks();
    }

    private void refreshTasks() {
        Task current = taskComboBox.getValue();
        java.util.List<Task> pending = taskService.getMyTasks().stream()
                .filter(t -> !t.isCompleted()).toList();
        taskComboBox.setItems(FXCollections.observableArrayList(pending));
        if (current != null && taskComboBox.getItems().contains(current))
            taskComboBox.setValue(current);
    }

    private void setupTimer(int minutes) {
        secondsRemaining = minutes * 60;
        updateTimerLabel();
        if (timeline != null) timeline.stop();
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            secondsRemaining--;
            updateTimerLabel();
            if (secondsRemaining <= 0) handleSessionComplete();
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
    }

    private void updateTimerLabel() {
        int m = secondsRemaining / 60, s = secondsRemaining % 60;
        timerLabel.setText(String.format("%02d:%02d", m, s));
    }

    @FXML
    public void toggleTimer() {
        if (isRunning) {
            timeline.pause();
            startPauseButton.setText("Resume");
            isRunning = false;
            setGifState(TimerState.PAUSED);
        } else {
            if (startedAt == null) startedAt = LocalDateTime.now();
            timeline.play();
            startPauseButton.setText("Pause");
            isRunning = true;
            MultimediaEventBus.getInstance().publish(MultimediaEvent.TIMER_START);
        }
    }

    @FXML
    public void stopTimer() {
        if (!isRunning && startedAt == null) return;

        if (timeline != null) timeline.stop();

        if (startedAt != null) {
            int minutesPassed = getMinutesForType(currentType) - (secondsRemaining / 60);
            if (minutesPassed > 0) {
                pomodoroService.saveSession(getSelectedTaskId(), currentType,
                        minutesPassed, startedAt, SessionStatus.INTERRUPTED);
                notifySessionSaved();
            }
        }

        isRunning = false;
        startedAt = null;
        startPauseButton.setText("Start");

        MultimediaEventBus.getInstance().publish(MultimediaEvent.TIMER_STOP);

        setupTimer(getMinutesForType(currentType));
    }

    @FXML
    public void skipSession() {
        if (timeline != null) timeline.stop();

        if (startedAt != null) {
            int minutesPassed = getMinutesForType(currentType) - (secondsRemaining / 60);
            pomodoroService.saveSession(getSelectedTaskId(), currentType,
                    minutesPassed, startedAt, SessionStatus.INTERRUPTED);
            notifySessionSaved();
        }

        MultimediaEventBus.getInstance().publish(MultimediaEvent.TIMER_SKIP);

        transitionToNextSession();
    }

    private void handleSessionComplete() {
        timeline.stop();
        isRunning = false;
        startPauseButton.setText("Start");

        if (startedAt != null) {
            pomodoroService.saveSession(getSelectedTaskId(), currentType,
                    getMinutesForType(currentType), startedAt, SessionStatus.COMPLETED);
            notifySessionSaved();
        }

        MultimediaEventBus.getInstance().publish(MultimediaEvent.TIMER_COMPLETE);

        transitionToNextSession();
    }

    private void transitionToNextSession() {
        startedAt = null;
        if (currentType == SessionType.FOCUS) {
            sessionCount++;
            if (sessionCount % 4 == 0) {
                currentType = SessionType.LONG_BREAK;
                sessionTypeLabel.setText("Long Break");
                setupTimer(LONG_BREAK_MINUTES);
            } else {
                currentType = SessionType.SHORT_BREAK;
                sessionTypeLabel.setText("Short Break");
                setupTimer(SHORT_BREAK_MINUTES);
            }
        } else {
            currentType = SessionType.FOCUS;
            sessionTypeLabel.setText("Focus Session");
            setupTimer(FOCUS_MINUTES);
        }
    }

    private Integer getSelectedTaskId() {
        return (taskComboBox != null && taskComboBox.getValue() != null)
                ? taskComboBox.getValue().getId() : null;
    }

    private int getMinutesForType(SessionType t) {
        return switch (t) {
            case FOCUS       -> FOCUS_MINUTES;
            case SHORT_BREAK -> SHORT_BREAK_MINUTES;
            case LONG_BREAK  -> LONG_BREAK_MINUTES;
        };
    }

    private void setGifState(TimerState state) {
        if (progressGifComponent != null) progressGifComponent.setState(state);
    }
}
