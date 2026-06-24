package com.eswasthya.desktop.controller;

import com.eswasthya.desktop.api.ApiService;
import com.eswasthya.desktop.model.UserInfo;
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

public class AdminUsersController {

    @FXML private TableView<UserInfo>           userTable;
    @FXML private TableColumn<UserInfo, String> colName;
    @FXML private TableColumn<UserInfo, String> colUsername;
    @FXML private TableColumn<UserInfo, String> colEmail;
    @FXML private TableColumn<UserInfo, String> colAge;
    @FXML private TableColumn<UserInfo, String> colGender;
    @FXML private TableColumn<UserInfo, String> colRole;
    @FXML private TableColumn<UserInfo, String> colJoined;

    @FXML private TextField         searchField;
    @FXML private ComboBox<String>  roleFilter;
    @FXML private Label             countLabel;
    @FXML private VBox              loadingBox;
    @FXML private Label             errorLabel;

    private final ObservableList<UserInfo> allUsers = FXCollections.observableArrayList();
    private FilteredList<UserInfo> filtered;

    @FXML
    public void initialize() {
        setupTable();
        roleFilter.getItems().addAll("ALL", "STUDENT", "EMPLOYEE", "ADMIN");
        roleFilter.setValue("ALL");

        filtered = new FilteredList<>(allUsers, u -> true);
        userTable.setItems(filtered);

        searchField.textProperty().addListener((obs, old, val) -> applyFilter());
        roleFilter.valueProperty().addListener((obs, old, val) -> applyFilter());

        loadUsers();
    }

    private void setupTable() {
        bindColumnWidths();

        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));
        colUsername.setCellValueFactory(c ->
            new SimpleStringProperty("@" + c.getValue().getUsername()));
        colEmail.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEmail()));
        colAge.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getAge() != null ? String.valueOf(c.getValue().getAge()) : "—"));
        colGender.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue().getGender() != null ? c.getValue().getGender() : "—"));
        colRole.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRole()));
        colJoined.setCellValueFactory(c -> {
            String dt = c.getValue().getCreatedAt();
            return new SimpleStringProperty(dt != null && dt.length() >= 10 ? dt.substring(0,10) : "—");
        });

        colRole.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                getStyleClass().removeAll("cell-info","cell-warning","cell-danger");
                if (!empty && item != null) switch (item) {
                    case "STUDENT"  -> getStyleClass().add("cell-info");
                    case "EMPLOYEE" -> getStyleClass().add("cell-warning");
                    case "ADMIN"    -> getStyleClass().add("cell-danger");
                }
            }
        });
        userTable.setPlaceholder(new Label("No users found."));
    }

    private void bindColumnWidths() {
        colName.prefWidthProperty().bind(userTable.widthProperty().subtract(20).multiply(0.20));
        colUsername.prefWidthProperty().bind(userTable.widthProperty().subtract(20).multiply(0.16));
        colEmail.prefWidthProperty().bind(userTable.widthProperty().subtract(20).multiply(0.26));
        colAge.prefWidthProperty().bind(userTable.widthProperty().subtract(20).multiply(0.08));
        colGender.prefWidthProperty().bind(userTable.widthProperty().subtract(20).multiply(0.10));
        colRole.prefWidthProperty().bind(userTable.widthProperty().subtract(20).multiply(0.11));
        colJoined.prefWidthProperty().bind(userTable.widthProperty().subtract(20).multiply(0.09));
    }

    private void loadUsers() {
        loadingBox.setVisible(true);
        Task<List<UserInfo>> task = new Task<>() {
            @Override protected List<UserInfo> call() throws Exception {
                return ApiService.getInstance().getAllUsers();
            }
        };
        task.setOnSucceeded(e -> Platform.runLater(() -> {
            allUsers.setAll(task.getValue());
            applyFilter();
            loadingBox.setVisible(false);
        }));
        task.setOnFailed(e -> Platform.runLater(() -> {
            loadingBox.setVisible(false);
            errorLabel.setText("Failed: " +
                (task.getException() != null ? task.getException().getMessage() : "Error"));
            errorLabel.setVisible(true);
        }));
        new Thread(task, "users-load").start();
    }

    private void applyFilter() {
        String search = searchField.getText().toLowerCase().trim();
        String role   = roleFilter.getValue();
        filtered.setPredicate(u -> {
            boolean matchSearch = search.isEmpty()
                || u.getName().toLowerCase().contains(search)
                || u.getUsername().toLowerCase().contains(search)
                || (u.getEmail() != null && u.getEmail().toLowerCase().contains(search));
            boolean matchRole = "ALL".equals(role) || role.equals(u.getRole());
            return matchSearch && matchRole;
        });
        countLabel.setText(filtered.size() + " / " + allUsers.size() + " users");
    }

    @FXML private void handleRefresh() { loadUsers(); }
}
