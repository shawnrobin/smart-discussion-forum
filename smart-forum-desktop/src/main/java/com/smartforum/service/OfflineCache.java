package com.smartforum.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OfflineCache {

    private static OfflineCache instance;
    private Connection conn;
    private final Gson gson = new GsonBuilder().create();
    private static final Logger LOG = Logger.getLogger(OfflineCache.class.getName());

    private OfflineCache() {
        try {
            File dbDir = new File(System.getProperty("user.home"), ".smartforum");
            dbDir.mkdirs();
            String dbPath = new File(dbDir, "offline.db").getAbsolutePath();
            conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            initSchema();
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Could not open offline cache", e);
        }
    }

    public static OfflineCache getInstance() {
        if (instance == null) instance = new OfflineCache();
        return instance;
    }

    private void initSchema() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS cache (key TEXT PRIMARY KEY, value TEXT NOT NULL, updated_at INTEGER NOT NULL)");
            stmt.execute("CREATE TABLE IF NOT EXISTS pending_posts (id INTEGER PRIMARY KEY AUTOINCREMENT, endpoint TEXT NOT NULL, payload TEXT NOT NULL, created_at INTEGER NOT NULL)");
        }
    }

    public void putRaw(String key, String json) {
        if (conn == null) return;
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT OR REPLACE INTO cache(key,value,updated_at) VALUES(?,?,?)")) {
            ps.setString(1, key); ps.setString(2, json); ps.setLong(3, System.currentTimeMillis());
            ps.executeUpdate();
        } catch (SQLException e) { LOG.log(Level.WARNING, "Cache write failed for key=" + key, e); }
    }

    public String getRaw(String key) {
        if (conn == null) return null;
        try (PreparedStatement ps = conn.prepareStatement("SELECT value FROM cache WHERE key=?")) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getString(1) : null; }
        } catch (SQLException e) { return null; }
    }

    public <T> T get(String key, Class<T> type) {
        String raw = getRaw(key);
        return raw != null ? gson.fromJson(raw, type) : null;
    }

    public void queuePost(String endpoint, String payloadJson) {
        if (conn == null) return;
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO pending_posts(endpoint,payload,created_at) VALUES(?,?,?)")) {
            ps.setString(1, endpoint); ps.setString(2, payloadJson); ps.setLong(3, System.currentTimeMillis());
            ps.executeUpdate();
        } catch (SQLException e) { LOG.log(Level.WARNING, "Failed to queue offline post", e); }
    }

    public List<PendingPost> getPendingPosts() {
        List<PendingPost> list = new ArrayList<>();
        if (conn == null) return list;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id,endpoint,payload FROM pending_posts ORDER BY id")) {
            while (rs.next()) {
                PendingPost p = new PendingPost();
                p.id = rs.getLong(1); p.endpoint = rs.getString(2); p.payload = rs.getString(3);
                list.add(p);
            }
        } catch (SQLException e) { LOG.log(Level.WARNING, "Failed to read pending posts", e); }
        return list;
    }

    public void removePendingPost(long id) {
        if (conn == null) return;
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM pending_posts WHERE id=?")) {
            ps.setLong(1, id); ps.executeUpdate();
        } catch (SQLException ignored) {}
    }

    public static class PendingPost {
        public long id; public String endpoint; public String payload;
    }
}
