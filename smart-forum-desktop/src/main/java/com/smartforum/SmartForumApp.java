package com.smartforum;

import com.smartforum.service.ApiService;
import com.smartforum.service.SessionManager;
import com.smartforum.ui.LoginView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Entry point for the Smart Forum Desktop application.
 * Mirrors the web interface design: orange-600 brand color, white cards,
 * gray-50 backgrounds, Figtree-style typography.
 */
public class SmartForumApp extends Application {

    public static final String BASE_URL = "https://smart-discussion-forum-x0vd.onrender.com/api";
    public static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        stage.setTitle("SmartForum — Academic Discussion Platform");
        stage.setMinWidth(960);
        stage.setMinHeight(640);
        stage.setWidth(1280);
        stage.setHeight(800);

        // Start with login screen
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
    }

    public static void main(String[] args) {
        launch(args);
    }
}
