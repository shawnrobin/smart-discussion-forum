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
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Groups list — mirrors GroupList/Index.vue.
 * Desktop shows only the groups the user has joined (desktopIndex endpoint).
 * Lecturers also see a "Create Group" button.
 */
public class GroupsView {

    private final MainView shell;
    private FlowPane grid;
    private StackPane gridHolder;
    private TextField searchField;
    private ComboBox<String> sortBox;
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
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        User user = SessionManager.getInstance().getCurrentUser();
        headerRow.getChildren().add(titleBox);
        if (user != null && user.isLecturer()) {
            Button createBtn = UiHelper.primaryButton("+ Create Group");
            createBtn.setOnAction(e -> showCreateGroupDialog());
            headerRow.getChildren().add(createBtn);
        }

        // ── Toolbar: search + sort ──────────────────────────────────
        HBox toolbar = new HBox(12);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        StackPane searchWrap = new StackPane();
        searchField = new TextField();
        searchField.setPromptText("🔍  Search groups by name or description...");
        searchField.getStyleClass().add("text-field");
        HBox.setHgrow(searchWrap, Priority.ALWAYS);
        searchField.textProperty().addListener((obs, o, n) -> applyFilterAndSort());
        searchWrap.getChildren().add(searchField);

        Label sortLbl = new Label("Sort:");
        sortLbl.getStyleClass().add("muted-text");
        sortBox = new ComboBox<>();
        sortBox.getStyleClass().add("combo-box");
        sortBox.getItems().addAll("Name (A–Z)", "Most members", "Most topics");
        sortBox.setValue("Name (A–Z)");
        sortBox.valueProperty().addListener((obs, o, n) -> applyFilterAndSort());

        toolbar.getChildren().addAll(searchWrap, sortLbl, sortBox);

        // ── Groups grid ───────────────────────────────────────────
        grid = new FlowPane(16, 16);
        grid.setPrefWrapLength(1000); // re-measured responsively by the ScrollPane width

        gridHolder = new StackPane(grid);
        gridHolder.setAlignment(Pos.TOP_LEFT);
        showLoadingState();

        // Re-wrap columns as the window resizes
        scroll.widthProperty().addListener((obs, o, n) -> grid.setPrefWrapLength(Math.max(320, n.doubleValue() - 60)));

        root.getChildren().addAll(headerRow, toolbar, gridHolder);
        scroll.setContent(root);

        loadGroups();
        return scroll;
    }

    private void showLoadingState() {
        grid.getChildren().clear();
        VBox loading = new VBox(10);
        loading.setAlignment(Pos.CENTER);
        loading.setPadding(new Insets(48));
        ProgressIndicator spinner = new ProgressIndicator();
        spinner.getStyleClass().add("progress-indicator");
        spinner.setMaxSize(28, 28);
        Label loadingLbl = new Label("Loading your groups...");
        loadingLbl.getStyleClass().add("muted-text");
        loading.getChildren().addAll(spinner, loadingLbl);
        grid.getChildren().add(loading);
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
                Platform.runLater(this::applyFilterAndSort);
            } catch (Exception e) {
                Platform.runLater(() -> {
                    grid.getChildren().clear();
                    Label err = new Label("Could not load groups: " + e.getMessage());
                    err.getStyleClass().add("error-text");
                    grid.getChildren().add(err);
                });
            }
        }).start();
    }

    private void applyFilterAndSort() {
        String q = searchField.getText() == null ? "" : searchField.getText().toLowerCase().trim();
        List<Group> filtered = allGroups.stream()
                .filter(g -> g.name.toLowerCase().contains(q) ||
                        (g.description != null && g.description.toLowerCase().contains(q)))
                .collect(java.util.stream.Collectors.toList());

        Comparator<Group> comparator = switch (sortBox.getValue()) {
            case "Most members" -> Comparator.comparingInt((Group g) -> g.members).reversed();
            case "Most topics" -> Comparator.comparingInt((Group g) -> g.topics).reversed();
            default -> Comparator.comparing(g -> g.name.toLowerCase());
        };
        filtered.sort(comparator);

        renderGroups(filtered);
    }

    private void renderGroups(List<Group> groups) {
        grid.getChildren().clear();
        if (groups.isEmpty()) {
            VBox empty = new VBox(10);
            empty.setAlignment(Pos.CENTER);
            empty.setPadding(new Insets(56));
            empty.setPrefWidth(600);
            Label emptyIcon = new Label();
            emptyIcon.setGraphic(Icons.of(FontAwesomeSolid.USERS, 40, "#d1d5db"));
            Label emptyMsg = new Label(allGroups.isEmpty() ? "You haven't joined any groups yet" : "No groups match your search");
            emptyMsg.setStyle("-fx-font-size:14px;-fx-font-weight:600;-fx-text-fill:#374151;");
            Label emptyHint = new Label(allGroups.isEmpty()
                    ? "Groups you join or that a lecturer adds you to will show up here."
                    : "Try a different search term or clear the search box.");
            emptyHint.getStyleClass().add("muted-text");
            empty.getChildren().addAll(emptyIcon, emptyMsg, emptyHint);
            grid.getChildren().add(empty);
            return;
        }

        for (Group g : groups) {
            grid.getChildren().add(groupCard(g));
        }
    }

    private VBox groupCard(Group g) {
        VBox card = new VBox(14);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(20, 22, 20, 22));
        card.setPrefWidth(320);
        card.setMinWidth(280);
        card.setMaxWidth(360);
        card.setStyle(card.getStyle() + "-fx-cursor:hand;");

        // Header: avatar + name
        HBox headerRow = new HBox(12);
        headerRow.setAlignment(Pos.CENTER_LEFT);
        String initial = (g.name != null && !g.name.isEmpty()) ? g.name.substring(0, 1).toUpperCase() : "?";
        StackPane avatar = UiHelper.orangeAvatar(initial);

        VBox nameBox = new VBox(2);
        Label name = new Label(g.name);
        name.setStyle("-fx-font-size:15px;-fx-font-weight:bold;-fx-text-fill:#111827;");
        name.setWrapText(true);
        nameBox.getChildren().add(name);
        HBox.setHgrow(nameBox, Priority.ALWAYS);

        headerRow.getChildren().addAll(avatar, nameBox);

        // Description (clamped to a few lines by fixed max height)
        Label desc = new Label(g.description != null && !g.description.isBlank() ? g.description : "No description provided.");
        desc.getStyleClass().add("muted-text");
        desc.setWrapText(true);
        desc.setMaxHeight(54);
        desc.setStyle(desc.getStyle() + ";-fx-font-size:12px;");

        // Meta pills
        HBox metaRow = new HBox(8);
        metaRow.setAlignment(Pos.CENTER_LEFT);
        Label membersMeta = pill(FontAwesomeSolid.USERS, g.members + (g.members == 1 ? " member" : " members"));
        Label topicsMeta = pill(FontAwesomeSolid.COMMENTS, g.topics + (g.topics == 1 ? " topic" : " topics"));
        metaRow.getChildren().addAll(membersMeta, topicsMeta);

        Separator sep = new Separator();

        // Open button
        Button openBtn = UiHelper.primaryButton("Open Group  →");
        openBtn.setMaxWidth(Double.MAX_VALUE);
        openBtn.setOnAction(e -> shell.openGroup(g.id, g.name));

        card.setOnMouseClicked(e -> shell.openGroup(g.id, g.name));
        card.getChildren().addAll(headerRow, desc, metaRow, sep, openBtn);
        return card;
    }

    private Label pill(FontAwesomeSolid icon, String text) {
        Label l = new Label(text);
        l.setGraphic(Icons.of(icon, 11, "#4b5563"));
        l.setStyle("-fx-background-color:#f9fafb;-fx-text-fill:#4b5563;-fx-font-size:11px;-fx-font-weight:600;" +
                "-fx-background-radius:20;-fx-padding:4 10 4 10;-fx-border-color:#f3f4f6;-fx-border-radius:20;" +
                "-fx-graphic-text-gap:6;");
        return l;
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
