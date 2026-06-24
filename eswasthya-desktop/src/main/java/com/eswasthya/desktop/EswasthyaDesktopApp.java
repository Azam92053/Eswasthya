package com.eswasthya.desktop;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

/** JavaFX desktop client entry point. */
public class EswasthyaDesktopApp extends Application {

    public static final String APP_TITLE   = "eSwasthya — Health Portal";
    public static final double MIN_WIDTH   = 1100;
    public static final double MIN_HEIGHT  = 680;
    public static final String BASE_URL    = "http://localhost:8080";

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        stage.setTitle(APP_TITLE);
        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);

        try {
            stage.getIcons().add(new Image(
                Objects.requireNonNull(
                    getClass().getResourceAsStream("/com/eswasthya/desktop/images/icon.png")
                )
            ));
        } catch (Exception ignored) { /* Optional icon is not required. */ }

        showLogin();
        stage.show();
    }

    // Scene navigation

    public static void showLogin() throws IOException {
        loadScene("fxml/login.fxml", MIN_WIDTH, MIN_HEIGHT);
        primaryStage.setResizable(false);
    }

    public static void showRegister() throws IOException {
        loadScene("fxml/register.fxml", MIN_WIDTH, MIN_HEIGHT);
    }

    public static void showMain() throws IOException {
        loadScene("fxml/main.fxml", MIN_WIDTH + 100, MIN_HEIGHT + 80);
        primaryStage.setResizable(true);
    }

    public static void loadScene(String fxmlPath, double width, double height) throws IOException {
        FXMLLoader loader = new FXMLLoader(
            EswasthyaDesktopApp.class.getResource(fxmlPath)
        );
        Scene scene = new Scene(loader.load(), width, height);
        java.net.URL cssUrl = EswasthyaDesktopApp.class.getResource("css/styles.css");
        if (cssUrl != null) scene.getStylesheets().add(cssUrl.toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
    }

    public static Stage getPrimaryStage() { return primaryStage; }

    public static void main(String[] args) {
        launch(args);
    }
}
