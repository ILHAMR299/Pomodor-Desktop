package com.focusmaxxing.controller;

import com.focusmaxxing.model.Statistic;
import com.focusmaxxing.service.PdfReportService;
import com.focusmaxxing.service.StatisticsService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
    @FXML private Label tasksCompletedCaptionLabel;
    @FXML private Label bestDayMinutesLabel;
    @FXML private Label bestDayDateLabel;
    @FXML private Label periodFocusTitleLabel;
    @FXML private Label focusChartTitleLabel;
    @FXML private Label tasksChartTitleLabel;
    @FXML private ToggleButton dailyToggle;
    @FXML private ToggleButton weeklyToggle;
    @FXML private ToggleButton monthlyToggle;

    private StatisticsService statisticsService;
    private PdfReportService pdfReportService;
    private Timeline realtimeRefreshTimeline;
    private PeriodMode currentPeriod = PeriodMode.DAILY;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MM/dd");
    private static final DateTimeFormatter FULL_DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy", new Locale("id", "ID"));

    private enum PeriodMode {
        DAILY,
        WEEKLY,
        MONTHLY
    }

    @FXML
    public void initialize() {
        statisticsService = new StatisticsService();
        pdfReportService = new PdfReportService();
        setupPeriodToggles();
        loadChartData();
        startRealtimeRefresh();
    }

    private void setupPeriodToggles() {
        ToggleGroup periodGroup = new ToggleGroup();
        dailyToggle.setToggleGroup(periodGroup);
        weeklyToggle.setToggleGroup(periodGroup);
        monthlyToggle.setToggleGroup(periodGroup);
        dailyToggle.setSelected(true);

        dailyToggle.setOnAction(e -> switchPeriod(PeriodMode.DAILY));
        weeklyToggle.setOnAction(e -> switchPeriod(PeriodMode.WEEKLY));
        monthlyToggle.setOnAction(e -> switchPeriod(PeriodMode.MONTHLY));
    }

    private void switchPeriod(PeriodMode mode) {
        currentPeriod = mode;
        loadChartData();
    }

    private void startRealtimeRefresh() {
        realtimeRefreshTimeline = new Timeline(new KeyFrame(Duration.seconds(3), e -> loadChartData()));
        realtimeRefreshTimeline.setCycleCount(Timeline.INDEFINITE);
        realtimeRefreshTimeline.play();
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

            updateLabelsForPeriod();
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
        LocalDate startDate = switch (currentPeriod) {
            case DAILY -> today;
            case WEEKLY -> today.minusDays(6);
            case MONTHLY -> today.withDayOfMonth(1);
        };

        int periodMinutes = stats.stream()
                .filter(s -> !s.getStatDate().isBefore(startDate) && !s.getStatDate().isAfter(today))
                .mapToInt(Statistic::getTotalFocusMinutes)
                .sum();
        todayMinutesLabel.setText(periodMinutes + " min");
        int estimatedSessions = periodMinutes / 25;
        todaySessionsLabel.setText(estimatedSessions + " sesi fokus");

        int totalMinutes = stats.stream().mapToInt(Statistic::getTotalFocusMinutes).sum();
        totalMinutesLabel.setText(totalMinutes + " min");
        totalDaysLabel.setText(stats.size() + " hari aktif");

        int totalTasks = stats.stream()
                .filter(s -> !s.getStatDate().isBefore(startDate) && !s.getStatDate().isAfter(today))
                .mapToInt(Statistic::getTasksCompleted)
                .sum();
        tasksCompletedLabel.setText(String.valueOf(totalTasks));

        stats.stream()
                .filter(s -> !s.getStatDate().isBefore(startDate) && !s.getStatDate().isAfter(today))
                .max(Comparator.comparingInt(Statistic::getTotalFocusMinutes))
                .ifPresentOrElse(
                        best -> {
                            bestDayMinutesLabel.setText(best.getTotalFocusMinutes() + " min");
                            bestDayDateLabel.setText(best.getStatDate().format(FULL_DATE_FMT));
                        },
                        () -> {
                            bestDayMinutesLabel.setText("0 min");
                            bestDayDateLabel.setText("-");
                        }
                );
    }

    private void updateBarChart(List<Statistic> stats) {
        focusBarChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Menit fokus");

        for (Map.Entry<String, Integer> entry : buildFocusSeries(stats).entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
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

    private void updateLineChart(List<Statistic> stats) {
        tasksLineChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Tugas");

        for (Map.Entry<String, Integer> entry : buildTaskSeries(stats).entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
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

    private Map<String, Integer> buildFocusSeries(List<Statistic> stats) {
        return buildSeries(stats, true);
    }

    private Map<String, Integer> buildTaskSeries(List<Statistic> stats) {
        return buildSeries(stats, false);
    }

    private Map<String, Integer> buildSeries(List<Statistic> stats, boolean focusMinutes) {
        Map<String, Integer> data = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();
        WeekFields weekFields = WeekFields.of(new Locale("id", "ID"));

        switch (currentPeriod) {
            case DAILY -> {
                for (int i = 6; i >= 0; i--) {
                    LocalDate date = today.minusDays(i);
                    String key = date.format(DATE_FMT);
                    int value = stats.stream()
                            .filter(s -> s.getStatDate().equals(date))
                            .mapToInt(s -> focusMinutes ? s.getTotalFocusMinutes() : s.getTasksCompleted())
                            .findFirst()
                            .orElse(0);
                    data.put(key, value);
                }
            }
            case WEEKLY -> {
                for (int i = 7; i >= 0; i--) {
                    LocalDate weekStart = today.minusWeeks(i).with(weekFields.dayOfWeek(), 1);
                    LocalDate weekEnd = weekStart.plusDays(6);
                    String key = "M" + weekStart.get(weekFields.weekOfWeekBasedYear());
                    int value = stats.stream()
                            .filter(s -> !s.getStatDate().isBefore(weekStart) && !s.getStatDate().isAfter(weekEnd))
                            .mapToInt(s -> focusMinutes ? s.getTotalFocusMinutes() : s.getTasksCompleted())
                            .sum();
                    data.put(key, value);
                }
            }
            case MONTHLY -> {
                for (int i = 5; i >= 0; i--) {
                    LocalDate month = today.minusMonths(i).withDayOfMonth(1);
                    LocalDate monthEnd = month.plusMonths(1).minusDays(1);
                    String key = month.format(DateTimeFormatter.ofPattern("MMM", new Locale("id", "ID")));
                    int value = stats.stream()
                            .filter(s -> !s.getStatDate().isBefore(month) && !s.getStatDate().isAfter(monthEnd))
                            .mapToInt(s -> focusMinutes ? s.getTotalFocusMinutes() : s.getTasksCompleted())
                            .sum();
                    data.put(key, value);
                }
            }
        }
        return data;
    }

    private void updateLabelsForPeriod() {
        switch (currentPeriod) {
            case DAILY -> {
                periodFocusTitleLabel.setText("Fokus Hari Ini");
                tasksCompletedCaptionLabel.setText("selesai hari ini");
                focusChartTitleLabel.setText("Fokus Harian (menit)");
                tasksChartTitleLabel.setText("Tugas Selesai per Hari");
            }
            case WEEKLY -> {
                periodFocusTitleLabel.setText("Fokus Minggu Ini");
                tasksCompletedCaptionLabel.setText("selesai minggu ini");
                focusChartTitleLabel.setText("Fokus Mingguan (menit)");
                tasksChartTitleLabel.setText("Tugas Selesai per Minggu");
            }
            case MONTHLY -> {
                periodFocusTitleLabel.setText("Fokus Bulan Ini");
                tasksCompletedCaptionLabel.setText("selesai bulan ini");
                focusChartTitleLabel.setText("Fokus Bulanan (menit)");
                tasksChartTitleLabel.setText("Tugas Selesai per Bulan");
            }
        }
    }

    // ── PDF Export ────────────────────────────────────────────────────────────

    @FXML
    public void exportPdfReport(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Simpan Laporan PDF");
        fileChooser.setInitialFileName("FocusMaxxing_Laporan_" + LocalDate.now() + ".pdf");
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
                alert.setTitle("Ekspor Berhasil");
                alert.setHeaderText(null);
                alert.setContentText("Laporan PDF berhasil dibuat di:\n" + pdfTask.getValue());
                alert.showAndWait();
            });

            pdfTask.setOnFailed(e -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Ekspor Gagal");
                alert.setHeaderText("Terjadi kesalahan saat membuat PDF.");
                alert.setContentText(pdfTask.getException().getMessage());
                alert.showAndWait();
            });

            Thread backgroundThread = new Thread(pdfTask);
            backgroundThread.setDaemon(true);
            backgroundThread.start();
        }
    }
}
