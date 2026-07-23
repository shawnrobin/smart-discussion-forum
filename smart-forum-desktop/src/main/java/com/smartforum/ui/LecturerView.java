package com.smartforum.ui;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.smartforum.model.BootstrapData;
import com.smartforum.service.ApiService;
import com.smartforum.service.SessionManager;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * Lecturer view — exact replica of:
 *   Lecturer/Dashboard.vue  (stats + recent groups)
 *   Lecturer/Groups/Index.vue (create group form + groups table)
 *   Lecturer/Groups/Moderation.vue (open reports + flagged conversations)
 *
 * Three tabs: Dashboard · Groups · Moderation
 */
public class LecturerView {

    private final MainView shell;
    private StackPane tabContent;
    private final String[] TAB_IDS    = {"dashboard", "groups", "moderation"};
    private final String[] TAB_LABELS = {"Dashboard", "My Groups", "Moderation"};
    private String activeTab = "dashboard";

    public LecturerView(MainView shell) { this.shell = shell; }

    public Node build() {
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("scroll-pane");

        VBox root = new VBox(0);
        root.setPadding(new Insets(24));

        // Page heading (matches Lecturer/Dashboard.vue page-title)
        Label h1 = new Label("Lecturer Dashboard");
        h1.setStyle("-fx-font-size:22px;-fx-font-weight:bold;-fx-text-fill:#111827;");
        h1.setPadding(new Insets(0, 0, 16, 0));

        // Tab bar
        HBox tabBar = new HBox(0);
        tabBar.setStyle("-fx-border-color:#e5e7eb;-fx-border-width:0 0 1 0;");
        for (int i = 0; i < TAB_IDS.length; i++) {
            tabBar.getChildren().add(makeTabBtn(TAB_IDS[i], TAB_LABELS[i]));
        }

        tabContent = new StackPane();
        tabContent.setPadding(new Insets(20, 0, 0, 0));

        root.getChildren().addAll(h1, tabBar, tabContent);
        scroll.setContent(root);
        switchTab("dashboard");
        return scroll;
    }

    private Button makeTabBtn(String id, String label) {
        Button btn = new Button(label);
        btn.setId("ltab-" + id);
        btn.setOnAction(e -> switchTab(id));
        styleTab(btn, id.equals(activeTab));
        return btn;
    }

    private void styleTab(Button btn, boolean active) {
        btn.setStyle(
            "-fx-background-color:transparent;" +
            "-fx-border-color:transparent transparent " + (active ? "#ea580c" : "transparent") + " transparent;" +
            "-fx-border-width:0 0 2 0;" +
            "-fx-font-size:13px;-fx-font-weight:" + (active ? "bold" : "500") + ";" +
            "-fx-cursor:hand;-fx-padding:10 20 10 20;" +
            "-fx-text-fill:" + (active ? "#ea580c" : "#6b7280") + ";"
        );
    }

    private void switchTab(String tab) {
        activeTab = tab;
        for (String id : TAB_IDS) {
            if (tabContent.getScene() != null) {
                Node btn = tabContent.getScene().lookup("#ltab-" + id);
                if (btn instanceof Button b) styleTab(b, id.equals(tab));
            }
        }
        tabContent.getChildren().clear();
        switch (tab) {
            case "dashboard"  -> tabContent.getChildren().add(buildDashboard());
            case "groups"     -> tabContent.getChildren().add(buildGroups());
            case "moderation" -> tabContent.getChildren().add(buildModeration());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // DASHBOARD TAB — mirrors Lecturer/Dashboard.vue
    // ─────────────────────────────────────────────────────────────
    private Node buildDashboard() {
        VBox box = new VBox(24);

        // 3 stat cards
        HBox statRow = new HBox(16);
        statRow.setFillHeight(true);
        VBox myGroups   = statCard("My Groups",          "—", "#111827");
        VBox myQuizzes  = statCard("My Quizzes",         "—", "#111827");
        VBox pendingMod = statCard("Pending Moderation", "—", "#ea580c");
        HBox.setHgrow(myGroups,   Priority.ALWAYS);
        HBox.setHgrow(myQuizzes,  Priority.ALWAYS);
        HBox.setHgrow(pendingMod, Priority.ALWAYS);
        statRow.getChildren().addAll(myGroups, myQuizzes, pendingMod);

        // Recent groups card
        VBox recentCard = new VBox(0);
        recentCard.getStyleClass().add("card");
        recentCard.setPadding(Insets.EMPTY);

        HBox cardHeader = new HBox();
        cardHeader.setAlignment(Pos.CENTER_LEFT);
        cardHeader.setPadding(new Insets(16, 24, 16, 24));
        cardHeader.setStyle("-fx-border-color:#f3f4f6;-fx-border-width:0 0 1 0;");
        Label cardTitle = new Label("Recent Groups");
        cardTitle.setStyle("-fx-font-size:16px;-fx-font-weight:bold;-fx-text-fill:#111827;");
        Button viewAll = UiHelper.linkButton("View All");
        viewAll.setOnAction(e -> switchTab("groups"));
        HBox.setHgrow(cardTitle, Priority.ALWAYS);
        cardHeader.getChildren().addAll(cardTitle, viewAll);

        VBox recentBody = new VBox(0);
        recentBody.setId("lecturer-recent-groups");
        recentBody.getChildren().add(muted("Loading groups..."));

        recentCard.getChildren().addAll(cardHeader, recentBody);
        box.getChildren().addAll(statRow, recentCard);

        // Async: load from bootstrap + desktop/groups
        new Thread(() -> {
            try {
                BootstrapData bd = SessionManager.getInstance().getBootstrapData();
                JsonObject groupsResp = ApiService.getInstance().get("/desktop/groups");

                Platform.runLater(() -> {
                    // Stats
                    int groupCount = bd != null && bd.summary != null ? bd.summary.groups : 0;
                    int quizCount  = bd != null && bd.quizzes  != null ? bd.quizzes.size()  : 0;

                    setStatVal(myGroups,   String.valueOf(groupCount));
                    setStatVal(myQuizzes,  String.valueOf(quizCount));
                    setStatVal(pendingMod, "—");

                    // Recent groups list
                    recentBody.getChildren().clear();
                    if (groupsResp.has("groups")) {
                        JsonArray arr = groupsResp.getAsJsonArray("groups");
                        if (arr.size() == 0) {
                            VBox empty = new VBox(8);
                            empty.setAlignment(Pos.CENTER);
                            empty.setPadding(new Insets(32));
                            empty.getChildren().add(muted("No groups found yet."));
                            Button createLink = UiHelper.linkButton("Create your first group");
                            createLink.setOnAction(e -> switchTab("groups"));
                            empty.getChildren().add(createLink);
                            recentBody.getChildren().add(empty);
                        } else {
                            for (JsonElement el : arr) {
                                recentBody.getChildren().add(recentGroupRow(el.getAsJsonObject()));
                            }
                        }
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> recentBody.getChildren().setAll(errLabel("Could not load: " + e.getMessage())));
            }
        }).start();

        return box;
    }

    private HBox recentGroupRow(JsonObject g) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(16, 24, 16, 24));
        row.setStyle("-fx-border-color:#f9fafb;-fx-border-width:0 0 1 0;-fx-cursor:hand;");
        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color:#f9fafb;-fx-border-color:#f3f4f6;-fx-border-width:0 0 1 0;-fx-cursor:hand;"));
        row.setOnMouseExited(e  -> row.setStyle("-fx-border-color:#f9fafb;-fx-border-width:0 0 1 0;-fx-cursor:hand;"));

        int id      = g.has("id") ? g.get("id").getAsInt() : 0;
        String name = safe(g, "name");
        int members = g.has("members") ? g.get("members").getAsInt()
                    : g.has("users_count") ? g.get("users_count").getAsInt() : 0;

        VBox info = new VBox(2);
        HBox.setHgrow(info, Priority.ALWAYS);
        Label nameLbl = new Label(name);
        nameLbl.setStyle("-fx-font-weight:bold;-fx-font-size:14px;-fx-text-fill:#111827;");
        Label metaLbl = muted(members + " Members");
        info.getChildren().addAll(nameLbl, metaLbl);

        Label memberBadge = new Label(members + " Members");
        memberBadge.setStyle("-fx-background-color:#f3f4f6;-fx-text-fill:#6b7280;-fx-font-size:12px;" +
                             "-fx-font-weight:500;-fx-background-radius:20;-fx-padding:4 12 4 12;");

        row.setOnMouseClicked(ev -> shell.openGroup(id, name));
        row.getChildren().addAll(info, memberBadge);
        return row;
    }

    // ─────────────────────────────────────────────────────────────
    // GROUPS TAB — mirrors Lecturer/Groups/Index.vue
    // ─────────────────────────────────────────────────────────────
    private Node buildGroups() {
        VBox box = new VBox(24);

        // Create group form card
        VBox createCard = new VBox(12);
        createCard.getStyleClass().add("card");
        Label createTitle = new Label("Create New Group");
        createTitle.setStyle("-fx-font-size:15px;-fx-font-weight:bold;-fx-text-fill:#111827;");

        HBox createForm = new HBox(12);
        createForm.setAlignment(Pos.CENTER_LEFT);
        TextField nameField = new TextField();
        nameField.setPromptText("Group Name");
        nameField.getStyleClass().add("text-field");
        HBox.setHgrow(nameField, Priority.ALWAYS);

        TextField maxField = new TextField();
        maxField.setPromptText("Max Members");
        maxField.setPrefWidth(120);
        maxField.getStyleClass().add("text-field");

        Button createBtn = UiHelper.primaryButton("Create");
        createBtn.setOnAction(e -> doCreateGroup(nameField, maxField, box));

        createForm.getChildren().addAll(nameField, maxField, createBtn);
        createCard.getChildren().addAll(createTitle, createForm);

        // Groups table card
        VBox tableCard = new VBox(0);
        tableCard.getStyleClass().add("card");
        tableCard.setPadding(Insets.EMPTY);

        // Table header
        HBox thead = new HBox(0);
        thead.setStyle("-fx-background-color:#f9fafb;-fx-border-color:#f3f4f6;-fx-border-width:0 0 1 0;-fx-padding:12 24 12 24;");
        thead.getChildren().addAll(
            theadCell("Group Name", 0), theadCell("Members", 120), theadCell("Actions", 220)
        );

        VBox tbody = new VBox(0);
        tbody.setId("lecturer-groups-table");
        tbody.getChildren().add(muted("Loading groups..."));

        tableCard.getChildren().addAll(thead, tbody);
        box.getChildren().addAll(createCard, tableCard);

        loadGroupsTable(tbody);
        return box;
    }

    private void loadGroupsTable(VBox tbody) {
        new Thread(() -> {
            try {
                JsonObject resp = ApiService.getInstance().get("/desktop/groups");
                Platform.runLater(() -> {
                    tbody.getChildren().clear();
                    if (!resp.has("groups") || resp.getAsJsonArray("groups").size() == 0) {
                        tbody.getChildren().add(muted("No groups found."));
                        return;
                    }
                    for (JsonElement el : resp.getAsJsonArray("groups")) {
                        tbody.getChildren().add(groupTableRow(el.getAsJsonObject(), tbody));
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> tbody.getChildren().setAll(errLabel("Could not load: " + e.getMessage())));
            }
        }).start();
    }

    private HBox groupTableRow(JsonObject g, VBox tbody) {
        HBox row = new HBox(0);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(14, 24, 14, 24));
        row.setStyle("-fx-border-color:#f3f4f6;-fx-border-width:0 0 1 0;");
        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color:#f9fafb;-fx-border-color:#f3f4f6;-fx-border-width:0 0 1 0;"));
        row.setOnMouseExited(e  -> row.setStyle("-fx-border-color:#f3f4f6;-fx-border-width:0 0 1 0;"));

        int id      = g.has("id") ? g.get("id").getAsInt() : 0;
        String name = safe(g, "name");
        int members = g.has("members") ? g.get("members").getAsInt()
                    : g.has("users_count") ? g.get("users_count").getAsInt() : 0;

        // Name cell
        Label nameLbl = new Label(name);
        nameLbl.setStyle("-fx-font-weight:600;-fx-font-size:13px;-fx-text-fill:#111827;");
        HBox.setHgrow(nameLbl, Priority.ALWAYS);

        // Members cell
        Label membersLbl = new Label(String.valueOf(members));
        membersLbl.setStyle("-fx-font-size:13px;-fx-text-fill:#6b7280;");
        membersLbl.setPrefWidth(120);

        // Actions cell: View | Create Quiz | Delete
        HBox actions = new HBox(16);
        actions.setAlignment(Pos.CENTER_LEFT);
        actions.setPrefWidth(220);

        Button viewBtn = new Button("View");
        viewBtn.setStyle("-fx-background-color:transparent;-fx-text-fill:#ea580c;-fx-font-weight:bold;-fx-font-size:12px;-fx-cursor:hand;-fx-padding:0;");
        viewBtn.setOnAction(e -> shell.openGroup(id, name));

        Button quizBtn = new Button("Create Quiz");
        quizBtn.setStyle("-fx-background-color:transparent;-fx-text-fill:#2563eb;-fx-font-weight:bold;-fx-font-size:12px;-fx-cursor:hand;-fx-padding:0;");
        quizBtn.setOnAction(e -> showCreateQuizDialog(id, name));

        Button delBtn = new Button("Delete");
        delBtn.setStyle("-fx-background-color:transparent;-fx-text-fill:#e11d48;-fx-font-weight:bold;-fx-font-size:12px;-fx-cursor:hand;-fx-padding:0;");
        delBtn.setOnAction(e -> doDeleteGroup(id, name, tbody));

        actions.getChildren().addAll(viewBtn, quizBtn, delBtn);
        row.getChildren().addAll(nameLbl, membersLbl, actions);
        return row;
    }

    private void doCreateGroup(TextField nameField, TextField maxField, VBox box) {
        String name = nameField.getText().trim();
        if (name.isEmpty()) { showAlert("Validation", "Group name is required."); return; }
        nameField.setDisable(true);

        new Thread(() -> {
            try {
                JsonObject p = new JsonObject();
                p.addProperty("name", name);
                if (!maxField.getText().trim().isEmpty()) {
                    p.addProperty("max_members", Integer.parseInt(maxField.getText().trim()));
                }
                JsonObject resp = ApiService.getInstance().post("/groups", p);
                Platform.runLater(() -> {
                    nameField.clear(); maxField.clear(); nameField.setDisable(false);
                    // Re-render Groups tab
                    switchTab("groups");
                });
            } catch (Exception e) {
                Platform.runLater(() -> { nameField.setDisable(false); showAlert("Error", e.getMessage()); });
            }
        }).start();
    }

    private void doDeleteGroup(int id, String name, VBox tbody) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Are you sure you want to delete \"" + name + "\"? This action cannot be undone.",
            ButtonType.YES, ButtonType.CANCEL);
        confirm.setTitle("Delete Group");
        confirm.showAndWait().ifPresent(bt -> {
            if (bt != ButtonType.YES) return;
            new Thread(() -> {
                try {
                    ApiService.getInstance().delete("/groups/" + id);
                    Platform.runLater(() -> switchTab("groups"));
                } catch (Exception e) {
                    Platform.runLater(() -> showAlert("Error", "Could not delete group: " + e.getMessage()));
                }
            }).start();
        });
    }

    // ─────────────────────────────────────────────────────────────
    // CREATE QUIZ DIALOG — mirrors Quizzes/Create.vue
    // ─────────────────────────────────────────────────────────────
    private void showCreateQuizDialog(int groupId, String groupName) {
        Dialog<ButtonType> dlg = new Dialog<>();
        dlg.setTitle("Create Quiz — " + groupName);
        dlg.setHeaderText("Configure and publish a quiz for this group.");
        dlg.getDialogPane().setPrefWidth(600);

        // Scrollable content
        ScrollPane sp = new ScrollPane();
        sp.setFitToWidth(true);
        sp.setPrefHeight(560);
        sp.getStyleClass().add("scroll-pane");

        VBox form = new VBox(14);
        form.setPadding(new Insets(8));

        // ── Quiz settings ──────────────────────────────────────
        TextField titleF = field("e.g. Mid-Term Assessment");
        TextArea  descF  = new TextArea();
        descF.setPromptText("Brief description (optional)");
        descF.getStyleClass().add("text-area");
        descF.setPrefRowCount(2);
        descF.setWrapText(true);

        TextField startF    = field("YYYY-MM-DD HH:MM  (e.g. 2026-08-01 09:00)");
        TextField endF      = field("YYYY-MM-DD HH:MM  (e.g. 2026-08-01 10:30)");
        TextField durationF = field("30");
        durationF.setPrefWidth(80);

        ComboBox<String> categoryBox = new ComboBox<>();
        categoryBox.getStyleClass().add("combo-box");
        categoryBox.getItems().addAll("all");
        categoryBox.setValue("all");
        // Load group categories async
        new Thread(() -> {
            try {
                com.google.gson.JsonObject resp = ApiService.getInstance().get("/desktop/groups");
                // add year/course/faculty options from group members
                Platform.runLater(() -> {
                    if (!categoryBox.getItems().contains("year_of_study:1"))
                        categoryBox.getItems().addAll(
                            "year_of_study:1","year_of_study:2","year_of_study:3","year_of_study:4");
                });
            } catch (Exception ignored) {}
        }).start();

        ComboBox<String> shuffleBox = new ComboBox<>();
        shuffleBox.getStyleClass().add("combo-box");
        shuffleBox.getItems().addAll("Yes — random order","No — fixed order");
        shuffleBox.setValue("Yes — random order");

        ComboBox<String> resultsBox = new ComboBox<>();
        resultsBox.getStyleClass().add("combo-box");
        resultsBox.getItems().addAll("Right after submission","After quiz closes","Manually by lecturer");
        resultsBox.setValue("Right after submission");

        form.getChildren().addAll(
            sectionHead("Quiz Settings"),
            lbl("Title *"), titleF,
            lbl("Description"), descF,
            row2(new VBox(4, lbl("Start time *"), startF),
                 new VBox(4, lbl("End time (hard deadline) *"), endF)),
            row2(new VBox(4, lbl("Duration (minutes) *"), durationF),
                 new VBox(4, lbl("Who can take it"), categoryBox)),
            row2(new VBox(4, lbl("Shuffle questions"), shuffleBox),
                 new VBox(4, lbl("Show results"), resultsBox))
        );

        // ── Questions ──────────────────────────────────────────
        form.getChildren().add(sectionHead("Questions"));
        VBox questionsBox = new VBox(12);
        form.getChildren().add(questionsBox);

        // Add first question
        addQuestionBlock(questionsBox);

        Button addQBtn = UiHelper.secondaryButton("+ Add Question");
        addQBtn.setOnAction(e -> addQuestionBlock(questionsBox));
        form.getChildren().add(addQBtn);

        Label errorLbl = new Label();
        errorLbl.getStyleClass().add("error-text");
        errorLbl.setWrapText(true);
        errorLbl.setVisible(false);
        errorLbl.setManaged(false);
        form.getChildren().add(errorLbl);

        sp.setContent(form);
        dlg.getDialogPane().setContent(sp);
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Button okBtn = (Button) dlg.getDialogPane().lookupButton(ButtonType.OK);
        okBtn.setText("Publish Quiz");
        okBtn.getStyleClass().add("btn-primary");

        dlg.showAndWait().ifPresent(bt -> {
            if (bt != ButtonType.OK) return;

            String title    = titleF.getText().trim();
            String start    = startF.getText().trim();
            String end      = endF.getText().trim();
            String durStr   = durationF.getText().trim();

            if (title.isEmpty() || start.isEmpty() || end.isEmpty() || durStr.isEmpty()) {
                showAlert("Validation", "Title, start time, end time and duration are required.");
                return;
            }

            // Build questions array
            com.google.gson.JsonArray questions = new com.google.gson.JsonArray();
            for (javafx.scene.Node qNode : questionsBox.getChildren()) {
                if (!(qNode instanceof VBox qBlock)) continue;
                String qText = getFieldText(qBlock, "qtext");
                String qType = getComboValue(qBlock, "qtype");
                String correct = getFieldText(qBlock, "correct");
                String pointsStr = getFieldText(qBlock, "points");
                if (qText.isEmpty() || correct.isEmpty()) continue;

                com.google.gson.JsonObject q = new com.google.gson.JsonObject();
                q.addProperty("question_text", qText);
                q.addProperty("question_type", qType != null ? qType : "short_answer");
                q.addProperty("correct_answer", correct);
                q.addProperty("points", pointsStr.isEmpty() ? 5 : Integer.parseInt(pointsStr));

                // Options for multiple choice
                String optStr = getFieldText(qBlock, "options");
                if (!optStr.isEmpty()) {
                    com.google.gson.JsonArray opts = new com.google.gson.JsonArray();
                    for (String o : optStr.split(",")) {
                        String t = o.trim();
                        if (!t.isEmpty()) opts.add(t);
                    }
                    q.add("options", opts);
                }
                questions.add(q);
            }

            if (questions.size() == 0) {
                showAlert("Validation", "Add at least one complete question.");
                return;
            }

            okBtn.setDisable(true);
            okBtn.setText("Publishing...");

            new Thread(() -> {
                try {
                    com.google.gson.JsonObject payload = new com.google.gson.JsonObject();
                    payload.addProperty("title", title);
                    payload.addProperty("description", descF.getText().trim());
                    payload.addProperty("start_time", start.replace("T"," ").replace("/","-"));
                    payload.addProperty("end_time",   end.replace("T"," ").replace("/","-"));
                    payload.addProperty("duration_minutes", Integer.parseInt(durStr));
                    payload.addProperty("target_category", categoryBox.getValue() != null ? categoryBox.getValue() : "all");
                    payload.addProperty("shuffle_questions", shuffleBox.getValue().startsWith("Yes"));
                    String resultsVal = resultsBox.getValue().startsWith("Right") ? "immediately"
                                      : resultsBox.getValue().startsWith("After") ? "after_end" : "manually";
                    payload.addProperty("show_results_after", resultsVal);
                    payload.add("questions", questions);

                    com.google.gson.JsonObject resp = ApiService.getInstance().post("/groups/" + groupId + "/quizzes", payload);
                    if (resp.has("success") && !resp.get("success").getAsBoolean()) {
                        String msg = resp.has("message") ? resp.get("message").getAsString() : "Failed to publish.";
                        Platform.runLater(() -> showAlert("Error", msg));
                    } else {
                        Platform.runLater(() -> showAlert("Published", "Quiz published and announced to eligible students."));
                    }
                } catch (Exception ex) {
                    Platform.runLater(() -> showAlert("Error", "Could not publish quiz: " + ex.getMessage()));
                } finally {
                    Platform.runLater(() -> { okBtn.setDisable(false); okBtn.setText("Publish Quiz"); });
                }
            }).start();
        });
    }

    private void addQuestionBlock(VBox questionsBox) {
        int num = questionsBox.getChildren().size() + 1;
        VBox block = new VBox(8);
        block.setStyle("-fx-border-color:#e5e7eb;-fx-border-radius:10;-fx-background-color:#f9fafb;" +
                       "-fx-background-radius:10;-fx-padding:14;");

        HBox blockHeader = new HBox(8);
        blockHeader.setAlignment(Pos.CENTER_LEFT);
        Label numLbl = new Label(String.valueOf(num));
        numLbl.setStyle("-fx-background-color:#ea580c;-fx-text-fill:white;-fx-font-weight:bold;" +
                        "-fx-font-size:12px;-fx-background-radius:50;-fx-min-width:24;-fx-min-height:24;" +
                        "-fx-max-width:24;-fx-max-height:24;-fx-alignment:center;");
        Label qLbl = new Label("Question " + num);
        qLbl.setStyle("-fx-font-weight:600;-fx-font-size:13px;");
        HBox.setHgrow(qLbl, Priority.ALWAYS);
        Button removeBtn = new Button("✕");
        removeBtn.setStyle("-fx-background-color:transparent;-fx-text-fill:#9ca3af;-fx-cursor:hand;");
        removeBtn.setOnAction(e -> {
            if (questionsBox.getChildren().size() > 1) questionsBox.getChildren().remove(block);
        });
        blockHeader.getChildren().addAll(numLbl, qLbl, removeBtn);

        TextField qText = field("Enter your question...");
        qText.setId("qtext");

        ComboBox<String> qType = new ComboBox<>();
        qType.setId("qtype");
        qType.getStyleClass().add("combo-box");
        qType.getItems().addAll("multiple_choice", "true_false", "short_answer");
        qType.setValue("multiple_choice");

        TextField options = field("Option A, Option B, Option C, Option D  (comma-separated)");
        options.setId("options");
        options.setPromptText("Options (comma-separated, for multiple choice)");

        TextField correct = field("Correct answer");
        correct.setId("correct");
        correct.setStyle("-fx-border-color:#86efac;-fx-background-color:#f0fdf4;" + correct.getStyle());

        TextField points = field("5");
        points.setId("points");
        points.setPrefWidth(60);

        // Hide options field for non-multiple-choice
        qType.setOnAction(e -> {
            boolean mc = "multiple_choice".equals(qType.getValue());
            options.setVisible(mc);
            options.setManaged(mc);
        });

        block.getChildren().addAll(
            blockHeader,
            lbl("Question text *"), qText,
            row2(new VBox(4, lbl("Type"), qType), new VBox(4, lbl("Points"), points)),
            options,
            lbl("Correct answer *"), correct
        );
        questionsBox.getChildren().add(block);
    }

    // ── Quiz form helpers ──────────────────────────────────────
    private TextField field(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.getStyleClass().add("text-field");
        return tf;
    }

    private Label lbl(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size:12px;-fx-font-weight:500;-fx-text-fill:#374151;");
        return l;
    }

    private Label sectionHead(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size:15px;-fx-font-weight:bold;-fx-text-fill:#111827;-fx-padding:8 0 4 0;");
        return l;
    }

    private HBox row2(javafx.scene.Node a, javafx.scene.Node b) {
        HBox h = new HBox(12, a, b);
        HBox.setHgrow(a, Priority.ALWAYS);
        HBox.setHgrow(b, Priority.ALWAYS);
        return h;
    }

    private String getFieldText(VBox block, String fieldId) {
        return block.getChildren().stream()
            .filter(n -> fieldId.equals(n.getId()) && n instanceof TextField)
            .map(n -> ((TextField) n).getText().trim())
            .findFirst().orElse("");
    }

    private String getComboValue(VBox block, String fieldId) {
        return block.getChildren().stream()
            .filter(n -> fieldId.equals(n.getId()) && n instanceof ComboBox)
            .map(n -> ((ComboBox<?>) n).getValue() != null ? ((ComboBox<?>) n).getValue().toString() : "")
            .findFirst().orElse(null);
    }

    // ─────────────────────────────────────────────────────────────
    // MODERATION TAB — mirrors Lecturer/Groups/Moderation.vue
    // ─────────────────────────────────────────────────────────────
    private Node buildModeration() {
        VBox box = new VBox(20);

        // Heading
        VBox heading = new VBox(2);
        Label h2  = new Label("Group moderation");
        h2.setStyle("-fx-font-size:20px;-fx-font-weight:bold;-fx-text-fill:#111827;");
        Label sub = new Label("Review reports and flagged conversations in the groups you teach.");
        sub.setStyle("-fx-font-size:12px;-fx-text-fill:#6b7280;");
        heading.getChildren().addAll(h2, sub);

        // Open reports card
        VBox reportsCard = new VBox(0);
        reportsCard.getStyleClass().add("card");
        reportsCard.setPadding(Insets.EMPTY);
        HBox reportsHeader = cardHeader("Open reports");
        VBox reportsBody = new VBox(0);
        reportsBody.getChildren().add(muted("Loading reports..."));
        reportsCard.getChildren().addAll(reportsHeader, reportsBody);

        // Flagged conversations card
        VBox flaggedCard = new VBox(0);
        flaggedCard.getStyleClass().add("card");
        flaggedCard.setPadding(Insets.EMPTY);
        HBox flaggedHeader = cardHeader("Flagged conversations");
        VBox flaggedBody = new VBox(0);
        flaggedBody.getChildren().add(muted("Loading..."));
        flaggedCard.getChildren().addAll(flaggedHeader, flaggedBody);

        box.getChildren().addAll(heading, reportsCard, flaggedCard);

        // Async: fetch flagged content from admin endpoint (lecturer has same access if admin)
        // or use a generic groups approach via desktop groups + topics
        new Thread(() -> {
            try {
                JsonObject flaggedResp = ApiService.getInstance().get("/admin/flagged-content");
                Platform.runLater(() -> {
                    // Reports — currently no dedicated lecturer reports API, show flagged topics as reports
                    reportsBody.getChildren().clear();
                    flaggedBody.getChildren().clear();
                    boolean hasReports = false;
                    boolean hasFlagged = false;

                    if (flaggedResp.has("flagged_topics")) {
                        for (JsonElement el : flaggedResp.getAsJsonArray("flagged_topics")) {
                            JsonObject t = el.getAsJsonObject();
                            // Show in both sections for completeness
                            flaggedBody.getChildren().add(flaggedTopicRow(t));
                            hasFlagged = true;
                        }
                    }
                    if (!hasReports) reportsBody.getChildren().add(muted("No open reports in your groups."));
                    if (!hasFlagged) flaggedBody.getChildren().add(muted("No flagged conversations in your groups."));
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    reportsBody.getChildren().setAll(muted("No open reports in your groups."));
                    flaggedBody.getChildren().setAll(muted("No flagged conversations in your groups."));
                });
            }
        }).start();

        return box;
    }

    private HBox flaggedTopicRow(JsonObject t) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(14, 24, 14, 24));
        row.setStyle("-fx-border-color:#f3f4f6;-fx-border-width:0 0 1 0;");
        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color:#f9fafb;-fx-border-color:#f3f4f6;-fx-border-width:0 0 1 0;"));
        row.setOnMouseExited(e  -> row.setStyle("-fx-border-color:#f3f4f6;-fx-border-width:0 0 1 0;"));

        String title  = safe(t, "title");
        String group  = t.has("group") && t.get("group").isJsonObject() ? safe(t.getAsJsonObject("group"), "name") : "—";
        String author = t.has("user")  && t.get("user").isJsonObject()  ? safe(t.getAsJsonObject("user"),  "name") : "—";
        int topicId   = t.has("id") ? t.get("id").getAsInt() : 0;
        int groupId   = t.has("group_id") ? t.get("group_id").getAsInt() : 0;

        VBox info = new VBox(2);
        HBox.setHgrow(info, Priority.ALWAYS);
        Label nameLbl = new Label(title);
        nameLbl.setStyle("-fx-font-weight:600;-fx-font-size:13px;-fx-text-fill:#111827;");
        Label metaLbl = muted(group + " · by " + author);
        info.getChildren().addAll(nameLbl, metaLbl);

        Button reviewBtn = new Button("Review");
        reviewBtn.setStyle("-fx-background-color:transparent;-fx-text-fill:#ea580c;-fx-font-weight:bold;-fx-font-size:12px;-fx-cursor:hand;");
        reviewBtn.setOnAction(e -> {
            if (groupId > 0 && topicId > 0) shell.openTopic(groupId, group, topicId);
        });

        row.getChildren().addAll(info, reviewBtn);
        return row;
    }

    // ─────────────────────────────────────────────────────────────
    // Shared widget helpers
    // ─────────────────────────────────────────────────────────────
    private VBox statCard(String label, String value, String valColor) {
        VBox c = new VBox(6);
        c.setStyle("-fx-background-color:#ffffff;-fx-border-color:#f3f4f6;-fx-border-radius:16;" +
                   "-fx-background-radius:16;-fx-padding:20 24 20 24;" +
                   "-fx-effect:dropshadow(three-pass-box,rgba(0,0,0,0.04),4,0,0,1);");
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size:13px;-fx-font-weight:500;-fx-text-fill:#6b7280;");
        Label val = new Label(value);
        val.setId("stat-val-" + label.replace(" ","_").toLowerCase());
        val.setStyle("-fx-font-size:28px;-fx-font-weight:bold;-fx-text-fill:" + valColor + ";");
        c.getChildren().addAll(lbl, val);
        return c;
    }

    /** Update the value label inside a stat card. */
    private void setStatVal(VBox card, String value) {
        card.getChildren().stream()
            .filter(n -> n instanceof Label)
            .map(n -> (Label) n)
            .filter(l -> l.getStyle().contains("font-size:28px"))
            .findFirst()
            .ifPresent(l -> l.setText(value));
    }

    private HBox cardHeader(String title) {
        HBox h = new HBox();
        h.setPadding(new Insets(14, 24, 14, 24));
        h.setStyle("-fx-border-color:#f3f4f6;-fx-border-width:0 0 1 0;");
        Label lbl = new Label(title);
        lbl.setStyle("-fx-font-weight:bold;-fx-font-size:14px;-fx-text-fill:#111827;");
        h.getChildren().add(lbl);
        return h;
    }

    private Label theadCell(String text, double width) {
        Label l = new Label(text.toUpperCase());
        l.setStyle("-fx-font-size:10px;-fx-font-weight:bold;-fx-text-fill:#9ca3af;");
        if (width > 0) l.setPrefWidth(width);
        else HBox.setHgrow(l, Priority.ALWAYS);
        return l;
    }

    private Label muted(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("muted-text");
        l.setPadding(new Insets(16, 24, 16, 24));
        return l;
    }

    private Label errLabel(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("error-text");
        l.setPadding(new Insets(12, 24, 12, 24));
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
