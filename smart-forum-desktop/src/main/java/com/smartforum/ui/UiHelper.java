package com.smartforum.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Factory methods for common UI components that mirror the web design system.
 */
public class UiHelper {

    /** Circular avatar with initials — orange gradient (for topics / authors). */
    public static StackPane orangeAvatar(String initials) {
        Label lbl = new Label(initials);
        lbl.getStyleClass().add("avatar-text");
        StackPane pane = new StackPane(lbl);
        pane.getStyleClass().add("avatar-orange");
        return pane;
    }

    /** Circular avatar with initials — slate gradient (for replies). */
    public static StackPane slateAvatar(String initials) {
        Label lbl = new Label(initials);
        lbl.getStyleClass().add("avatar-text");
        StackPane pane = new StackPane(lbl);
        pane.getStyleClass().add("avatar-slate");
        return pane;
    }

    /** Status badge for quizzes or topics. */
    public static Label badge(String text, String styleCls) {
        Label lbl = new Label(text);
        lbl.getStyleClass().add(styleCls);
        return lbl;
    }

    /** Primary orange button. */
    public static Button primaryButton(String text) {
        Button btn = new Button(text);
        btn.getStyleClass().add("btn-primary");
        return btn;
    }

    /** Secondary outlined button. */
    public static Button secondaryButton(String text) {
        Button btn = new Button(text);
        btn.getStyleClass().add("btn-secondary");
        return btn;
    }

    /** Danger (red) button. */
    public static Button dangerButton(String text) {
        Button btn = new Button(text);
        btn.getStyleClass().add("btn-danger");
        return btn;
    }

    /** Link-style button. */
    public static Button linkButton(String text) {
        Button btn = new Button(text);
        btn.getStyleClass().add("btn-link");
        return btn;
    }

    /** Form field label. */
    public static Label formLabel(String text) {
        Label lbl = new Label(text);
        lbl.getStyleClass().add("form-label");
        return lbl;
    }

    /** Section title (bold, gray-900). */
    public static Label sectionTitle(String text) {
        Label lbl = new Label(text);
        lbl.getStyleClass().add("section-title");
        return lbl;
    }

    /** Muted gray metadata text. */
    public static Label metaText(String text) {
        Label lbl = new Label(text);
        lbl.getStyleClass().add("meta-text");
        return lbl;
    }

    /** Body text (gray-700). */
    public static Label bodyText(String text) {
        Label lbl = new Label(text);
        lbl.getStyleClass().add("body-text");
        lbl.setWrapText(true);
        return lbl;
    }

    /** Horizontal divider with label text on the left. */
    public static HBox dividerRow(String label) {
        Label lbl = new Label(label);
        lbl.getStyleClass().add("section-title");
        javafx.scene.control.Separator sep = new javafx.scene.control.Separator();
        HBox.setHgrow(sep, javafx.scene.layout.Priority.ALWAYS);
        HBox row = new HBox(8, lbl, sep);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    /** Spacer that grows in VBox. */
    public static javafx.scene.layout.Region vSpacer() {
        javafx.scene.layout.Region r = new javafx.scene.layout.Region();
        VBox.setVgrow(r, javafx.scene.layout.Priority.ALWAYS);
        return r;
    }

    /** Spacer that grows in HBox. */
    public static javafx.scene.layout.Region hSpacer() {
        javafx.scene.layout.Region r = new javafx.scene.layout.Region();
        HBox.setHgrow(r, javafx.scene.layout.Priority.ALWAYS);
        return r;
    }

    /** Wraps a node in a styled white card. */
    public static VBox card(javafx.scene.Node... children) {
        VBox box = new VBox(children);
        box.getStyleClass().add("card");
        box.setPadding(new Insets(16, 24, 16, 24));
        return box;
    }
}
