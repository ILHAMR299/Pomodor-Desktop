package com.focusmaxxing.service;

import com.focusmaxxing.model.Statistic;
import com.focusmaxxing.model.User;
import com.focusmaxxing.util.SessionManager;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class PdfReportService {
    private final StatisticsService statisticsService;

    public PdfReportService() {
        this.statisticsService = new StatisticsService();
    }

    public String generateReport(String filePath) throws Exception {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null) {
            throw new IllegalStateException("User not logged in");
        }

        List<Statistic> stats = statisticsService.getMyStatistics();

        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(filePath));
        
        document.open();

        Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
        Font normalFont = new Font(Font.HELVETICA, 12, Font.NORMAL);

        document.add(new Paragraph("FocusMaxxing Activity Report", titleFont));
        document.add(new Paragraph("User: " + user.getUsername(), normalFont));
        document.add(new Paragraph("Date Generated: " + java.time.LocalDate.now().toString(), normalFont));
        document.add(new Paragraph(" "));

        int totalFocusAllTime = stats.stream().mapToInt(Statistic::getTotalFocusMinutes).sum();
        int totalTasksAllTime = stats.stream().mapToInt(Statistic::getTasksCompleted).sum();

        document.add(new Paragraph("Lifetime Statistics:", titleFont));
        document.add(new Paragraph("Total Focus Time: " + totalFocusAllTime + " minutes", normalFont));
        document.add(new Paragraph("Total Tasks Completed: " + totalTasksAllTime, normalFont));
        document.add(new Paragraph(" "));

        // Create a summary table for daily statistics
        document.add(new Paragraph("Daily Summary:", titleFont));
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(20f);
        table.addCell("Date");
        table.addCell("Focus Minutes");
        table.addCell("Tasks Completed");

        for (Statistic stat : stats) {
            table.addCell(stat.getStatDate().toString());
            table.addCell(String.valueOf(stat.getTotalFocusMinutes()));
            table.addCell(String.valueOf(stat.getTasksCompleted()));
        }
        document.add(table);

        // Fetch Tasks and add to PDF
        com.focusmaxxing.service.TaskService taskService = new com.focusmaxxing.service.TaskService();
        List<com.focusmaxxing.model.Task> userTasks = taskService.getMyTasks();

        document.add(new Paragraph("Your To-Do List:", titleFont));
        PdfPTable taskTable = new PdfPTable(3);
        taskTable.setWidthPercentage(100);
        taskTable.setSpacingBefore(10f);
        taskTable.addCell("Task Title");
        taskTable.addCell("Priority");
        taskTable.addCell("Status");

        for (com.focusmaxxing.model.Task t : userTasks) {
            taskTable.addCell(t.getTitle());
            taskTable.addCell(t.getPriority().name());
            taskTable.addCell(t.isCompleted() ? "Completed" : "Pending");
        }
        document.add(taskTable);

        document.close();

        return filePath;
    }
}
