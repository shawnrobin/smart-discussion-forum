package com.smartforum.ui;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.smartforum.service.ApiService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Requirement #10: "Before a quiz is published to the students, the lecturer
 * logs in and sets the configuration for the quiz, including time, date,
 * duration ... The configurations must be set before the actual time for
 * the quiz."
 *
 * A full quiz-builder dialog so lecturers can create quizzes directly from
 * the desktop app, instead of needing the web interface.
 */
public class QuizBuilderDialog {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** Opens the dialog for the given group; onCreated runs (on FX thread) after a successful save. */
    public static void open(int groupId, String groupName, Runnable onCreated) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Create Quiz — " + groupName);
        dialog.setResizable(true);

        VBox root = new VBox(14);
        root.setPadding(new Insets(16));
        root.setPrefWidth(560);

        Label heading = new Label("Quiz configuration");
        heading.setStyle("-fx-font-size:14px;-fx-font-weight:bold;-fx-text-fill:#111827;");

        TextField titleField = labeledField(root, "Title");
        TextField descField = labeledField(root, "Description (optional)");

        LocalDateTime now = LocalDateTime.now().plusMinutes(5).withSecond(0);
        LocalDateTime inHour = now.plusHours(1);
        TextField startField = labeledField(root, "Start time (yyyy-MM-dd HH:mm:ss)");
        startField.setText(now.format(FMT));
        TextField endField = labeledField(root, "End time (yyyy-MM-dd HH:mm:ss)");
        endField.setText(inHour.format(FMT));
        TextField durationField = labeledField(root, "Duration (minutes)");
        durationField.setText("30");

        CheckBox shuffleBox = new CheckBox("Shuffle questions for each student");
        shuffleBox.setSelected(true);

        ComboBox<String> resultsMode = new ComboBox<>();
        resultsMode.getItems().addAll("immediately", "after_end", "manually");
        resultsMode.setValue("after_end");
        VBox resultsBox = new VBox(4, new Label("Show results"), resultsMode);

        root.getChildren().addAll(heading, shuffleBox, resultsBox);

        // ── Questions ────────────────────────────────────────────
        Separator sep = new Separator();
        Label qHeading = new Label("Questions");
        qHeading.setStyle("-fx-font-size:14px;-fx-font-weight:bold;-fx-text-fill:#111827;");

        VBox questionsBox = new VBox(10);
        List<QuestionRow> rows = new ArrayList<>();

        Button addQuestionBtn = new Button("+ Add question");
        addQuestionBtn.setOnAction(e -> rows.add(new QuestionRow(questionsBox, rows)));

        root.getChildren().addAll(sep, qHeading, questionsBox, addQuestionBtn);
        // Start with one question row
        rows.add(new QuestionRow(questionsBox, rows));

        Label errorLbl = new Label();
        errorLbl.getStyleClass().add("error-text");
        errorLbl.setWrapText(true);
        errorLbl.setVisible(false);
        errorLbl.setManaged(false);
        root.getChildren().add(errorLbl);

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        scroll.setPrefViewportHeight(520);
        dialog.getDialogPane().setContent(scroll);

        ButtonType createType = new ButtonType("Create Quiz", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createType, ButtonType.CANCEL);

        // Validate + submit on click, without closing the dialog on failure
        Button createBtn = (Button) dialog.getDialogPane().lookupButton(createType);
        createBtn.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            errorLbl.setVisible(false);
            errorLbl.setManaged(false);

            String title = titleField.getText().trim();
            if (title.isEmpty()) {
                showError(errorLbl, "Title is required.");
                ev.consume();
                return;
            }
            if (rows.isEmpty()) {
                showError(errorLbl, "Add at least one question.");
                ev.consume();
                return;
            }
            int duration;
            try {
                duration = Integer.parseInt(durationField.getText().trim());
                if (duration < 5 || duration > 480) throw new NumberFormatException();
            } catch (NumberFormatException nfe) {
                showError(errorLbl, "Duration must be a number between 5 and 480 minutes.");
                ev.consume();
                return;
            }

            JsonArray questionsJson = new JsonArray();
            for (QuestionRow r : rows) {
                String err = r.validate();
                if (err != null) {
                    showError(errorLbl, err);
                    ev.consume();
                    return;
                }
                questionsJson.add(r.toJson());
            }

            JsonObject payload = new JsonObject();
            payload.addProperty("title", title);
            if (!descField.getText().trim().isEmpty()) payload.addProperty("description", descField.getText().trim());
            payload.addProperty("start_time", startField.getText().trim());
            payload.addProperty("end_time", endField.getText().trim());
            payload.addProperty("duration_minutes", duration);
            payload.addProperty("shuffle_questions", shuffleBox.isSelected());
            payload.addProperty("show_results_after", resultsMode.getValue());
            payload.add("questions", questionsJson);

            createBtn.setDisable(true);
            new Thread(() -> {
                try {
                    JsonObject resp = ApiService.getInstance().post("/groups/" + groupId + "/quizzes", payload);
                    boolean ok = resp.has("success") && resp.get("success").getAsBoolean();
                    Platform.runLater(() -> {
                        createBtn.setDisable(false);
                        if (ok) {
                            dialog.close();
                            if (onCreated != null) onCreated.run();
                        } else {
                            String msg = resp.has("message") ? resp.get("message").getAsString() : "Could not create quiz.";
                            showError(errorLbl, msg);
                        }
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        createBtn.setDisable(false);
                        showError(errorLbl, "Could not create quiz: " + ex.getMessage());
                    });
                }
            }).start();

            ev.consume(); // we close the dialog manually once the request succeeds
        });

        dialog.showAndWait();
    }

    private static TextField labeledField(VBox parent, String label) {
        Label l = new Label(label);
        l.setStyle("-fx-font-size:12px;-fx-font-weight:500;-fx-text-fill:#475569;");
        TextField tf = new TextField();
        tf.getStyleClass().add("text-field");
        parent.getChildren().addAll(l, tf);
        return tf;
    }

    private static void showError(Label lbl, String msg) {
        lbl.setText(msg);
        lbl.setVisible(true);
        lbl.setManaged(true);
    }

    /** One question's editable fields, backed by a card in the questions list. */
    private static class QuestionRow {
        final TextField textField = new TextField();
        final ComboBox<String> typeBox = new ComboBox<>();
        final TextField optionsField = new TextField();
        final TextField correctField = new TextField();
        final TextField pointsField = new TextField("1");
        final VBox card;

        QuestionRow(VBox container, List<QuestionRow> rows) {
            typeBox.getItems().addAll("multiple_choice", "true_false", "short_answer");
            typeBox.setValue("multiple_choice");

            textField.setPromptText("Question text");
            optionsField.setPromptText("Options, comma-separated (multiple choice only)");
            correctField.setPromptText("Correct answer");
            pointsField.setPrefWidth(60);

            HBox typeRow = new HBox(8, new Label("Type:"), typeBox, new Label("Points:"), pointsField);
            typeRow.setAlignment(Pos.CENTER_LEFT);

            Button removeBtn = new Button("Remove");
            removeBtn.setStyle("-fx-text-fill:#e11d48;-fx-background-color:transparent;-fx-cursor:hand;");

            card = new VBox(6, textField, typeRow, optionsField, correctField, removeBtn);
            card.getStyleClass().add("card");
            card.setPadding(new Insets(10));

            removeBtn.setOnAction(e -> {
                container.getChildren().remove(card);
                rows.remove(this);
            });

            container.getChildren().add(card);
        }

        /** Returns a user-facing error message, or null if this question is valid. */
        String validate() {
            if (textField.getText().trim().isEmpty()) return "Every question needs its text filled in.";
            if (correctField.getText().trim().isEmpty()) return "Every question needs a correct answer.";
            if ("multiple_choice".equals(typeBox.getValue()) && optionsField.getText().trim().isEmpty()) {
                return "Multiple-choice questions need comma-separated options.";
            }
            try {
                int pts = Integer.parseInt(pointsField.getText().trim());
                if (pts < 1) throw new NumberFormatException();
            } catch (NumberFormatException nfe) {
                return "Points must be a positive whole number.";
            }
            return null;
        }

        JsonObject toJson() {
            JsonObject q = new JsonObject();
            q.addProperty("question_text", textField.getText().trim());
            q.addProperty("question_type", typeBox.getValue());
            if ("multiple_choice".equals(typeBox.getValue())) {
                JsonArray opts = new JsonArray();
                for (String o : optionsField.getText().split(",")) {
                    String trimmed = o.trim();
                    if (!trimmed.isEmpty()) opts.add(trimmed);
                }
                q.add("options", opts);
            }
            q.addProperty("correct_answer", correctField.getText().trim());
            q.addProperty("points", Integer.parseInt(pointsField.getText().trim()));
            return q;
        }
    }
}
