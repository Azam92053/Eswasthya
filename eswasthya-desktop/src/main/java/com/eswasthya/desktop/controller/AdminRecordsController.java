package com.eswasthya.desktop.controller;

import com.eswasthya.desktop.api.ApiService;
import com.eswasthya.desktop.model.HealthRecord;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.List;

public class AdminRecordsController {

    @FXML private TableView<HealthRecord>            recordTable;
    @FXML private TableColumn<HealthRecord, String>  colDate;
    @FXML private TableColumn<HealthRecord, String>  colUser;
    @FXML private TableColumn<HealthRecord, String>  colBmi;
    @FXML private TableColumn<HealthRecord, String>  colBmiCat;
    @FXML private TableColumn<HealthRecord, String>  colBp;
    @FXML private TableColumn<HealthRecord, String>  colGlucose;
    @FXML private TableColumn<HealthRecord, String>  colActivity;

    @FXML private TextField searchField;
    @FXML private Label     countLabel;
    @FXML private VBox      loadingBox;
    @FXML private Label     errorLabel;

    private final ObservableList<HealthRecord> all      = FXCollections.observableArrayList();
    private FilteredList<HealthRecord>          filtered;

    @FXML
    public void initialize() {
        setupTable();
        filtered = new FilteredList<>(all, r -> true);
        recordTable.setItems(filtered);
        searchField.textProperty().addListener((obs, old, val) -> applyFilter());
        loadRecords();
    }

    private void setupTable() {
        bindColumnWidths();

        colDate.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue().getRecordDate()));
        colUser.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue().getUserName()));
        colBmi.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue().getBmiFormatted()));
        colBmiCat.setCellValueFactory(c ->
            new SimpleStringProperty(nvl(c.getValue().getBmiCategory())));
        colBp.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue().getBpFormatted()));
        colGlucose.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue().getGlucoseFormatted()));
        colActivity.setCellValueFactory(c ->
            new SimpleStringProperty(nvl(c.getValue().getActivityLevel())));

        colBmiCat.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                getStyleClass().removeAll("cell-normal","cell-warning","cell-danger","cell-info");
                if (!empty && item != null) switch (item) {
                    case "Normal weight" -> getStyleClass().add("cell-normal");
                    case "Overweight"    -> getStyleClass().add("cell-warning");
                    case "Obese"         -> getStyleClass().add("cell-danger");
                    case "Underweight"   -> getStyleClass().add("cell-info");
                }
            }
        });
        recordTable.setPlaceholder(new Label("No records found."));
    }

    private void bindColumnWidths() {
        colDate.prefWidthProperty().bind(recordTable.widthProperty().subtract(20).multiply(0.13));
        colUser.prefWidthProperty().bind(recordTable.widthProperty().subtract(20).multiply(0.18));
        colBmi.prefWidthProperty().bind(recordTable.widthProperty().subtract(20).multiply(0.09));
        colBmiCat.prefWidthProperty().bind(recordTable.widthProperty().subtract(20).multiply(0.18));
        colBp.prefWidthProperty().bind(recordTable.widthProperty().subtract(20).multiply(0.18));
        colGlucose.prefWidthProperty().bind(recordTable.widthProperty().subtract(20).multiply(0.14));
        colActivity.prefWidthProperty().bind(recordTable.widthProperty().subtract(20).multiply(0.10));
    }

    private void loadRecords() {
        loadingBox.setVisible(true);
        errorLabel.setVisible(false);
        Task<List<HealthRecord>> task = new Task<>() {
            @Override protected List<HealthRecord> call() throws Exception {
                return ApiService.getInstance().getAllHealthRecords();
            }
        };
        task.setOnSucceeded(e -> Platform.runLater(() -> {
            all.setAll(task.getValue());
            applyFilter();
            loadingBox.setVisible(false);
        }));
        task.setOnFailed(e -> Platform.runLater(() -> {
            loadingBox.setVisible(false);
            errorLabel.setText("Failed: " +
                (task.getException() != null ? task.getException().getMessage() : "Error"));
            errorLabel.setVisible(true);
        }));
        new Thread(task, "admin-records-load").start();
    }

    private void applyFilter() {
        String q = searchField.getText().toLowerCase().trim();
        filtered.setPredicate(r -> q.isEmpty()
            || (r.getUserName() != null && r.getUserName().toLowerCase().contains(q))
            || (r.getRecordDate() != null && r.getRecordDate().contains(q)));
        countLabel.setText(filtered.size() + " / " + all.size() + " records");
    }

    @FXML private void handleRefresh() { loadRecords(); }

    private String nvl(String s) { return s != null ? s : "—"; }
}
