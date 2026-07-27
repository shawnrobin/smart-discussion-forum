package com.smartforum.ui;

import com.google.gson.JsonObject;
import com.smartforum.SmartForumApp;
import com.smartforum.model.User;
import com.smartforum.service.ApiService;
import com.smartforum.service.SessionManager;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 * Login screen — mirrors the web GuestLayout + Login.vue.
 * Matches the app's orange brand palette (see app.css), with the
 * same logo mark used in the sidebar.
 */
public class LoginView {

    private Label errorLabel;
    private TextField emailField;
    private PasswordField passwordField;
    private Button signInBtn;
    private Hyperlink registerLink;
    private boolean showingRegister = false;

    // Register fields
    private TextField nameField;
    private TextField usernameField;
    private PasswordField confirmPasswordField;
    private VBox registerForm;
    private VBox loginForm;

    public Pane build() {
        StackPane root = new StackPane();
        root.getStyleClass().add("login-root");

        VBox centerBox = new VBox(16);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setMaxWidth(440);

        // ── Header ──────────────────────────────────────────────
        VBox header = new VBox(8);
        header.setAlignment(Pos.CENTER);

        HBox logoRow = new HBox(10);
        logoRow.setAlignment(Pos.CENTER);

        Region logoSquare = new Region();
        logoSquare.getStyleClass().add("logo-square");
        StackPane logoMark = new StackPane(logoSquare);
        Label logoLetter = new Label("S");
        logoLetter.getStyleClass().add("avatar-text");
        logoMark.getChildren().add(logoLetter);

        Label brandTitle = new Label("Smart Forum");
        brandTitle.getStyleClass().add("login-title");

        logoRow.getChildren().addAll(logoMark, brandTitle);

        Label brandSubtitle = new Label("Academic Discussion Platform");
        brandSubtitle.getStyleClass().add("login-subtitle");

        header.getChildren().addAll(logoRow, brandSubtitle);

        // ── Card ─────────────────────────────────────────────────
        VBox card = new VBox(16);
        card.getStyleClass().add("login-card");
        card.setAlignment(Pos.TOP_LEFT);

        // Login form
        loginForm = buildLoginForm();
        // Register form (hidden initially)
        registerForm = buildRegisterForm();
        registerForm.setVisible(false);
        registerForm.setManaged(false);

        card.getChildren().addAll(loginForm, registerForm);

        // ── Footer ──────────────────────────────────────────────
        Label footer = new Label("© 2026 Smart Discussion Forum. All rights reserved.");
        footer.getStyleClass().add("login-footer");
        footer.setAlignment(Pos.CENTER);

        centerBox.getChildren().addAll(header, card, footer);
        StackPane.setAlignment(centerBox, Pos.CENTER);
        root.getChildren().add(centerBox);

        return root;
    }

    private VBox buildLoginForm() {
        VBox form = new VBox(14);

        Label title = new Label("Sign In");
        title.getStyleClass().add("login-form-title");

        errorLabel = new Label();
        errorLabel.getStyleClass().add("error-text");
        errorLabel.setWrapText(true);
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        // Email
        VBox emailBox = new VBox(4);
        emailBox.getChildren().addAll(UiHelper.formLabel("Email Address"), emailField = styledTextField("your@email.com", false));

        // Password
        VBox passBox = new VBox(4);
        passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.getStyleClass().add("password-field");
        passBox.getChildren().addAll(UiHelper.formLabel("Password"), passwordField);

        signInBtn = UiHelper.primaryButton("Sign In");
        signInBtn.setMaxWidth(Double.MAX_VALUE);
        signInBtn.setOnAction(e -> doLogin());
        passwordField.setOnAction(e -> doLogin());

        // Switch to register
        HBox switchRow = new HBox(4);
        switchRow.setAlignment(Pos.CENTER);
        Label noAccount = new Label("Don't have an account?");
        noAccount.getStyleClass().add("muted-text");
        registerLink = new Hyperlink("Create one here");
        registerLink.getStyleClass().add("btn-link");
        registerLink.setOnAction(e -> toggleRegister(true));
        switchRow.getChildren().addAll(noAccount, registerLink);

        form.getChildren().addAll(title, errorLabel, emailBox, passBox, signInBtn, switchRow);
        return form;
    }

    private VBox buildRegisterForm() {
        VBox form = new VBox(14);

        Label title = new Label("Create Account");
        title.getStyleClass().add("login-form-title");

        Label regError = new Label();
        regError.getStyleClass().add("error-text");
        regError.setWrapText(true);
        regError.setId("reg-error");
        regError.setVisible(false);
        regError.setManaged(false);

        // Fields
        VBox nameBox = new VBox(4);
        nameField = styledTextField("John Doe", false);
        nameBox.getChildren().addAll(UiHelper.formLabel("Full Name"), nameField);

        VBox emailBox2 = new VBox(4);
        TextField regEmail = styledTextField("your@email.com", false);
        regEmail.setId("reg-email");
        emailBox2.getChildren().addAll(UiHelper.formLabel("Email Address"), regEmail);

        VBox userBox = new VBox(4);
        usernameField = styledTextField("Choose a username", false);
        userBox.getChildren().addAll(UiHelper.formLabel("Username"), usernameField);

        VBox passBox2 = new VBox(4);
        PasswordField regPass = new PasswordField();
        regPass.setPromptText("Enter strong password (min 8)");
        regPass.getStyleClass().add("password-field");
        regPass.setId("reg-password");
        passBox2.getChildren().addAll(UiHelper.formLabel("Password"), regPass);

        VBox confBox = new VBox(4);
        confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm your password");
        confirmPasswordField.getStyleClass().add("password-field");
        confBox.getChildren().addAll(UiHelper.formLabel("Confirm Password"), confirmPasswordField);

        Button createBtn = UiHelper.primaryButton("Create Account");
        createBtn.setMaxWidth(Double.MAX_VALUE);
        createBtn.setOnAction(e -> doRegister(regEmail, regPass, regError, createBtn));

        HBox backRow = new HBox(4);
        backRow.setAlignment(Pos.CENTER);
        Label hasAccount = new Label("Already have an account?");
        hasAccount.getStyleClass().add("muted-text");
        Hyperlink signInLink = new Hyperlink("Sign in here");
        signInLink.getStyleClass().add("btn-link");
        signInLink.setOnAction(ev -> toggleRegister(false));
        backRow.getChildren().addAll(hasAccount, signInLink);

        form.getChildren().addAll(title, regError, nameBox, emailBox2, userBox, passBox2, confBox, createBtn, backRow);
        return form;
    }

    private void toggleRegister(boolean show) {
        showingRegister = show;
        loginForm.setVisible(!show);
        loginForm.setManaged(!show);
        registerForm.setVisible(show);
        registerForm.setManaged(show);
    }

    private void doLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Please enter your email and password.");
            return;
        }

        signInBtn.setDisable(true);
        signInBtn.setText("Signing in...");
        hideError();

        new Thread(() -> {
            try {
                JsonObject payload = new JsonObject();
                payload.addProperty("email", email);
                payload.addProperty("password", password);

                JsonObject response = ApiService.getInstance().post("/auth/login", payload);

                if (response.has("success") && response.get("success").getAsBoolean()) {
                    String token = response.get("token").getAsString();
                    User user = ApiService.getInstance().gson.fromJson(response.get("user"), User.class);
                    SessionManager.getInstance().login(token, user);
                    System.out.println("[DEBUG-LOGIN] /auth/login raw user JSON = " + response.get("user"));
                    System.out.println("[DEBUG-LOGIN] parsed user.role = '" + user.role + "'  isAdmin()=" + user.isAdmin());

                    // Fetch bootstrap data
                    JsonObject bootstrap = ApiService.getInstance().get("/desktop/bootstrap");
                    if (bootstrap.has("success") && bootstrap.get("success").getAsBoolean()) {
                        com.smartforum.model.BootstrapData bd = ApiService.getInstance().gson.fromJson(bootstrap, com.smartforum.model.BootstrapData.class);
                        SessionManager.getInstance().setBootstrapData(bd);
                        System.out.println("[DEBUG-LOGIN] /desktop/bootstrap raw user JSON = " + bootstrap.get("user"));
                        System.out.println("[DEBUG-LOGIN] bootstrap user.role = '" + (bd.user != null ? bd.user.role : "null") + "'");
                    }

                    Platform.runLater(SmartForumApp::showMain);
                } else {
                    String msg = response.has("message") ? response.get("message").getAsString() : "Login failed.";
                    Platform.runLater(() -> showError(msg));
                }
            } catch (Exception ex) {
                Platform.runLater(() -> showError("Could not connect to server. Make sure it is running."));
            } finally {
                Platform.runLater(() -> {
                    signInBtn.setDisable(false);
                    signInBtn.setText("Sign In");
                });
            }
        }).start();
    }

    private void doRegister(TextField regEmail, PasswordField regPass, Label regError, Button createBtn) {
        String name = nameField.getText().trim();
        String email = regEmail.getText().trim();
        String username = usernameField.getText().trim();
        String password = regPass.getText();
        String confirm = confirmPasswordField.getText();

        if (name.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty()) {
            showRegError(regError, "All fields are required.");
            return;
        }
        if (!password.equals(confirm)) {
            showRegError(regError, "Passwords do not match.");
            return;
        }
        if (password.length() < 8) {
            showRegError(regError, "Password must be at least 8 characters.");
            return;
        }

        createBtn.setDisable(true);
        createBtn.setText("Creating account...");
        hideRegError(regError);

        new Thread(() -> {
            try {
                // Fetch rules version first
                JsonObject rulesResp = ApiService.getInstance().get("/onboarding/rules");
                int rulesVersion = 1;
                if (rulesResp.has("rules") && rulesResp.get("rules").isJsonObject()) {
                    rulesVersion = rulesResp.get("rules").getAsJsonObject().get("version").getAsInt();
                }

                JsonObject payload = new JsonObject();
                payload.addProperty("name", name);
                payload.addProperty("email", email);
                payload.addProperty("username", username);
                payload.addProperty("password", password);
                payload.addProperty("password_confirmation", confirm);
                payload.addProperty("rules_version", rulesVersion);
                payload.addProperty("agreed_to_rules", true);

                JsonObject response = ApiService.getInstance().post("/auth/register", payload);

                if (response.has("success") && response.get("success").getAsBoolean()) {
                    String token = response.get("token").getAsString();
                    User user = ApiService.getInstance().gson.fromJson(response.get("user"), User.class);
                    SessionManager.getInstance().login(token, user);
                    Platform.runLater(SmartForumApp::showMain);
                } else {
                    String msg = response.has("message") ? response.get("message").getAsString() : "Registration failed.";
                    // Check errors object
                    if (response.has("errors")) {
                        JsonObject errors = response.get("errors").getAsJsonObject();
                        if (errors.has("email")) msg = errors.get("email").getAsJsonArray().get(0).getAsString();
                        else if (errors.has("username")) msg = errors.get("username").getAsJsonArray().get(0).getAsString();
                    }
                    final String finalMsg = msg;
                    Platform.runLater(() -> showRegError(regError, finalMsg));
                }
            } catch (Exception ex) {
                Platform.runLater(() -> showRegError(regError, "Could not connect to server."));
            } finally {
                Platform.runLater(() -> {
                    createBtn.setDisable(false);
                    createBtn.setText("Create Account");
                });
            }
        }).start();
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void hideError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    private void showRegError(Label lbl, String msg) {
        lbl.setText(msg);
        lbl.setVisible(true);
        lbl.setManaged(true);
    }

    private void hideRegError(Label lbl) {
        lbl.setVisible(false);
        lbl.setManaged(false);
    }

    private TextField styledTextField(String prompt, boolean disabled) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.getStyleClass().add("text-field");
        tf.setDisable(disabled);
        return tf;
    }
}