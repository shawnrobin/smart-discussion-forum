package com.smartforum.ui;

import com.smartforum.model.BootstrapData;
import com.smartforum.model.Quiz;
import com.smartforum.service.SessionManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Quizzes list — mirrors Quizzes/Index.vue.
 * Shows active, upcoming, and completed quizzes in separate lists with stat cards.
 */
public class QuizzesView {

    private final MainView shell;
    private String selectedFilter = "active";
    private VBox listBox;

    public QuizzesView(MainView shell) {
        this.shell = shell;
    }

    public Node build() {
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("scroll-pane");

        VBox root = new VBox(20);
        root.setPadding(new Insets(24));
        root.setFillWidth(true);

        // ── Stat cards ─────────────────────────────────────────────
        BootstrapData bd = SessionManager.getInstance().getBootstrapData();
        List<Quiz> allQuizzes = bd != null && bd.quizzes != null ? bd.quizzes : new ArrayList<>();

        long activeCount = allQuizzes.stream().filter(q -> "active".equals(q.status)).count();
        long upcomingCount = allQuizzes.stream().filter(q -> "upcoming".equals(q.status)).count();
        long closedCount = allQuizzes.stream().filter(q -> "closed".equals(q.status)).count();

        HBox statRow = new HBox(16);
        statRow.setFillHeight(true);
        VBox activeStat = filterStatCard("Active", String.valueOf(activeCount), "active", "#dcfce7", "#16a34a");
        VBox upcomingStat = filterStatCard("Upcoming", String.valueOf(upcomingCount), "upcoming", "#dbeafe", "#1d4ed8");
        VBox completedStat = filterStatCard("Completed", String.valueOf(closedCount), "closed", "#f3f4f6", "#6b7280");
        HBox.setHgrow(activeStat, Priority.ALWAYS);
        HBox.setHgrow(upcomingStat, Priority.ALWAYS);
        HBox.setHgrow(completedStat, Priority.ALWAYS);
        statRow.getChildren().addAll(activeStat, upcomingStat, completedStat);

        // ── List card ──────────────────────────────────────────────
        VBox card = new VBox();
        card.getStyleClass().add("card");

        HBox header = new HBox();
        header.getStyleClass().add("card-header");
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label(capitalize(selectedFilter) + " Quizzes");
        title.getStyleClass().add("card-title");
        title.setId("quiz-section-title");
        header.getChildren().add(title);

        listBox = new VBox(12);
        listBox.setPadding(new Insets(16, 24, 16, 24));

        card.getChildren().addAll(header, listBox);

        root.getChildren().addAll(statRow, card);
        scroll.setContent(root);

        renderQuizzes();
        return scroll;
    }

    private VBox filterStatCard(String label, String count, String filter, String bgColor, String textColor) {
        VBox card = new VBox(8);
        card.getStyleClass().add("stat-card");
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color:" + bgColor + ";-fx-cursor:hand;-fx-border-color:" + textColor + ";");
        card.setOnMouseClicked(e -> {
            selectedFilter = filter;
            renderQuizzes();
            Label titleLbl = (Label) card.getScene().lookup("#quiz-section-title");
            if (titleLbl != null) titleLbl.setText(capitalize(filter) + " Quizzes");
        });

        Label h3 = new Label(label);
        h3.setStyle("-fx-font-size:16px;-fx-font-weight:600;-fx-text-fill:" + textColor + ";");
        Label numLbl = new Label(count);
        numLbl.setStyle("-fx-font-size:32px;-fx-font-weight:bold;-fx-text-fill:" + textColor + ";");
        card.getChildren().addAll(h3, numLbl);
        return card;
    }

    private void renderQuizzes() {
        listBox.getChildren().clear();

        BootstrapData bd = SessionManager.getInstance().getBootstrapData();
        if (bd == null || bd.quizzes == null) {
            Label empty = new Label("No quizzes available.");
            empty.getStyleClass().add("muted-text");
            listBox.getChildren().add(empty);
            return;
        }

        List<Quiz> filtered = bd.quizzes.stream()
                .filter(q -> selectedFilter.equals(q.status))
                .toList();

        if (filtered.isEmpty()) {
            Label empty = new Label("No " + selectedFilter + " quizzes.");
            empty.getStyleClass().add("muted-text");
            listBox.getChildren().add(empty);
            return;
        }

        for (Quiz q : filtered) {
            listBox.getChildren().add(quizRow(q));
        }
    }

    private HBox quizRow(Quiz q) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(16, 0, 16, 0));
        row.setStyle("-fx-border-color:#f3f4f6;-fx-border-width:0 0 1 0;");

        VBox info = new VBox(4);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label nameLbl = new Label(q.title);
        nameLbl.setStyle("-fx-font-size:16px;-fx-font-weight:600;-fx-text-fill:#111827;");

        Label metaLbl = new Label("⏱ " + q.durationMinutes + " minutes");
        metaLbl.getStyleClass().add("muted-text");

        String dateStr;
        if ("active".equals(q.status)) {
            dateStr = "⏰ Closes: " + formatDateTime(q.endTime);
        } else if ("upcoming".equals(q.status)) {
            dateStr = "📅 Starts: " + formatDateTime(q.startTime);
        } else {
            dateStr = "✅ Closed: " + formatDateTime(q.endTime);
        }
        Label dateLbl = new Label(dateStr);
        dateLbl.setStyle("-fx-font-size:11px;-fx-text-fill:#9ca3af;");

        info.getChildren().addAll(nameLbl, metaLbl, dateLbl);

        // Action button
        if ("active".equals(q.status)) {
            Button takeBtn = UiHelper.primaryButton("Take Quiz");
            takeBtn.setOnAction(e -> shell.openQuiz(q.id));
            row.getChildren().addAll(info, takeBtn);
        } else if ("upcoming".equals(q.status)) {
            Label badge = UiHelper.badge("📅 Upcoming", "badge-upcoming");
            row.getChildren().addAll(info, badge);
        } else {
            Label badge = UiHelper.badge("✓ Completed", "badge-closed");
            row.getChildren().addAll(info, badge);
        }

        return row;
    }

    private String formatDateTime(String iso) {
        if (iso == null || iso.isEmpty()) return "";
        // Simple substring (first 16 chars = YYYY-MM-DD HH:MM)
        return iso.length() >= 16 ? iso.substring(0, 16).replace("T", " ") : iso;
    }

    private String capitalize(String s) {
        return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
