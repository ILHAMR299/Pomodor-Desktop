package com.focusmaxxing.controller;

import com.focusmaxxing.model.Statistic;
import com.focusmaxxing.service.PdfReportService;
import com.focusmaxxing.service.StatisticsService;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

public class StatisticsController {

    // Charts
    @FXML private BarChart<String, Number> focusBarChart;
    @FXML private LineChart<String, Number> tasksLineChart;

    // Summary card labels
    @FXML private Label todayMinutesLabel;
    @FXML private Label todaySessionsLabel;
    @FXML private Label totalMinutesLabel;
    @FXML private Label totalDaysLabel;
    @FXML private Label tasksCompletedLabel;
    @FXML private Label bestDayMinutesLabel;
    @FXML private Label bestDayDateLabel;

    private StatisticsService statisticsService;
    private PdfReportService pdfReportService;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MM/dd");

    @FXML
    public void initialize() {
        statisticsService = new StatisticsService();
        pdfReportService = new PdfReportService();
        loadChartData();
    }

    /** Called externally (e.g. from PomodoroController after a session is saved) to refresh data. */
    public void refreshData() {
        loadChartData();
    }

    @FXML
    public void refreshChart() {
        loadChartData();
    }

    private void loadChartData() {
        try {
            List<Statistic> stats = statisticsService.getMyStatistics();

            updateSummaryCards(stats);
            updateBarChart(stats);
            updateLineChart(stats);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ── Summary Cards ──────────────────────────────────────────────────────────

    private void updateSummaryCards(List<Statistic> stats) {
        LocalDate today = LocalDate.now();

        // Today's focus
        Statistic todayStat = stats.stream()
                .filter(s -> s.getStatDate().equals(today))
                .findFirst()
                .orElse(null);

        int todayMinutes = todayStat != null ? todayStat.getTotalFocusMinutes() : 0;
        todayMinutesLabel.setText(todayMinutes + " min");
        // Sessions estimate: 1 session ≈ 25 min
        int estimatedSessions = todayMinutes / 25;
        todaySessionsLabel.setText(estimatedSessions + " session" + (estimatedSessions != 1 ? "s" : ""));

        // Total all-time
        int totalMinutes = stats.stream().mapToInt(Statistic::getTotalFocusMinutes).sum();
        totalMinutesLabel.setText(totalMinutes + " min");
        totalDaysLabel.setText(stats.size() + " active day" + (stats.size() != 1 ? "s" : ""));

        // Total tasks completed
        int totalTasks = stats.stream().mapToInt(Statistic::getTasksCompleted).sum();
        tasksCompletedLabel.setText(String.valueOf(totalTasks));

        // Best day
        stats.stream()
                .max(Comparator.comparingInt(Statistic::getTotalFocusMinutes))
                .ifPresentOrElse(
                        best -> {
                            bestDayMinutesLabel.setText(best.getTotalFocusMinutes() + " min");
                            bestDayDateLabel.setText(best.getStatDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
                        },
                        () -> {
                            bestDayMinutesLabel.setText("0 min");
                            bestDayDateLabel.setText("-");
                        }
                );
    }

    // ── Bar Chart: Daily Focus Minutes ────────────────────────────────────────

    private void updateBarChart(List<Statistic> stats) {
        focusBarChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Focus Minutes");

        if (stats.isEmpty()) {
            series.getData().add(new XYChart.Data<>(LocalDate.now().format(DATE_FMT), 0));
        } else {
            // Show last 7 days, fill missing days with 0
            LocalDate today = LocalDate.now();
            for (int i = 6; i >= 0; i--) {
                LocalDate date = today.minusDays(i);
                int minutes = stats.stream()
                        .filter(s -> s.getStatDate().equals(date))
                        .mapToInt(Statistic::getTotalFocusMinutes)
                        .findFirst()
                        .orElse(0);
                series.getData().add(new XYChart.Data<>(date.format(DATE_FMT), minutes));
            }
        }

        focusBarChart.getData().add(series);

        // Apply color to bars after render
        javafx.application.Platform.runLater(() -> {
            for (XYChart.Data<String, Number> data : series.getData()) {
                if (data.getNode() != null) {
                    data.getNode().setStyle("-fx-bar-fill: #78a890;");
                }
            }
        });
    }

    // ── Line Chart: Tasks Completed per Day ───────────────────────────────────

    private void updateLineChart(List<Statistic> stats) {
        tasksLineChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Tasks");

        if (stats.isEmpty()) {
            series.getData().add(new XYChart.Data<>(LocalDate.now().format(DATE_FMT), 0));
        } else {
            LocalDate today = LocalDate.now();
            for (int i = 6; i >= 0; i--) {
                LocalDate date = today.minusDays(i);
                int tasks = stats.stream()
                        .filter(s -> s.getStatDate().equals(date))
                        .mapToInt(Statistic::getTasksCompleted)
                        .findFirst()
                        .orElse(0);
                series.getData().add(new XYChart.Data<>(date.format(DATE_FMT), tasks));
            }
        }

        tasksLineChart.getData().add(series);

        javafx.application.Platform.runLater(() -> {
            for (XYChart.Data<String, Number> data : series.getData()) {
                if (data.getNode() != null) {
                    data.getNode().setStyle(
                            "-fx-background-color: #5b8a6e, white;" +
                            "-fx-background-insets: 0, 2;" +
                            "-fx-background-radius: 5px;" +
                            "-fx-padding: 5px;"
                    );
                }
            }
            // Style the line
            Node line = tasksLineChart.lookup(".chart-series-line");
            if (line != null) {
                line.setStyle("-fx-stroke: #5b8a6e; -fx-stroke-width: 2.5px;");
            }
        });
    }

    // ── PDF Export ────────────────────────────────────────────────────────────

    @FXML
    public void exportPdfReport(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save PDF Report");
        fileChooser.setInitialFileName("FocusMaxxing_Report_" + LocalDate.now() + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            Task<String> pdfTask = new Task<>() {
                @Override
                protected String call() throws Exception {
                    return pdfReportService.generateReport(file.getAbsolutePath());
                }
            };

            pdfTask.setOnSucceeded(e -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Export Successful");
                alert.setHeaderText(null);
                alert.setContentText("PDF Report generated successfully at:\n" + pdfTask.getValue());
                alert.showAndWait();
            });

            pdfTask.setOnFailed(e -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Export Failed");
                alert.setHeaderText("An error occurred during PDF generation.");
                alert.setContentText(pdfTask.getException().getMessage());
                alert.showAndWait();
            });

            Thread backgroundThread = new Thread(pdfTask);
            backgroundThread.setDaemon(true);
            backgroundThread.start();
        }
    }
}
