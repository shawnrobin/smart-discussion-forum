package com.smartforum.ui;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.smartforum.model.Group;
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
 * Groups list — mirrors GroupList/Index.vue.
 * Desktop shows only the groups the user has joined (desktopIndex endpoint).
 * Lecturers also see a "Create Group" button.
 */
public class GroupsView {

    private final MainView shell;
    private VBox listBox;
    private TextField searchField;
    private List<Group> allGroups = new ArrayList<>();

    public GroupsView(MainView shell) {
        this.shell = shell;
    }

    public Node build() {
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("scroll-pane");

        VBox root = new VBox(20);
        root.setPadding(new Insets(24));
        root.setFillWidth(true);

        // ── Header ────────────────────────────────────────────────
        HBox headerRow = new HBox(12);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(2);
        Label h1 = new Label("Discussion Groups");
        h1.setStyle("-fx-font-size:22px;-fx-font-weight:bold;-fx-text-fill:#111827;");
        Label h1sub = new Label("Browse and open your discussion groups");
        h1sub.getStyleClass().add("muted-text");
        titleBox.getChildren().addAll(h1, h1sub);

        User user = SessionManager.getInstance().getCurrentUser();
        if (user != null && user.isLecturer()) {
            Button createBtn = UiHelper.primaryButton("+ Create Group");
            createBtn.setOnAction(e -> showCreateGroupDialog());
            headerRow.getChildren().addAll(titleBox, UiHelper.hSpacer(), createBtn);
        } else {
            headerRow.getChildren().addAll(titleBox);
        }

        // ── Search bar ────────────────────────────────────────────
        HBox searchRow = new HBox(12);
        searchRow.setAlignment(Pos.CENTER_LEFT);
        searchField = new TextField();
        searchField.setPromptText("Search groups...");
        searchField.getStyleClass().add("text-field");
        HBox.setHgrow(searchField, Priority.ALWAYS);
        searchField.textProperty().addListener((obs, o, n) -> filterGroups(n));
        searchRow.getChildren().add(searchField);

        // ── Groups grid ───────────────────────────────────────────
        listBox = new VBox(12);

        Label loadingLbl = new Label("Loading groups...");
        loadingLbl.getStyleClass().add("muted-text");
        loadingLbl.setPadding(new Insets(32, 0, 0, 0));
        listBox.getChildren().add(loadingLbl);

        root.getChildren().addAll(headerRow, searchRow, listBox);
        scroll.setContent(root);

        // Async load
        loadGroups();
        return scroll;
    }

    private void loadGroups() {
        new Thread(() -> {
            try {
                JsonObject resp = ApiService.getInstance().get("/desktop/groups");
                List<Group> groups = new ArrayList<>();
                if (resp.has("groups")) {
                    for (JsonElement el : resp.getAsJsonArray("groups")) {
                        groups.add(ApiService.getInstance().gson.fromJson(el, Group.class));
                    }
                }
                allGroups = groups;
                Platform.runLater(() -> renderGroups(groups));
            } catch (Exception e) {
                Platform.runLater(() -> {
                    listBox.getChildren().clear();
                    Label err = new Label("Could not load groups: " + e.getMessage());
                    err.getStyleClass().add("error-text");
                    listBox.getChildren().add(err);
                });
            }
        }).start();
    }

    private void filterGroups(String query) {
        String q = query.toLowerCase().trim();
        List<Group> filtered = allGroups.stream()
                .filter(g -> g.name.toLowerCase().contains(q) ||
                        (g.description != null && g.description.toLowerCase().contains(q)))
                .toList();
        renderGroups(filtered);
    }

    private void renderGroups(List<Group> groups) {
        listBox.getChildren().clear();
        if (groups.isEmpty()) {
            VBox empty = new VBox(8);
            empty.setAlignment(Pos.CENTER);
            empty.setPadding(new Insets(48));
            Label emptyIcon = new Label("👥");
            emptyIcon.setStyle("-fx-font-size:36px;");
            Label emptyMsg = new Label("No groups found");
            emptyMsg.getStyleClass().add("muted-text");
            empty.getChildren().addAll(emptyIcon, emptyMsg);
            listBox.getChildren().add(empty);
            return;
        }

        // 2-column grid
        int cols = 2;
        HBox row = null;
        for (int i = 0; i < groups.size(); i++) {
            if (i % cols == 0) {
                row = new HBox(12);
                row.setFillHeight(false);
                listBox.getChildren().add(row);
            }
            VBox card = groupCard(groups.get(i));
            HBox.setHgrow(card, Priority.ALWAYS);
            row.getChildren().add(card);
        }
        // fill last row if odd
        if (groups.size() % cols != 0 && row != null) {
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            row.getChildren().add(spacer);
        }
    }

    private VBox groupCard(Group g) {
        VBox card = new VBox(12);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(20, 24, 20, 24));
        card.setStyle("-fx-cursor:hand;" + card.getStyle());

        // Name + description
        Label name = new Label(g.name);
        name.setStyle("-fx-font-size:16px;-fx-font-weight:bold;-fx-text-fill:#111827;");
        Label desc = new Label(g.description != null ? g.description : "");
        desc.getStyleClass().add("muted-text");
        desc.setWrapText(true);

        // Meta row
        HBox metaRow = new HBox(16);
        metaRow.setAlignment(Pos.CENTER_LEFT);
        Label membersMeta = new Label("👥 " + g.members + " members");
        membersMeta.getStyleClass().add("meta-text");
        Label topicsMeta = new Label("💬 " + g.topics + " topics");
        topicsMeta.getStyleClass().add("meta-text");
        metaRow.getChildren().addAll(membersMeta, topicsMeta);

        // Open button
        Button openBtn = UiHelper.primaryButton("Open Group");
        openBtn.setMaxWidth(Double.MAX_VALUE);
        openBtn.setOnAction(e -> shell.openGroup(g.id, g.name));

        card.setOnMouseClicked(e -> shell.openGroup(g.id, g.name));
        card.getChildren().addAll(name, desc, metaRow, openBtn);
        return card;
    }

    // ── Create group dialog (lecturers only) ──────────────────────
    private void showCreateGroupDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Create New Group");
        dialog.setHeaderText("Fill in the group details");

        VBox content = new VBox(12);
        content.setPadding(new Insets(8));
        content.setMinWidth(360);

        TextField nameField = new TextField();
        nameField.setPromptText("Group name");
        nameField.getStyleClass().add("text-field");

        TextArea descField = new TextArea();
        descField.setPromptText("What is this group about?");
        descField.getStyleClass().add("text-area");
        descField.setPrefRowCount(3);

        TextField keywordsField = new TextField();
        keywordsField.setPromptText("e.g. algorithms, arrays, trees (comma-separated)");
        keywordsField.getStyleClass().add("text-field");

        Label err = new Label();
        err.getStyleClass().add("error-text");
        err.setVisible(false);
        err.setManaged(false);

        content.getChildren().addAll(
                UiHelper.formLabel("Group Name"), nameField,
                UiHelper.formLabel("Description"), descField,
                UiHelper.formLabel("Allowed keywords"), keywordsField,
                err
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button okBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okBtn.setText("Create Group");
        okBtn.getStyleClass().add("btn-primary");

        dialog.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.OK) {
                String gname = nameField.getText().trim();
                if (gname.isEmpty()) return;
                new Thread(() -> {
                    try {
                        JsonObject payload = new JsonObject();
                        payload.addProperty("name", gname);
                        payload.addProperty("description", descField.getText().trim());
                        JsonArray kwArr = new JsonArray();
                        for (String kw : keywordsField.getText().split(",")) {
                            String k = kw.trim();
                            if (!k.isEmpty()) kwArr.add(k);
                        }
                        payload.add("topic_keywords", kwArr);
                        ApiService.getInstance().post("/groups", payload);
                        Platform.runLater(this::loadGroups);
                    } catch (Exception ex) {
                        Platform.runLater(() -> showAlert("Error", "Could not create group: " + ex.getMessage()));
                    }
                }).start();
            }
        });
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.setTitle(title);
        alert.showAndWait();
    }
}
