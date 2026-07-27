package com.smartforum.ui;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.smartforum.model.Notification;
import com.smartforum.service.ApiService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.ArrayList;
import java.util.List;

/**
 * Notifications page — mirrors Notifications/Index.vue.
 * Shows all notifications with unread highlights, per-type icons,
 * All/Unread filtering, delete, and click-to-navigate for topic/quiz alerts.
 */
public class NotificationsView {

    private final MainView shell;
    private VBox listBox;
    private Button allTab, unreadTab;
    private String activeFilter = "all";
    private List<Notification> notifications = new ArrayList<>();

    public NotificationsView(MainView shell) {
        this.shell = shell;
    }

    public Node build() {
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("scroll-pane");

        VBox root = new VBox(18);
        root.setPadding(new Insets(24));
        root.setMaxWidth(760);

        // ── Header ─────────────────────────────────────────────────
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        VBox titleBox = new VBox(2);
        Label h2 = new Label("Notifications");
        h2.setStyle("-fx-font-size:22px;-fx-font-weight:bold;-fx-text-fill:#111827;");
        Label sub = new Label("Warnings, quiz announcements, replies, and other alerts");
        sub.getStyleClass().add("muted-text");
        titleBox.getChildren().addAll(h2, sub);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        Button markAllBtn = UiHelper.linkButton("Mark all as read");
        markAllBtn.setOnAction(e -> markAllRead());
        header.getChildren().addAll(titleBox, markAllBtn);

        // ── Filter tabs ────────────────────────────────────────────
        HBox tabRow = new HBox(6);
        tabRow.setAlignment(Pos.CENTER_LEFT);
        allTab = new Button("All");
        allTab.getStyleClass().add("settings-tab");
        allTab.setOnAction(e -> { activeFilter = "all"; updateTabStyles(); renderFiltered(); });
        unreadTab = new Button("Unread");
        unreadTab.getStyleClass().add("settings-tab");
        unreadTab.setOnAction(e -> { activeFilter = "unread"; updateTabStyles(); renderFiltered(); });
        tabRow.getChildren().addAll(allTab, unreadTab);
        updateTabStyles();

        listBox = new VBox(10);
        Label loadingLbl = new Label("Loading notifications...");
        loadingLbl.getStyleClass().add("muted-text");
        listBox.getChildren().add(loadingLbl);

        root.getChildren().addAll(header, tabRow, listBox);
        scroll.setContent(root);

        loadNotifications();
        return scroll;
    }

    private void updateTabStyles() {
        allTab.setText("All (" + notifications.size() + ")");
        long unread = notifications.stream().filter(n -> !n.read).count();
        unreadTab.setText("Unread (" + unread + ")");

        allTab.getStyleClass().removeAll("settings-tab-active");
        unreadTab.getStyleClass().removeAll("settings-tab-active");
        if (activeFilter.equals("all")) allTab.getStyleClass().add("settings-tab-active");
        else unreadTab.getStyleClass().add("settings-tab-active");
    }

    private void loadNotifications() {
        new Thread(() -> {
            try {
                JsonObject resp = ApiService.getInstance().get("/notifications");
                List<Notification> list = new ArrayList<>();

                if (resp.isJsonArray()) {
                    JsonArray arr = resp.getAsJsonArray();
                    for (JsonElement el : arr) {
                        list.add(ApiService.getInstance().gson.fromJson(el, Notification.class));
                    }
                } else if (resp.isJsonObject() && resp.has("data")) {
                    for (JsonElement el : resp.getAsJsonArray("data")) {
                        list.add(ApiService.getInstance().gson.fromJson(el, Notification.class));
                    }
                }

                notifications = list;
                Platform.runLater(() -> { updateTabStyles(); renderFiltered(); });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    listBox.getChildren().clear();
                    Label err = new Label("Could not load notifications: " + e.getMessage());
                    err.getStyleClass().add("error-text");
                    listBox.getChildren().add(err);
                });
            }
        }).start();
    }

    private void renderFiltered() {
        List<Notification> filtered = activeFilter.equals("unread")
                ? notifications.stream().filter(n -> !n.read).toList()
                : notifications;
        renderNotifications(filtered);
    }

    private void renderNotifications(List<Notification> list) {
        listBox.getChildren().clear();
        if (list.isEmpty()) {
            VBox empty = new VBox(8);
            empty.setAlignment(Pos.CENTER);
            empty.setPadding(new Insets(48));
            Label icon = new Label(activeFilter.equals("unread") ? "✅" : "🔔");
            icon.setStyle("-fx-font-size:32px;");
            Label msg = new Label(activeFilter.equals("unread") ? "You're all caught up!" : "No notifications yet.");
            msg.getStyleClass().add("muted-text");
            empty.getChildren().addAll(icon, msg);
            listBox.getChildren().add(empty);
            return;
        }
        for (Notification n : list) {
            listBox.getChildren().add(notificationItem(n));
        }
    }

    /** Maps a notification type to a FontAwesome icon for quick visual scanning. */
    private FontAwesomeSolid iconFor(String type) {
        if (type == null) return FontAwesomeSolid.BELL;
        return switch (type) {
            case "quiz_announcement" -> FontAwesomeSolid.CLIPBOARD_LIST;
            case "new_reply" -> FontAwesomeSolid.COMMENT;
            case "answer_accepted" -> FontAwesomeSolid.CHECK_CIRCLE;
            case "topic_created" -> FontAwesomeSolid.PLUS_CIRCLE;
            case "moderation_warning" -> FontAwesomeSolid.SHIELD_ALT;
            case "inactivity_warning" -> FontAwesomeSolid.EXCLAMATION_TRIANGLE;
            case "inactivity_blacklist" -> FontAwesomeSolid.BAN;
            default -> FontAwesomeSolid.BELL;
        };
    }

    private String accentFor(String type) {
        if (type == null) return "#6b7280";
        return switch (type) {
            case "quiz_announcement", "new_reply" -> "#2563eb";
            case "answer_accepted" -> "#16a34a";
            case "topic_created" -> "#ea580c";
            case "moderation_warning", "inactivity_blacklist" -> "#e11d48";
            case "inactivity_warning" -> "#b45309";
            default -> "#6b7280";
        };
    }

    private HBox notificationItem(Notification n) {
        HBox row = new HBox(14);
        row.getStyleClass().add(n.read ? "notification-read" : "notification-unread");
        row.setAlignment(Pos.CENTER_LEFT);
        boolean navigable = n.relatedEntityId != null && n.relatedEntityType != null
                && (n.relatedEntityType.contains("Quiz") || n.relatedEntityType.contains("Topic"));
        if (navigable) row.setStyle(row.getStyle() + "-fx-cursor:hand;");

        // Icon badge
        StackPane iconBadge = new StackPane();
        iconBadge.setMinSize(36, 36);
        iconBadge.setMaxSize(36, 36);
        iconBadge.setStyle("-fx-background-color:" + accentFor(n.type) + "22;-fx-background-radius:18;");
        FontIcon iconGlyph = Icons.of(iconFor(n.type), 16, accentFor(n.type));
        iconBadge.getChildren().add(iconGlyph);

        VBox body = new VBox(3);
        HBox.setHgrow(body, Priority.ALWAYS);

        Label titleLbl = new Label(n.title != null ? n.title : "Notification");
        titleLbl.setStyle("-fx-font-weight:600;-fx-text-fill:#111827;-fx-font-size:13px;");

        Label msgLbl = new Label(n.message != null ? n.message : "");
        msgLbl.setStyle("-fx-font-size:12px;-fx-text-fill:#4b5563;");
        msgLbl.setWrapText(true);

        String dateStr = n.createdAt != null ? n.createdAt.substring(0, Math.min(16, n.createdAt.length())) : "";
        Label dateLbl = new Label(dateStr.replace("T", " "));
        dateLbl.getStyleClass().add("meta-text");

        body.getChildren().addAll(titleLbl, msgLbl, dateLbl);

        VBox actions = new VBox(6);
        actions.setAlignment(Pos.TOP_RIGHT);
        if (!n.read) {
            Button markBtn = new Button("Mark read");
            markBtn.setStyle("-fx-background-color:transparent;-fx-text-fill:#ea580c;-fx-font-size:11px;-fx-font-weight:bold;-fx-cursor:hand;-fx-padding:0;");
            markBtn.setOnAction(e -> { e.consume(); markRead(n); });
            actions.getChildren().add(markBtn);
        }
        Button delBtn = new Button("Remove");
        delBtn.setStyle("-fx-background-color:transparent;-fx-text-fill:#9ca3af;-fx-font-size:11px;-fx-cursor:hand;-fx-padding:0;");
        delBtn.setOnAction(e -> { e.consume(); deleteNotification(n); });
        actions.getChildren().add(delBtn);

        row.getChildren().addAll(iconBadge, body, actions);

        if (navigable) {
            row.setOnMouseClicked(e -> {
                if (!n.read) markRead(n);
                if (n.relatedEntityType.contains("Quiz")) {
                    shell.openQuiz(n.relatedEntityId);
                } else {
                    shell.openTopicById(n.relatedEntityId);
                }
            });
        }

        return row;
    }

    private void markRead(Notification n) {
        new Thread(() -> {
            try {
                ApiService.getInstance().post("/notifications/" + n.id + "/read");
                n.read = true;
                Platform.runLater(() -> {
                    updateTabStyles();
                    renderFiltered();
                    shell.refreshBadge();
                });
            } catch (Exception ignored) {}
        }).start();
    }

    private void deleteNotification(Notification n) {
        new Thread(() -> {
            try {
                ApiService.getInstance().delete("/notifications/" + n.id);
                Platform.runLater(() -> {
                    notifications.remove(n);
                    updateTabStyles();
                    renderFiltered();
                    shell.refreshBadge();
                });
            } catch (Exception ignored) {}
        }).start();
    }

    private void markAllRead() {
        new Thread(() -> {
            try {
                ApiService.getInstance().post("/notifications/mark-all-read");
                Platform.runLater(() -> {
                    notifications.forEach(n -> n.read = true);
                    updateTabStyles();
                    renderFiltered();
                    shell.refreshBadge();
                });
            } catch (Exception ignored) {}
        }).start();
    }
}
