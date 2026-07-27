package com.smartforum.ui;

import com.google.gson.JsonObject;
import com.smartforum.SmartForumApp;
import com.smartforum.model.BootstrapData;
import com.smartforum.model.Notification;
import com.smartforum.model.Quiz;
import com.smartforum.model.User;
import com.smartforum.service.ApiService;
import com.smartforum.service.ConnectivityService;
import com.smartforum.service.SessionManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    // Sidebar nav buttons
    private Button btnDashboard, btnGroups, btnQuizzes, btnNotifications, btnSettings, btnAdmin, btnLecturer;
    private Button btnLogout;

    // Desktop Chat Screen Objects: connection status dot + pending/draft counter
    private javafx.scene.shape.Circle connectionDot;
    private Label connectionStatusLabel;
    private Label draftCounterBadge;
    private Timeline draftCounterPoller;

    // Requirement #10: quizzes that have already been auto-launched this session,
    // so the popup doesn't keep re-appearing after the user is on it / has finished it.
    private final Set<Integer> launchedQuizIds = new HashSet<>();
    private Timeline quizLockdownPoller;
    private boolean quizInProgress = false;

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
        contentHolder = new StackPane();
        VBox.setVgrow(contentHolder, Priority.ALWAYS);
        contentHolder.getStyleClass().add("content-area");

        rightPane.getChildren().addAll(header, contentHolder);
        root.getChildren().addAll(sidebar, rightPane);

        // Show dashboard by default
        navigateTo(Page.DASHBOARD);

        startQuizLockdownPoller();
        startConnectivityMonitoring();
        com.smartforum.service.SystemTrayService.getInstance().install(SmartForumApp.primaryStage);

        return root;
    }

    /**
     * Requirement #8 / Desktop Chat Screen Objects (connection status indicator + draft
     * counter). ConnectivityService.start() was never actually called anywhere in the
     * app before this — its polling loop was dormant, so isOnline() never reflected
     * reality. Wiring it up here drives both the status dot and the offline-sync flow.
     */
    private void startConnectivityMonitoring() {
        ConnectivityService.getInstance().start(online -> updateConnectionIndicator(online));
        updateConnectionIndicator(ConnectivityService.getInstance().isOnline());

        draftCounterPoller = new Timeline(new KeyFrame(Duration.seconds(5), e -> updateDraftCounter()));
        draftCounterPoller.setCycleCount(Timeline.INDEFINITE);
        draftCounterPoller.play();
        updateDraftCounter();
    }

    private void updateConnectionIndicator(boolean online) {
        Platform.runLater(() -> {
            connectionDot.setFill(online ? javafx.scene.paint.Color.web("#16a34a") : javafx.scene.paint.Color.web("#dc2626"));
            connectionStatusLabel.setText(online ? "Online" : "Offline");
        });
    }

    private void updateDraftCounter() {
        int pending = com.smartforum.service.OfflineCache.getInstance().getPendingPosts().size();
        Platform.runLater(() -> {
            draftCounterBadge.setText(String.valueOf(pending));
            draftCounterBadge.setVisible(pending > 0);
            draftCounterBadge.setManaged(pending > 0);
        });
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

        btnDashboard     = navButton("Dashboard",     FontAwesomeSolid.HOME, Page.DASHBOARD);
        btnGroups        = navButton("Groups",        FontAwesomeSolid.USERS, Page.GROUPS);
        btnNotifications = navButton("Notifications", FontAwesomeSolid.BELL, Page.NOTIFICATIONS);
        btnSettings      = navButton("Settings",      FontAwesomeSolid.COG, Page.SETTINGS);

        navBox.getChildren().addAll(btnDashboard, btnGroups);

        User user = SessionManager.getInstance().getCurrentUser();
        if (user != null && user.isStudent()) {
            btnQuizzes = navButton("Quizzes", FontAwesomeSolid.TROPHY, Page.QUIZZES);
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
                btnAdmin = new Button("Admin Panel");
                btnAdmin.setGraphic(Icons.of(FontAwesomeSolid.SHIELD_ALT, 14));
                btnAdmin.getStyleClass().add("nav-item");
                btnAdmin.setMaxWidth(Double.MAX_VALUE);
                btnAdmin.setOnAction(e -> navigateTo(Page.ADMIN));
                adminBox.getChildren().add(btnAdmin);
            }
            if (user.isLecturer()) {
                btnLecturer = new Button("Lecturer View");
                btnLecturer.setGraphic(Icons.of(FontAwesomeSolid.CHALKBOARD_TEACHER, 14));
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
        Button logoutBtn = new Button("Logout");
        logoutBtn.setGraphic(Icons.of(FontAwesomeSolid.SIGN_OUT_ALT, 14, "#e11d48"));
        logoutBtn.getStyleClass().add("nav-logout");
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setOnAction(e -> doLogout());
        btnLogout = logoutBtn;
        bottomBox.getChildren().add(logoutBtn);

        VBox.setVgrow(navBox, Priority.ALWAYS);
        sidebar.getChildren().addAll(logoRow, navBox, UiHelper.vSpacer(), bottomBox);
        return sidebar;
    }

    private Button navButton(String text, FontAwesomeSolid icon, Page page) {
        Button btn = new Button(text);
        btn.setGraphic(Icons.of(icon, 14));
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
        bellLabel = new Label();
        bellLabel.setGraphic(Icons.of(FontAwesomeSolid.BELL, 18, "#4b5563"));
        bellLabel.getStyleClass().add("bell-btn");
        badgeLabel = new Label("0");
        badgeLabel.getStyleClass().add("badge");
        badgeLabel.setVisible(false);
        StackPane.setAlignment(badgeLabel, Pos.TOP_RIGHT);
        bellStack.getChildren().addAll(bellLabel, badgeLabel);
        bellStack.setOnMouseClicked(e -> navigateTo(Page.NOTIFICATIONS));
        bellStack.setStyle("-fx-cursor:hand;");

        // Connection status indicator (Desktop Chat Screen Objects: ● Green/Yellow/Red)
        connectionDot = new javafx.scene.shape.Circle(4);
        connectionDot.setFill(javafx.scene.paint.Color.web("#16a34a"));
        connectionStatusLabel = new Label("Online");
        connectionStatusLabel.setStyle("-fx-font-size:11px;-fx-text-fill:#6b7280;-fx-font-weight:600;");
        HBox connectionBox = new HBox(6, connectionDot, connectionStatusLabel);
        connectionBox.setAlignment(Pos.CENTER_LEFT);

        // Draft/pending message counter (Desktop Chat Screen Objects)
        draftCounterBadge = new Label("0");
        draftCounterBadge.setStyle("-fx-background-color:#f59e0b;-fx-text-fill:white;-fx-font-size:10px;-fx-font-weight:bold;" +
                "-fx-background-radius:10;-fx-padding:2 7 2 7;-fx-cursor:hand;");
        draftCounterBadge.setVisible(false);
        draftCounterBadge.setManaged(false);
        javafx.scene.control.Tooltip.install(draftCounterBadge, new javafx.scene.control.Tooltip("Messages waiting to sync while offline"));

        // Separator
        Separator sep = new Separator(javafx.geometry.Orientation.VERTICAL);
        sep.setStyle("-fx-background-color:#e5e7eb;");

        // User name + role badge
        User user = SessionManager.getInstance().getCurrentUser();
        String uname = user != null ? user.name : "Guest";
        Button userBtn = new Button(uname);
        userBtn.getStyleClass().add("header-user-btn");
        userBtn.setOnAction(e -> navigateTo(Page.SETTINGS));

        Label roleBadge = new Label(formatRole(user != null ? user.role : null));
        roleBadge.setStyle("-fx-background-color:#eef2ff;-fx-text-fill:#4338ca;-fx-font-size:10px;-fx-font-weight:bold;" +
                "-fx-background-radius:10;-fx-padding:2 8 2 8;");

        HBox userBox = new HBox(6, userBtn, roleBadge);
        userBox.setAlignment(Pos.CENTER_LEFT);

        rightControls.getChildren().addAll(connectionBox, draftCounterBadge, bellStack, sep, userBox);

        HBox.setHgrow(headerTitle, Priority.ALWAYS);
        header.getChildren().addAll(headerTitle, UiHelper.hSpacer(), rightControls);
        header.setPadding(new Insets(0, 32, 0, 32));

        // Update notification badge
        refreshBadge();

        return header;
    }

    private String formatRole(String role) {
        if (role == null || role.isBlank()) return "Unknown";
        String[] parts = role.split("_");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (!p.isEmpty()) sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1)).append(" ");
        }
        return sb.toString().trim();
    }

    private int lastKnownUnread = -1;

    public void refreshBadge() {
        BootstrapData bd = SessionManager.getInstance().getBootstrapData();
        if (bd != null && bd.summary != null) {
            int unread = bd.summary.unread_notifications;
            if (lastKnownUnread != -1 && unread > lastKnownUnread) {
                com.smartforum.service.SystemTrayService.getInstance()
                        .notify_("New notification", "You have " + unread + " unread notification" + (unread == 1 ? "" : "s") + ".");
            }
            lastKnownUnread = unread;
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

    /**
     * Navigate to a topic when only its id is known (e.g. from a notification).
     * Looks up the topic's group first, then opens it.
     */
    public void openTopicById(int topicId) {
        headerTitle.setText("Discussion");
        updateNavActive(Page.GROUPS);
        new Thread(() -> {
            int groupId = 0;
            try {
                JsonObject resp = ApiService.getInstance().get("/topics/" + topicId);
                if (resp.has("topic") && resp.get("topic").getAsJsonObject().has("group_id")) {
                    groupId = resp.get("topic").getAsJsonObject().get("group_id").getAsInt();
                }
            } catch (Exception ignored) {
                // Fall through with groupId = 0; TopicDetailView will still show the topic itself.
            }
            final int finalGroupId = groupId;
            Platform.runLater(() -> {
                Node view = new TopicDetailView(this, finalGroupId, "Group", topicId).build();
                contentHolder.getChildren().setAll(view);
            });
        }).start();
    }

    /** Navigate to the quiz-taking screen. */
    public void openQuiz(int quizId) {
        launchedQuizIds.add(quizId);
        quizInProgress = true;
        setSidebarLocked(true);
        headerTitle.setText("Quiz");
        Node view = new QuizTakeView(this, quizId).build();
        contentHolder.getChildren().setAll(view);
    }

    /** Called by QuizTakeView once the quiz is submitted (or fails to start) so normal navigation resumes. */
    public void onQuizFinished() {
        quizInProgress = false;
        setSidebarLocked(false);
    }

    /** Requirement #10: while a quiz is active, the interface should prevent the user from doing other things. */
    private void setSidebarLocked(boolean locked) {
        Button[] all = new Button[]{ btnDashboard, btnGroups, btnQuizzes, btnNotifications, btnSettings, btnAdmin, btnLecturer, btnLogout };
        for (Button b : all) {
            if (b != null) b.setDisable(locked);
        }
    }

    /**
     * Requirement #10: "Once the time comes for the quiz, and the interface is open,
     * the quiz should appear ... and prevent the user from doing other things, and the
     * quiz pops up." Polls the cached quiz list every 15s and force-opens any quiz that
     * has just become active for this (student) user.
     */
    private void startQuizLockdownPoller() {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null || !user.isStudent()) return;

        quizLockdownPoller = new Timeline(new KeyFrame(Duration.seconds(15), e -> checkForActiveQuiz()));
        quizLockdownPoller.setCycleCount(Timeline.INDEFINITE);
        quizLockdownPoller.play();
        checkForActiveQuiz();
    }

    private void checkForActiveQuiz() {
        if (quizInProgress || !SessionManager.getInstance().isLoggedIn()) return;

        // Pull the latest quiz list in the background, then evaluate on the FX thread.
        new Thread(() -> {
            try {
                JsonObject bootstrap = ApiService.getInstance().get("/desktop/bootstrap");
                if (bootstrap.has("success") && bootstrap.get("success").getAsBoolean()) {
                    BootstrapData bd = ApiService.getInstance().gson.fromJson(bootstrap, BootstrapData.class);
                    SessionManager.getInstance().setBootstrapData(bd);
                }
            } catch (Exception ignored) {
                // Offline, or server unreachable — fall back to whatever is already cached.
            }
            Platform.runLater(() -> {
                refreshBadge();
                launchAnyNewlyActiveQuiz();
            });
        }).start();
    }

    private void launchAnyNewlyActiveQuiz() {
        if (quizInProgress) return; // don't interrupt an in-progress quiz with another
        BootstrapData bd = SessionManager.getInstance().getBootstrapData();
        if (bd == null || bd.quizzes == null) return;

        long now = System.currentTimeMillis();
        for (Quiz q : bd.quizzes) {
            if (launchedQuizIds.contains(q.id)) continue;
            Long start = parseIsoMillis(q.startTime);
            Long end = parseIsoMillis(q.endTime);
            boolean isActiveNow = "active".equals(q.status)
                    || (start != null && end != null && now >= start && now <= end);
            if (isActiveNow) {
                openQuiz(q.id);
                return; // one at a time
            }
        }
    }

    /** Parses a Laravel ISO-8601 timestamp ("...Z") to epoch millis; null if unparseable. */
    private Long parseIsoMillis(String iso) {
        if (iso == null || iso.isEmpty()) return null;
        try {
            return java.time.Instant.parse(iso).toEpochMilli();
        } catch (Exception e) {
            return null;
        }
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

    private void doLogout() {
        if (quizLockdownPoller != null) quizLockdownPoller.stop();
        if (draftCounterPoller != null) draftCounterPoller.stop();
        new Thread(() -> {
            try { ApiService.getInstance().post("/auth/logout"); } catch (Exception ignored) {}
            SessionManager.getInstance().logout();
            Platform.runLater(SmartForumApp::showLogin);
        }).start();
    }
}
