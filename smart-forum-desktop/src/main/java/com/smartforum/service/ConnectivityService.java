package com.smartforum.service;

import com.google.gson.JsonObject;
import com.smartforum.SmartForumApp;
import javafx.application.Platform;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Polls server connectivity every 10 seconds.
 * On reconnect:
 *  1. Replays any pending offline posts (messages written while offline).
 *  2. Re-fetches the bootstrap to pull new data.
 *  3. Notifies registered listeners.
 */
public class ConnectivityService {

    private static ConnectivityService instance;
    private static final Logger LOG = Logger.getLogger(ConnectivityService.class.getName());

    private boolean online = true;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "connectivity-check");
        t.setDaemon(true);
        return t;
    });

    private Consumer<Boolean> onStateChange;   // called on FX thread with new state

    private ConnectivityService() {}

    public static ConnectivityService getInstance() {
        if (instance == null) instance = new ConnectivityService();
        return instance;
    }

    /** Start polling. Call once after login. */
    public void start(Consumer<Boolean> onStateChange) {
        this.onStateChange = onStateChange;
        scheduler.scheduleWithFixedDelay(this::check, 0, 10, TimeUnit.SECONDS);
    }

    public void stop() {
        scheduler.shutdownNow();
    }

    public boolean isOnline() { return online; }

    // ── Internal ──────────────────────────────────────────────────
    private void check() {
        boolean reachable = probe();
        if (reachable == online) return;   // no change

        boolean wasOffline = !online;
        online = reachable;

        if (wasOffline && online) {
            // Just came back online — sync pending data
            syncPendingPosts();
            refreshBootstrap();
        }

        if (onStateChange != null) {
            Platform.runLater(() -> onStateChange.accept(online));
        }
    }

    /** TCP probe to the API host on port 8001. */
    private boolean probe() {
        try (Socket s = new Socket()) {
            s.connect(new InetSocketAddress("127.0.0.1", 8001), 2000);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /** Replay queued offline posts in order. */
    private void syncPendingPosts() {
        List<OfflineCache.PendingPost> pending = OfflineCache.getInstance().getPendingPosts();
        if (pending.isEmpty()) return;

        LOG.info("Syncing " + pending.size() + " offline post(s)…");
        for (OfflineCache.PendingPost p : pending) {
            try {
                JsonObject payload = com.google.gson.JsonParser.parseString(p.payload).getAsJsonObject();
                ApiService.getInstance().post(p.endpoint, payload);
                OfflineCache.getInstance().removePendingPost(p.id);
                LOG.info("Synced: " + p.endpoint);
            } catch (Exception ex) {
                LOG.log(Level.WARNING, "Failed to sync post to " + p.endpoint, ex);
                break;  // stop on first failure; will retry next cycle
            }
        }
    }

    /** Pull fresh bootstrap and update SessionManager. */
    private void refreshBootstrap() {
        if (!SessionManager.getInstance().isLoggedIn()) return;
        try {
            JsonObject bootstrap = ApiService.getInstance().get("/desktop/bootstrap");
            if (bootstrap.has("success") && bootstrap.get("success").getAsBoolean()) {
                com.smartforum.model.BootstrapData bd =
                        ApiService.getInstance().gson.fromJson(bootstrap, com.smartforum.model.BootstrapData.class);
                SessionManager.getInstance().setBootstrapData(bd);
                // Cache the fresh bootstrap
                OfflineCache.getInstance().putRaw("bootstrap", bootstrap.toString());
                LOG.info("Bootstrap refreshed after reconnect.");
            }
        } catch (Exception ex) {
            LOG.log(Level.WARNING, "Bootstrap refresh failed", ex);
        }
    }
}
