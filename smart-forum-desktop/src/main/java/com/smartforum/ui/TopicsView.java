package com.smartforum.ui;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.smartforum.model.Topic;
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
 * Topics list for a group — mirrors Groups/Show.vue.
 * Shows threads, search/filter, and a "Start conversation" button.
 */
public class TopicsView {

    private final MainView shell;
    private final int groupId;
    private final String groupName;

    private VBox listBox;
    private TextField searchField;
    private ComboBox<String> filterBox;
    private List<Topic> allTopics = new ArrayList<>();
    private String groupDescription = "";
    private int groupMembers = 0;
    private List<String> keywords = new ArrayList<>();

    public TopicsView(MainView shell, int groupId, String groupName) {
        this.shell = shell;
        this.groupId = groupId;
        this.groupName = groupName;
    }

    public Node build() {
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("scroll-pane");

        VBox root = new VBox(16);
        root.setPadding(new Insets(24));
        root.setFillWidth(true);

        // Back link
        Button backBtn = new Button("← Back to Groups");
        backBtn.getStyleClass().add("btn-link");
        backBtn.setOnAction(e -> shell.navigateTo(MainView.Page.GROUPS));

        // Group header card
        VBox groupCard = buildGroupHeader();

        // Info banner
        VBox infoBanner = new VBox(4);
        infoBanner.getStyleClass().add("info-banner");
        Label infoBannerTitle = new Label("Keep this group focused");
        infoBannerTitle.setStyle("-fx-font-weight:600;-fx-text-fill:#431407;-fx-font-size:13px;");
        Label infoBannerBody = new Label("Start a clear conversation for one subject, then use replies to chat in that thread.");
        infoBannerBody.setStyle("-fx-text-fill:#7c2d12;-fx-font-size:12px;-fx-wrap-text:true;");
        infoBannerBody.setWrapText(true);
        infoBanner.getChildren().addAll(infoBannerTitle, infoBannerBody);

        // Search + filter
        HBox searchRow = new HBox(12);
        searchRow.setAlignment(Pos.CENTER_LEFT);
        searchField = new TextField();
        searchField.setPromptText("Find a conversation or topic...");
        searchField.getStyleClass().add("text-field");
        HBox.setHgrow(searchField, Priority.ALWAYS);
        searchField.textProperty().addListener((o, ov, nv) -> filterTopics());

        filterBox = new ComboBox<>();
        filterBox.getStyleClass().add("combo-box");
        filterBox.getItems().addAll("Most Recent", "Most Replied", "Unanswered");
        filterBox.setValue("Most Recent");
        filterBox.setOnAction(e -> filterTopics());

        searchRow.getChildren().addAll(searchField, filterBox);

        // Section heading
        HBox sectionHead = new HBox(8);
        sectionHead.setAlignment(Pos.CENTER_LEFT);
        Label sectionTitle = UiHelper.sectionTitle("Group chat");
        Label sectionSub = new Label("Open a thread to read and send live replies.");
        sectionSub.getStyleClass().add("muted-text");
        VBox sectionInfo = new VBox(2, sectionTitle, sectionSub);
        sectionHead.getChildren().add(sectionInfo);

        // List area
        listBox = new VBox(10);
        Label loadingLbl = new Label("Loading conversations...");
        loadingLbl.getStyleClass().add("muted-text");
        listBox.getChildren().add(loadingLbl);

        root.getChildren().addAll(backBtn, groupCard, infoBanner, searchRow, sectionHead, listBox);
        scroll.setContent(root);

        loadTopics();
        return scroll;
    }

    private VBox buildGroupHeader() {
        VBox card = new VBox(12);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(20, 24, 20, 24));

        HBox topRow = new HBox(12);
        topRow.setAlignment(Pos.TOP_LEFT);

        // Letter avatar
        StackPane avatar = new StackPane();
        avatar.setStyle("-fx-background-color:#ea580c;-fx-background-radius:8;-fx-min-width:40;-fx-min-height:40;-fx-max-width:40;-fx-max-height:40;-fx-alignment:center;");
        Label avatarLbl = new Label(String.valueOf(groupName.charAt(0)).toUpperCase());
        avatarLbl.setStyle("-fx-text-fill:white;-fx-font-weight:bold;-fx-font-size:16px;");
        avatar.getChildren().add(avatarLbl);

        VBox nameBlock = new VBox(2);
        Label nameLbl = new Label(groupName);
        nameLbl.setStyle("-fx-font-size:18px;-fx-font-weight:bold;-fx-text-fill:#111827;");
        Label descLbl = new Label("Loading description...");
        descLbl.getStyleClass().add("muted-text");
        descLbl.setId("group-desc");
        descLbl.setWrapText(true);
        nameBlock.getChildren().addAll(nameLbl, descLbl);
        HBox.setHgrow(nameBlock, Priority.ALWAYS);

        Button newTopicBtn = UiHelper.primaryButton("+ Start conversation");
        newTopicBtn.setOnAction(e -> showCreateTopicDialog());

        topRow.getChildren().addAll(avatar, nameBlock, newTopicBtn);

        HBox metaRow = new HBox(20);
        metaRow.setAlignment(Pos.CENTER_LEFT);
        Label membersLbl = new Label("👥 members");
        membersLbl.getStyleClass().add("muted-text");
        membersLbl.setId("group-members");
        Label topicsLbl = new Label("💬 topics");
        topicsLbl.getStyleClass().add("muted-text");
        topicsLbl.setId("group-topics-count");
        metaRow.getChildren().addAll(membersLbl, topicsLbl);

        card.getChildren().addAll(topRow, metaRow);
        return card;
    }

    private void loadTopics() {
        new Thread(() -> {
            try {
                JsonObject resp = ApiService.getInstance().get("/groups/" + groupId + "/topics");
                List<Topic> topics = new ArrayList<>();

                // API returns {"success":true,"topics":{"data":[...],"current_page":1,...}}
                // The "topics" value is a Laravel paginator object with a "data" array.
                if (resp.has("topics")) {
                    com.google.gson.JsonElement topicsEl = resp.get("topics");
                    JsonArray dataArr = null;
                    if (topicsEl.isJsonObject()) {
                        JsonObject topicsObj = topicsEl.getAsJsonObject();
                        if (topicsObj.has("data")) {
                            dataArr = topicsObj.getAsJsonArray("data");
                        }
                    } else if (topicsEl.isJsonArray()) {
                        dataArr = topicsEl.getAsJsonArray();
                    }
                    if (dataArr != null) {
                        for (JsonElement el : dataArr) {
                            topics.add(ApiService.getInstance().gson.fromJson(el, Topic.class));
                        }
                    }
                }

                allTopics = topics;
                final List<Topic> finalTopics = topics;
                Platform.runLater(() -> renderTopics(finalTopics));
            } catch (Exception e) {
                Platform.runLater(() -> {
                    listBox.getChildren().clear();
                    Label err = new Label("Could not load topics: " + e.getMessage());
                    err.getStyleClass().add("error-text");
                    listBox.getChildren().add(err);
                });
            }
        }).start();
    }

    private void filterTopics() {
        String q = searchField.getText().toLowerCase().trim();
        String sort = filterBox.getValue();

        List<Topic> filtered = allTopics.stream()
                .filter(t -> q.isEmpty() || t.title.toLowerCase().contains(q))
                .collect(java.util.stream.Collectors.toList());

        if ("Most Replied".equals(sort)) {
            filtered.sort((a, b) -> {
                int aReplies = a.messages != null ? a.messages.size() : 0;
                int bReplies = b.messages != null ? b.messages.size() : 0;
                return Integer.compare(bReplies, aReplies);
            });
        } else if ("Unanswered".equals(sort)) {
            filtered = filtered.stream().filter(t -> !t.isAnswered).collect(java.util.stream.Collectors.toList());
        }

        renderTopics(filtered);
    }

    private void renderTopics(List<Topic> topics) {
        listBox.getChildren().clear();
        if (topics.isEmpty()) {
            VBox empty = new VBox(8);
            empty.setAlignment(Pos.CENTER);
            empty.setPadding(new Insets(40));
            Label icon = new Label("💬");
            icon.setStyle("-fx-font-size:36px;");
            Label msg = new Label("No conversations yet. Be the first to start one!");
            msg.getStyleClass().add("muted-text");
            empty.getChildren().addAll(icon, msg);
            listBox.getChildren().add(empty);
            return;
        }
        for (Topic t : topics) {
            listBox.getChildren().add(topicCard(t));
        }
    }

    private VBox topicCard(Topic t) {
        VBox card = new VBox();
        card.getStyleClass().add("card");
        card.setStyle("-fx-cursor:hand;");
        card.setPadding(new Insets(16, 20, 16, 20));

        HBox innerRow = new HBox(12);
        innerRow.setAlignment(Pos.TOP_LEFT);

        // Avatar
        String initials = t.user != null ? (t.user.initials != null ? t.user.initials : "??") : "??";
        StackPane avatar = UiHelper.orangeAvatar(initials);

        VBox info = new VBox(6);
        HBox.setHgrow(info, Priority.ALWAYS);

        // Title row with badge
        HBox titleRow = new HBox(6);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Label threadBadge = UiHelper.badge("Thread", "badge-thread");
        Label titleLbl = new Label(t.title);
        titleLbl.setStyle("-fx-font-weight:600;-fx-text-fill:#111827;-fx-font-size:13px;");
        titleLbl.setWrapText(false);
        titleRow.getChildren().addAll(threadBadge, titleLbl);

        // Body preview
        Label body = new Label(t.description != null ? t.description : "");
        body.getStyleClass().add("muted-text");
        body.setWrapText(true);
        body.setMaxHeight(36);

        // Status badge
        Label statusBadge;
        if (t.isAnswered) {
            statusBadge = UiHelper.badge("Answered", "badge-answered");
        } else if (t.isQuestion) {
            statusBadge = UiHelper.badge("Open", "badge-open");
        } else {
            statusBadge = UiHelper.badge("Discussion", "badge-open");
        }

        // Bottom meta
        HBox metaRow = new HBox(12);
        metaRow.setAlignment(Pos.CENTER_LEFT);
        String authorName = t.user != null ? t.user.name : "Unknown";
        Label authorLbl = new Label("Started by " + authorName + " · " + (t.createdAt != null ? t.createdAt : ""));
        authorLbl.getStyleClass().add("meta-text");

        int replyCount = t.messages != null ? t.messages.size() : 0;
        Label repliesLbl = new Label("💬 " + replyCount);
        repliesLbl.getStyleClass().add("meta-text");
        Label likesLbl = new Label("👁 " + t.likes);
        likesLbl.getStyleClass().add("meta-text");

        HBox rightMeta = new HBox(8, repliesLbl, likesLbl);
        HBox.setHgrow(authorLbl, Priority.ALWAYS);
        metaRow.getChildren().addAll(authorLbl, rightMeta);

        info.getChildren().addAll(titleRow, body, metaRow);

        // Actions (edit/delete for own topics)
        User currentUser = SessionManager.getInstance().getCurrentUser();
        VBox actions = new VBox(4);
        actions.setAlignment(Pos.TOP_RIGHT);
        actions.getChildren().add(statusBadge);

        if (currentUser != null && t.user != null && currentUser.id == t.user.id) {
            Button deleteBtn = new Button("🗑");
            deleteBtn.setStyle("-fx-background-color:transparent;-fx-cursor:hand;-fx-text-fill:#9ca3af;");
            deleteBtn.setOnAction(ev -> confirmDeleteTopic(t));
            actions.getChildren().add(deleteBtn);
        }

        innerRow.getChildren().addAll(avatar, info, actions);
        card.getChildren().add(innerRow);
        card.setOnMouseClicked(e -> shell.openTopic(groupId, groupName, t.id));
        return card;
    }

    private void confirmDeleteTopic(Topic t) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete \"" + t.title + "\" and all its replies? This cannot be undone.",
                ButtonType.YES, ButtonType.CANCEL);
        confirm.setTitle("Delete Topic");
        confirm.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                new Thread(() -> {
                    try {
                        ApiService.getInstance().delete("/topics/" + t.id);
                        Platform.runLater(this::loadTopics);
                    } catch (Exception ex) {
                        Platform.runLater(() -> showAlert("Error", "Could not delete topic."));
                    }
                }).start();
            }
        });
    }

    private void showCreateTopicDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Start a Conversation");
        dialog.setHeaderText(groupName);

        VBox content = new VBox(10);
        content.setPadding(new Insets(8));
        content.setMinWidth(440);

        // Info
        VBox infoBanner = new VBox(4);
        infoBanner.getStyleClass().add("info-banner");
        Label infoLbl = new Label("Keep it relevant. Title ≥ 8 chars, message ≥ 30 chars.");
        infoLbl.setStyle("-fx-text-fill:#7c2d12;-fx-font-size:12px;-fx-wrap-text:true;");
        infoLbl.setWrapText(true);
        if (!keywords.isEmpty()) {
            Label kwLbl = new Label("Keywords: " + String.join(", ", keywords));
            kwLbl.setStyle("-fx-font-weight:600;-fx-text-fill:#7c2d12;-fx-font-size:12px;");
            infoBanner.getChildren().addAll(infoLbl, kwLbl);
        } else {
            infoBanner.getChildren().add(infoLbl);
        }

        TextField titleField = new TextField();
        titleField.setPromptText("Ask a clear, relevant question or name the subject");
        titleField.getStyleClass().add("text-field");

        TextArea bodyField = new TextArea();
        bodyField.setPromptText("Give enough detail (at least 30 characters)...");
        bodyField.getStyleClass().add("text-area");
        bodyField.setPrefRowCount(4);
        bodyField.setWrapText(true);

        CheckBox isQuestionBox = new CheckBox("This is a question — I'm looking for an answer");
        isQuestionBox.setStyle("-fx-text-fill:#374151;-fx-font-size:13px;");

        Label errorLbl = new Label();
        errorLbl.getStyleClass().add("error-text");
        errorLbl.setVisible(false);
        errorLbl.setManaged(false);

        content.getChildren().addAll(
                infoBanner,
                UiHelper.formLabel("Conversation title"), titleField,
                UiHelper.formLabel("Opening message"), bodyField,
                isQuestionBox, errorLbl
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Button okBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okBtn.setText("Start conversation");
        okBtn.getStyleClass().add("btn-primary");

        dialog.showAndWait().ifPresent(bt -> {
            if (bt != ButtonType.OK) return;
            String title = titleField.getText().trim();
            String body = bodyField.getText().trim();
            if (title.length() < 8 || body.length() < 30) {
                showAlert("Validation", "Title needs ≥ 8 chars; message needs ≥ 30 chars.");
                return;
            }
            new Thread(() -> {
                try {
                    JsonObject payload = new JsonObject();
                    payload.addProperty("title", title);
                    payload.addProperty("description", body);
                    payload.addProperty("is_question", isQuestionBox.isSelected());
                    payload.addProperty("visibility", "all");
                    JsonObject result = ApiService.getInstance().post("/groups/" + groupId + "/topics", payload);
                    if (result.has("success") && !result.get("success").getAsBoolean()) {
                        String msg = result.has("message") ? result.get("message").getAsString() : "Could not post topic.";
                        Platform.runLater(() -> showAlert("Error", msg));
                    } else {
                        Platform.runLater(this::loadTopics);
                    }
                } catch (Exception ex) {
                    Platform.runLater(() -> showAlert("Error", "Could not post topic: " + ex.getMessage()));
                }
            }).start();
        });
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setTitle(title);
        a.showAndWait();
    }
}
