package com.eswasthya.desktop.controller;

import com.eswasthya.desktop.api.ApiService;
import com.eswasthya.desktop.model.DashboardSummary;
import com.eswasthya.desktop.model.HealthRecord;
import javafx.application.Platform;


import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;

import java.util.List;

public class DashboardController {

    // Stat labels
    @FXML private Label totalRecordsLabel;
    @FXML private Label unreadAlertsLabel;
    @FXML private Label avgBmiLabel;
    @FXML private Label avgGlucoseLabel;

    // Latest snapshot
    @FXML private Label bmiValueLabel;
    @FXML private Label bmiCategoryLabel;
    @FXML private Label bpValueLabel;
    @FXML private Label bpCategoryLabel;
    @FXML private Label glucoseValueLabel;
    @FXML private Label glucoseCategoryLabel;
    @FXML private Label activityLabel;
    @FXML private ProgressBar bmiBar;

    // Charts
    @FXML private LineChart<String, Number>  bmiChart;
    @FXML private LineChart<String, Number>  glucoseChart;
    @FXML private BarChart<String, Number>   activityChart;

    @FXML private VBox  loadingBox;
    @FXML private VBox  contentBox;
    @FXML private Label errorLabel;

    @FXML
    public void initialize() {
        loadingBox.setVisible(true);
        contentBox.setVisible(false);
        errorLabel.setVisible(false);
        loadData();
    }

    private void loadData() {
        Task<DashboardSummary> task = new Task<>() {
            @Override protected DashboardSummary call() throws Exception {
                return ApiService.getInstance().getDashboard();
            }
        };

        task.setOnSucceeded(e -> Platform.runLater(() -> {
            populateDashboard(task.getValue());
            loadingBox.setVisible(false);
            contentBox.setVisible(true);
        }));

        task.setOnFailed(e -> Platform.runLater(() -> {
            loadingBox.setVisible(false);
            errorLabel.setText("Failed to load dashboard: " +
                    (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
            errorLabel.setVisible(true);
        }));

        new Thread(task, "dashboard-load").start();
    }

    private void populateDashboard(DashboardSummary s) {
        // Stats
        totalRecordsLabel.setText(String.valueOf(s.getTotalRecords()));
        unreadAlertsLabel.setText(String.valueOf(s.getUnreadAlertCount()));
        avgBmiLabel.setText(s.getAvgBmi30Days() != null
                ? String.format("%.1f", s.getAvgBmi30Days()) : "—");
        avgGlucoseLabel.setText(s.getAvgGlucose30Days() != null
                ? String.format("%.0f", s.getAvgGlucose30Days()) : "—");

        // Latest snapshot
        bmiValueLabel.setText(s.getLatestBmi() != null
                ? String.format("%.1f", s.getLatestBmi()) : "—");
        bmiCategoryLabel.setText(nvl(s.getLatestBmiCategory()));
        bpValueLabel.setText(nvl(s.getLatestBloodPressure(), "mmHg"));
        bpCategoryLabel.setText(nvl(s.getLatestBpCategory()));
        glucoseValueLabel.setText(s.getLatestGlucose() != null
                ? String.format("%.0f mg/dL", s.getLatestGlucose()) : "—");
        glucoseCategoryLabel.setText(nvl(s.getLatestGlucoseCategory()));
        activityLabel.setText(nvl(s.getLatestActivityLevel()));

        // BMI progress bar (range 10–50)
        if (s.getLatestBmi() != null) {
            double pct = Math.min(Math.max((s.getLatestBmi() - 10) / 40.0, 0), 1);
            bmiBar.setProgress(pct);
            String styleClass = s.getLatestBmi() < 18.5 ? "bmi-low"
                    : s.getLatestBmi() < 25 ? "bmi-normal"
                    : s.getLatestBmi() < 30 ? "bmi-high" : "bmi-obese";
            bmiBar.getStyleClass().removeAll("bmi-low","bmi-normal","bmi-high","bmi-obese");
            bmiBar.getStyleClass().add(styleClass);
        }

        // Populate charts
        buildBmiChart(s.getRecentRecords());
        buildGlucoseChart(s.getRecentRecords());
        buildActivityChart(s.getRecentRecords());
    }

    private void buildBmiChart(List<HealthRecord> records) {
        if (records == null || records.isEmpty()) return;
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("BMI");
        int startBmi = Math.max(0, records.size() - 10);
        for (int i = startBmi; i < records.size(); i++) {
            HealthRecord r = records.get(i);
            if (r.getBmi() != null)
                series.getData().add(new XYChart.Data<>(shortDate(r.getRecordDate()), r.getBmi()));
        }
        bmiChart.getData().clear();
        bmiChart.getData().add(series);
    }

    private void buildGlucoseChart(List<HealthRecord> records) {
        if (records == null || records.isEmpty()) return;
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Glucose (mg/dL)");
        int startGl = Math.max(0, records.size() - 10);
        for (int i = startGl; i < records.size(); i++) {
            HealthRecord r = records.get(i);
            if (r.getGlucose() != null)
                series.getData().add(new XYChart.Data<>(shortDate(r.getRecordDate()), r.getGlucose()));
        }
        glucoseChart.getData().clear();
        glucoseChart.getData().add(series);
    }

    private void buildActivityChart(List<HealthRecord> records) {
        if (records == null || records.isEmpty()) return;
        long low = 0, mod = 0, high = 0;
        for (HealthRecord r : records) {
            if ("LOW".equals(r.getActivityLevel()))      low++;
            else if ("MODERATE".equals(r.getActivityLevel())) mod++;
            else if ("HIGH".equals(r.getActivityLevel()))     high++;
        }
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Count");
        series.getData().add(new XYChart.Data<>("Low",      low));
        series.getData().add(new XYChart.Data<>("Moderate", mod));
        series.getData().add(new XYChart.Data<>("High",     high));
        activityChart.getData().clear();
        activityChart.getData().add(series);
    }

    private String shortDate(String date) {
        if (date == null) return "";
        return date.length() >= 7 ? date.substring(5) : date; // "MM-DD"
    }

    private String nvl(String s) { return s != null ? s : "—"; }
    private String nvl(String s, String unit) {
        return s != null ? s + " " + unit : "—";
    }
}
