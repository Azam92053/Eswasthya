package com.eswasthya.desktop.controller;

import com.eswasthya.desktop.api.ApiService;
import com.eswasthya.desktop.model.Alert;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.stream.Collectors;

public class AlertsController {

    @FXML private TableView<Alert>           alertTable;
    @FXML private TableColumn<Alert, String> colStatus;
    @FXML private TableColumn<Alert, String> colType;
    @FXML private TableColumn<Alert, String> colMessage;
    @FXML private TableColumn<Alert, String> colDate;

    @FXML private Label  totalLabel;
    @FXML private Label  unreadLabel;
    @FXML private Label  errorLabel;
    @FXML private VBox   loadingBox;
    @FXML private Button btnMarkRead;
    @FXML private Button btnMarkAll;
    @FXML private ToggleButton toggleUnread;

    private final ObservableList<Alert> allAlerts    = FXCollections.observableArrayList();
    private final ObservableList<Alert> shownAlerts  = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        btnMarkRead.setDisable(true);
        loadAlerts();

        alertTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, old, sel) -> btnMarkRead.setDisable(sel == null || Boolean.TRUE.equals(sel.getIsRead()))
        );

        toggleUnread.selectedProperty().addListener((obs, old, val) -> applyFilter());
    }

    private void setupTable() {
        bindColumnWidths();

        colStatus.setCellValueFactory(c ->
            new SimpleStringProperty(Boolean.TRUE.equals(c.getValue().getIsRead()) ? "Read" : "● Unread"));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                getStyleClass().removeAll("cell-info","cell-muted");
                if (!empty) getStyleClass().add("Read".equals(item) ? "cell-muted" : "cell-info");
            }
        });

        colType.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue().getAlertLabel()));

        colMessage.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue().getMessage()));
        colMessage.setStyle("-fx-wrap-text: true;");

        colDate.setCellValueFactory(c -> {
            String dt = c.getValue().getCreatedAt();
            return new SimpleStringProperty(dt != null && dt.length() >= 10 ? dt.substring(0,10) : dt);
        });

        alertTable.setItems(shownAlerts);
        alertTable.setPlaceholder(new Label("No alerts. Alerts appear automatically when abnormal metrics are recorded."));
    }

    private void bindColumnWidths() {
        colStatus.prefWidthProperty().bind(alertTable.widthProperty().subtract(20).multiply(0.12));
        colType.prefWidthProperty().bind(alertTable.widthProperty().subtract(20).multiply(0.22));
        colMessage.prefWidthProperty().bind(alertTable.widthProperty().subtract(20).multiply(0.52));
        colDate.prefWidthProperty().bind(alertTable.widthProperty().subtract(20).multiply(0.14));
    }

    private void loadAlerts() {
        loadingBox.setVisible(true);
        errorLabel.setVisible(false);

        Task<List<Alert>> task = new Task<>() {
            @Override protected List<Alert> call() throws Exception {
                return ApiService.getInstance().getAllAlerts();
            }
        };
        task.setOnSucceeded(e -> Platform.runLater(() -> {
            allAlerts.setAll(task.getValue());
            applyFilter();
            updateCounts();
            loadingBox.setVisible(false);
        }));
        task.setOnFailed(e -> Platform.runLater(() -> {
            loadingBox.setVisible(false);
            errorLabel.setText("Failed to load alerts: " +
                (task.getException() != null ? task.getException().getMessage() : "Error"));
            errorLabel.setVisible(true);
        }));
        new Thread(task, "alerts-load").start();
    }

    @FXML
    private void handleMarkRead() {
        Alert sel = alertTable.getSelectionModel().getSelectedItem();
        if (sel == null || Boolean.TRUE.equals(sel.getIsRead())) return;

        Task<Void> task = new Task<>() {
            @Override protected Void call() throws Exception {
                ApiService.getInstance().markAlertRead(sel.getId());
                return null;
            }
        };
        task.setOnSucceeded(e -> Platform.runLater(() -> {
            sel.setIsRead(true);
            alertTable.refresh();
            updateCounts();
            btnMarkRead.setDisable(true);
        }));
        task.setOnFailed(e -> Platform.runLater(() ->
            showError("Failed to mark alert: " +
                (task.getException() != null ? task.getException().getMessage() : "Error"))));
        new Thread(task, "alert-mark").start();
    }

    @FXML
    private void handleMarkAll() {
        Task<Void> task = new Task<>() {
            @Override protected Void call() throws Exception {
                ApiService.getInstance().markAllAlertsRead();
                return null;
            }
        };
        task.setOnSucceeded(e -> Platform.runLater(() -> {
            allAlerts.forEach(a -> a.setIsRead(true));
            alertTable.refresh();
            updateCounts();
            btnMarkRead.setDisable(true);
        }));
        task.setOnFailed(e -> Platform.runLater(() ->
            showError("Failed: " + (task.getException() != null ? task.getException().getMessage() : ""))));
        new Thread(task, "alerts-markall").start();
    }

    @FXML private void handleRefresh() { loadAlerts(); }

    private void applyFilter() {
        if (toggleUnread.isSelected()) {
            shownAlerts.setAll(allAlerts.stream()
                .filter(a -> !Boolean.TRUE.equals(a.getIsRead()))
                .collect(Collectors.toList()));
        } else {
            shownAlerts.setAll(allAlerts);
        }
    }

    private void updateCounts() {
        long unread = allAlerts.stream().filter(a -> !Boolean.TRUE.equals(a.getIsRead())).count();
        totalLabel.setText(allAlerts.size() + " total");
        unreadLabel.setText(unread + " unread");
        btnMarkAll.setDisable(unread == 0);
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
    }
}
