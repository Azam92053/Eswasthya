package com.eswasthya.desktop.controller;

import com.eswasthya.desktop.api.ApiService;
import com.eswasthya.desktop.model.AdminStats;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;



public class AdminDashboardController {

    @FXML private Label totalUsersLabel;
    @FXML private Label totalRecordsLabel;
    @FXML private Label totalAlertsLabel;
    @FXML private Label unreadAlertsLabel;
    @FXML private Label avgBmiLabel;
    @FXML private Label avgGlucoseLabel;

    @FXML private BarChart<String, Number>  userRoleChart;
    @FXML private PieChart                  alertTypeChart;

    @FXML private TableView<AdminStats.UserSummary>           summaryTable;
    @FXML private TableColumn<AdminStats.UserSummary, String> colName;
    @FXML private TableColumn<AdminStats.UserSummary, String> colRole;
    @FXML private TableColumn<AdminStats.UserSummary, String> colRecords;
    @FXML private TableColumn<AdminStats.UserSummary, String> colBmi;
    @FXML private TableColumn<AdminStats.UserSummary, String> colBmiCat;
    @FXML private TableColumn<AdminStats.UserSummary, String> colUnread;

    @FXML private VBox  loadingBox;
    @FXML private Label errorLabel;

    @FXML
    public void initialize() {
        setupTable();
        loadStats();
    }

    private void setupTable() {
        bindColumnWidths();

        colName.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue().getName()));
        colRole.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue().getRole()));
        colRecords.setCellValueFactory(c ->
            new SimpleStringProperty(String.valueOf(c.getValue().getRecordCount())));
        colBmi.setCellValueFactory(c -> {
            Double bmi = c.getValue().getLatestBmi();
            return new SimpleStringProperty(bmi != null ? String.format("%.1f", bmi) : "—");
        });
        colBmiCat.setCellValueFactory(c ->
            new SimpleStringProperty(nvl(c.getValue().getLatestBmiCategory())));
        colUnread.setCellValueFactory(c ->
            new SimpleStringProperty(String.valueOf(c.getValue().getUnreadAlertCount())));

        // Highlight unread alerts > 0
        colUnread.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                getStyleClass().removeAll("cell-danger","cell-normal");
                if (!empty && !"0".equals(item)) getStyleClass().add("cell-danger");
            }
        });

        summaryTable.setPlaceholder(new Label("No users found."));
    }

    private void bindColumnWidths() {
        colName.prefWidthProperty().bind(summaryTable.widthProperty().subtract(20).multiply(0.28));
        colRole.prefWidthProperty().bind(summaryTable.widthProperty().subtract(20).multiply(0.16));
        colRecords.prefWidthProperty().bind(summaryTable.widthProperty().subtract(20).multiply(0.13));
        colBmi.prefWidthProperty().bind(summaryTable.widthProperty().subtract(20).multiply(0.15));
        colBmiCat.prefWidthProperty().bind(summaryTable.widthProperty().subtract(20).multiply(0.18));
        colUnread.prefWidthProperty().bind(summaryTable.widthProperty().subtract(20).multiply(0.10));
    }

    private void loadStats() {
        loadingBox.setVisible(true);
        errorLabel.setVisible(false);

        Task<AdminStats> task = new Task<>() {
            @Override protected AdminStats call() throws Exception {
                return ApiService.getInstance().getAdminStats();
            }
        };
        task.setOnSucceeded(e -> Platform.runLater(() -> {
            populateStats(task.getValue());
            loadingBox.setVisible(false);
        }));
        task.setOnFailed(e -> Platform.runLater(() -> {
            loadingBox.setVisible(false);
            errorLabel.setText("Failed: " +
                (task.getException() != null ? task.getException().getMessage() : "Error"));
            errorLabel.setVisible(true);
        }));
        new Thread(task, "admin-stats-load").start();
    }

    private void populateStats(AdminStats s) {
        totalUsersLabel.setText(String.valueOf(s.getTotalUsers()));
        totalRecordsLabel.setText(String.valueOf(s.getTotalHealthRecords()));
        if (totalAlertsLabel != null) {
            totalAlertsLabel.setText(String.valueOf(s.getTotalAlerts()));
        }
        unreadAlertsLabel.setText(String.valueOf(s.getUnreadAlerts()));
        avgBmiLabel.setText(s.getAverageBmi() != null
            ? String.format("%.1f", s.getAverageBmi()) : "—");
        avgGlucoseLabel.setText(s.getAverageGlucose() != null
            ? String.format("%.0f mg/dL", s.getAverageGlucose()) : "—");

        // User role bar chart
        if (s.getUsersByRole() != null) {
            XYChart.Series<String, Number> roleSeries = new XYChart.Series<>();
            roleSeries.setName("Users");
            s.getUsersByRole().forEach((role, count) ->
                roleSeries.getData().add(new XYChart.Data<>(role, count)));
            userRoleChart.getData().clear();
            userRoleChart.getData().add(roleSeries);
        }

        // Alert type pie chart
        if (s.getAlertsByType() != null && !s.getAlertsByType().isEmpty()) {
            alertTypeChart.getData().clear();
            s.getAlertsByType().forEach((type, count) ->
                alertTypeChart.getData().add(new PieChart.Data(
                    type.replace("_", " "), count)));
        }

        // User summaries table
        if (s.getUserSummaries() != null)
            summaryTable.setItems(FXCollections.observableArrayList(s.getUserSummaries()));
    }

    @FXML private void handleRefresh() { loadStats(); }

    private String nvl(String s) { return s != null ? s : "—"; }
}
