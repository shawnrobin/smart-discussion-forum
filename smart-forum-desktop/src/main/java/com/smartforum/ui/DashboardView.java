package com.smartforum.ui;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.smartforum.model.BootstrapData;
import com.smartforum.model.Group;
import com.smartforum.model.Quiz;
import com.smartforum.model.Recommendation;
import com.smartforum.model.User;
import com.smartforum.service.ApiService;
import com.smartforum.service.SessionManager;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Dashboard — mirrors Dashboard.vue.
 * Welcome banner + stat cards + groups list + recent quizzes + activity feed.
 */
public class DashboardView {

    private final MainView shell;
    private VBox root;

    public DashboardView(MainView shell) {
        this.shell = shell;
    }

    public Node build() {
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("scroll-pane");

        root = new VBox(20);
        root.setPadding(new Insets(24));
        root.setFillWidth(true);

        User user = SessionManager.getInstance().getCurrentUser();
        BootstrapData bd = SessionManager.getInstance().getBootstrapData();

        // ── Welcome banner ────────────────────────────────────────
        VBox banner = new VBox(6);
        banner.getStyleClass().add("welcome-banner");
        banner.setPadding(new Insets(28, 32, 28, 32));
        Label welcomeTitle = new Label("Welcome back, " + (user != null ? user.name : "User") + "!");
        welcomeTitle.getStyleClass().add("welcome-title");
        Label welcomeSub = new Label("Here's what's happening across your groups.");
        welcomeSub.getStyleClass().add("welcome-subtitle");
        banner.getChildren().addAll(welcomeTitle, welcomeSub);

        // ── Stat grid ─────────────────────────────────────────────
        HBox statGrid = new HBox(12);
        statGrid.setFillHeight(true);

        if (bd != null && bd.summary != null) {
            statGrid.getChildren().addAll(
                statCard(String.valueOf(bd.summary.groups),   "My Groups",          "stat-number"),
                statCard(String.valueOf(bd.summary.topics),   "Total Discussions",  "stat-number-orange"),
                statCard(String.valueOf(bd.summary.replies),  "Replies",            "stat-number"),
                statCard(String.valueOf(bd.summary.active_quizzes), "Active Quizzes", "stat-number-green"),
                statCard(String.valueOf(bd.summary.unread_notifications), "Unread",  "stat-number-blue")
            );
        }

        for (Node n : statGrid.getChildren()) HBox.setHgrow(n, Priority.ALWAYS);

        // ── Two column layout ─────────────────────────────────────
        HBox columns = new HBox(16);
        columns.setFillHeight(false);

        VBox leftCol = new VBox(16);
        VBox rightCol = new VBox(16);
        HBox.setHgrow(leftCol, Priority.ALWAYS);
        HBox.setHgrow(rightCol, Priority.ALWAYS);

        // Groups card (async loaded)
        VBox groupsCard = buildGroupsCard();
        leftCol.getChildren().add(groupsCard);

        // Quizzes card (from bootstrap)
        if (user != null && user.isStudent() && bd != null && bd.quizzes != null) {
            VBox quizzesCard = buildQuizzesCard(bd.quizzes);
            rightCol.getChildren().add(quizzesCard);
        }

        // Recommended topics (ML-based, Requirement #11)
        if (user != null && user.isStudent() && bd != null && bd.recommendations != null && !bd.recommendations.isEmpty()) {
            VBox recCard = buildRecommendationsCard(bd.recommendations);
            rightCol.getChildren().add(recCard);
        }

        // Activity card (from bootstrap - notifications as activity)
        VBox activityCard = buildActivityCard(bd);
        leftCol.getChildren().add(activityCard);

        columns.getChildren().addAll(leftCol, rightCol);

        root.getChildren().addAll(banner, statGrid, columns);
        scroll.setContent(root);
        return scroll;
    }

    // ── Stat card ──────────────────────────────────────────────────
    private VBox statCard(String number, String label, String numberStyle) {
        VBox card = new VBox(4);
        card.getStyleClass().add("stat-card");
        card.setAlignment(Pos.CENTER);
        Label numLbl = new Label(number);
        numLbl.getStyleClass().addAll("stat-number", numberStyle);
        Label lblTxt = new Label(label);
        lblTxt.getStyleClass().add("stat-label");
        card.getChildren().addAll(numLbl, lblTxt);
        return card;
    }

    // ── Groups card ────────────────────────────────────────────────
    private VBox buildGroupsCard() {
        VBox card = new VBox();
        card.getStyleClass().add("card");

        HBox header = new HBox();
        header.getStyleClass().add("card-header");
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("👥  My Groups");
        title.getStyleClass().add("card-title");
        Button viewAll = UiHelper.linkButton("View All");
        viewAll.setOnAction(e -> shell.navigateTo(MainView.Page.GROUPS));
        header.getChildren().addAll(title, UiHelper.hSpacer(), viewAll);

        VBox body = new VBox(0);
        body.getStyleClass().add("card-body");
        body.setPadding(new Insets(0));

        Label loading = new Label("Loading groups...");
        loading.getStyleClass().add("muted-text");
        loading.setPadding(new Insets(16, 24, 16, 24));
        body.getChildren().add(loading);

        card.getChildren().addAll(header, body);

        // Async fetch
        new Thread(() -> {
            try {
                JsonObject resp = ApiService.getInstance().get("/desktop/groups");
                if (resp.has("groups")) {
                    JsonArray arr = resp.getAsJsonArray("groups");
                    List<Group> groups = new ArrayList<>();
                    for (JsonElement el : arr) {
                        groups.add(ApiService.getInstance().gson.fromJson(el, Group.class));
                    }
                    Platform.runLater(() -> {
                        body.getChildren().clear();
                        if (groups.isEmpty()) {
                            Label empty = new Label("You haven't joined any groups yet.");
                            empty.getStyleClass().add("muted-text");
                            empty.setPadding(new Insets(20, 24, 20, 24));
                            body.getChildren().add(empty);
                        } else {
                            for (Group g : groups.subList(0, Math.min(5, groups.size()))) {
                                body.getChildren().add(groupRow(g));
                            }
                        }
                    });
                }
            } catch (Exception e) {
                Platform.runLater(() -> loading.setText("Could not load groups."));
            }
        }).start();

        return card;
    }

    private HBox groupRow(Group g) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 24, 12, 24));
        row.setStyle("-fx-border-color:#f9fafb;-fx-border-width:0 0 1 0;-fx-cursor:hand;");
        row.setOnMouseClicked(ev -> shell.openGroup(g.id, g.name));
        row.setOnMouseEntered(ev -> row.setStyle("-fx-background-color:#f9fafb;-fx-border-color:#f3f4f6;-fx-border-width:0 0 1 0;-fx-cursor:hand;"));
        row.setOnMouseExited(ev -> row.setStyle("-fx-border-color:#f9fafb;-fx-border-width:0 0 1 0;-fx-cursor:hand;"));

        VBox info = new VBox(2);
        Label name = new Label(g.name);
        name.setStyle("-fx-font-weight:600;-fx-text-fill:#111827;-fx-font-size:13px;");
        Label meta = new Label(g.members + " members · " + g.topics + " topics");
        meta.getStyleClass().add("meta-text");
        info.getChildren().addAll(name, meta);

        Label chevron = new Label("›");
        chevron.setStyle("-fx-text-fill:#d1d5db;-fx-font-size:18px;");

        HBox.setHgrow(info, Priority.ALWAYS);
        row.getChildren().addAll(info, chevron);
        return row;
    }

    // ── Quizzes card ───────────────────────────────────────────────
    private VBox buildQuizzesCard(List<Quiz> quizzes) {
        VBox card = new VBox();
        card.getStyleClass().add("card");

        HBox header = new HBox();
        header.getStyleClass().add("card-header");
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("📋  Recent Quizzes");
        title.getStyleClass().add("card-title");
        Button viewAll = UiHelper.linkButton("View All");
        viewAll.setOnAction(e -> shell.navigateTo(MainView.Page.QUIZZES));
        header.getChildren().addAll(title, UiHelper.hSpacer(), viewAll);

        VBox body = new VBox(0);
        body.setPadding(Insets.EMPTY);

        if (quizzes.isEmpty()) {
            Label empty = new Label("No quizzes available yet.");
            empty.getStyleClass().add("muted-text");
            empty.setPadding(new Insets(20, 24, 20, 24));
            body.getChildren().add(empty);
        } else {
            for (Quiz q : quizzes.subList(0, Math.min(5, quizzes.size()))) {
                body.getChildren().add(quizRow(q));
            }
        }

        card.getChildren().addAll(header, body);
        return card;
    }

    private HBox quizRow(Quiz q) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 24, 12, 24));
        row.setStyle("-fx-border-color:#f9fafb;-fx-border-width:0 0 1 0;-fx-cursor:hand;");
        row.setOnMouseEntered(ev -> row.setStyle("-fx-background-color:#f9fafb;-fx-border-color:#f3f4f6;-fx-border-width:0 0 1 0;-fx-cursor:hand;"));
        row.setOnMouseExited(ev -> row.setStyle("-fx-border-color:#f9fafb;-fx-border-width:0 0 1 0;-fx-cursor:hand;"));
        row.setOnMouseClicked(ev -> {
            if ("active".equals(q.status)) shell.openQuiz(q.id);
        });

        VBox info = new VBox(2);
        Label name = new Label(q.title);
        name.setStyle("-fx-font-weight:600;-fx-text-fill:#111827;-fx-font-size:13px;");
        Label meta = new Label(q.durationMinutes + " min");
        meta.getStyleClass().add("meta-text");
        info.getChildren().addAll(name, meta);

        Label badge = quizBadge(q.status);
        Label chevron = new Label("›");
        chevron.setStyle("-fx-text-fill:#d1d5db;-fx-font-size:18px;");

        HBox.setHgrow(info, Priority.ALWAYS);
        row.getChildren().addAll(info, badge, chevron);
        return row;
    }

    private Label quizBadge(String status) {
        return switch (status) {
            case "active"   -> UiHelper.badge("Active",   "badge-active");
            case "upcoming" -> UiHelper.badge("Upcoming", "badge-upcoming");
            default         -> UiHelper.badge("Closed",   "badge-closed");
        };
    }

    // ── Recommendations card (Requirement #11) ─────────────────────
    private VBox buildRecommendationsCard(List<Recommendation> recs) {
        VBox card = new VBox();
        card.getStyleClass().add("card");

        HBox header = new HBox();
        header.getStyleClass().add("card-header");
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("✨  Recommended For You");
        title.getStyleClass().add("card-title");
        header.getChildren().add(title);

        VBox body = new VBox(0);
        body.setPadding(Insets.EMPTY);

        for (Recommendation r : recs.subList(0, Math.min(6, recs.size()))) {
            body.getChildren().add(recommendationRow(r));
        }

        card.getChildren().addAll(header, body);
        return card;
    }

    private HBox recommendationRow(Recommendation r) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 24, 12, 24));
        row.setStyle("-fx-border-color:#f9fafb;-fx-border-width:0 0 1 0;-fx-cursor:hand;");
        row.setOnMouseEntered(ev -> row.setStyle("-fx-background-color:#f9fafb;-fx-border-color:#f3f4f6;-fx-border-width:0 0 1 0;-fx-cursor:hand;"));
        row.setOnMouseExited(ev -> row.setStyle("-fx-border-color:#f9fafb;-fx-border-width:0 0 1 0;-fx-cursor:hand;"));
        row.setOnMouseClicked(ev -> shell.openTopic(r.groupId, r.groupName, r.topicId));

        VBox info = new VBox(2);
        Label name = new Label(r.title);
        name.setStyle("-fx-font-weight:600;-fx-text-fill:#111827;-fx-font-size:13px;");
        name.setWrapText(true);
        String metaText = r.groupName + " · " + r.replies + " replies" + (r.isAnswered ? " · answered" : "");
        Label meta = new Label(metaText);
        meta.getStyleClass().add("meta-text");
        info.getChildren().addAll(name, meta);

        Label matchBadge = new Label(Math.round(r.score) + "% match");
        matchBadge.setStyle("-fx-background-color:#fef3c7;-fx-text-fill:#92400e;-fx-font-size:10px;-fx-font-weight:bold;-fx-background-radius:20;-fx-padding:2 8 2 8;");

        HBox.setHgrow(info, Priority.ALWAYS);
        row.getChildren().addAll(info, matchBadge);
        return row;
    }

    // ── Activity card ──────────────────────────────────────────────
    private VBox buildActivityCard(BootstrapData bd) {
        VBox card = new VBox();
        card.getStyleClass().add("card");

        HBox header = new HBox();
        header.getStyleClass().add("card-header");
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("🔔  Recent Notifications");
        title.getStyleClass().add("card-title");
        header.getChildren().add(title);

        VBox body = new VBox(0);
        body.setPadding(Insets.EMPTY);

        if (bd == null || bd.notifications == null || bd.notifications.isEmpty()) {
            Label empty = new Label("No recent activity.");
            empty.getStyleClass().add("muted-text");
            empty.setPadding(new Insets(20, 24, 20, 24));
            body.getChildren().add(empty);
        } else {
            for (var n : bd.notifications.subList(0, Math.min(6, bd.notifications.size()))) {
                HBox row = new HBox(12);
                row.setPadding(new Insets(12, 24, 12, 24));
                row.setAlignment(Pos.CENTER_LEFT);
                row.setStyle("-fx-border-color:#f9fafb;-fx-border-width:0 0 1 0;");

                StackPane dot = new StackPane();
                dot.setStyle("-fx-background-color:" + (n.read ? "#d1d5db" : "#ea580c") + ";-fx-background-radius:50;-fx-min-width:8;-fx-min-height:8;-fx-max-width:8;-fx-max-height:8;");

                VBox info = new VBox(2);
                Label msg = new Label(n.message != null ? n.message : n.title);
                msg.setStyle("-fx-font-size:13px;-fx-text-fill:#111827;-fx-wrap-text:true;");
                msg.setWrapText(true);
                Label time = new Label(n.createdAt != null ? n.createdAt.substring(0, Math.min(10, n.createdAt.length())) : "");
                time.getStyleClass().add("meta-text");
                info.getChildren().addAll(msg, time);

                HBox.setHgrow(info, Priority.ALWAYS);
                row.getChildren().addAll(dot, info);
                body.getChildren().add(row);
            }
        }

        card.getChildren().addAll(header, body);
        return card;
    }
}
