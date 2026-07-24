package com.smartforum.service;

import com.google.gson.JsonObject;
import javafx.application.Platform;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConnectivityService {

    private static ConnectivityService instance;
    private static final Logger LOG = Logger.getLogger(ConnectivityService.class.getName());

    private boolean online = true;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "connectivity-check");
        t.setDaemon(true);
        return t;
    });
    private Consumer<Boolean> onStateChange;

    private ConnectivityService() {}

    public static ConnectivityService getInstance() {
        if (instance == null) instance = new ConnectivityService();
        return instance;
    }

    public void start(Consumer<Boolean> onStateChange) {
        this.onStateChange = onStateChange;
        scheduler.scheduleWithFixedDelay(this::check, 0, 10, TimeUnit.SECONDS);
    }

    public void stop() { scheduler.shutdownNow(); }
    public boolean isOnline() { return online; }

    private void check() {
        boolean reachable = probe();
        if (reachable == online) return;
        boolean wasOffline = !online;
        online = reachable;
        if (wasOffline && online) { syncPendingPosts(); refreshBootstrap(); }
        if (onStateChange != null) Platform.runLater(() -> onStateChange.accept(online));
    }

    private boolean probe() {
        try (Socket s = new Socket()) {
            s.connect(new InetSocketAddress("127.0.0.1", 8001), 2000);
            return true;
        } catch (IOException e) { return false; }
    }

    private void syncPendingPosts() {
        List<OfflineCache.PendingPost> pending = OfflineCache.getInstance().getPendingPosts();
        if (pending.isEmpty()) return;
        for (OfflineCache.PendingPost p : pending) {
            try {
                JsonObject payload = com.google.gson.JsonParser.parseString(p.payload).getAsJsonObject();
                ApiService.getInstance().post(p.endpoint, payload);
                OfflineCache.getInstance().removePendingPost(p.id);
            } catch (Exception ex) {
                LOG.log(Level.WARNING, "Failed to sync post to " + p.endpoint, ex);
                break;
            }
        }
    }

    private void refreshBootstrap() {
        if (!SessionManager.getInstance().isLoggedIn()) return;
        try {
            JsonObject bootstrap = ApiService.getInstance().get("/desktop/bootstrap");
            if (bootstrap.has("success") && bootstrap.get("success").getAsBoolean()) {
                com.smartforum.model.BootstrapData bd =
                        ApiService.getInstance().gson.fromJson(bootstrap, com.smartforum.model.BootstrapData.class);
                SessionManager.getInstance().setBootstrapData(bd);
                OfflineCache.getInstance().putRaw("bootstrap", bootstrap.toString());
            }
        } catch (Exception ex) { LOG.log(Level.WARNING, "Bootstrap refresh failed", ex); }
    }
}
