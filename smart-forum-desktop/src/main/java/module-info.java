module com.smartforum {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires com.google.gson;
    requires okhttp3;
    requires kotlin.stdlib;
    requires java.sql;
    requires java.desktop;
    requires org.xerial.sqlitejdbc;

    opens com.smartforum to javafx.graphics, javafx.fxml;
    opens com.smartforum.model to com.google.gson;
    opens com.smartforum.ui to javafx.fxml;
    opens com.smartforum.service to javafx.fxml;
}
