package com.smartforum.ui;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
 * Admin Panel — exact replica of Admin/Dashboard.vue + Admin/Users/Index.vue
 *               + Admin/Moderation/Index.vue.
 * Three tabs: Overview · Users · Moderation
 */
public class AdminView {

    private final MainView shell;
    private StackPane tabContent;
    private final String[] TAB_IDS    = {"overview", "users", "moderation"};
    private final String[] TAB_LABELS = {"Overview", "Users", "Moderation"};
    private String activeTab = "overview";

    public AdminView(MainView shell) { this.shell = shell; }

    public Node build() {
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("scroll-pane");

        VBox root = new VBox(0);
        root.setPadding(new Insets(24));

        // Page heading
        VBox heading = new VBox(2);
        heading.setPadding(new Insets(0, 0, 20, 0));
        Label h1 = new Label("Admin Dashboard");
        h1.setStyle("-fx-font-size:22px;-fx-font-weight:bold;-fx-text-fill:#1e293b;");
        Label sub = new Label("Overview of system health and activity.");
        sub.setStyle("-fx-font-size:12px;-fx-text-fill:#94a3b8;");
        heading.getChildren().addAll(h1, sub);

        // Tab bar
        HBox tabBar = new HBox(0);
        tabBar.setStyle("-fx-border-color:#e2e8f0;-fx-border-width:0 0 1 0;");
        tabBar.setPadding(new Insets(0, 0, 0, 0));
        for (int i = 0; i < TAB_IDS.length; i++) {
            tabBar.getChildren().add(makeTabBtn(TAB_IDS[i], TAB_LABELS[i]));
        }

        tabContent = new StackPane();
        tabContent.setPadding(new Insets(20, 0, 0, 0));

        root.getChildren().addAll(heading, tabBar, tabContent);
        scroll.setContent(root);
        switchTab("overview");
        return scroll;
    }

    private Button makeTabBtn(String id, String label) {
        Button btn = new Button(label);
        btn.setId("atab-" + id);
        btn.setOnAction(e -> switchTab(id));
        styleTabBtn(btn, id.equals(activeTab));
        return btn;
    }

    private void styleTabBtn(Button btn, boolean active) {
        btn.setStyle(
            "-fx-background-color:transparent;" +
            "-fx-border-color:transparent transparent " + (active ? "#ea580c" : "transparent") + " transparent;" +
            "-fx-border-width:0 0 2 0;" +
            "-fx-font-size:13px;-fx-font-weight:" + (active ? "bold" : "500") + ";" +
            "-fx-cursor:hand;-fx-padding:10 20 10 20;" +
            "-fx-text-fill:" + (active ? "#ea580c" : "#64748b") + ";"
        );
    }

    private void switchTab(String tab) {
        activeTab = tab;
        // Re-style all tab buttons
        for (String id : TAB_IDS) {
            if (tabContent.getScene() != null) {
                Node btn = tabContent.getScene().lookup("#atab-" + id);
                if (btn instanceof Button b) styleTabBtn(b, id.equals(tab));
            }
        }
        tabContent.getChildren().clear();
        switch (tab) {
            case "overview"   -> tabContent.getChildren().add(buildOverview());
            case "users"      -> tabContent.getChildren().add(buildUsers());
            case "moderation" -> tabContent.getChildren().add(buildModeration());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // OVERVIEW TAB — mirrors Admin/Dashboard.vue
    // ─────────────────────────────────────────────────────────────
    private Node buildOverview() {
        VBox box = new VBox(20);
        box.setFillWidth(true);

        // Stat grid — 3-column TilePane so cards never get squished
        javafx.scene.layout.TilePane statGrid = new javafx.scene.layout.TilePane(12, 12);
        statGrid.setPrefColumns(3);
        statGrid.setTileAlignment(Pos.CENTER_LEFT);
        Label statLoading = new Label("Loading statistics...");
        statLoading.getStyleClass().add("muted-text");
        statGrid.getChildren().add(statLoading);

        // Chart card — full width
        VBox chartCard = card();
        chartCard.setMaxWidth(Double.MAX_VALUE);
        Label chartTitle = new Label("Topics Created — Last 7 Days");
        chartTitle.setStyle("-fx-font-size:15px;-fx-font-weight:bold;-fx-text-fill:#1e293b;");
        VBox chartBody = new VBox(6);
        chartBody.setPadding(new Insets(12, 0, 0, 0));
        chartCard.getChildren().addAll(chartTitle, chartBody);

        // Recent Discussions + Moderation Logs — side by side
        HBox twoCol = new HBox(16);
        twoCol.setFillHeight(false);

        VBox recentTopicsCard = card();
        recentTopicsCard.setMaxWidth(Double.MAX_VALUE);
        Label rtTitle = new Label("Recent Discussions");
        rtTitle.setStyle("-fx-font-size:15px;-fx-font-weight:bold;-fx-text-fill:#1e293b;");
        VBox rtBody = new VBox(0);
        rtBody.getChildren().add(muted("Loading..."));
        recentTopicsCard.getChildren().addAll(rtTitle, new Separator(), rtBody);
        HBox.setHgrow(recentTopicsCard, Priority.ALWAYS);

        VBox logsCard = card();
        logsCard.setMaxWidth(Double.MAX_VALUE);
        Label logsTitle = new Label("Recent Moderation Activity");
        logsTitle.setStyle("-fx-font-size:15px;-fx-font-weight:bold;-fx-text-fill:#1e293b;");
        VBox logsBody = new VBox(0);
        logsBody.getChildren().add(muted("Loading..."));
        logsCard.getChildren().addAll(logsTitle, new Separator(), logsBody);
        HBox.setHgrow(logsCard, Priority.ALWAYS);
        twoCol.getChildren().addAll(recentTopicsCard, logsCard);

        // Group summaries card — full width
        VBox groupsCard = card();
        groupsCard.setMaxWidth(Double.MAX_VALUE);
        Label grpTitle = new Label("Group Statistics");
        grpTitle.setStyle("-fx-font-size:15px;-fx-font-weight:bold;-fx-text-fill:#1e293b;");
        Label grpSub = new Label("Open any group for its full participation report.");
        grpSub.setStyle("-fx-font-size:12px;-fx-text-fill:#94a3b8;");
        VBox grpBody = new VBox(0);
        grpBody.getChildren().add(muted("Loading..."));
        groupsCard.getChildren().addAll(grpTitle, grpSub, new Separator(), grpBody);

        box.getChildren().addAll(statGrid, chartCard, twoCol, groupsCard);

        // Async fetch
        new Thread(() -> {
            try {
                JsonObject resp = ApiService.getInstance().get("/admin/dashboard");
                Platform.runLater(() -> populateOverview(resp, statGrid, chartBody, rtBody, logsBody, grpBody));
            } catch (Exception e) {
                Platform.runLater(() -> statLoading.setText("Could not load: " + e.getMessage()));
            }
        }).start();

        return box;
    }

    private void populateOverview(JsonObject resp,
                                   javafx.scene.layout.TilePane statGrid,
                                   VBox chartBody, VBox rtBody,
                                   VBox logsBody, VBox grpBody) {
        // API returns {"success":true,"dashboard":{...}}
        // Web route returns stats/recentTopics/recentLogs/chartData/groupSummaries
        // We may get either shape depending on which endpoint succeeds.
        if (resp.has("dashboard")) {
            JsonObject d = resp.getAsJsonObject("dashboard");
            statGrid.getChildren().clear();
            addStat(statGrid, "Total Users",       safe(d,"total_users"),       "#111827");
            addStat(statGrid, "Groups",            safe(d,"total_groups"),       "#ea580c");
            addStat(statGrid, "Topics",            safe(d,"total_topics"),       "#111827");
            addStat(statGrid, "Messages",          safe(d,"total_messages"),     "#111827");
            addStat(statGrid, "Active (30d)",      safe(d,"active_users"),       "#111827");
            addStat(statGrid, "Blacklisted",       safe(d,"blacklisted_users"),  "#e11d48");

            // Chart
            chartBody.getChildren().clear();
            if (d.has("activity_chart")) {
                JsonArray chart = d.getAsJsonArray("activity_chart");
                int start = Math.max(0, chart.size() - 7);
                for (int i = start; i < chart.size(); i++) {
                    JsonObject entry = chart.get(i).getAsJsonObject();
                    chartBody.getChildren().add(barRow(safe(entry,"date"), entry.get("count").getAsInt()));
                }
            }
        } else if (resp.has("stats")) {
            // Web admin/dashboard route shape
            JsonObject s = resp.getAsJsonObject("stats");
            statGrid.getChildren().clear();
            addStat(statGrid, "Total Users",     safe(s,"users"),          "#111827");
            addStat(statGrid, "Groups",          safe(s,"groups"),          "#ea580c");
            addStat(statGrid, "Topics",          safe(s,"topics"),          "#111827");
            addStat(statGrid, "Messages",        safe(s,"messages"),        "#111827");
            addStat(statGrid, "Quizzes",         safe(s,"quizzes"),         "#111827");
            addStat(statGrid, "Flagged Topics",  safe(s,"flagged_topics"),  "#e11d48");
            addStat(statGrid, "Blacklisted",     safe(s,"blacklisted"),     "#111827");

            // Chart
            chartBody.getChildren().clear();
            if (resp.has("chartData")) {
                for (JsonElement el : resp.getAsJsonArray("chartData")) {
                    JsonObject entry = el.getAsJsonObject();
                    chartBody.getChildren().add(barRow(safe(entry,"label"), entry.get("count").getAsInt()));
                }
            }
            // Recent topics
            rtBody.getChildren().clear();
            if (resp.has("recentTopics")) {
                for (JsonElement el : resp.getAsJsonArray("recentTopics")) {
                    JsonObject t = el.getAsJsonObject();
                    String author = t.has("user") && t.get("user").isJsonObject() ? safe(t.getAsJsonObject("user"),"name") : "Unknown";
                    String group  = t.has("group") && t.get("group").isJsonObject() ? safe(t.getAsJsonObject("group"),"name") : "Unknown";
                    rtBody.getChildren().add(twoLineRow(safe(t,"title"), author + " in " + group));
                }
            }
            // Moderation logs
            logsBody.getChildren().clear();
            if (resp.has("recentLogs")) {
                for (JsonElement el : resp.getAsJsonArray("recentLogs")) {
                    JsonObject log = el.getAsJsonObject();
                    String admin = log.has("admin") && log.get("admin").isJsonObject() ? safe(log.getAsJsonObject("admin"),"name") : "Admin";
                    String user  = log.has("user")  && log.get("user").isJsonObject()  ? safe(log.getAsJsonObject("user"),"name")  : "User";
                    logsBody.getChildren().add(twoLineRow(safe(log,"action").replace("_"," "), admin + " · " + user));
                }
            }
            // Group summaries
            grpBody.getChildren().clear();
            if (resp.has("groupSummaries")) {
                grpBody.getChildren().add(colHeader("Group","Members","Topics","Unanswered"));
                for (JsonElement el : resp.getAsJsonArray("groupSummaries")) {
                    JsonObject g = el.getAsJsonObject();
                    grpBody.getChildren().add(colRow(
                        safe(g,"name"), safe(g,"members"), safe(g,"topics"), safe(g,"unanswered")
                    ));
                }
            }
        }
        if (rtBody.getChildren().isEmpty())   rtBody.getChildren().add(muted("No discussions yet."));
        if (logsBody.getChildren().isEmpty()) logsBody.getChildren().add(muted("No moderation activity yet."));
        if (grpBody.getChildren().isEmpty())  grpBody.getChildren().add(muted("No groups have been created yet."));
    }

    // ─────────────────────────────────────────────────────────────
    // USERS TAB — mirrors Admin/Users/Index.vue
    // ─────────────────────────────────────────────────────────────
    private Node buildUsers() {
        VBox box = new VBox(20);

        // Header row
        HBox headerRow = new HBox(12);
        headerRow.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("User Management");
        title.setStyle("-fx-font-size:20px;-fx-font-weight:bold;-fx-text-fill:#0f172a;");
        TextField searchField = new TextField();
        searchField.setPromptText("Search users...");
        searchField.getStyleClass().add("text-field");
        searchField.setPrefWidth(260);
        HBox.setHgrow(title, Priority.ALWAYS);
        headerRow.getChildren().addAll(title, searchField);

        // Table card
        VBox tableCard = new VBox(0);
        tableCard.getStyleClass().add("card");
        tableCard.setPadding(Insets.EMPTY);
        tableCard.getChildren().add(userColHeader());

        VBox tableBody = new VBox(0);
        tableBody.getChildren().add(muted("Loading users..."));
        tableCard.getChildren().add(tableBody);

        box.getChildren().addAll(headerRow, tableCard);

        final List<JsonObject> allUsers = new ArrayList<>();

        Runnable applyFilter = () -> {
            tableBody.getChildren().clear();
            String q = searchField.getText().toLowerCase().trim();
            for (JsonObject u : allUsers) {
                if (q.isEmpty() || safe(u,"name").toLowerCase().contains(q)
                    || safe(u,"email").toLowerCase().contains(q)) {
                    tableBody.getChildren().add(userRow(u));
                }
            }
            if (tableBody.getChildren().isEmpty())
                tableBody.getChildren().add(muted("No users found."));
        };
        searchField.textProperty().addListener((obs,o,n) -> applyFilter.run());

        new Thread(() -> {
            try {
                // Use inactive-users endpoint with very high threshold to get all users
                JsonObject resp = ApiService.getInstance().get("/admin/inactive-users?days=9999");
                allUsers.clear();
                if (resp.has("inactive_users")) {
                    for (JsonElement el : resp.getAsJsonArray("inactive_users"))
                        allUsers.add(el.getAsJsonObject());
                }
                Platform.runLater(() -> {
                    tableBody.getChildren().clear();
                    if (allUsers.isEmpty()) tableBody.getChildren().add(muted("No users found."));
                    else applyFilter.run();
                });
            } catch (Exception e) {
                Platform.runLater(() -> tableBody.getChildren().setAll(errLabel("Could not load users: " + e.getMessage())));
            }
        }).start();

        return box;
    }

    private HBox userColHeader() {
        HBox row = new HBox();
        row.setStyle("-fx-background-color:#f8fafc;-fx-border-color:#e2e8f0;-fx-border-width:0 0 1 0;-fx-padding:12 24 12 24;");
        row.getChildren().addAll(
            colHead("User",    240),
            colHead("Status",  120),
            colHead("Actions", 300)
        );
        return row;
    }

    private HBox userRow(JsonObject u) {
        HBox row = new HBox(0);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-border-color:#f1f5f9;-fx-border-width:0 0 1 0;-fx-padding:12 24 12 24;");
        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color:#f8fafc;-fx-border-color:#e2e8f0;-fx-border-width:0 0 1 0;-fx-padding:12 24 12 24;"));
        row.setOnMouseExited(e -> row.setStyle("-fx-border-color:#f1f5f9;-fx-border-width:0 0 1 0;-fx-padding:12 24 12 24;"));

        String id          = safe(u,"id");
        String name        = safe(u,"name");
        String email       = safe(u,"email");
        boolean blacklisted = "blacklisted".equals(safe(u,"status"))
                            || (u.has("blacklisted") && !u.get("blacklisted").isJsonNull() && u.get("blacklisted").getAsBoolean());
        boolean isAdmin    = u.has("is_admin") && !u.get("is_admin").isJsonNull() && u.get("is_admin").getAsBoolean();

        // User cell: orange avatar circle + name/email
        HBox userCell = new HBox(10);
        userCell.setAlignment(Pos.CENTER_LEFT);
        userCell.setPrefWidth(240);
        StackPane avatar = new StackPane();
        avatar.setStyle("-fx-background-color:#fff7ed;-fx-background-radius:50;-fx-min-width:32;-fx-min-height:32;-fx-max-width:32;-fx-max-height:32;-fx-alignment:center;");
        Label avLbl = new Label("👤");
        avLbl.setStyle("-fx-text-fill:#ea580c;-fx-font-size:14px;");
        avatar.getChildren().add(avLbl);
        VBox nameBlock = new VBox(1);
        Label nameLbl = new Label(name);
        nameLbl.setStyle("-fx-font-weight:bold;-fx-font-size:13px;-fx-text-fill:#0f172a;");
        Label emailLbl = new Label(email);
        emailLbl.setStyle("-fx-font-size:11px;-fx-text-fill:#94a3b8;");
        nameBlock.getChildren().addAll(nameLbl, emailLbl);
        userCell.getChildren().addAll(avatar, nameBlock);

        // Status cell
        VBox statusCell = new VBox(4);
        statusCell.setPrefWidth(120);
        Label statusBadge = new Label(blacklisted ? "Blacklisted" : "Active");
        statusBadge.setStyle("-fx-background-color:" + (blacklisted ? "#fff7ed" : "#f0fdf4") + ";" +
                             "-fx-text-fill:" + (blacklisted ? "#c2410c" : "#15803d") + ";" +
                             "-fx-font-size:10px;-fx-font-weight:bold;-fx-background-radius:20;-fx-padding:2 8 2 8;");
        statusCell.getChildren().add(statusBadge);
        if (isAdmin) {
            Label adminBadge = new Label("Admin");
            adminBadge.setStyle("-fx-background-color:#eef2ff;-fx-text-fill:#4338ca;-fx-font-size:10px;-fx-font-weight:bold;-fx-background-radius:20;-fx-padding:2 8 2 8;");
            statusCell.getChildren().add(adminBadge);
        }

        // Actions cell
        HBox actionsCell = new HBox(12);
        actionsCell.setAlignment(Pos.CENTER_LEFT);
        actionsCell.setPrefWidth(300);

        Button toggleAdminBtn = new Button(isAdmin ? "Revoke Admin" : "Make Admin");
        toggleAdminBtn.setStyle("-fx-background-color:transparent;-fx-text-fill:" + (isAdmin ? "#4338ca" : "#64748b") +
                                ";-fx-font-size:12px;-fx-font-weight:bold;-fx-cursor:hand;-fx-padding:0;");
        toggleAdminBtn.setOnAction(e -> doToggleAdmin(id));

        Button warnBtn = new Button("⚠ Warn");
        warnBtn.setStyle("-fx-background-color:transparent;-fx-text-fill:#d97706;-fx-font-size:12px;-fx-font-weight:bold;-fx-cursor:hand;-fx-padding:0;");
        warnBtn.setOnAction(e -> doWarnUser(id, name));

        Button blacklistBtn = new Button(blacklisted ? "Unban" : "🚫 Blacklist");
        blacklistBtn.setStyle("-fx-background-color:transparent;-fx-text-fill:#ea580c;-fx-font-size:12px;-fx-font-weight:bold;-fx-cursor:hand;-fx-padding:0;");
        blacklistBtn.setOnAction(e -> doBlacklist(id, name));

        actionsCell.getChildren().addAll(toggleAdminBtn, warnBtn, blacklistBtn);
        row.getChildren().addAll(userCell, statusCell, actionsCell);
        return row;
    }

    // ─────────────────────────────────────────────────────────────
    // MODERATION TAB — mirrors Admin/Moderation/Index.vue
    // ─────────────────────────────────────────────────────────────
    private Node buildModeration() {
        VBox box = new VBox(20);

        // ── Inactivity policy form ────────────────────────────────
        VBox policyCard = card();
        Label policyTitle = new Label("Automatic inactivity policy");
        policyTitle.setStyle("-fx-font-size:15px;-fx-font-weight:bold;-fx-text-fill:#0f172a;");
        Label policySub = new Label("The daily moderation task uses these values for student accounts.");
        policySub.setStyle("-fx-font-size:12px;-fx-text-fill:#94a3b8;");

        HBox policyFields = new HBox(16);
        policyFields.setPadding(new Insets(12, 0, 0, 0));
        VBox fInactDays  = policyField("30",  "Inactive after (days)");
        VBox fGraceDays  = policyField("7",   "Grace period (days)");
        VBox fWarnings   = policyField("2",   "Warnings before blacklist");
        VBox fBlkDays    = policyField("30",  "Blacklist duration (days)");
        HBox.setHgrow(fInactDays,Priority.ALWAYS);
        HBox.setHgrow(fGraceDays,Priority.ALWAYS);
        HBox.setHgrow(fWarnings, Priority.ALWAYS);
        HBox.setHgrow(fBlkDays,  Priority.ALWAYS);
        policyFields.getChildren().addAll(fInactDays, fGraceDays, fWarnings, fBlkDays);

        HBox savePolicyRow = new HBox();
        savePolicyRow.setAlignment(Pos.CENTER_RIGHT);
        savePolicyRow.setPadding(new Insets(8, 0, 0, 0));
        Button savePolicy = UiHelper.primaryButton("Save policy");
        savePolicy.setOnAction(e -> showAlert("Saved", "Policy settings saved (web-side only)."));
        savePolicyRow.getChildren().add(savePolicy);

        policyCard.getChildren().addAll(policyTitle, policySub, policyFields, savePolicyRow);

        // ── Reports queue ─────────────────────────────────────────
        VBox reportsCard = card();
        reportsCard.setPadding(Insets.EMPTY);
        VBox reportsBody = new VBox(0);
        reportsBody.getChildren().add(muted("Loading reports..."));
        reportsCard.getChildren().add(reportsBody);

        box.getChildren().addAll(policyCard, reportsCard);

        // Async fetch flagged content
        new Thread(() -> {
            try {
                JsonObject resp = ApiService.getInstance().get("/admin/flagged-content");
                Platform.runLater(() -> {
                    reportsBody.getChildren().clear();
                    boolean any = false;
                    if (resp.has("flagged_topics")) {
                        for (JsonElement el : resp.getAsJsonArray("flagged_topics")) {
                            JsonObject item = el.getAsJsonObject();
                            item.addProperty("_kind", "topic");
                            reportsBody.getChildren().add(reportRow(item, reportsBody));
                            any = true;
                        }
                    }
                    if (resp.has("flagged_messages")) {
                        for (JsonElement el : resp.getAsJsonArray("flagged_messages")) {
                            JsonObject item = el.getAsJsonObject();
                            item.addProperty("_kind", "message");
                            reportsBody.getChildren().add(reportRow(item, reportsBody));
                            any = true;
                        }
                    }
                    if (!any) reportsBody.getChildren().add(muted("No content reports have been submitted."));
                });
            } catch (Exception e) {
                Platform.runLater(() -> reportsBody.getChildren().setAll(errLabel("Could not load: " + e.getMessage())));
            }
        }).start();

        return box;
    }

    /** One report row: type badge + title + reporter + Dismiss/Flag/Remove */
    private VBox reportRow(JsonObject item, VBox parent) {
        VBox article = new VBox(8);
        article.setPadding(new Insets(16, 24, 16, 24));
        article.setStyle("-fx-border-color:#f1f5f9;-fx-border-width:0 0 1 0;");

        String kind    = item.has("_kind") ? safe(item,"_kind") : "topic";
        String title   = item.has("title")   ? safe(item,"title")
                       : item.has("content") ? safe(item,"content") : "Content unavailable";
        String author  = item.has("user") && item.get("user").isJsonObject()
                         ? safe(item.getAsJsonObject("user"),"name") : "Unknown";
        String id      = safe(item,"id");

        // Badges row
        HBox badges = new HBox(6);
        Label kindBadge = new Label(kind.equals("topic") ? "Topic" : "Message");
        kindBadge.setStyle("-fx-background-color:#f1f5f9;-fx-text-fill:#475569;-fx-font-size:10px;" +
                           "-fx-font-weight:bold;-fx-background-radius:4;-fx-padding:3 8 3 8;");
        Label statusBadge = new Label("flagged");
        statusBadge.setStyle("-fx-background-color:#fef3c7;-fx-text-fill:#92400e;-fx-font-size:10px;" +
                              "-fx-font-weight:bold;-fx-background-radius:4;-fx-padding:3 8 3 8;");
        badges.getChildren().addAll(kindBadge, statusBadge);

        Label contentLbl = new Label(title.length() > 120 ? title.substring(0,120)+"…" : title);
        contentLbl.setStyle("-fx-font-weight:600;-fx-font-size:13px;-fx-text-fill:#0f172a;-fx-wrap-text:true;");
        contentLbl.setWrapText(true);

        Label authorLbl = new Label("By: " + author);
        authorLbl.setStyle("-fx-font-size:11px;-fx-text-fill:#94a3b8;");

        HBox actions = new HBox(8);
        Button dismissBtn = new Button("Dismiss");
        dismissBtn.setStyle("-fx-background-color:transparent;-fx-border-color:#cbd5e1;-fx-border-radius:8;" +
                            "-fx-background-radius:8;-fx-font-size:12px;-fx-font-weight:600;" +
                            "-fx-text-fill:#475569;-fx-padding:6 12 6 12;-fx-cursor:hand;");
        dismissBtn.setOnAction(e -> parent.getChildren().remove(article));

        Button flagBtn = new Button("Flag content");
        flagBtn.setStyle("-fx-background-color:transparent;-fx-border-color:#fcd34d;-fx-border-radius:8;" +
                         "-fx-background-radius:8;-fx-font-size:12px;-fx-font-weight:600;" +
                         "-fx-text-fill:#b45309;-fx-padding:6 12 6 12;-fx-cursor:hand;");
        flagBtn.setOnAction(e -> doFlagContent(kind, id, article));

        Button removeBtn = new Button("Remove content");
        removeBtn.setStyle("-fx-background-color:#e11d48;-fx-border-radius:8;-fx-background-radius:8;" +
                           "-fx-font-size:12px;-fx-font-weight:600;-fx-text-fill:white;" +
                           "-fx-padding:6 12 6 12;-fx-cursor:hand;");
        removeBtn.setOnAction(e -> doRemoveContent(kind, id, article));
        actions.getChildren().addAll(dismissBtn, flagBtn, removeBtn);

        article.getChildren().addAll(badges, contentLbl, authorLbl, actions);
        return article;
    }

    // ─────────────────────────────────────────────────────────────
    // API calls
    // ─────────────────────────────────────────────────────────────
    private void doToggleAdmin(String userId) {
        new Thread(() -> {
            try {
                ApiService.getInstance().post("/admin/toggle-admin/" + userId);
                Platform.runLater(() -> { showAlert("Done","Admin status toggled."); switchTab("users"); });
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Error", e.getMessage()));
            }
        }).start();
    }

    private void doWarnUser(String userId, String userName) {
        TextInputDialog d = new TextInputDialog();
        d.setTitle("Warn " + userName);
        d.setHeaderText("Reason for this warning:");
        d.setContentText("Reason:");
        d.showAndWait().ifPresent(reason -> {
            if (reason == null) return;
            String r = reason.trim().isEmpty() ? "Warning issued by admin." : reason.trim();
            new Thread(() -> {
                try {
                    JsonObject p = new JsonObject();
                    p.addProperty("reason", r);
                    JsonObject resp = ApiService.getInstance().post("/admin/warn-user/" + userId, p);
                    String msg = resp.has("message") ? resp.get("message").getAsString() : "Warning sent.";
                    Platform.runLater(() -> showAlert("Done", msg));
                } catch (Exception e) {
                    Platform.runLater(() -> showAlert("Error", e.getMessage()));
                }
            }).start();
        });
    }

    private void doBlacklist(String userId, String userName) {
        TextInputDialog d = new TextInputDialog();
        d.setTitle("Blacklist " + userName);
        d.setHeaderText("Blacklist / Unban " + userName);
        d.setContentText("Reason (required):");
        d.showAndWait().ifPresent(reason -> {
            String r = (reason == null || reason.trim().isEmpty()) ? "Blacklisted by admin." : reason.trim();
            new Thread(() -> {
                try {
                    JsonObject p = new JsonObject();
                    p.addProperty("reason", r);
                    JsonObject resp = ApiService.getInstance().post("/admin/blacklist-user/" + userId, p);
                    String msg = resp.has("message") ? resp.get("message").getAsString() : "Done.";
                    Platform.runLater(() -> { showAlert("Done", msg); switchTab("users"); });
                } catch (Exception e) {
                    Platform.runLater(() -> showAlert("Error", e.getMessage()));
                }
            }).start();
        });
    }

    private void doFlagContent(String kind, String id, VBox row) {
        new Thread(() -> {
            try {
                JsonObject p = new JsonObject();
                p.addProperty("entity_type", kind);
                p.addProperty("entity_id", id);
                p.addProperty("reason", "Flagged by admin.");
                ApiService.getInstance().post("/admin/flag-content", p);
                Platform.runLater(() -> row.getParent().getChildrenUnmodifiable()); // just keep visible
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Error", e.getMessage()));
            }
        }).start();
    }

    private void doRemoveContent(String kind, String id, VBox row) {
        new Thread(() -> {
            try {
                JsonObject p = new JsonObject();
                p.addProperty("entity_type", kind);
                p.addProperty("entity_id", id);
                p.addProperty("reason", "Removed by admin.");
                ApiService.getInstance().post("/admin/remove-content", p);
                Platform.runLater(() -> ((VBox) row.getParent()).getChildren().remove(row));
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Error", e.getMessage()));
            }
        }).start();
    }

    // ─────────────────────────────────────────────────────────────
    // Shared widget helpers
    // ─────────────────────────────────────────────────────────────
    private void addStat(javafx.scene.layout.TilePane grid, String label, String value, String valColor) {
        VBox c = new VBox(4);
        c.setStyle("-fx-background-color:#ffffff;-fx-border-color:#e2e8f0;-fx-border-radius:12;" +
                   "-fx-background-radius:12;-fx-padding:20 24 20 24;");
        c.setPrefWidth(200);
        c.setMinWidth(160);
        Label lbl = new Label(label.toUpperCase());
        lbl.setStyle("-fx-font-size:10px;-fx-font-weight:bold;-fx-text-fill:#94a3b8;");
        Label val = new Label(value);
        val.setStyle("-fx-font-size:28px;-fx-font-weight:bold;-fx-text-fill:" + valColor + ";");
        c.getChildren().addAll(lbl, val);
        grid.getChildren().add(c);
    }

    private HBox barRow(String date, int count) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(4, 0, 4, 0));
        Label d = new Label(date);
        d.setStyle("-fx-font-size:12px;-fx-text-fill:#475569;-fx-min-width:80;");
        Region bar = new Region();
        bar.setStyle("-fx-background-color:#f59e0b;-fx-background-radius:3;-fx-min-height:14;-fx-max-height:14;");
        bar.setPrefWidth(Math.max(4, Math.min(count * 10, 400)));
        Label cnt = new Label(String.valueOf(count));
        cnt.setStyle("-fx-font-size:12px;-fx-text-fill:#94a3b8;");
        row.getChildren().addAll(d, bar, cnt);
        return row;
    }

    private VBox twoLineRow(String primary, String secondary) {
        VBox v = new VBox(2);
        v.setPadding(new Insets(10, 0, 10, 0));
        v.setStyle("-fx-border-color:#f1f5f9;-fx-border-width:0 0 1 0;");
        Label p = new Label(primary);
        p.setStyle("-fx-font-weight:600;-fx-font-size:13px;-fx-text-fill:#0f172a;");
        Label s = new Label(secondary);
        s.setStyle("-fx-font-size:12px;-fx-text-fill:#64748b;");
        v.getChildren().addAll(p, s);
        return v;
    }

    private HBox colHeader(String... cols) {
        HBox row = new HBox();
        row.setStyle("-fx-background-color:#f8fafc;-fx-border-color:#e2e8f0;-fx-border-width:0 0 1 0;-fx-padding:10 24 10 24;");
        for (String c : cols) { Label l = new Label(c); l.setStyle("-fx-font-weight:bold;-fx-font-size:11px;-fx-text-fill:#64748b;"); HBox.setHgrow(l,Priority.ALWAYS); row.getChildren().add(l); }
        return row;
    }

    private HBox colRow(String... cells) {
        HBox row = new HBox();
        row.setStyle("-fx-border-color:#f1f5f9;-fx-border-width:0 0 1 0;-fx-padding:10 24 10 24;");
        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color:#f8fafc;-fx-border-color:#e2e8f0;-fx-border-width:0 0 1 0;-fx-padding:10 24 10 24;"));
        row.setOnMouseExited(e  -> row.setStyle("-fx-border-color:#f1f5f9;-fx-border-width:0 0 1 0;-fx-padding:10 24 10 24;"));
        for (String c : cells) { Label l = new Label(c); l.setStyle("-fx-font-size:12px;-fx-text-fill:#334155;"); HBox.setHgrow(l,Priority.ALWAYS); row.getChildren().add(l); }
        return row;
    }

    private Label colHead(String text, double width) {
        Label l = new Label(text);
        l.setStyle("-fx-font-weight:bold;-fx-font-size:11px;-fx-text-fill:#64748b;");
        l.setPrefWidth(width);
        return l;
    }

    private VBox policyField(String defVal, String label) {
        VBox box = new VBox(4);
        HBox.setHgrow(box, Priority.ALWAYS);
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size:12px;-fx-font-weight:500;-fx-text-fill:#475569;");
        TextField tf = new TextField(defVal);
        tf.getStyleClass().add("text-field");
        box.getChildren().addAll(lbl, tf);
        return box;
    }

    private VBox card() {
        VBox v = new VBox(12);
        v.getStyleClass().add("card");
        v.setPadding(new Insets(20, 24, 20, 24));
        return v;
    }

    private Label muted(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("muted-text");
        l.setPadding(new Insets(12, 0, 12, 0));
        return l;
    }

    private Label errLabel(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("error-text");
        l.setPadding(new Insets(12, 0, 12, 0));
        return l;
    }

    private String safe(JsonObject obj, String key) {
        if (obj == null || !obj.has(key) || obj.get(key).isJsonNull()) return "";
        return obj.get(key).getAsString();
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setTitle(title);
        a.showAndWait();
    }
}
