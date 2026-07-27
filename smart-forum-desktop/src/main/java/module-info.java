module com.smartforum {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires com.google.gson;
    requires okhttp3;
    requires kotlin.stdlib;
    requires java.desktop;
    requires java.logging;
    requires java.sql;
    requires org.apache.pdfbox;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome5;

    opens com.smartforum to javafx.graphics, javafx.fxml;
    opens com.smartforum.model to com.google.gson;
    opens com.smartforum.ui to javafx.fxml;
    opens com.smartforum.service to javafx.fxml;
}
