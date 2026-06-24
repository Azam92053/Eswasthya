package com.eswasthya.desktop.controller;

import com.eswasthya.desktop.EswasthyaDesktopApp;
import com.eswasthya.desktop.api.ApiService;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;

public class LoginController {

    @FXML private TextField     usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button        loginButton;
    @FXML private Label         errorLabel;
    @FXML private ProgressIndicator spinner;
    @FXML private Hyperlink     registerLink;

    @FXML
    public void initialize() {
        errorLabel.setVisible(false);
        spinner.setVisible(false);

        // Enter key triggers login from either field
        usernameField.setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ENTER) handleLogin(); });
        passwordField.setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ENTER) handleLogin(); });
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Username and password are required.");
            return;
        }

        setLoading(true);

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                ApiService.getInstance().login(username, password);
                return null;
            }
        };

        task.setOnSucceeded(e -> Platform.runLater(() -> {
            setLoading(false);
            try { EswasthyaDesktopApp.showMain(); }
            catch (Exception ex) { showError("Failed to open main window."); }
        }));

        task.setOnFailed(e -> Platform.runLater(() -> {
            setLoading(false);
            Throwable cause = task.getException();
            showError(cause != null ? cause.getMessage() : "Login failed.");
        }));

        new Thread(task, "login-thread").start();
    }

    @FXML
    private void handleRegisterLink() {
        try { EswasthyaDesktopApp.showRegister(); }
        catch (Exception e) { showError("Cannot open registration."); }
    }

    private void setLoading(boolean loading) {
        loginButton.setDisable(loading);
        spinner.setVisible(loading);
        errorLabel.setVisible(false);
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
    }
}
