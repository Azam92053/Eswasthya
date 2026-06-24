package com.eswasthya.desktop.controller;

import com.eswasthya.desktop.AuthSession;
import com.eswasthya.desktop.api.ApiService;
import com.eswasthya.desktop.model.UserInfo;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.Map;

public class ProfileController {

    @FXML private Label  initialLabel;
    @FXML private Label  nameLabel;
    @FXML private Label  roleLabel;
    @FXML private Label  usernameLabel;

    // View mode labels
    @FXML private Label  viewName;
    @FXML private Label  viewEmail;
    @FXML private Label  viewAge;
    @FXML private Label  viewGender;
    @FXML private Label  viewRole;
    @FXML private Label  viewJoined;

    // Edit mode fields
    @FXML private VBox   viewPane;
    @FXML private VBox   editPane;
    @FXML private TextField     editName;
    @FXML private TextField     editEmail;
    @FXML private TextField     editAge;
    @FXML private ComboBox<String> editGender;

    @FXML private Button  btnEdit;
    @FXML private Button  btnSave;
    @FXML private Button  btnCancel;
    @FXML private Label   errorLabel;
    @FXML private Label   successLabel;
    @FXML private ProgressIndicator spinner;

    private UserInfo currentUser;

    @FXML
    public void initialize() {
        editGender.getItems().addAll("MALE", "FEMALE", "OTHER");
        errorLabel.setVisible(false);
        successLabel.setVisible(false);
        spinner.setVisible(false);
        setEditing(false);
        loadProfile();
    }

    private void loadProfile() {
        Task<UserInfo> task = new Task<>() {
            @Override protected UserInfo call() throws Exception {
                return ApiService.getInstance().getProfile();
            }
        };
        task.setOnSucceeded(e -> Platform.runLater(() -> {
            currentUser = task.getValue();
            populateView(currentUser);
        }));
        task.setOnFailed(e -> Platform.runLater(() -> {
            // Fall back to session user
            currentUser = AuthSession.getInstance().getUser();
            if (currentUser != null) populateView(currentUser);
        }));
        new Thread(task, "profile-load").start();
    }

    private void populateView(UserInfo u) {
        initialLabel.setText(u.getInitial());
        nameLabel.setText(u.getName());
        roleLabel.setText(u.getRole());
        usernameLabel.setText("@" + u.getUsername());

        viewName.setText(nvl(u.getName()));
        viewEmail.setText(nvl(u.getEmail()));
        viewAge.setText(u.getAge() != null ? u.getAge() + " years" : "—");
        viewGender.setText(nvl(u.getGender()));
        viewRole.setText(nvl(u.getRole()));
        viewJoined.setText(u.getCreatedAt() != null
            ? u.getCreatedAt().substring(0, Math.min(10, u.getCreatedAt().length())) : "—");
    }

    @FXML
    private void handleEdit() {
        if (currentUser == null) return;
        editName.setText(nvl(currentUser.getName()));
        editEmail.setText(nvl(currentUser.getEmail()));
        editAge.setText(currentUser.getAge() != null ? String.valueOf(currentUser.getAge()) : "");
        editGender.setValue(currentUser.getGender());
        setEditing(true);
    }

    @FXML
    private void handleSave() {
        String nameVal = editName.getText().trim();
        if (nameVal.isEmpty()) { showError("Name is required."); return; }

        Map<String, Object> payload = new HashMap<>();
        payload.put("name", nameVal);
        if (!editEmail.getText().isBlank()) payload.put("email", editEmail.getText().trim());
        if (!editAge.getText().isBlank()) {
            try { payload.put("age", Integer.parseInt(editAge.getText().trim())); }
            catch (NumberFormatException e) { showError("Age must be a number."); return; }
        }
        if (editGender.getValue() != null) payload.put("gender", editGender.getValue());

        spinner.setVisible(true);
        btnSave.setDisable(true);
        errorLabel.setVisible(false);

        Task<UserInfo> task = new Task<>() {
            @Override protected UserInfo call() throws Exception {
                return ApiService.getInstance().updateProfile(payload);
            }
        };
        task.setOnSucceeded(e -> Platform.runLater(() -> {
            spinner.setVisible(false);
            btnSave.setDisable(false);
            currentUser = task.getValue();
            populateView(currentUser);
            setEditing(false);
            successLabel.setText("Profile updated successfully!");
            successLabel.setVisible(true);
        }));
        task.setOnFailed(e -> Platform.runLater(() -> {
            spinner.setVisible(false);
            btnSave.setDisable(false);
            showError(task.getException() != null
                ? task.getException().getMessage() : "Update failed.");
        }));
        new Thread(task, "profile-save").start();
    }

    @FXML
    private void handleCancel() {
        setEditing(false);
        errorLabel.setVisible(false);
    }

    private void setEditing(boolean editing) {
        viewPane.setVisible(!editing);
        viewPane.setManaged(!editing);
        editPane.setVisible(editing);
        editPane.setManaged(editing);
        btnEdit.setVisible(!editing);
        btnSave.setVisible(editing);
        btnCancel.setVisible(editing);
        if (!editing) successLabel.setVisible(false);
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
    }

    private String nvl(String s) { return s != null ? s : ""; }
}
