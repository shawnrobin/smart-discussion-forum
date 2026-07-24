package com.smartforum;

import com.smartforum.service.ConnectivityService;
import com.smartforum.service.OfflineCache;
import com.smartforum.service.RealtimeService;
import com.smartforum.service.SessionManager;
import com.smartforum.ui.LoginView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SmartForumApp extends Application {

    public static final String BASE_URL = "https://smart-discussion-forum-x0vd.onrender.com/api";
    public static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        try {
            primaryStage = stage;
            stage.setTitle("SmartForum — Academic Discussion Platform");
            stage.setMinWidth(960);
            stage.setMinHeight(640);
            stage.setWidth(1280);
            stage.setHeight(800);
            OfflineCache.getInstance();
            showLogin();
            stage.show();
        } catch (Exception e) {
            // Write crash to a log file next to the EXE
            try {
                java.io.File log = new java.io.File(
                    System.getProperty("user.dir"), "smartforum-crash.log");
                try (java.io.PrintWriter pw = new java.io.PrintWriter(log)) {
                    pw.println("SmartForum crashed at startup:");
                    e.printStackTrace(pw);
                }
            } catch (Exception ignored) {}
            // Also show an alert so the user knows something went wrong
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("SmartForum — Startup Error");
            alert.setHeaderText("Could not start the application.");
            alert.setContentText(e.getClass().getSimpleName() + ": " + e.getMessage()
                    + "\n\nA crash log has been saved to: smartforum-crash.log");
            alert.showAndWait();
            javafx.application.Platform.exit();
        }
    }

    public static void showLogin() {
        LoginView loginView = new LoginView();
        Scene scene = new Scene(loginView.build(), 1280, 800);
        scene.getStylesheets().add(SmartForumApp.class.getResource("/css/app.css").toExternalForm());
        primaryStage.setScene(scene);
    }

    public static void showMain() {
        com.smartforum.ui.MainView mainView = new com.smartforum.ui.MainView();
        Scene scene = new Scene(mainView.build(), 1280, 800);
        scene.getStylesheets().add(SmartForumApp.class.getResource("/css/app.css").toExternalForm());
        primaryStage.setScene(scene);
        ConnectivityService.getInstance().start(online ->
                Platform.runLater(() -> mainView.setOnlineStatus(online)));
        RealtimeService.getInstance().connect();
    }

    @Override
    public void stop() {
        ConnectivityService.getInstance().stop();
        RealtimeService.getInstance().disconnect();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
