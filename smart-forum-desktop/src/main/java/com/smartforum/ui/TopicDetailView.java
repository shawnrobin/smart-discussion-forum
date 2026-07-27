package com.smartforum.ui;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.smartforum.model.GroupMember;
import com.smartforum.model.Message;
import com.smartforum.model.Topic;
import com.smartforum.model.User;
import com.smartforum.service.ApiService;
import com.smartforum.service.PdfExportService;
import com.smartforum.service.SessionManager;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Topic detail + threaded replies — mirrors Topics/Show.vue.
 * Shows topic card, threaded messages with depth indents, and a reply form.
 */
public class TopicDetailView {

    private final MainView shell;
    private final int groupId;
    private final String groupName;
    private final int topicId;

    private VBox repliesBox;
    private VBox topicCardRef;   // kept so reload can re-render it
    private Topic currentTopic;
    private Integer replyParentId = null;
    private Label replyParentLabel;
    private TextArea replyInput;
    private CheckBox markAnswerBox;
    private Label replyError;

    private List<Message> currentMessages = new ArrayList<>();
    private List<GroupMember> groupMembers = new ArrayList<>();
    private final Set<Integer> excludedUserIds = new HashSet<>();
    private Label excludeSummaryLabel;

    public TopicDetailView(MainView shell, int groupId, String groupName, int topicId) {
        this.shell = shell;
        this.groupId = groupId;
        this.groupName = groupName;
        this.topicId = topicId;
    }

    public Node build() {
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("scroll-pane");

        VBox root = new VBox(16);
        root.setPadding(new Insets(24));
        root.setMaxWidth(Double.MAX_VALUE);

        // Back link
        Button backBtn = new Button("← Back to " + groupName);
        backBtn.getStyleClass().add("btn-link");
        backBtn.setOnAction(e -> shell.openGroup(groupId, groupName));

        // Placeholder cards
        topicCardRef = new VBox(8);
        topicCardRef.getStyleClass().add("card");
        topicCardRef.setPadding(new Insets(20, 24, 20, 24));
        Label loadingLbl = new Label("Loading discussion...");
        loadingLbl.getStyleClass().add("muted-text");
        topicCardRef.getChildren().add(loadingLbl);

        repliesBox = new VBox(8);

        // Reply form
        VBox replyForm = buildReplyForm();

        root.getChildren().addAll(backBtn, topicCardRef, repliesBox, replyForm);
        scroll.setContent(root);

        loadTopic();
        return scroll;
    }

    private void loadTopic() {
        new Thread(() -> {
            try {
                // Fetch topic
                JsonObject resp = ApiService.getInstance().get("/topics/" + topicId);
                if (resp.has("topic")) {
                    currentTopic = ApiService.getInstance().gson.fromJson(resp.get("topic"), Topic.class);
                }
                // Fetch messages (flat top-level + nested replies)
                JsonObject msgResp = ApiService.getInstance().get("/topics/" + topicId + "/messages");
                List<Message> messages = new ArrayList<>();
                if (msgResp.has("messages")) {
                    for (JsonElement el : msgResp.getAsJsonArray("messages")) {
                        Message m = ApiService.getInstance().gson.fromJson(el, Message.class);
                        m.depth = 0;
                        messages.add(m);
                        if (m.replies != null) messages.addAll(flattenReplies(m.replies, 1));
                    }
                } else if (currentTopic != null && currentTopic.messages != null) {
                    for (Message m : currentTopic.messages) {
                        m.depth = 0;
                        messages.add(m);
                    }
                }

                final List<Message> finalMessages = messages;
                currentMessages = messages;
                Platform.runLater(() -> {
                    if (currentTopic != null) renderTopicCard(topicCardRef, currentTopic);
                    renderReplies(finalMessages);
                });

                loadGroupMembers();
            } catch (Exception e) {
                Platform.runLater(() -> {
                    topicCardRef.getChildren().clear();
                    Label err = new Label("Could not load discussion: " + e.getMessage());
                    err.getStyleClass().add("error-text");
                    topicCardRef.getChildren().add(err);
                });
            }
        }).start();
    }

    private void loadGroupMembers() {
        try {
            JsonObject resp = ApiService.getInstance().get("/groups/" + groupId + "/members");
            List<GroupMember> members = new ArrayList<>();
            if (resp.has("members")) {
                for (JsonElement el : resp.getAsJsonArray("members")) {
                    members.add(ApiService.getInstance().gson.fromJson(el, GroupMember.class));
                }
            }
            groupMembers = members;
        } catch (Exception ignored) {
            // Offline or endpoint unavailable — exclusion picker will just show "no members found".
        }
    }

    private List<Message> flattenReplies(List<Message> replies, int depth) {
        List<Message> flat = new ArrayList<>();
        for (Message m : replies) {
            m.depth = depth;
            flat.add(m);
            if (m.replies != null) flat.addAll(flattenReplies(m.replies, depth + 1));
        }
        return flat;
    }

    private void renderTopicCard(VBox topicCard, Topic t) {
        topicCard.getChildren().clear();
        topicCard.setSpacing(12);

        HBox topRow = new HBox(12);
        topRow.setAlignment(Pos.TOP_LEFT);

        // Avatar
        String initials = t.user != null && t.user.initials != null ? t.user.initials : "??";
        StackPane avatar = UiHelper.orangeAvatar(initials);

        VBox content = new VBox(6);
        HBox.setHgrow(content, Priority.ALWAYS);

        // Author + role
        HBox authorRow = new HBox(8);
        authorRow.setAlignment(Pos.CENTER_LEFT);
        Label authorLbl = new Label(t.user != null ? t.user.name : "Unknown");
        authorLbl.setStyle("-fx-font-weight:600;-fx-text-fill:#111827;-fx-font-size:13px;");
        if (t.user != null && "lecturer".equals(t.user.role)) {
            Label roleBadge = new Label("Lecturer");
            roleBadge.setStyle("-fx-background-color:#fef3c7;-fx-text-fill:#92400e;-fx-font-size:10px;-fx-font-weight:bold;-fx-background-radius:20;-fx-padding:2 8 2 8;");
            authorRow.getChildren().addAll(authorLbl, roleBadge);
        } else {
            authorRow.getChildren().add(authorLbl);
        }
        Label timeLbl = new Label(t.createdAt != null ? t.createdAt : "");
        timeLbl.getStyleClass().add("meta-text");

        HBox authorMeta = new HBox(8);
        HBox.setHgrow(authorRow, Priority.ALWAYS);
        authorMeta.getChildren().addAll(authorRow, timeLbl);

        // Title
        Label titleLbl = new Label(t.title);
        titleLbl.setStyle("-fx-font-size:18px;-fx-font-weight:bold;-fx-text-fill:#111827;");
        titleLbl.setWrapText(true);

        // Body
        Label bodyLbl = new Label(t.description != null ? t.description : "");
        bodyLbl.setStyle("-fx-font-size:13px;-fx-text-fill:#374151;-fx-wrap-text:true;");
        bodyLbl.setWrapText(true);

        // Action bar
        Separator sep = new Separator();
        HBox actionBar = new HBox(16);
        actionBar.setAlignment(Pos.CENTER_LEFT);

        Label likesLbl = new Label("👍 " + t.likes);
        likesLbl.setStyle("-fx-font-size:13px;-fx-text-fill:#6b7280;-fx-cursor:hand;");
        likesLbl.setOnMouseClicked(ev -> doToggleTopicLike(likesLbl));
        int replyCount = t.messages != null ? t.messages.size() : 0;
        Label repliesLbl = new Label("💬 " + replyCount + " replies");
        repliesLbl.setStyle("-fx-font-size:13px;-fx-text-fill:#6b7280;");

        Button exportBtn = new Button("📄 Export PDF");
        exportBtn.getStyleClass().add("btn-secondary");
        exportBtn.setStyle("-fx-font-size:12px;-fx-cursor:hand;-fx-padding:4 10 4 10;");
        exportBtn.setOnAction(ev -> doExportPdf());

        Button shareBtn = new Button("🔗 Share");
        shareBtn.getStyleClass().add("btn-secondary");
        shareBtn.setStyle("-fx-font-size:12px;-fx-cursor:hand;-fx-padding:4 10 4 10;");
        shareBtn.setOnAction(ev -> doShare());

        Button reportBtn = new Button("🚩 Report");
        reportBtn.setStyle("-fx-background-color:transparent;-fx-text-fill:#9ca3af;-fx-font-size:12px;-fx-cursor:hand;-fx-padding:4 10 4 10;");
        reportBtn.setOnAction(ev -> doReport("topics/" + t.id));

        actionBar.getChildren().addAll(likesLbl, repliesLbl, UiHelper.hSpacer(), exportBtn, shareBtn, reportBtn);

        content.getChildren().addAll(authorMeta, titleLbl, bodyLbl, sep, actionBar);
        topRow.getChildren().addAll(avatar, content);
        topicCard.getChildren().add(topRow);
    }

    private void renderReplies(List<Message> messages) {
        repliesBox.getChildren().clear();

        if (messages.isEmpty()) {
            Label empty = new Label("No replies yet. Be the first to respond!");
            empty.getStyleClass().add("muted-text");
            empty.setPadding(new Insets(12, 0, 0, 0));
            repliesBox.getChildren().add(empty);
            return;
        }

        // Section header
        HBox sectionHead = UiHelper.dividerRow(messages.size() + " " + (messages.size() == 1 ? "Reply" : "Replies"));
        repliesBox.getChildren().add(sectionHead);

        for (Message m : messages) {
            repliesBox.getChildren().add(replyCard(m));
        }
    }

    private VBox replyCard(Message m) {
        int depth = m.depth;
        double indent = depth * 20.0;

        VBox outer = new VBox();
        outer.setPadding(new Insets(0, 0, 0, indent));

        VBox card = new VBox(8);
        card.getStyleClass().add(m.isAnswer ? "reply-answer" : "reply-normal");

        HBox topRow = new HBox(10);
        topRow.setAlignment(Pos.TOP_LEFT);

        // Avatar
        String initials = m.user != null && m.user.initials != null ? m.user.initials : "??";
        StackPane avatar = UiHelper.slateAvatar(initials);

        VBox content = new VBox(6);
        HBox.setHgrow(content, Priority.ALWAYS);

        // Author row
        HBox authorRow = new HBox(8);
        authorRow.setAlignment(Pos.CENTER_LEFT);
        Label authorLbl = new Label(m.user != null ? m.user.name : "Unknown");
        authorLbl.setStyle("-fx-font-weight:600;-fx-text-fill:#111827;-fx-font-size:13px;");
        if (m.isAnswer) {
            Label answerBadge = UiHelper.badge("✓ Answer", "badge-answer-mark");
            authorRow.getChildren().addAll(authorLbl, answerBadge);
        } else {
            authorRow.getChildren().add(authorLbl);
        }
        Label timeLbl = new Label(m.createdAt != null ? m.createdAt : "");
        timeLbl.getStyleClass().add("meta-text");
        HBox.setHgrow(authorRow, Priority.ALWAYS);

        HBox metaFull = new HBox(8);
        metaFull.getChildren().addAll(authorRow, timeLbl);

        // Body
        Label body = new Label(m.content);
        body.setStyle("-fx-font-size:13px;-fx-text-fill:#374151;-fx-wrap-text:true;");
        body.setWrapText(true);

        // Action row
        HBox actionRow = new HBox(16);
        actionRow.setAlignment(Pos.CENTER_LEFT);
        actionRow.setPadding(new Insets(4, 0, 0, 0));
        actionRow.setStyle("-fx-border-color:#f3f4f6;-fx-border-width:1 0 0 0;");

        Label likesLbl = new Label("👍 " + m.likes);
        likesLbl.setStyle("-fx-font-size:12px;-fx-text-fill:#6b7280;-fx-cursor:hand;");
        likesLbl.setOnMouseClicked(ev -> doToggleMessageLike(m.id, likesLbl));

        Button replyBtn = new Button("↩ Reply");
        replyBtn.setStyle("-fx-background-color:transparent;-fx-text-fill:#6b7280;-fx-font-size:12px;-fx-cursor:hand;-fx-padding:0;");
        replyBtn.setOnAction(ev -> setReplyParent(m));

        Button reportBtn = new Button("🚩 Report");
        reportBtn.setStyle("-fx-background-color:transparent;-fx-text-fill:#9ca3af;-fx-font-size:12px;-fx-cursor:hand;-fx-padding:0;");
        reportBtn.setOnAction(ev -> doReport("messages/" + m.id));

        // Mark as answer (topic owner or admin/lecturer)
        User currentUser = SessionManager.getInstance().getCurrentUser();
        boolean canMark = currentUser != null && currentTopic != null &&
                (currentTopic.userId == currentUser.id || currentUser.isAdmin() || currentUser.isLecturer());
        if (canMark && !m.isAnswer) {
            Button markBtn = new Button("✓ Mark Answer");
            markBtn.setStyle("-fx-background-color:transparent;-fx-text-fill:#16a34a;-fx-font-size:12px;-fx-cursor:hand;-fx-padding:0;");
            markBtn.setOnAction(ev -> doMarkAnswer(m.id));
            actionRow.getChildren().addAll(likesLbl, replyBtn, markBtn, reportBtn);
        } else {
            actionRow.getChildren().addAll(likesLbl, replyBtn, reportBtn);
        }

        content.getChildren().addAll(metaFull, body, actionRow);
        topRow.getChildren().addAll(avatar, content);
        card.getChildren().add(topRow);
        outer.getChildren().add(card);
        return outer;
    }

    private void setReplyParent(Message m) {
        replyParentId = m.id;
        String preview = m.content.length() > 60 ? m.content.substring(0, 60) + "..." : m.content;
        replyParentLabel.setText("Replying to " + (m.user != null ? m.user.name : "Unknown") + ": \"" + preview + "\"");
        replyParentLabel.setVisible(true);
        replyParentLabel.setManaged(true);
        replyInput.requestFocus();
    }

    private VBox buildReplyForm() {
        VBox form = new VBox(12);
        form.getStyleClass().add("card");
        form.setPadding(new Insets(20, 24, 20, 24));

        Label formTitle = new Label("Write a Reply");
        formTitle.setStyle("-fx-font-size:15px;-fx-font-weight:bold;-fx-text-fill:#111827;");

        replyParentLabel = new Label();
        replyParentLabel.setStyle("-fx-text-fill:#6b7280;-fx-font-size:12px;-fx-font-style:italic;");
        replyParentLabel.setWrapText(true);
        replyParentLabel.setVisible(false);
        replyParentLabel.setManaged(false);

        replyInput = new TextArea();
        replyInput.setPromptText("Share your thoughts...");
        replyInput.getStyleClass().add("text-area");
        replyInput.setPrefRowCount(4);
        replyInput.setWrapText(true);

        replyError = new Label();
        replyError.getStyleClass().add("error-text");
        replyError.setVisible(false);
        replyError.setManaged(false);

        markAnswerBox = new CheckBox("Mark as answer");
        markAnswerBox.setStyle("-fx-text-fill:#374151;-fx-font-size:13px;");

        Button excludeBtn = new Button("🚫 Exclude members...");
        excludeBtn.getStyleClass().add("btn-secondary");
        excludeBtn.setStyle("-fx-font-size:12px;-fx-cursor:hand;-fx-padding:4 10 4 10;");
        excludeBtn.setOnAction(e -> openExcludePicker());

        excludeSummaryLabel = new Label();
        excludeSummaryLabel.setStyle("-fx-font-size:12px;-fx-text-fill:#b45309;-fx-font-style:italic;");
        excludeSummaryLabel.setVisible(false);
        excludeSummaryLabel.setManaged(false);

        HBox excludeRow = new HBox(10);
        excludeRow.setAlignment(Pos.CENTER_LEFT);
        excludeRow.getChildren().addAll(excludeBtn, excludeSummaryLabel);

        HBox bottomRow = new HBox(12);
        bottomRow.setAlignment(Pos.CENTER_LEFT);

        bottomRow.getChildren().add(markAnswerBox);

        Button cancelReplyBtn = new Button("Cancel");
        cancelReplyBtn.getStyleClass().add("btn-secondary");
        cancelReplyBtn.setVisible(false);
        cancelReplyBtn.setManaged(false);
        cancelReplyBtn.setOnAction(e -> {
            replyParentId = null;
            replyParentLabel.setVisible(false);
            replyParentLabel.setManaged(false);
            cancelReplyBtn.setVisible(false);
            cancelReplyBtn.setManaged(false);
        });

        replyParentLabel.visibleProperty().addListener((obs, o, n) -> {
            cancelReplyBtn.setVisible(n);
            cancelReplyBtn.setManaged(n);
        });

        Button postBtn = UiHelper.primaryButton("Post Reply");
        postBtn.setOnAction(e -> doPostReply(postBtn));

        bottomRow.getChildren().addAll(UiHelper.hSpacer(), cancelReplyBtn, postBtn);

        form.getChildren().addAll(formTitle, replyParentLabel, replyInput, replyError, excludeRow, bottomRow);
        return form;
    }

    /** Requirement #3: let the author pick group members to exclude from this message's notifications. */
    private void openExcludePicker() {
        if (groupMembers.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION,
                    "No other group members were found to exclude (or the list hasn't loaded yet — try again in a moment).",
                    ButtonType.OK);
            alert.setTitle("Exclude members");
            alert.showAndWait();
            return;
        }

        Dialog<Set<Integer>> dialog = new Dialog<>();
        dialog.setTitle("Exclude members from this message");
        dialog.setHeaderText("Selected members will not be notified about this reply.");

        VBox box = new VBox(6);
        box.setPadding(new Insets(12));
        List<CheckBox> boxes = new ArrayList<>();
        for (GroupMember gm : groupMembers) {
            CheckBox cb = new CheckBox(gm.name + (gm.role != null && !gm.role.equals("student") ? " (" + gm.role + ")" : ""));
            cb.setUserData(gm.id);
            cb.setSelected(excludedUserIds.contains(gm.id));
            boxes.add(cb);
            box.getChildren().add(cb);
        }
        ScrollPane scroll = new ScrollPane(box);
        scroll.setFitToWidth(true);
        scroll.setPrefViewportHeight(Math.min(240, boxes.size() * 32 + 20));
        dialog.getDialogPane().setContent(scroll);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(bt -> {
            if (bt != ButtonType.OK) return null;
            Set<Integer> selected = new HashSet<>();
            for (CheckBox cb : boxes) {
                if (cb.isSelected()) selected.add((Integer) cb.getUserData());
            }
            return selected;
        });

        dialog.showAndWait().ifPresent(selected -> {
            excludedUserIds.clear();
            excludedUserIds.addAll(selected);
            if (excludedUserIds.isEmpty()) {
                excludeSummaryLabel.setVisible(false);
                excludeSummaryLabel.setManaged(false);
            } else {
                excludeSummaryLabel.setText("Excluding " + excludedUserIds.size() + " member(s) from this reply");
                excludeSummaryLabel.setVisible(true);
                excludeSummaryLabel.setManaged(true);
            }
        });
    }

    private void doPostReply(Button postBtn) {
        String content = replyInput.getText().trim();
        if (content.isEmpty()) {
            showReplyError("Reply cannot be empty.");
            return;
        }
        if (content.length() < 2) {
            showReplyError("Reply must be at least 2 characters.");
            return;
        }

        postBtn.setDisable(true);
        postBtn.setText("Posting...");
        hideReplyError();

        final Integer parentId = replyParentId;
        final boolean isAnswer = markAnswerBox.isSelected();
        final Set<Integer> excludedSnapshot = new HashSet<>(excludedUserIds);

        new Thread(() -> {
            try {
                JsonObject payload = new JsonObject();
                payload.addProperty("content", content);
                payload.addProperty("is_answer", isAnswer);
                if (parentId != null) payload.addProperty("parent_message_id", parentId);
                if (!excludedSnapshot.isEmpty()) {
                    JsonArray excludedArr = new JsonArray();
                    for (int id : excludedSnapshot) excludedArr.add(id);
                    payload.add("excluded_users", excludedArr);
                }

                JsonObject resp = ApiService.getInstance().post("/topics/" + topicId + "/messages", payload);

                Platform.runLater(() -> {
                    replyInput.clear();
                    markAnswerBox.setSelected(false);
                    replyParentId = null;
                    replyParentLabel.setVisible(false);
                    replyParentLabel.setManaged(false);
                    excludedUserIds.clear();
                    excludeSummaryLabel.setVisible(false);
                    excludeSummaryLabel.setManaged(false);
                    loadTopic();
                });
            } catch (Exception ex) {
                Platform.runLater(() -> showReplyError("Could not post reply: " + ex.getMessage()));
            } finally {
                Platform.runLater(() -> {
                    postBtn.setDisable(false);
                    postBtn.setText("Post Reply");
                });
            }
        }).start();
    }

    /** Requirement #6: export this topic + all its replies to a real PDF file. */
    private void doExportPdf() {
        if (currentTopic == null) return;

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export discussion to PDF");
        String suggested = (currentTopic.title != null ? currentTopic.title : "topic")
                .replaceAll("[^a-zA-Z0-9-_ ]", "").trim().replace(" ", "_");
        chooser.setInitialFileName((suggested.isEmpty() ? "topic" : suggested) + ".pdf");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF files", "*.pdf"));

        File dest = chooser.showSaveDialog(topicCardRef.getScene().getWindow());
        if (dest == null) return;

        final Topic topicSnapshot = currentTopic;
        final List<Message> messagesSnapshot = new ArrayList<>(currentMessages);

        new Thread(() -> {
            try {
                PdfExportService.export(topicSnapshot, messagesSnapshot, dest);
                // Best-effort audit call to the server; export itself already succeeded locally.
                try { ApiService.getInstance().post("/topics/" + topicId + "/export-pdf"); } catch (Exception ignored) {}

                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Exported");
                    alert.setHeaderText("Discussion exported to PDF");
                    alert.setContentText(dest.getAbsolutePath());
                    ButtonType openBtn = new ButtonType("Open file");
                    alert.getButtonTypes().add(openBtn);
                    alert.showAndWait().ifPresent(bt -> {
                        if (bt == openBtn) {
                            try { Desktop.getDesktop().open(dest); } catch (Exception ignored) {}
                        }
                    });
                });
            } catch (Exception ex) {
                Platform.runLater(() -> showReplyError("Could not export PDF: " + ex.getMessage()));
            }
        }).start();
    }

    /** Requirement #12: forward this post to a social media platform of choice. */
    private void doShare() {
        if (currentTopic == null) return;

        ChoiceDialog<String> dialog = new ChoiceDialog<>("twitter", List.of("twitter", "facebook", "linkedin"));
        dialog.setTitle("Share to social media");
        dialog.setHeaderText("Forward \"" + currentTopic.title + "\" to:");
        dialog.setContentText("Platform:");
        dialog.showAndWait().ifPresent(platform -> new Thread(() -> {
            try {
                JsonObject payload = new JsonObject();
                payload.addProperty("platform", platform);
                JsonObject resp = ApiService.getInstance().post("/topics/" + topicId + "/share", payload);
                if (resp.has("success") && resp.get("success").getAsBoolean() && resp.has("share_url")) {
                    String shareUrl = resp.get("share_url").getAsString();
                    Platform.runLater(() -> {
                        try {
                            Desktop.getDesktop().browse(new URI(shareUrl));
                        } catch (Exception ex) {
                            showReplyError("Could not open browser: " + ex.getMessage());
                        }
                    });
                } else {
                    Platform.runLater(() -> showReplyError("Could not build share link."));
                }
            } catch (Exception ex) {
                Platform.runLater(() -> showReplyError("Sharing requires an internet connection."));
            }
        }).start());
    }

    /** Toggles a like on the topic itself and updates the label in place. */
    private void doToggleTopicLike(Label likesLbl) {
        if (currentTopic == null) return;
        new Thread(() -> {
            try {
                JsonObject resp = ApiService.getInstance().post("/topics/" + topicId + "/like");
                if (resp.has("success") && resp.get("success").getAsBoolean()) {
                    int likes = resp.get("likes").getAsInt();
                    currentTopic.likes = likes;
                    Platform.runLater(() -> likesLbl.setText("👍 " + likes));
                }
            } catch (Exception ex) {
                Platform.runLater(() -> showReplyError("Could not update like — check your connection."));
            }
        }).start();
    }

    /** Toggles a like on a single reply and updates the label in place. */
    private void doToggleMessageLike(int messageId, Label likesLbl) {
        new Thread(() -> {
            try {
                JsonObject resp = ApiService.getInstance().post("/messages/" + messageId + "/like");
                if (resp.has("success") && resp.get("success").getAsBoolean()) {
                    int likes = resp.get("likes").getAsInt();
                    Platform.runLater(() -> likesLbl.setText("👍 " + likes));
                }
            } catch (Exception ex) {
                Platform.runLater(() -> showReplyError("Could not update like — check your connection."));
            }
        }).start();
    }

    /** Requirement (Forum Thread screen objects): Report action on a topic or reply. */
    private void doReport(String path) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Report content");
        dialog.setHeaderText("Why are you reporting this?");
        dialog.setContentText("Reason:");
        dialog.showAndWait().ifPresent(reason -> {
            if (reason.isBlank()) return;
            new Thread(() -> {
                try {
                    JsonObject payload = new JsonObject();
                    payload.addProperty("reason", reason.trim());
                    ApiService.getInstance().post("/" + path + "/report", payload);
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Report submitted for review.", ButtonType.OK);
                        alert.setTitle("Reported");
                        alert.showAndWait();
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> showReplyError("Could not submit report — check your connection."));
                }
            }).start();
        });
    }

    private void doMarkAnswer(int messageId) {
        new Thread(() -> {
            try {
                ApiService.getInstance().post("/messages/" + messageId + "/mark-answer");
                Platform.runLater(this::loadTopic);
            } catch (Exception ex) {
                Platform.runLater(() -> showReplyError("Could not mark as answer."));
            }
        }).start();
    }

    private void showReplyError(String msg) {
        replyError.setText(msg);
        replyError.setVisible(true);
        replyError.setManaged(true);
    }

    private void hideReplyError() {
        replyError.setVisible(false);
        replyError.setManaged(false);
    }
}
