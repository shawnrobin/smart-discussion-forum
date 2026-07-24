package com.smartforum.ui;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.smartforum.model.Message;
import com.smartforum.model.Topic;
import com.smartforum.model.User;
import com.smartforum.service.ApiService;
import com.smartforum.service.RealtimeService;
import com.smartforum.service.SessionManager;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.awt.Desktop;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

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
    private final String realtimeChannel; // private-topic.{topicId}

    public TopicDetailView(MainView shell, int groupId, String groupName, int topicId) {
        this.shell = shell;
        this.groupId = groupId;
        this.groupName = groupName;
        this.topicId = topicId;
        this.realtimeChannel = "private-topic." + topicId;
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
        subscribeRealtime();
        return scroll;
    }

    /** Subscribe to live reply/like events from Reverb. */
    private void subscribeRealtime() {
        RealtimeService rt = RealtimeService.getInstance();
        // New reply pushed from the server
        rt.subscribe(realtimeChannel, "App\\Events\\ReplyCreated", data -> {
            Platform.runLater(this::loadTopic);
        });
        // Like count updated
        rt.subscribe(realtimeChannel, "App\\Events\\LikeUpdated", data -> {
            Platform.runLater(this::loadTopic);
        });
    }

    /** Unsubscribe when navigating away (called by MainView). */
    public void cleanup() {
        RealtimeService.getInstance().unsubscribe(realtimeChannel);
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
                Platform.runLater(() -> {
                    if (currentTopic != null) renderTopicCard(topicCardRef, currentTopic);
                    renderReplies(finalMessages);
                });
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
        int replyCount = t.messages != null ? t.messages.size() : 0;
        Label repliesLbl = new Label("💬 " + replyCount + " replies");
        repliesLbl.setStyle("-fx-font-size:13px;-fx-text-fill:#6b7280;");

        // Social sharing buttons (Gap 13)
        Button shareBtn = new Button("🔗 Share");
        shareBtn.setStyle("-fx-background-color:transparent;-fx-text-fill:#6b7280;-fx-font-size:12px;-fx-cursor:hand;-fx-border-color:#e5e7eb;-fx-border-radius:6;-fx-background-radius:6;-fx-padding:4 10 4 10;");
        shareBtn.setOnAction(ev -> showShareDialog(t.title));

        actionBar.getChildren().addAll(likesLbl, repliesLbl, UiHelper.hSpacer(), shareBtn);

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

        Button replyBtn = new Button("↩ Reply");
        replyBtn.setStyle("-fx-background-color:transparent;-fx-text-fill:#6b7280;-fx-font-size:12px;-fx-cursor:hand;-fx-padding:0;");
        replyBtn.setOnAction(ev -> setReplyParent(m));

        // Mark as answer (topic owner or admin/lecturer)
        User currentUser = SessionManager.getInstance().getCurrentUser();
        boolean canMark = currentUser != null && currentTopic != null &&
                (currentTopic.userId == currentUser.id || currentUser.isAdmin() || currentUser.isLecturer());
        if (canMark && !m.isAnswer) {
            Button markBtn = new Button("✓ Mark Answer");
            markBtn.setStyle("-fx-background-color:transparent;-fx-text-fill:#16a34a;-fx-font-size:12px;-fx-cursor:hand;-fx-padding:0;");
            markBtn.setOnAction(ev -> doMarkAnswer(m.id));
            actionRow.getChildren().addAll(likesLbl, replyBtn, markBtn);
        } else {
            actionRow.getChildren().addAll(likesLbl, replyBtn);
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

        form.getChildren().addAll(formTitle, replyParentLabel, replyInput, replyError, bottomRow);
        return form;
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

        new Thread(() -> {
            try {
                JsonObject payload = new JsonObject();
                payload.addProperty("content", content);
                payload.addProperty("is_answer", isAnswer);
                if (parentId != null) payload.addProperty("parent_message_id", parentId);

                JsonObject resp = ApiService.getInstance().post("/topics/" + topicId + "/messages", payload);

                Platform.runLater(() -> {
                    replyInput.clear();
                    markAnswerBox.setSelected(false);
                    replyParentId = null;
                    replyParentLabel.setVisible(false);
                    replyParentLabel.setManaged(false);
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

    /** Social sharing dialog — mirrors Topics/Show.vue share modal (Gap 13). */
    private void showShareDialog(String title) {
        String encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8);
        // Build a shareable web URL pointing to the topic on the web app
        String topicUrl = "https://smart-discussion-forum-x0vd.onrender.com/groups/" + groupId + "/topics/" + topicId;
        String encodedUrl  = URLEncoder.encode(topicUrl, StandardCharsets.UTF_8);

        String[][] platforms = {
            {"Twitter / X",  "https://twitter.com/intent/tweet?text=" + encodedTitle + "%20" + encodedUrl},
            {"Facebook",     "https://www.facebook.com/sharer/sharer.php?u=" + encodedUrl},
            {"LinkedIn",     "https://www.linkedin.com/sharing/share-offsite/?url=" + encodedUrl},
            {"WhatsApp",     "https://wa.me/?text=" + encodedTitle + "%20" + encodedUrl},
        };

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Share Topic");
        dialog.setHeaderText("Share \"" + (title.length() > 50 ? title.substring(0, 50) + "…" : title) + "\"");

        VBox content = new VBox(8);
        content.setPadding(new Insets(8));
        content.setMinWidth(320);

        Label sub = new Label("Open on social media:");
        sub.setStyle("-fx-text-fill:#6b7280;-fx-font-size:12px;");
        content.getChildren().add(sub);

        for (String[] p : platforms) {
            Button btn = new Button(p[0]);
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setStyle("-fx-background-color:white;-fx-border-color:#e5e7eb;-fx-border-radius:8;" +
                         "-fx-background-radius:8;-fx-font-size:13px;-fx-padding:10 16 10 16;" +
                         "-fx-cursor:hand;-fx-alignment:CENTER_LEFT;");
            btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color:#f9fafb;-fx-border-color:#d1d5db;" +
                                                    "-fx-border-radius:8;-fx-background-radius:8;-fx-font-size:13px;" +
                                                    "-fx-padding:10 16 10 16;-fx-cursor:hand;-fx-alignment:CENTER_LEFT;"));
            btn.setOnMouseExited(e  -> btn.setStyle("-fx-background-color:white;-fx-border-color:#e5e7eb;" +
                                                    "-fx-border-radius:8;-fx-background-radius:8;-fx-font-size:13px;" +
                                                    "-fx-padding:10 16 10 16;-fx-cursor:hand;-fx-alignment:CENTER_LEFT;"));
            final String url = p[1];
            btn.setOnAction(e -> {
                try {
                    if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                        Desktop.getDesktop().browse(new URI(url));
                    } else {
                        // Fallback: copy URL to clipboard
                        javafx.scene.input.Clipboard cb = javafx.scene.input.Clipboard.getSystemClipboard();
                        javafx.scene.input.ClipboardContent cc = new javafx.scene.input.ClipboardContent();
                        cc.putString(topicUrl);
                        cb.setContent(cc);
                        showAlert("Copied", "Link copied to clipboard.");
                    }
                } catch (Exception ex) {
                    showAlert("Error", "Could not open browser: " + ex.getMessage());
                }
                dialog.close();
            });
            content.getChildren().add(btn);
        }

        // Copy link button
        Button copyBtn = UiHelper.secondaryButton("📋 Copy Link");
        copyBtn.setMaxWidth(Double.MAX_VALUE);
        copyBtn.setOnAction(e -> {
            javafx.scene.input.Clipboard cb = javafx.scene.input.Clipboard.getSystemClipboard();
            javafx.scene.input.ClipboardContent cc = new javafx.scene.input.ClipboardContent();
            cc.putString(topicUrl);
            cb.setContent(cc);
            copyBtn.setText("✓ Copied!");
        });
        content.getChildren().add(copyBtn);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setTitle(title);
        a.showAndWait();
    }
}
