package com.smartforum.ui;

import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Thin wrapper around Ikonli's FontIcon so every screen creates icons the
 * same way (size + color), instead of scattering emoji glyphs everywhere.
 */
public class Icons {

    public static FontIcon of(FontAwesomeSolid icon, int size, String colorHex) {
        FontIcon fi = new FontIcon(icon);
        fi.setIconSize(size);
        fi.setIconColor(javafx.scene.paint.Color.web(colorHex));
        return fi;
    }

    public static FontIcon of(FontAwesomeSolid icon, int size) {
        return of(icon, size, "#4b5563"); // gray-600, matches nav-item text color
    }
}
