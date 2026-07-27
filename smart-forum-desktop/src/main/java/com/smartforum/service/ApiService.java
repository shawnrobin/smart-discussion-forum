package com.smartforum.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.smartforum.SmartForumApp;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Centralised HTTP client for all API calls.
 * All requests use Bearer token auth (Laravel Sanctum).
 *
 * When the app is offline, POST requests are queued in the local SQLite
 * cache and replayed automatically by ConnectivityService on reconnect.
 */
public class ApiService {

    private static ApiService instance;
    private final OkHttpClient client;
    public final Gson gson;
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private ApiService() {
        client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        gson = new GsonBuilder().create();
    }

    public static ApiService getInstance() {
        if (instance == null) instance = new ApiService();
        return instance;
    }

    // ---------------------------------------------------------------
    // Core request helpers
    // ---------------------------------------------------------------

    private Request.Builder authorized() {
        String token = SessionManager.getInstance().getToken();
        Request.Builder builder = new Request.Builder();
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        builder.header("Accept", "application/json");
        return builder;
    }

    private String url(String path) {
        return SmartForumApp.BASE_URL + path;
    }

    /** Synchronous GET — returns cached data when offline. */
    public JsonObject get(String path) throws IOException {
        // Offline: serve from cache if available
        if (!ConnectivityService.getInstance().isOnline()) {
            String cached = OfflineCache.getInstance().getRaw("req:" + path);
            if (cached != null) {
                return JsonParser.parseString(cached).getAsJsonObject();
            }
            throw new IOException("Offline and no cached data for " + path);
        }

        Request request = authorized().url(url(path)).get().build();
        try (Response response = client.newCall(request).execute()) {
            String body = response.body() != null ? response.body().string() : "{}";
            // Cache for offline use
            OfflineCache.getInstance().putRaw("req:" + path, body);
            return JsonParser.parseString(body).getAsJsonObject();
        }
    }

    /** Synchronous POST. Queues to offline cache when offline. */
    public JsonObject post(String path, JsonObject payload) throws IOException {
        if (!ConnectivityService.getInstance().isOnline()) {
            // Queue for later replay
            OfflineCache.getInstance().queuePost(path, payload != null ? payload.toString() : "{}");
            // Return optimistic success so UI can update immediately
            JsonObject optimistic = new JsonObject();
            optimistic.addProperty("success", true);
            optimistic.addProperty("offline_queued", true);
            return optimistic;
        }

        RequestBody body = RequestBody.create(
                payload != null ? payload.toString() : "{}", JSON);
        Request request = authorized().url(url(path)).post(body).build();
        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "{}";
            return JsonParser.parseString(responseBody).getAsJsonObject();
        }
    }

    /** Synchronous POST with no body. */
    public JsonObject post(String path) throws IOException {
        return post(path, new JsonObject());
    }

    /** Synchronous DELETE. */
    public JsonObject delete(String path) throws IOException {
        Request request = authorized().url(url(path)).delete().build();
        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "{}";
            return JsonParser.parseString(responseBody).getAsJsonObject();
        }
    }
}
