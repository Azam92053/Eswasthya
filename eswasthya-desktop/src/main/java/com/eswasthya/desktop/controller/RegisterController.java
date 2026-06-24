package com.eswasthya.desktop.controller;

import com.eswasthya.desktop.EswasthyaDesktopApp;
import com.eswasthya.desktop.api.ApiService;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.HashMap;
import java.util.Map;

public class RegisterController {

    @FXML private TextField     usernameField;
    @FXML private TextField     emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmField;
    @FXML private TextField     nameField;
    @FXML private TextField     ageField;
    @FXML private ComboBox<String> genderCombo;
    @FXML private ComboBox<String> roleCombo;
    @FXML private Button        registerButton;
    @FXML private Label         errorLabel;
    @FXML private Label         successLabel;
    @FXML private ProgressIndicator spinner;
    @FXML private Hyperlink     loginLink;

    @FXML
    public void initialize() {
        genderCombo.getItems().addAll("MALE", "FEMALE", "OTHER");
        roleCombo.getItems().addAll("STUDENT", "EMPLOYEE", "ADMIN");
        roleCombo.setValue("STUDENT");
        errorLabel.setVisible(false);
        successLabel.setVisible(false);
        spinner.setVisible(false);
    }

    @FXML
    private void handleRegister() {
        // Validate
        String error = validate();
        if (error != null) { showError(error); return; }

        setLoading(true);

        Map<String, Object> payload = new HashMap<>();
        payload.put("username", usernameField.getText().trim());
        payload.put("email",    emailField.getText().trim());
        payload.put("password", passwordField.getText());
        payload.put("name",     nameField.getText().trim());
        payload.put("role",     roleCombo.getValue());
        if (!ageField.getText().isBlank())
            payload.put("age", Integer.parseInt(ageField.getText().trim()));
        if (genderCombo.getValue() != null)
            payload.put("gender", genderCombo.getValue());

        Task<Void> task = new Task<>() {
            @Override protected Void call() throws Exception {
                ApiService.getInstance().register(payload);
                return null;
            }
        };

        task.setOnSucceeded(e -> Platform.runLater(() -> {
            setLoading(false);
            successLabel.setText("Account created! Redirecting to login...");
            successLabel.setVisible(true);
            new Thread(() -> {
                try { Thread.sleep(1500); }
                catch (InterruptedException ignored) {}
                Platform.runLater(() -> {
                    try { EswasthyaDesktopApp.showLogin(); }
                    catch (Exception ex) { ex.printStackTrace(); }
                });
            }).start();
        }));

        task.setOnFailed(e -> Platform.runLater(() -> {
            setLoading(false);
            showError(task.getException() != null
                    ? task.getException().getMessage() : "Registration failed.");
        }));

        new Thread(task, "register-thread").start();
    }

    @FXML
    private void handleLoginLink() {
        try { EswasthyaDesktopApp.showLogin(); }
        catch (Exception e) { showError("Cannot open login."); }
    }

    private String validate() {
        String u = usernameField.getText().trim();
        String e = emailField.getText().trim();
        String p = passwordField.getText();
        String c = confirmField.getText();
        String n = nameField.getText().trim();

        if (u.isEmpty()) return "Username is required.";
        if (u.length() < 3) return "Username must be at least 3 characters.";
        if (!u.matches("[a-zA-Z0-9_]+")) return "Username: letters, numbers, underscore only.";
        if (e.isEmpty() || !e.contains("@")) return "Valid email is required.";
        if (p.length() < 8) return "Password must be at least 8 characters.";
        if (!p.equals(c)) return "Passwords do not match.";
        if (n.isEmpty()) return "Full name is required.";
        if (!ageField.getText().isBlank()) {
            try {
                int age = Integer.parseInt(ageField.getText().trim());
                if (age < 1 || age > 120) return "Age must be between 1 and 120.";
            } catch (NumberFormatException ex) { return "Age must be a number."; }
        }
        if (roleCombo.getValue() == null) return "Role is required.";
        return null;
    }

    private void setLoading(boolean loading) {
        registerButton.setDisable(loading);
        spinner.setVisible(loading);
        errorLabel.setVisible(false);
        successLabel.setVisible(false);
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
    }
}
