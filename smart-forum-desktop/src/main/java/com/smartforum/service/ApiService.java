package com.smartforum.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.smartforum.SmartForumApp;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

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

    private Request.Builder authorized() {
        String token = SessionManager.getInstance().getToken();
        Request.Builder builder = new Request.Builder();
        if (token != null) builder.header("Authorization", "Bearer " + token);
        builder.header("Accept", "application/json");
        return builder;
    }

    private String url(String path) { return SmartForumApp.BASE_URL + path; }

    public JsonObject get(String path) throws IOException {
        if (!ConnectivityService.getInstance().isOnline()) {
            String cached = OfflineCache.getInstance().getRaw("req:" + path);
            if (cached != null) {
                try { return JsonParser.parseString(cached).getAsJsonObject(); } catch (Exception ignored) {}
            }
            throw new IOException("Offline and no cached data for " + path);
        }
        Request request = authorized().url(url(path)).get().build();
        try (Response response = client.newCall(request).execute()) {
            String body = response.body() != null ? response.body().string() : "{}";
            OfflineCache.getInstance().putRaw("req:" + path, body);
            try { return JsonParser.parseString(body).getAsJsonObject(); }
            catch (Exception e) { return new JsonObject(); }
        }
    }

    public String getRaw(String path) throws IOException {
        if (!ConnectivityService.getInstance().isOnline()) {
            String cached = OfflineCache.getInstance().getRaw("req:" + path);
            if (cached != null) return cached;
            throw new IOException("Offline and no cached data for " + path);
        }
        Request request = authorized().url(url(path)).get().build();
        try (Response response = client.newCall(request).execute()) {
            String body = response.body() != null ? response.body().string() : "[]";
            OfflineCache.getInstance().putRaw("req:" + path, body);
            return body;
        }
    }

    public JsonObject post(String path, JsonObject payload) throws IOException {
        if (!ConnectivityService.getInstance().isOnline()) {
            OfflineCache.getInstance().queuePost(path, payload != null ? payload.toString() : "{}");
            JsonObject optimistic = new JsonObject();
            optimistic.addProperty("success", true);
            optimistic.addProperty("offline_queued", true);
            return optimistic;
        }
        RequestBody body = RequestBody.create(payload != null ? payload.toString() : "{}", JSON);
        Request request = authorized().url(url(path)).post(body).build();
        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "{}";
            return JsonParser.parseString(responseBody).getAsJsonObject();
        }
    }

    public JsonObject post(String path) throws IOException { return post(path, new JsonObject()); }

    public JsonObject delete(String path) throws IOException {
        Request request = authorized().url(url(path)).delete().build();
        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "{}";
            return JsonParser.parseString(responseBody).getAsJsonObject();
        }
    }
}
