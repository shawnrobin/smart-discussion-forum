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

import java.util.ArrayList;
import java.util.List;

/**
 * Notifications page — mirrors Notifications/Index.vue.
 * Shows all notifications with unread highlights and mark-as-read actions.
 */
public class NotificationsView {

    private final MainView shell;
    private VBox listBox;
    private List<Notification> notifications = new ArrayList<>();

    public NotificationsView(MainView shell) {
        this.shell = shell;
    }

    public Node build() {
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("scroll-pane");

        VBox root = new VBox(20);
        root.setPadding(new Insets(24));
        root.setMaxWidth(800);

        // ── Header ─────────────────────────────────────────────────
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        Label h2 = new Label("All Notifications");
        h2.setStyle("-fx-font-size:20px;-fx-font-weight:bold;-fx-text-fill:#111827;");
        Button markAllBtn = UiHelper.linkButton("Mark all as read");
        markAllBtn.setOnAction(e -> markAllRead());
        HBox.setHgrow(h2, Priority.ALWAYS);
        header.getChildren().addAll(h2, markAllBtn);

        listBox = new VBox(10);
        Label loadingLbl = new Label("Loading notifications...");
        loadingLbl.getStyleClass().add("muted-text");
        listBox.getChildren().add(loadingLbl);

        root.getChildren().addAll(header, listBox);
        scroll.setContent(root);

        loadNotifications();
        return scroll;
    }

    private void loadNotifications() {
        new Thread(() -> {
            try {
                // The notifications API returns a plain JSON array: [...]
                // Use raw string then parse manually
                String raw = ApiService.getInstance().getRaw("/notifications");
                List<Notification> list = new ArrayList<>();

                com.google.gson.JsonElement parsed = com.google.gson.JsonParser.parseString(raw);

                if (parsed.isJsonArray()) {
                    for (com.google.gson.JsonElement el : parsed.getAsJsonArray()) {
                        list.add(ApiService.getInstance().gson.fromJson(el, Notification.class));
                    }
                } else if (parsed.isJsonObject()) {
                    JsonObject resp = parsed.getAsJsonObject();
                    if (resp.has("data") && resp.get("data").isJsonArray()) {
                        for (com.google.gson.JsonElement el : resp.getAsJsonArray("data")) {
                            list.add(ApiService.getInstance().gson.fromJson(el, Notification.class));
                        }
                    }
                }

                notifications = list;
                Platform.runLater(() -> renderNotifications(list));
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

    private void renderNotifications(List<Notification> list) {
        listBox.getChildren().clear();
        if (list.isEmpty()) {
            Label empty = new Label("No notifications yet.");
            empty.getStyleClass().add("muted-text");
            empty.setPadding(new Insets(32));
            listBox.getChildren().add(empty);
            return;
        }
        for (Notification n : list) {
            listBox.getChildren().add(notificationItem(n));
        }
    }

    private HBox notificationItem(Notification n) {
        HBox row = new HBox(12);
        row.getStyleClass().add(n.read ? "notification-read" : "notification-unread");
        row.setAlignment(Pos.CENTER_LEFT);

        VBox body = new VBox(4);
        HBox.setHgrow(body, Priority.ALWAYS);

        Label titleLbl = new Label(n.title != null ? n.title : "Notification");
        titleLbl.setStyle("-fx-font-weight:600;-fx-text-fill:#111827;-fx-font-size:13px;");

        Label msgLbl = new Label(n.message != null ? n.message : "");
        msgLbl.setStyle("-fx-font-size:13px;-fx-text-fill:#4b5563;-fx-wrap-text:true;");
        msgLbl.setWrapText(true);

        String dateStr = n.createdAt != null ? n.createdAt.substring(0, Math.min(16, n.createdAt.length())) : "";
        Label dateLbl = new Label(dateStr.replace("T", " "));
        dateLbl.getStyleClass().add("meta-text");

        body.getChildren().addAll(titleLbl, msgLbl, dateLbl);

        if (!n.read) {
            Button markBtn = new Button("Mark read");
            markBtn.setStyle("-fx-background-color:transparent;-fx-text-fill:#ea580c;-fx-font-size:11px;-fx-font-weight:bold;-fx-cursor:hand;");
            markBtn.setOnAction(e -> markRead(n, row));
            row.getChildren().addAll(body, markBtn);
        } else {
            row.getChildren().add(body);
        }

        return row;
    }

    private void markRead(Notification n, HBox row) {
        new Thread(() -> {
            try {
                ApiService.getInstance().post("/notifications/" + n.id + "/read");
                n.read = true;
                Platform.runLater(() -> {
                    // Refresh the whole list for simplicity
                    renderNotifications(notifications);
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
                    renderNotifications(notifications);
                    shell.refreshBadge();
                });
            } catch (Exception ignored) {}
        }).start();
    }
}
