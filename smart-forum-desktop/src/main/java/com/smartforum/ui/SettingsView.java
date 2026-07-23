package com.smartforum.ui;

import com.google.gson.JsonObject;
import com.smartforum.model.User;
import com.smartforum.service.ApiService;
import com.smartforum.service.SessionManager;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * Settings screen — mirrors Settings/Index.vue.
 * Tab sidebar + tab content panel for Profile, Account, Notifications, Danger Zone.
 */
public class SettingsView {

    private final MainView shell;
    private VBox contentPane;
    private String activeTab = "profile";
    private VBox tabButtonsBox;

    public SettingsView(MainView shell) {
        this.shell = shell;
    }

    public Node build() {
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("scroll-pane");

        VBox root = new VBox(24);
        root.setPadding(new Insets(24));

        HBox layout = new HBox(24);
        layout.setFillHeight(false);

        // ── Tab sidebar ───────────────────────────────────────────
        tabButtonsBox = new VBox(4);
        tabButtonsBox.setPrefWidth(200);
        tabButtonsBox.setMinWidth(200);
        tabButtonsBox.setMaxWidth(200);

        String[] tabIds = { "profile", "account", "notifications", "privacy", "danger" };
        String[] tabLabels = { "Profile", "Account", "Notifications", "Privacy", "Danger Zone" };

        for (int i = 0; i < tabIds.length; i++) {
            final String id = tabIds[i];
            Button btn = new Button(tabLabels[i]);
            btn.setId("tab-btn-" + id);
            btn.getStyleClass().add("settings-tab");
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setOnAction(e -> switchTab(id));
            tabButtonsBox.getChildren().add(btn);
        }

        // ── Content panel ─────────────────────────────────────────
        contentPane = new VBox(20);
        contentPane.getStyleClass().add("card");
        contentPane.setPadding(new Insets(28, 32, 28, 32));
        HBox.setHgrow(contentPane, Priority.ALWAYS);

        layout.getChildren().addAll(tabButtonsBox, contentPane);
        root.getChildren().add(layout);
        scroll.setContent(root);

        switchTab("profile");
        return scroll;
    }

    private void switchTab(String tab) {
        activeTab = tab;

        // Update tab button styles
        tabButtonsBox.getChildren().forEach(n -> {
            n.getStyleClass().removeAll("settings-tab-active");
            if (n.getId() != null && n.getId().equals("tab-btn-" + tab)) {
                n.getStyleClass().add("settings-tab-active");
            }
        });

        contentPane.getChildren().clear();
        switch (tab) {
            case "profile"       -> contentPane.getChildren().add(buildProfileTab());
            case "account"       -> contentPane.getChildren().add(buildAccountTab());
            case "notifications" -> contentPane.getChildren().add(buildNotificationsTab());
            case "privacy"       -> contentPane.getChildren().add(buildPrivacyTab());
            case "danger"        -> contentPane.getChildren().add(buildDangerTab());
        }
    }

    // ── Profile tab ───────────────────────────────────────────────
    private Node buildProfileTab() {
        VBox box = new VBox(16);
        Label title = new Label("Profile");
        title.setStyle("-fx-font-size:18px;-fx-font-weight:bold;-fx-text-fill:#111827;");

        User user = SessionManager.getInstance().getCurrentUser();

        // Profile header
        HBox profileHead = new HBox(16);
        profileHead.setAlignment(Pos.CENTER_LEFT);
        StackPane avatar = UiHelper.orangeAvatar(user != null ? user.getInitials() : "??");
        VBox nameBlock = new VBox(2);
        Label nameLbl = new Label(user != null ? user.name : "");
        nameLbl.setStyle("-fx-font-size:20px;-fx-font-weight:bold;");
        Label emailLbl = new Label(user != null ? user.email : "");
        emailLbl.getStyleClass().add("muted-text");
        nameBlock.getChildren().addAll(nameLbl, emailLbl);
        profileHead.getChildren().addAll(avatar, nameBlock);

        Separator sep = new Separator();

        Label formTitle = new Label("Edit Profile");
        formTitle.setStyle("-fx-font-size:16px;-fx-font-weight:bold;-fx-text-fill:#111827;");

        // Form fields
        TextField nameField = new TextField(user != null ? user.name : "");
        nameField.getStyleClass().add("text-field");

        TextField usernameField = new TextField(user != null && user.username != null ? user.username : "");
        usernameField.getStyleClass().add("text-field");

        TextArea bioField = new TextArea(user != null && user.bio != null ? user.bio : "");
        bioField.getStyleClass().add("text-area");
        bioField.setPrefRowCount(3);
        bioField.setWrapText(true);

        HBox academicRow = new HBox(12);
        TextField courseField = new TextField(user != null && user.course != null ? user.course : "");
        courseField.getStyleClass().add("text-field");
        courseField.setPromptText("Course");
        TextField facultyField = new TextField(user != null && user.faculty != null ? user.faculty : "");
        facultyField.getStyleClass().add("text-field");
        facultyField.setPromptText("Faculty");
        HBox.setHgrow(courseField, Priority.ALWAYS);
        HBox.setHgrow(facultyField, Priority.ALWAYS);
        academicRow.getChildren().addAll(courseField, facultyField);

        TextField yearField = new TextField(user != null ? String.valueOf(user.yearOfStudy) : "");
        yearField.getStyleClass().add("text-field");
        yearField.setPromptText("Year of study");

        Label savedLbl = new Label("Saved successfully.");
        savedLbl.setStyle("-fx-text-fill:#16a34a;-fx-font-size:13px;");
        savedLbl.setVisible(false);
        savedLbl.setManaged(false);

        Label errLbl = new Label();
        errLbl.getStyleClass().add("error-text");
        errLbl.setVisible(false);
        errLbl.setManaged(false);

        Button saveBtn = UiHelper.primaryButton("Save Changes");
        saveBtn.setOnAction(e -> {
            saveBtn.setDisable(true);
            saveBtn.setText("Saving...");
            new Thread(() -> {
                try {
                    JsonObject payload = new JsonObject();
                    payload.addProperty("name", nameField.getText().trim());
                    payload.addProperty("username", usernameField.getText().trim());
                    payload.addProperty("bio", bioField.getText().trim());
                    payload.addProperty("course", courseField.getText().trim());
                    payload.addProperty("faculty", facultyField.getText().trim());
                    payload.addProperty("year_of_study", yearField.getText().trim());
                    ApiService.getInstance().post("/profile", payload);
                    Platform.runLater(() -> {
                        savedLbl.setVisible(true);
                        savedLbl.setManaged(true);
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        errLbl.setText("Could not save: " + ex.getMessage());
                        errLbl.setVisible(true);
                        errLbl.setManaged(true);
                    });
                } finally {
                    Platform.runLater(() -> {
                        saveBtn.setDisable(false);
                        saveBtn.setText("Save Changes");
                    });
                }
            }).start();
        });

        box.getChildren().addAll(
                title, profileHead, sep, formTitle,
                UiHelper.formLabel("Full Name"), nameField,
                UiHelper.formLabel("Username"), usernameField,
                UiHelper.formLabel("Bio"), bioField,
                UiHelper.formLabel("Academic Details"), academicRow,
                UiHelper.formLabel("Year of Study"), yearField,
                errLbl, saveBtn, savedLbl
        );
        return box;
    }

    // ── Account tab ───────────────────────────────────────────────
    private Node buildAccountTab() {
        VBox box = new VBox(16);
        Label title = new Label("Account");
        title.setStyle("-fx-font-size:18px;-fx-font-weight:bold;-fx-text-fill:#111827;");

        User user = SessionManager.getInstance().getCurrentUser();

        TextField emailField = new TextField(user != null ? user.email : "");
        emailField.getStyleClass().add("text-field");

        Separator sep = new Separator();
        Label pwTitle = new Label("Change Password");
        pwTitle.setStyle("-fx-font-size:14px;-fx-font-weight:600;");
        Label pwSub = new Label("Leave blank if you don't want to change your password.");
        pwSub.getStyleClass().add("muted-text");

        PasswordField currentPw = new PasswordField();
        currentPw.setPromptText("Current password");
        currentPw.getStyleClass().add("password-field");

        PasswordField newPw = new PasswordField();
        newPw.setPromptText("New password");
        newPw.getStyleClass().add("password-field");

        PasswordField confirmPw = new PasswordField();
        confirmPw.setPromptText("Confirm new password");
        confirmPw.getStyleClass().add("password-field");

        Label savedLbl = new Label("Saved successfully.");
        savedLbl.setStyle("-fx-text-fill:#16a34a;-fx-font-size:13px;");
        savedLbl.setVisible(false);
        savedLbl.setManaged(false);

        Label errLbl = new Label();
        errLbl.getStyleClass().add("error-text");
        errLbl.setVisible(false);
        errLbl.setManaged(false);

        Button saveBtn = UiHelper.primaryButton("Save Changes");
        saveBtn.setOnAction(e -> {
            saveBtn.setDisable(true);
            saveBtn.setText("Saving...");
            new Thread(() -> {
                try {
                    JsonObject payload = new JsonObject();
                    payload.addProperty("email", emailField.getText().trim());
                    if (!currentPw.getText().isEmpty()) {
                        payload.addProperty("current_password", currentPw.getText());
                        payload.addProperty("new_password", newPw.getText());
                        payload.addProperty("new_password_confirmation", confirmPw.getText());
                    }
                    ApiService.getInstance().post("/profile", payload);
                    Platform.runLater(() -> {
                        savedLbl.setVisible(true);
                        savedLbl.setManaged(true);
                        currentPw.clear();
                        newPw.clear();
                        confirmPw.clear();
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        errLbl.setText("Could not save: " + ex.getMessage());
                        errLbl.setVisible(true);
                        errLbl.setManaged(true);
                    });
                } finally {
                    Platform.runLater(() -> { saveBtn.setDisable(false); saveBtn.setText("Save Changes"); });
                }
            }).start();
        });

        box.getChildren().addAll(
                title,
                UiHelper.formLabel("Email"), emailField,
                sep, pwTitle, pwSub,
                UiHelper.formLabel("Current Password"), currentPw,
                UiHelper.formLabel("New Password"), newPw,
                UiHelper.formLabel("Confirm New Password"), confirmPw,
                errLbl, saveBtn, savedLbl
        );
        return box;
    }

    // ── Notifications tab ─────────────────────────────────────────
    private Node buildNotificationsTab() {
        VBox box = new VBox(16);
        Label title = new Label("Notifications");
        title.setStyle("-fx-font-size:18px;-fx-font-weight:bold;-fx-text-fill:#111827;");
        Label sub = new Label("Choose which notifications should pop up on your screen.");
        sub.getStyleClass().add("muted-text");

        CheckBox replies = toggleRow("New replies");
        CheckBox mentions = toggleRow("Mentions (@username)");
        mentions.setSelected(true);
        CheckBox groupPosts = toggleRow("New group posts");
        groupPosts.setSelected(true);
        CheckBox quizAlerts = toggleRow("Quiz announcements");
        quizAlerts.setSelected(true);
        CheckBox emailNotif = toggleRow("Email notifications");

        replies.setSelected(true);

        Label savedLbl = new Label("Saved.");
        savedLbl.setStyle("-fx-text-fill:#16a34a;-fx-font-size:13px;");
        savedLbl.setVisible(false);
        savedLbl.setManaged(false);

        Button saveBtn = UiHelper.primaryButton("Save Changes");
        saveBtn.setOnAction(e -> {
            savedLbl.setVisible(true);
            savedLbl.setManaged(true);
        });

        box.getChildren().addAll(title, sub, replies, mentions, groupPosts, quizAlerts, emailNotif, saveBtn, savedLbl);
        return box;
    }

    private CheckBox toggleRow(String label) {
        CheckBox cb = new CheckBox(label);
        cb.setStyle("-fx-font-size:13px;-fx-text-fill:#374151;");
        return cb;
    }

    // ── Privacy tab ───────────────────────────────────────────────
    private Node buildPrivacyTab() {
        VBox box = new VBox(16);
        Label title = new Label("Privacy");
        title.setStyle("-fx-font-size:18px;-fx-font-weight:bold;-fx-text-fill:#111827;");

        Label visLabel = UiHelper.formLabel("Profile Visibility");
        ToggleGroup visGroup = new ToggleGroup();
        RadioButton publicBtn = new RadioButton("Public");
        RadioButton privateBtn = new RadioButton("Private");
        publicBtn.setToggleGroup(visGroup);
        privateBtn.setToggleGroup(visGroup);
        publicBtn.setSelected(true);
        publicBtn.setStyle("-fx-font-size:13px;");
        privateBtn.setStyle("-fx-font-size:13px;");
        HBox visRow = new HBox(16, publicBtn, privateBtn);

        CheckBox onlineStatus = new CheckBox("Show online status");
        onlineStatus.setSelected(true);
        onlineStatus.setStyle("-fx-font-size:13px;");

        Label msgLabel = UiHelper.formLabel("Who can message me");
        ComboBox<String> msgBox = new ComboBox<>();
        msgBox.getItems().addAll("Everyone", "Group members only", "Nobody");
        msgBox.setValue("Everyone");
        msgBox.getStyleClass().add("combo-box");

        Label savedLbl = new Label("Saved.");
        savedLbl.setStyle("-fx-text-fill:#16a34a;-fx-font-size:13px;");
        savedLbl.setVisible(false);
        savedLbl.setManaged(false);

        Button saveBtn = UiHelper.primaryButton("Save Changes");
        saveBtn.setOnAction(e -> { savedLbl.setVisible(true); savedLbl.setManaged(true); });

        box.getChildren().addAll(title, visLabel, visRow, onlineStatus, msgLabel, msgBox, saveBtn, savedLbl);
        return box;
    }

    // ── Danger Zone tab ───────────────────────────────────────────
    private Node buildDangerTab() {
        VBox box = new VBox(20);
        Label title = new Label("Danger Zone");
        title.setStyle("-fx-font-size:18px;-fx-font-weight:bold;-fx-text-fill:#e11d48;");

        // Deactivate
        VBox deactivateBox = new VBox(10);
        deactivateBox.setStyle("-fx-border-color:#fde047;-fx-border-radius:12;-fx-background-color:#fffbeb;-fx-background-radius:12;-fx-padding:16;");
        Label deactivateTitle = new Label("Deactivate Account");
        deactivateTitle.setStyle("-fx-font-weight:600;-fx-text-fill:#111827;-fx-font-size:14px;");
        Label deactivateSub = new Label("Your account will be hidden until you log back in.");
        deactivateSub.getStyleClass().add("muted-text");
        PasswordField deactivatePw = new PasswordField();
        deactivatePw.setPromptText("Enter your password");
        deactivatePw.getStyleClass().add("password-field");
        Button deactivateBtn = new Button("Deactivate Account");
        deactivateBtn.setStyle("-fx-background-color:#d97706;-fx-text-fill:white;-fx-font-weight:bold;-fx-background-radius:10;-fx-padding:8 16 8 16;-fx-cursor:hand;");
        deactivateBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Are you sure you want to deactivate your account?", ButtonType.YES, ButtonType.CANCEL);
            confirm.showAndWait();
        });
        deactivateBox.getChildren().addAll(deactivateTitle, deactivateSub, deactivatePw, deactivateBtn);

        // Delete
        VBox deleteBox = new VBox(10);
        deleteBox.setStyle("-fx-border-color:#fecaca;-fx-border-radius:12;-fx-background-color:#fff1f2;-fx-background-radius:12;-fx-padding:16;");
        Label deleteTitle = new Label("Delete Account");
        deleteTitle.setStyle("-fx-font-weight:600;-fx-text-fill:#111827;-fx-font-size:14px;");
        Label deleteSub = new Label("This permanently deletes your account and all your data. This cannot be undone.");
        deleteSub.setStyle("-fx-text-fill:#6b7280;-fx-font-size:12px;-fx-wrap-text:true;");
        deleteSub.setWrapText(true);
        PasswordField deletePw = new PasswordField();
        deletePw.setPromptText("Enter your password");
        deletePw.getStyleClass().add("password-field");
        Button deleteBtn = UiHelper.dangerButton("Delete Account Permanently");
        deleteBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "This will PERMANENTLY delete your account. Are you absolutely sure?",
                    ButtonType.YES, ButtonType.CANCEL);
            confirm.setTitle("Permanent Deletion");
            confirm.showAndWait();
        });
        deleteBox.getChildren().addAll(deleteTitle, deleteSub, deletePw, deleteBtn);

        box.getChildren().addAll(title, deactivateBox, deleteBox);
        return box;
    }
}
