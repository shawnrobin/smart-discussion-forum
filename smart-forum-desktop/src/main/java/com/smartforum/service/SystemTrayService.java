package com.smartforum.service;

import javafx.application.Platform;
import javafx.stage.Stage;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Requirement (Desktop Chat Screen Objects — System tray):
 * "OS-level icon | Right-click → Open, Settings, Exit | Minimize to tray, notifications"
 *
 * Closing the main window minimizes the app to the OS system tray instead of
 * quitting outright, matching the design doc's "always-available desktop
 * chat client" intent. Right-clicking the tray icon offers Open / Exit.
 */
public class SystemTrayService {

    private static SystemTrayService instance;
    private TrayIcon trayIcon;
    private boolean installed = false;

    private SystemTrayService() {}

    public static SystemTrayService getInstance() {
        if (instance == null) instance = new SystemTrayService();
        return instance;
    }

    /** Call once, right after the main stage is shown. Safe to call more than once. */
    public void install(Stage stage) {
        if (installed) return;
        if (!SystemTray.isSupported()) return; // gracefully skip on platforms without a tray

        try {
            Platform.setImplicitExit(false); // don't quit the JVM when the window is hidden

            SystemTray tray = SystemTray.getSystemTray();
            Image icon = buildTrayIconImage();

            PopupMenu menu = new PopupMenu();

            MenuItem openItem = new MenuItem("Open Smart Forum");
            openItem.addActionListener(e -> Platform.runLater(() -> {
                stage.show();
                stage.toFront();
                stage.setIconified(false);
            }));

            MenuItem exitItem = new MenuItem("Exit");
            exitItem.addActionListener(e -> {
                tray.remove(trayIcon);
                Platform.exit();
                System.exit(0);
            });

            menu.add(openItem);
            menu.addSeparator();
            menu.add(exitItem);

            trayIcon = new TrayIcon(icon, "Smart Discussion Forum", menu);
            trayIcon.setImageAutoSize(true);
            trayIcon.addActionListener(e -> Platform.runLater(() -> {
                stage.show();
                stage.toFront();
                stage.setIconified(false);
            }));

            tray.add(trayIcon);
            installed = true;

            // Minimize to tray instead of closing when the window's close button is pressed.
            stage.setOnCloseRequest(e -> {
                e.consume();
                stage.hide();
                notify_("Still running", "Smart Forum is running in the background. Right-click the tray icon to reopen or exit.");
            });
        } catch (Exception ignored) {
            // Tray integration is a nice-to-have — never let it break app startup.
        }
    }

    /** Shows a native tray balloon notification, if the tray is installed. */
    public void notify_(String title, String message) {
        if (trayIcon != null) {
            trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);
        }
    }

    /** Small generated "S" badge so the app doesn't need a bundled icon asset. */
    private Image buildTrayIconImage() {
        int size = 32;
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(0xea, 0x58, 0x0c)); // matches the app's orange-600 brand color
        g.fillRoundRect(0, 0, size, size, 8, 8);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 20));
        FontMetrics fm = g.getFontMetrics();
        String text = "S";
        int x = (size - fm.stringWidth(text)) / 2;
        int y = (size - fm.getHeight()) / 2 + fm.getAscent();
        g.drawString(text, x, y);
        g.dispose();
        return img;
    }
}
