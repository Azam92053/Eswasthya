package com.eswasthya.desktop.controller;

import com.eswasthya.desktop.api.ApiService;
import com.eswasthya.desktop.model.HealthRecord;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.*;
import java.time.LocalDate;

import java.util.*;

public class HealthRecordsController {

    @FXML private TableView<HealthRecord>           recordTable;
    @FXML private TableColumn<HealthRecord, String> colDate;
    @FXML private TableColumn<HealthRecord, String> colBmi;
    @FXML private TableColumn<HealthRecord, String> colBmiCat;
    @FXML private TableColumn<HealthRecord, String> colBp;
    @FXML private TableColumn<HealthRecord, String> colGlucose;
    @FXML private TableColumn<HealthRecord, String> colActivity;
    @FXML private TableColumn<HealthRecord, String> colNotes;

    @FXML private VBox    loadingBox;
    @FXML private Label   recordCountLabel;
    @FXML private Label   errorLabel;
    @FXML private Button  btnNew;
    @FXML private Button  btnEdit;
    @FXML private Button  btnDelete;
    @FXML private Button  btnDownload;

    // Form fields (inside dialog pane)
    @FXML private DatePicker          formDate;
    @FXML private TextField           formBmi;
    @FXML private TextField           formSystolic;
    @FXML private TextField           formDiastolic;
    @FXML private TextField           formGlucose;
    @FXML private ComboBox<String>    formActivity;
    @FXML private TextArea            formNotes;

    private final ObservableList<HealthRecord> records = FXCollections.observableArrayList();
    private HealthRecord editingRecord = null;

    @FXML
    public void initialize() {
        setupTable();
        btnEdit.setDisable(true);
        btnDelete.setDisable(true);
        loadRecords();

        // Enable edit/delete when a row is selected
        recordTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, old, sel) -> {
                boolean hasSelection = sel != null;
                btnEdit.setDisable(!hasSelection);
                btnDelete.setDisable(!hasSelection);
            }
        );
    }

    private void setupTable() {
        bindColumnWidths();

        colDate.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue().getRecordDate()));
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
        colNotes.setCellValueFactory(c ->
            new SimpleStringProperty(nvl(c.getValue().getNotes())));

        // Color-code BMI category column
        colBmiCat.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                getStyleClass().removeAll("cell-normal","cell-warning","cell-danger","cell-info");
                if (!empty && item != null) {
                    switch (item) {
                        case "Normal weight" -> getStyleClass().add("cell-normal");
                        case "Overweight"    -> getStyleClass().add("cell-warning");
                        case "Obese"         -> getStyleClass().add("cell-danger");
                        case "Underweight"   -> getStyleClass().add("cell-info");
                    }
                }
            }
        });

        recordTable.setItems(records);
        recordTable.setPlaceholder(new Label("No health records yet. Click 'New Record' to add one."));
    }

    private void bindColumnWidths() {
        colDate.prefWidthProperty().bind(recordTable.widthProperty().subtract(20).multiply(0.12));
        colBmi.prefWidthProperty().bind(recordTable.widthProperty().subtract(20).multiply(0.08));
        colBmiCat.prefWidthProperty().bind(recordTable.widthProperty().subtract(20).multiply(0.16));
        colBp.prefWidthProperty().bind(recordTable.widthProperty().subtract(20).multiply(0.17));
        colGlucose.prefWidthProperty().bind(recordTable.widthProperty().subtract(20).multiply(0.14));
        colActivity.prefWidthProperty().bind(recordTable.widthProperty().subtract(20).multiply(0.13));
        colNotes.prefWidthProperty().bind(recordTable.widthProperty().subtract(20).multiply(0.20));
    }

    private void loadRecords() {
        loadingBox.setVisible(true);
        errorLabel.setVisible(false);

        Task<List<HealthRecord>> task = new Task<>() {
            @Override protected List<HealthRecord> call() throws Exception {
                return ApiService.getInstance().getAllRecords();
            }
        };
        task.setOnSucceeded(e -> Platform.runLater(() -> {
            records.setAll(task.getValue());
            recordCountLabel.setText(records.size() + " record(s)");
            loadingBox.setVisible(false);
        }));
        task.setOnFailed(e -> Platform.runLater(() -> {
            loadingBox.setVisible(false);
            showError(task.getException());
        }));
        new Thread(task, "records-load").start();
    }

    @FXML
    private void handleNew() {
        editingRecord = null;
        showRecordDialog(null);
    }

    @FXML
    private void handleEdit() {
        HealthRecord sel = recordTable.getSelectionModel().getSelectedItem();
        if (sel != null) { editingRecord = sel; showRecordDialog(sel); }
    }

    @FXML
    private void handleDelete() {
        HealthRecord sel = recordTable.getSelectionModel().getSelectedItem();
        if (sel == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Delete record for " + sel.getRecordDate() + "?\nThis also deletes linked alerts.",
            ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Health Record");
        confirm.showAndWait().filter(r -> r == ButtonType.YES).ifPresent(r -> {
            Task<Void> task = new Task<>() {
                @Override protected Void call() throws Exception {
                    ApiService.getInstance().deleteRecord(sel.getId());
                    return null;
                }
            };
            task.setOnSucceeded(e -> Platform.runLater(() -> {
                records.remove(sel);
                recordCountLabel.setText(records.size() + " record(s)");
            }));
            task.setOnFailed(e -> Platform.runLater(() -> showError(task.getException())));
            new Thread(task, "record-delete").start();
        });
    }

    @FXML
    private void handleDownload() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Health Report");
        chooser.setInitialFileName("eswasthya-report-" + LocalDate.now() + ".txt");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        File file = chooser.showSaveDialog(btnDownload.getScene().getWindow());
        if (file == null) return;

        Task<Void> task = new Task<>() {
            @Override protected Void call() throws Exception {
                byte[] report = ApiService.getInstance().downloadReport();
                try (FileOutputStream fos = new FileOutputStream(file)) { fos.write(report); }
                return null;
            }
        };
        task.setOnSucceeded(e -> Platform.runLater(() -> {
            showInfo("Report saved to:\n" + file.getAbsolutePath());
        }));
        task.setOnFailed(e -> Platform.runLater(() -> showError(task.getException())));
        new Thread(task, "report-download").start();
    }

    private void showRecordDialog(HealthRecord existing) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "New Health Record" : "Edit Health Record");
        dialog.setHeaderText(existing == null
                ? "Log today's health metrics" : "Update record — " + existing.getRecordDate());

        // Form fields
        DatePicker datePicker = new DatePicker(
            existing != null && existing.getRecordDate() != null
                ? LocalDate.parse(existing.getRecordDate()) : LocalDate.now());
        TextField bmiField      = new TextField(existing != null && existing.getBmi() != null
                ? String.format("%.1f", existing.getBmi()) : "");
        TextField sysField      = new TextField(existing != null && existing.getSystolicBp() != null
                ? String.valueOf(existing.getSystolicBp()) : "");
        TextField diaField      = new TextField(existing != null && existing.getDiastolicBp() != null
                ? String.valueOf(existing.getDiastolicBp()) : "");
        TextField glucoseField  = new TextField(existing != null && existing.getGlucose() != null
                ? String.format("%.0f", existing.getGlucose()) : "");
        ComboBox<String> activityCombo = new ComboBox<>();
        activityCombo.getItems().addAll("LOW", "MODERATE", "HIGH");
        activityCombo.setValue(existing != null && existing.getActivityLevel() != null
                ? existing.getActivityLevel() : "MODERATE");
        TextArea notesArea = new TextArea(existing != null ? nvl(existing.getNotes()) : "");
        notesArea.setPrefRowCount(3);
        notesArea.setWrapText(true);
        Label formError = new Label();
        formError.getStyleClass().add("error-label");
        formError.setWrapText(true);
        formError.setVisible(false);
        formError.setManaged(false);

        // Layout
        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(12); grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(16));
        javafx.scene.layout.ColumnConstraints labelCol = new javafx.scene.layout.ColumnConstraints();
        labelCol.setMinWidth(120);
        javafx.scene.layout.ColumnConstraints fieldCol = new javafx.scene.layout.ColumnConstraints();
        fieldCol.setHgrow(javafx.scene.layout.Priority.ALWAYS);
        fieldCol.setFillWidth(true);
        grid.getColumnConstraints().addAll(labelCol, fieldCol);
        int row = 0;
        grid.add(new Label("Date *"),          0, row); grid.add(datePicker,    1, row++);
        grid.add(new Label("BMI *"),           0, row); grid.add(bmiField,      1, row++);
        grid.add(new Label("Systolic BP *"),   0, row); grid.add(sysField,      1, row++);
        grid.add(new Label("Diastolic BP *"),  0, row); grid.add(diaField,      1, row++);
        grid.add(new Label("Glucose (mg/dL) *"), 0, row); grid.add(glucoseField,  1, row++);
        grid.add(new Label("Activity Level *"),  0, row); grid.add(activityCombo, 1, row++);
        grid.add(new Label("Notes"),           0, row); grid.add(notesArea,     1, row);
        grid.add(formError, 0, ++row, 2, 1);
        datePicker.setMaxWidth(Double.MAX_VALUE);
        bmiField.setMaxWidth(Double.MAX_VALUE);
        sysField.setMaxWidth(Double.MAX_VALUE);
        diaField.setMaxWidth(Double.MAX_VALUE);
        glucoseField.setMaxWidth(Double.MAX_VALUE);
        activityCombo.setMaxWidth(Double.MAX_VALUE);
        notesArea.setMaxWidth(Double.MAX_VALUE);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        // Load stylesheet safely — won't crash if CSS not found
        java.net.URL cssUrl = getClass().getResource("/com/eswasthya/desktop/css/styles.css");
        if (cssUrl != null)
            dialog.getDialogPane().getStylesheets().add(cssUrl.toExternalForm());
        dialog.getDialogPane().setPrefWidth(460);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(ActionEvent.ACTION, event -> {
            try {
                buildRecordPayload(datePicker, bmiField, sysField, diaField,
                        glucoseField, activityCombo, notesArea);
                formError.setVisible(false);
                formError.setManaged(false);
            } catch (IllegalArgumentException ex) {
                formError.setText(ex.getMessage());
                formError.setVisible(true);
                formError.setManaged(true);
                event.consume();
            }
        });

        dialog.showAndWait().filter(r -> r == ButtonType.OK).ifPresent(r -> {
            Map<String, Object> payload = buildRecordPayload(datePicker, bmiField, sysField, diaField,
                    glucoseField, activityCombo, notesArea);

            Task<HealthRecord> task = new Task<>() {
                @Override protected HealthRecord call() throws Exception {
                    return existing == null
                        ? ApiService.getInstance().createRecord(payload)
                        : ApiService.getInstance().updateRecord(existing.getId(), payload);
                }
            };
            task.setOnSucceeded(e -> Platform.runLater(() -> {
                loadRecords();
                showInfo("Record saved successfully!");
            }));
            task.setOnFailed(e -> Platform.runLater(() -> showError(task.getException())));
            new Thread(task, "record-save").start();
        });
    }

    private Map<String, Object> buildRecordPayload(DatePicker datePicker,
                                                   TextField bmiField,
                                                   TextField sysField,
                                                   TextField diaField,
                                                   TextField glucoseField,
                                                   ComboBox<String> activityCombo,
                                                   TextArea notesArea) {
        if (datePicker.getValue() == null) {
            throw new IllegalArgumentException("Please select a record date.");
        }

        double bmi = parseDoubleRequired(bmiField, "BMI");
        int systolic = parseIntRequired(sysField, "Systolic BP");
        int diastolic = parseIntRequired(diaField, "Diastolic BP");
        double glucose = parseDoubleRequired(glucoseField, "Glucose");
        String activity = activityCombo.getValue();
        if (activity == null || activity.isBlank()) {
            throw new IllegalArgumentException("Please select an activity level.");
        }

        requireRange(bmi, 10, 80, "BMI");
        requireRange(systolic, 70, 250, "Systolic BP");
        requireRange(diastolic, 40, 150, "Diastolic BP");
        if (systolic <= diastolic) {
            throw new IllegalArgumentException("Systolic BP must be higher than diastolic BP.");
        }
        requireRange(glucose, 40, 500, "Glucose");

        Map<String, Object> payload = new HashMap<>();
        payload.put("recordDate", datePicker.getValue().toString());
        payload.put("bmi", bmi);
        payload.put("systolicBp", systolic);
        payload.put("diastolicBp", diastolic);
        payload.put("bloodPressure", systolic + "/" + diastolic);
        payload.put("glucose", glucose);
        payload.put("activityLevel", activity);
        if (!notesArea.getText().isBlank()) {
            payload.put("notes", notesArea.getText().trim());
        }
        return payload;
    }

    private double parseDoubleRequired(TextField field, String label) {
        String value = field.getText() != null ? field.getText().trim() : "";
        if (value.isBlank()) {
            throw new IllegalArgumentException(label + " is required.");
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(label + " must be a valid number.");
        }
    }

    private int parseIntRequired(TextField field, String label) {
        String value = field.getText() != null ? field.getText().trim() : "";
        if (value.isBlank()) {
            throw new IllegalArgumentException(label + " is required.");
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(label + " must be a whole number.");
        }
    }

    private void requireRange(double value, double min, double max, String label) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(label + " must be between " + formatRange(min) + " and " + formatRange(max) + ".");
        }
    }

    private String formatRange(double value) {
        return value == Math.rint(value) ? String.valueOf((int) value) : String.valueOf(value);
    }

    @FXML private void handleRefresh() { loadRecords(); }

    private void showError(Throwable ex) {
        errorLabel.setText("Error: " + (ex != null ? ex.getMessage() : "Unknown error"));
        errorLabel.setVisible(true);
    }
    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setHeaderText(null); a.showAndWait();
    }
    private String nvl(String s) { return s != null ? s : ""; }
}
