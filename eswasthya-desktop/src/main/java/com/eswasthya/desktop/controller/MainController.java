package com.eswasthya.desktop.controller;

import com.eswasthya.desktop.AuthSession;
import com.eswasthya.desktop.EswasthyaDesktopApp;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.Objects;

public class MainController {

    @FXML private Label    userNameLabel;
    @FXML private Label    userRoleLabel;
    @FXML private Label    userInitialLabel;
    @FXML private StackPane contentArea;
    @FXML private VBox     adminSection;

    // Nav buttons
    @FXML private Button btnDashboard;
    @FXML private Button btnRecords;
    @FXML private Button btnAlerts;
    @FXML private Button btnProfile;
    @FXML private Button btnAdminStats;
    @FXML private Button btnAdminUsers;
    @FXML private Button btnAdminRecords;

    private Button activeButton;

    @FXML
    public void initialize() {
        AuthSession session = AuthSession.getInstance();

        // Populate sidebar user info
        userNameLabel.setText(session.getFullName());
        userRoleLabel.setText(session.isAdmin() ? "Administrator"
                : session.getUser() != null ? session.getUser().getRole() : "");
        userInitialLabel.setText(session.getUser() != null
                ? session.getUser().getInitial() : "?");

        // Show admin nav only for ADMIN role
        adminSection.setVisible(session.isAdmin());
        adminSection.setManaged(session.isAdmin());

        // Load dashboard on startup
        showDashboard();
    }

    // Navigation handlers

    @FXML public void showDashboard()     { navigate("fxml/dashboard.fxml",       btnDashboard); }
    @FXML public void showRecords()       { navigate("fxml/health-records.fxml",  btnRecords); }
    @FXML public void showAlerts()        { navigate("fxml/alerts.fxml",          btnAlerts); }
    @FXML public void showProfile()       { navigate("fxml/profile.fxml",         btnProfile); }
    @FXML public void showAdminStats()    { navigate("fxml/admin-dashboard.fxml", btnAdminStats); }
    @FXML public void showAdminUsers()    { navigate("fxml/admin-users.fxml",     btnAdminUsers); }
    @FXML public void showAdminRecords()  { navigate("fxml/admin-records.fxml",   btnAdminRecords); }

    @FXML
    private void handleLogout() {
        AuthSession.getInstance().logout();
        try { EswasthyaDesktopApp.showLogin(); }
        catch (IOException e) { e.printStackTrace(); }
    }

    // Internal

    private void navigate(String fxmlPath, Button button) {
        try {
            FXMLLoader loader = new FXMLLoader(
                Objects.requireNonNull(
                    EswasthyaDesktopApp.class.getResource(fxmlPath)
                )
            );
            Node content = loader.load();
            contentArea.getChildren().setAll(content);

            // Highlight active nav button
            if (activeButton != null) activeButton.getStyleClass().remove("nav-active");
            button.getStyleClass().add("nav-active");
            activeButton = button;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
