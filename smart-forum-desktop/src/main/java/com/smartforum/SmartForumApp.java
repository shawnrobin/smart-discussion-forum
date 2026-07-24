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

/**
 * Entry point for the Smart Forum Desktop application.
 */
public class SmartForumApp extends Application {

    public static final String BASE_URL = "http://127.0.0.1:8001/api";
    public static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        stage.setTitle("SmartForum — Academic Discussion Platform");
        stage.setMinWidth(960);
        stage.setMinHeight(640);
        stage.setWidth(1280);
        stage.setHeight(800);

        // Pre-warm offline cache DB
        OfflineCache.getInstance();

        showLogin();
        stage.show();
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

        // Start connectivity monitor (offline detection + sync)
        ConnectivityService.getInstance().start(online ->
                Platform.runLater(() -> mainView.setOnlineStatus(online)));

        // Connect to Reverb WebSocket
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
