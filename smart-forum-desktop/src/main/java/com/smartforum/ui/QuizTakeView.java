package com.smartforum.ui;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.smartforum.SmartForumApp;
import com.smartforum.model.Quiz;
import com.smartforum.service.ApiService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Map;

/**
 * Quiz-taking screen — mirrors ActiveQuiz.vue.
 * Fullscreen, countdown timer, lockdown warning, submit on timer expiry.
 */
public class QuizTakeView {

    private final MainView shell;
    private final int quizId;

    private Quiz quiz;
    private int attemptId;
    private long deadlineMillis;
    private Label timerLabel;
    private Timeline countdown;
    private VBox questionsBox;
    private Map<Integer, String> answers = new HashMap<>();
    private Button submitBtn;
    private boolean submitting = false;
    private Label warningLabel;
    private int focusLossCount = 0;
    private javafx.beans.value.ChangeListener<Boolean> focusListener;

    public QuizTakeView(MainView shell, int quizId) {
        this.shell = shell;
        this.quizId = quizId;
    }

    public Node build() {
        // Enter fullscreen
        Stage stage = SmartForumApp.primaryStage;
        if (!stage.isFullScreen()) stage.setFullScreen(true);
        stage.setFullScreenExitHint("");
        stage.setFullScreenExitKeyCombination(javafx.scene.input.KeyCombination.NO_MATCH);

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("scroll-pane");
        scroll.setStyle("-fx-background-color:#f8fafc;");

        VBox root = new VBox(16);
        root.setPadding(new Insets(24));
        root.setMaxWidth(900);
        root.setAlignment(Pos.TOP_CENTER);

        // ── Header (timer + title) ─────────────────────────────────
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(12, 0, 12, 0));
        header.setStyle("-fx-border-color:#e5e7eb;-fx-border-width:0 0 1 0;-fx-background-color:#f8fafc;");

        VBox titleBox = new VBox(2);
        Label quizTitle = new Label("Loading quiz...");
        quizTitle.setStyle("-fx-font-size:20px;-fx-font-weight:bold;-fx-text-fill:#111827;");
        quizTitle.setId("quiz-title");
        Label subtitle = new Label("Submit before the timer reaches zero.");
        subtitle.getStyleClass().add("meta-text");
        titleBox.getChildren().addAll(quizTitle, subtitle);

        timerLabel = new Label("0:00");
        timerLabel.getStyleClass().add("quiz-timer");

        HBox.setHgrow(titleBox, Priority.ALWAYS);
        header.getChildren().addAll(titleBox, timerLabel);

        // ── Warning banner (hidden by default) ─────────────────────
        warningLabel = new Label("You left the quiz window. Stay on this page until you submit.");
        warningLabel.setStyle("-fx-background-color:#fef3c7;-fx-border-color:#fde047;-fx-border-radius:10;-fx-background-radius:10;-fx-padding:10 16 10 16;-fx-text-fill:#854d0e;-fx-font-size:13px;");
        warningLabel.setVisible(false);
        warningLabel.setManaged(false);

        // Requirement (Quiz Screen Objects — Lockdown warning): detect the window losing
        // focus (alt-tab / switching apps) and show a persistent, non-dismissible warning.
        focusListener = (obs, wasFocused, isFocused) -> {
            if (!isFocused) {
                focusLossCount++;
                Platform.runLater(() -> {
                    warningLabel.setText("⚠ You left the quiz window (" + focusLossCount + (focusLossCount == 1 ? " time" : " times")
                            + "). This has been recorded. Stay on this page until you submit.");
                    warningLabel.setVisible(true);
                    warningLabel.setManaged(true);
                });
            }
        };
        stage.focusedProperty().addListener(focusListener);

        // ── Questions box ──────────────────────────────────────────
        questionsBox = new VBox(16);
        Label loadingLbl = new Label("Starting quiz...");
        loadingLbl.getStyleClass().add("muted-text");
        questionsBox.getChildren().add(loadingLbl);

        // ── Submit button ──────────────────────────────────────────
        submitBtn = UiHelper.primaryButton("Submit final quiz");
        submitBtn.setMaxWidth(Double.MAX_VALUE);
        submitBtn.setOnAction(e -> submitQuiz());

        root.getChildren().addAll(header, warningLabel, questionsBox, submitBtn);

        // Wrap in StackPane to center
        StackPane container = new StackPane(root);
        container.setStyle("-fx-background-color:#f8fafc;");
        scroll.setContent(container);

        // Start the quiz attempt
        startAttempt();

        return scroll;
    }

    private void startAttempt() {
        new Thread(() -> {
            try {
                JsonObject resp = ApiService.getInstance().post("/quizzes/" + quizId + "/attempt");
                if (!resp.has("success") || !resp.get("success").getAsBoolean()) {
                    Platform.runLater(() -> showError("Could not start quiz."));
                    return;
                }

                attemptId = resp.get("attempt").getAsJsonObject().get("id").getAsInt();
                quiz = ApiService.getInstance().gson.fromJson(resp.get("quiz"), Quiz.class);
                int durationMinutes = resp.get("duration_minutes").getAsInt();

                // Requirement #10: "Those who join late shall not be given extra time."
                // The naive deadline is start + full duration, but a late joiner must not
                // run past the quiz's actual end_time — take whichever is sooner.
                long naiveDeadline = System.currentTimeMillis() + (durationMinutes * 60 * 1000L);
                long deadline = naiveDeadline;
                try {
                    long quizEndMillis = java.time.Instant.parse(quiz.endTime).toEpochMilli();
                    deadline = Math.min(naiveDeadline, quizEndMillis);
                } catch (Exception ignored) {
                    // Fall back to the naive deadline if end_time can't be parsed.
                }
                deadlineMillis = deadline;

                Platform.runLater(() -> {
                    Label titleLbl = (Label) questionsBox.getScene().lookup("#quiz-title");
                    if (titleLbl != null) titleLbl.setText(quiz.title);

                    renderQuestions();
                    startCountdown();
                });
            } catch (Exception e) {
                Platform.runLater(() -> showError("Failed to start quiz: " + e.getMessage()));
            }
        }).start();
    }

    private void renderQuestions() {
        questionsBox.getChildren().clear();
        if (quiz == null || quiz.questions == null) return;

        for (int i = 0; i < quiz.questions.size(); i++) {
            Quiz.QuizQuestion q = quiz.questions.get(i);
            VBox qCard = new VBox(10);
            qCard.getStyleClass().add("card");
            qCard.setPadding(new Insets(16, 20, 16, 20));

            Label qText = new Label((i + 1) + ". " + q.questionText);
            qText.setStyle("-fx-font-weight:600;-fx-text-fill:#111827;-fx-font-size:14px;-fx-wrap-text:true;");
            qText.setWrapText(true);

            Node inputNode;
            if ("multiple_choice".equals(q.questionType)) {
                inputNode = multipleChoiceInput(q);
            } else if ("true_false".equals(q.questionType)) {
                inputNode = trueFalseInput(q);
            } else {
                inputNode = shortAnswerInput(q);
            }

            qCard.getChildren().addAll(qText, inputNode);
            questionsBox.getChildren().add(qCard);
        }
    }

    private Node multipleChoiceInput(Quiz.QuizQuestion q) {
        VBox box = new VBox(8);
        ToggleGroup group = new ToggleGroup();
        for (String opt : q.options) {
            RadioButton rb = new RadioButton(opt);
            rb.setToggleGroup(group);
            rb.setStyle("-fx-font-size:13px;-fx-text-fill:#374151;");
            rb.selectedProperty().addListener((obs, o, n) -> {
                if (n) answers.put(q.id, opt);
            });
            box.getChildren().add(rb);
        }
        return box;
    }

    private Node trueFalseInput(Quiz.QuizQuestion q) {
        HBox box = new HBox(12);
        ToggleGroup group = new ToggleGroup();
        RadioButton trueBtn = new RadioButton("True");
        RadioButton falseBtn = new RadioButton("False");
        trueBtn.setToggleGroup(group);
        falseBtn.setToggleGroup(group);
        trueBtn.setStyle("-fx-font-size:13px;");
        falseBtn.setStyle("-fx-font-size:13px;");
        trueBtn.selectedProperty().addListener((obs, o, n) -> { if (n) answers.put(q.id, "True"); });
        falseBtn.selectedProperty().addListener((obs, o, n) -> { if (n) answers.put(q.id, "False"); });
        box.getChildren().addAll(trueBtn, falseBtn);
        return box;
    }

    private Node shortAnswerInput(Quiz.QuizQuestion q) {
        TextArea ta = new TextArea();
        ta.setPromptText("Write your answer...");
        ta.getStyleClass().add("text-area");
        ta.setPrefRowCount(3);
        ta.setWrapText(true);
        ta.textProperty().addListener((obs, o, n) -> answers.put(q.id, n));
        return ta;
    }

    private void startCountdown() {
        countdown = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateTimer()));
        countdown.setCycleCount(Timeline.INDEFINITE);
        countdown.play();
        updateTimer();
    }

    private void updateTimer() {
        long remaining = deadlineMillis - System.currentTimeMillis();
        if (remaining <= 0) {
            remaining = 0;
            if (!submitting) autoSubmit();
        }
        int seconds = (int) (remaining / 1000);
        int mins = seconds / 60;
        int secs = seconds % 60;
        timerLabel.setText(String.format("%d:%02d", mins, secs));

        // Warning color if < 60s
        if (seconds < 60) {
            timerLabel.getStyleClass().removeAll("quiz-timer", "quiz-timer-warning");
            timerLabel.getStyleClass().add("quiz-timer-warning");
        }
    }

    private void submitQuiz() {
        if (submitting) return;
        submitting = true;
        submitBtn.setDisable(true);
        submitBtn.setText("Submitting...");
        if (countdown != null) countdown.stop();

        new Thread(() -> {
            try {
                JsonObject payload = new JsonObject();
                JsonArray answersArr = new JsonArray();
                for (Quiz.QuizQuestion q : quiz.questions) {
                    JsonObject ans = new JsonObject();
                    ans.addProperty("question_id", q.id);
                    ans.addProperty("user_answer", answers.getOrDefault(q.id, ""));
                    answersArr.add(ans);
                }
                payload.add("answers", answersArr);

                JsonObject resp = ApiService.getInstance().post("/quiz-attempts/" + attemptId + "/submit", payload);

                if (resp.has("success") && resp.get("success").getAsBoolean()) {
                    double score = resp.has("score") ? resp.get("score").getAsDouble() : 0;
                    Platform.runLater(() -> {
                        exitFullscreen();
                        shell.onQuizFinished();
                        showResultAlert(score);
                        shell.navigateTo(MainView.Page.QUIZZES);
                    });
                } else {
                    String msg = resp.has("message") ? resp.get("message").getAsString() : "Failed to submit.";
                    Platform.runLater(() -> showError(msg));
                }
            } catch (Exception ex) {
                Platform.runLater(() -> showError("Could not submit quiz: " + ex.getMessage()));
            } finally {
                Platform.runLater(() -> {
                    submitting = false;
                    submitBtn.setDisable(false);
                    submitBtn.setText("Submit final quiz");
                });
            }
        }).start();
    }

    private void autoSubmit() {
        if (!submitting) {
            submitting = true;
            if (countdown != null) countdown.stop();
            submitQuiz();
        }
    }

    private void exitFullscreen() {
        Stage stage = SmartForumApp.primaryStage;
        if (focusListener != null) {
            stage.focusedProperty().removeListener(focusListener);
            focusListener = null;
        }
        stage.setFullScreenExitKeyCombination(javafx.scene.input.KeyCombination.keyCombination("ESC"));
        if (stage.isFullScreen()) stage.setFullScreen(false);
    }

    private void showResultAlert(double score) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Quiz Submitted");
        alert.setHeaderText("Your score: " + Math.round(score) + "%");
        alert.setContentText("Great work! Check the Quizzes page for detailed results.");
        alert.showAndWait();
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.setTitle("Quiz Error");
        alert.showAndWait();
        exitFullscreen();
        shell.onQuizFinished();
        shell.navigateTo(MainView.Page.QUIZZES);
    }
}
