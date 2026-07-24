package com.smartforum.ui;

import com.google.gson.JsonObject;
import com.smartforum.SmartForumApp;
import com.smartforum.model.BootstrapData;
import com.smartforum.model.Notification;
import com.smartforum.model.User;
import com.smartforum.service.ApiService;
import com.smartforum.service.SessionManager;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

/**
 * App shell — left sidebar + top header + content area.
 * Mirrors the AuthenticatedLayout.vue design:
 *   - 256 px white sidebar with nav links
 *   - 72 px white top header with bell + username
 *   - gray-50 scrollable content pane
 */
public class MainView {

    public enum Page { DASHBOARD, GROUPS, QUIZZES, NOTIFICATIONS, SETTINGS, ADMIN, LECTURER }

    private Page currentPage = Page.DASHBOARD;
    private StackPane contentHolder;
    private Label headerTitle;
    private Label bellLabel;
    private Label badgeLabel;
    private HBox offlineBanner;   // shown when no connectivity

    // Sidebar nav buttons
    private Button btnDashboard, btnGroups, btnQuizzes, btnNotifications, btnSettings, btnAdmin, btnLecturer;

    public Pane build() {
        HBox root = new HBox();
        root.getStyleClass().add("root");

        // ── Sidebar ──────────────────────────────────────────────
        VBox sidebar = buildSidebar();
        sidebar.setPrefWidth(256);
        sidebar.setMinWidth(256);
        sidebar.setMaxWidth(256);

        // ── Right pane (header + content) ────────────────────────
        VBox rightPane = new VBox();
        HBox.setHgrow(rightPane, Priority.ALWAYS);

        HBox header = buildHeader();

        // Offline banner (hidden by default)
        offlineBanner = new HBox(8);
        offlineBanner.setAlignment(Pos.CENTER);
        offlineBanner.setPadding(new Insets(6, 16, 6, 16));
        offlineBanner.setStyle("-fx-background-color:#fef3c7;-fx-border-color:#fde68a;-fx-border-width:0 0 1 0;");
        Label offlineIcon = new Label("⚠");
        offlineIcon.setStyle("-fx-text-fill:#92400e;-fx-font-size:13px;");
        Label offlineMsg  = new Label("You are offline. Viewing cached data. New replies will be sent when you reconnect.");
        offlineMsg.setStyle("-fx-text-fill:#92400e;-fx-font-size:12px;");
        offlineBanner.getChildren().addAll(offlineIcon, offlineMsg);
        offlineBanner.setVisible(false);
        offlineBanner.setManaged(false);

        contentHolder = new StackPane();
        VBox.setVgrow(contentHolder, Priority.ALWAYS);
        contentHolder.getStyleClass().add("content-area");

        rightPane.getChildren().addAll(header, offlineBanner, contentHolder);
        root.getChildren().addAll(sidebar, rightPane);

        // Show dashboard by default
        navigateTo(Page.DASHBOARD);

        return root;
    }

    // ── Sidebar ───────────────────────────────────────────────────
    private VBox buildSidebar() {
        VBox sidebar = new VBox();
        sidebar.getStyleClass().add("sidebar");

        // Logo row
        HBox logoRow = new HBox(8);
        logoRow.getStyleClass().add("sidebar-logo");
        logoRow.setAlignment(Pos.CENTER_LEFT);

        StackPane logoSquare = new StackPane(new Label("S") {{
            setStyle("-fx-text-fill:white;-fx-font-weight:bold;-fx-font-size:16px;");
        }});
        logoSquare.getStyleClass().add("logo-square");

        Label logoText = new Label("Smart");
        logoText.getStyleClass().add("logo-text");
        Label logoAccent = new Label("Forum");
        logoAccent.setStyle("-fx-font-size:18px;-fx-font-weight:bold;-fx-text-fill:#ea580c;");
        HBox logoFull = new HBox(logoText, logoAccent);
        logoFull.setAlignment(Pos.CENTER_LEFT);

        logoRow.getChildren().addAll(logoSquare, logoFull);

        // Nav links
        VBox navBox = new VBox(2);
        navBox.setPadding(new Insets(12, 8, 8, 8));

        btnDashboard     = navButton("🏠  Dashboard",     Page.DASHBOARD);
        btnGroups        = navButton("👥  Groups",        Page.GROUPS);
        btnNotifications = navButton("🔔  Notifications", Page.NOTIFICATIONS);
        btnSettings      = navButton("⚙  Settings",      Page.SETTINGS);

        navBox.getChildren().addAll(btnDashboard, btnGroups);

        User user = SessionManager.getInstance().getCurrentUser();
        if (user != null && user.isStudent()) {
            btnQuizzes = navButton("🏆  Quizzes", Page.QUIZZES);
            navBox.getChildren().add(btnQuizzes);
        }

        navBox.getChildren().addAll(btnNotifications, btnSettings);

        // Admin / Lecturer sections
        if (user != null && (user.isAdmin() || user.isLecturer())) {
            VBox adminBox = new VBox(2);
            adminBox.setPadding(new Insets(4, 8, 8, 8));
            adminBox.setStyle("-fx-border-color:#f3f4f6;-fx-border-width:1 0 0 0;");
            Label adminLbl = new Label(user.isAdmin() ? "ADMIN" : "LECTURER");
            adminLbl.getStyleClass().add("nav-section-label");
            adminBox.getChildren().add(adminLbl);

            if (user.isAdmin()) {
                btnAdmin = new Button("🛡  Admin Panel");
                btnAdmin.getStyleClass().add("nav-item");
                btnAdmin.setMaxWidth(Double.MAX_VALUE);
                btnAdmin.setOnAction(e -> navigateTo(Page.ADMIN));
                adminBox.getChildren().add(btnAdmin);
            }
            if (user.isLecturer()) {
                btnLecturer = new Button("📖  Lecturer View");
                btnLecturer.getStyleClass().add("nav-item");
                btnLecturer.setMaxWidth(Double.MAX_VALUE);
                btnLecturer.setOnAction(e -> navigateTo(Page.LECTURER));
                adminBox.getChildren().add(btnLecturer);
            }
            navBox.getChildren().add(adminBox);
        }

        // Logout at bottom
        VBox bottomBox = new VBox();
        bottomBox.setStyle("-fx-border-color:#f3f4f6;-fx-border-width:1 0 0 0;");
        bottomBox.setPadding(new Insets(8));
        Button logoutBtn = new Button("🚪  Logout");
        logoutBtn.getStyleClass().add("nav-logout");
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setOnAction(e -> doLogout());
        bottomBox.getChildren().add(logoutBtn);

        VBox.setVgrow(navBox, Priority.ALWAYS);
        sidebar.getChildren().addAll(logoRow, navBox, UiHelper.vSpacer(), bottomBox);
        return sidebar;
    }

    private Button navButton(String text, Page page) {
        Button btn = new Button(text);
        btn.getStyleClass().add("nav-item");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setOnAction(e -> navigateTo(page));
        return btn;
    }

    // ── Header ────────────────────────────────────────────────────
    private HBox buildHeader() {
        HBox header = new HBox();
        header.getStyleClass().add("top-header");
        header.setAlignment(Pos.CENTER_LEFT);

        headerTitle = new Label("Overview");
        headerTitle.getStyleClass().add("header-title");

        // Right controls
        HBox rightControls = new HBox(12);
        rightControls.setAlignment(Pos.CENTER_RIGHT);

        // Bell button with badge
        StackPane bellStack = new StackPane();
        bellLabel = new Label("🔔");
        bellLabel.getStyleClass().add("bell-btn");
        badgeLabel = new Label("0");
        badgeLabel.getStyleClass().add("badge");
        badgeLabel.setVisible(false);
        StackPane.setAlignment(badgeLabel, Pos.TOP_RIGHT);
        bellStack.getChildren().addAll(bellLabel, badgeLabel);
        bellStack.setOnMouseClicked(e -> navigateTo(Page.NOTIFICATIONS));
        bellStack.setStyle("-fx-cursor:hand;");

        // Separator
        Separator sep = new Separator(javafx.geometry.Orientation.VERTICAL);
        sep.setStyle("-fx-background-color:#e5e7eb;");

        // User name button
        User user = SessionManager.getInstance().getCurrentUser();
        String uname = user != null ? user.name : "Guest";
        Button userBtn = new Button(uname);
        userBtn.getStyleClass().add("header-user-btn");
        userBtn.setOnAction(e -> navigateTo(Page.SETTINGS));

        rightControls.getChildren().addAll(bellStack, sep, userBtn);

        HBox.setHgrow(headerTitle, Priority.ALWAYS);
        header.getChildren().addAll(headerTitle, UiHelper.hSpacer(), rightControls);
        header.setPadding(new Insets(0, 32, 0, 32));

        // Update notification badge
        refreshBadge();

        return header;
    }

    public void refreshBadge() {
        BootstrapData bd = SessionManager.getInstance().getBootstrapData();
        if (bd != null && bd.summary != null) {
            int unread = bd.summary.unread_notifications;
            Platform.runLater(() -> {
                badgeLabel.setText(String.valueOf(unread));
                badgeLabel.setVisible(unread > 0);
            });
        }
    }

    // ── Navigation ────────────────────────────────────────────────
    public void navigateTo(Page page) {
        currentPage = page;
        updateNavActive();

        Node view;
        String title;
        switch (page) {
            case DASHBOARD     -> { view = new DashboardView(this).build();        title = "Overview"; }
            case GROUPS        -> { view = new GroupsView(this).build();           title = "Groups"; }
            case QUIZZES       -> { view = new QuizzesView(this).build();          title = "Quizzes"; }
            case NOTIFICATIONS -> { view = new NotificationsView(this).build();    title = "Notifications"; }
            case SETTINGS      -> { view = new SettingsView(this).build();         title = "Settings"; }
            case ADMIN         -> { view = new AdminView(this).build();            title = "Admin Panel"; }
            case LECTURER      -> { view = new LecturerView(this).build();         title = "Lecturer View"; }
            default            -> { view = new DashboardView(this).build();        title = "Overview"; }
        }

        headerTitle.setText(title);
        contentHolder.getChildren().setAll(view);
    }

    /** Navigate to a specific group's topics. */
    public void openGroup(int groupId, String groupName) {
        headerTitle.setText(groupName);
        updateNavActive(Page.GROUPS);
        Node view = new TopicsView(this, groupId, groupName).build();
        contentHolder.getChildren().setAll(view);
    }

    /** Navigate to a specific topic's detail/replies view. */
    public void openTopic(int groupId, String groupName, int topicId) {
        headerTitle.setText("Discussion");
        updateNavActive(Page.GROUPS);
        Node view = new TopicDetailView(this, groupId, groupName, topicId).build();
        contentHolder.getChildren().setAll(view);
    }

    /** Navigate to the quiz-taking screen. */
    public void openQuiz(int quizId) {
        headerTitle.setText("Quiz");
        Node view = new QuizTakeView(this, quizId).build();
        contentHolder.getChildren().setAll(view);
    }

    private void updateNavActive() {
        updateNavActive(currentPage);
    }

    private void updateNavActive(Page active) {
        Button[] all   = new Button[]{ btnDashboard, btnGroups, btnQuizzes, btnNotifications, btnSettings, btnAdmin, btnLecturer };
        Page[]   pages = new Page[]  { Page.DASHBOARD, Page.GROUPS, Page.QUIZZES, Page.NOTIFICATIONS, Page.SETTINGS, Page.ADMIN, Page.LECTURER };
        for (int i = 0; i < all.length; i++) {
            if (all[i] == null) continue;
            all[i].getStyleClass().removeAll("nav-item-active");
            if (pages[i] == active) all[i].getStyleClass().add("nav-item-active");
        }
    }

    /** Called by ConnectivityService on FX thread when online state changes. */
    public void setOnlineStatus(boolean online) {
        offlineBanner.setVisible(!online);
        offlineBanner.setManaged(!online);
    }

    private void doLogout() {
        new Thread(() -> {
            try { ApiService.getInstance().post("/auth/logout"); } catch (Exception ignored) {}
            SessionManager.getInstance().logout();
            Platform.runLater(SmartForumApp::showLogin);
        }).start();
    }
}
