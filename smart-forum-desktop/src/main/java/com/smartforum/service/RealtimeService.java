package com.smartforum.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RealtimeService {

    private static RealtimeService instance;
    private static final Logger LOG = Logger.getLogger(RealtimeService.class.getName());
    private static final String WS_HOST  = "ws://127.0.0.1:8080/app/vqryuhowkvohjs8hcobc";
    private static final String AUTH_URL = "http://127.0.0.1:8001/broadcasting/auth";

    private final OkHttpClient wsClient = new OkHttpClient();
    private WebSocket socket;
    private boolean connected = false;
    private volatile String currentSocketId;

    private final ConcurrentHashMap<String, ConcurrentHashMap<String, Consumer<JsonObject>>> listeners
            = new ConcurrentHashMap<>();

    private RealtimeService() {}

    public static RealtimeService getInstance() {
        if (instance == null) instance = new RealtimeService();
        return instance;
    }

    public void connect() {
        if (connected) return;
        Request request = new Request.Builder().url(WS_HOST).build();
        socket = wsClient.newWebSocket(request, new WebSocketListener() {
            @Override public void onOpen(WebSocket ws, Response response) { connected = true; }
            @Override public void onMessage(WebSocket ws, String text) { handleMessage(text); }
            @Override public void onFailure(WebSocket ws, Throwable t, Response r) { connected = false; }
            @Override public void onClosed(WebSocket ws, int code, String reason) { connected = false; }
        });
    }

    public void disconnect() {
        if (socket != null) socket.close(1000, "App closing");
        connected = false;
    }

    public boolean isConnected() { return connected; }

    public void subscribe(String channel, String event, Consumer<JsonObject> callback) {
        listeners.computeIfAbsent(channel, c -> new ConcurrentHashMap<>()).put(event, callback);
        if (!connected) return;
        String token = SessionManager.getInstance().getToken();
        if (token == null) return;
        new Thread(() -> {
            try {
                OkHttpClient http = new OkHttpClient();
                String socketId = getSocketId();
                if (socketId == null) return;
                RequestBody body = RequestBody.create(
                        "channel_name=" + channel + "&socket_id=" + socketId,
                        MediaType.get("application/x-www-form-urlencoded"));
                Request req = new Request.Builder()
                        .url(AUTH_URL)
                        .header("Authorization", "Bearer " + token)
                        .post(body).build();
                try (Response resp = http.newCall(req).execute()) {
                    if (resp.isSuccessful() && resp.body() != null) {
                        JsonObject authJson = JsonParser.parseString(resp.body().string()).getAsJsonObject();
                        String auth = authJson.get("auth").getAsString();
                        JsonObject sub = new JsonObject();
                        sub.addProperty("event", "pusher:subscribe");
                        JsonObject data = new JsonObject();
                        data.addProperty("auth", auth);
                        data.addProperty("channel", channel);
                        sub.add("data", data);
                        socket.send(sub.toString());
                    }
                }
            } catch (Exception ex) { LOG.log(Level.WARNING, "Subscribe failed for " + channel, ex); }
        }).start();
    }

    public void unsubscribe(String channel) {
        listeners.remove(channel);
        if (socket == null || !connected) return;
        JsonObject unsub = new JsonObject();
        unsub.addProperty("event", "pusher:unsubscribe");
        JsonObject data = new JsonObject();
        data.addProperty("channel", channel);
        unsub.add("data", data);
        socket.send(unsub.toString());
    }

    private void handleMessage(String text) {
        try {
            JsonObject msg = JsonParser.parseString(text).getAsJsonObject();
            String event   = msg.has("event")   ? msg.get("event").getAsString()   : "";
            String channel = msg.has("channel") ? msg.get("channel").getAsString() : "";

            if ("pusher:connection_established".equals(event)) {
                JsonObject connData = JsonParser.parseString(msg.get("data").getAsString()).getAsJsonObject();
                currentSocketId = connData.has("socket_id") ? connData.get("socket_id").getAsString() : null;
                return;
            }

            var channelListeners = listeners.get(channel);
            if (channelListeners == null) return;
            Consumer<JsonObject> cb = channelListeners.get(event);
            if (cb == null) cb = channelListeners.get(event.replaceFirst("^\\.", ""));
            if (cb == null) return;

            JsonObject data;
            if (msg.has("data")) {
                var raw = msg.get("data");
                data = raw.isJsonObject() ? raw.getAsJsonObject()
                        : JsonParser.parseString(raw.getAsString()).getAsJsonObject();
            } else { data = new JsonObject(); }
            cb.accept(data);
        } catch (Exception ex) { LOG.log(Level.FINE, "handleMessage error", ex); }
    }

    private String getSocketId() {
        long deadline = System.currentTimeMillis() + 3000;
        while (currentSocketId == null && System.currentTimeMillis() < deadline) {
            try { Thread.sleep(100); } catch (InterruptedException ignored) {}
        }
        return currentSocketId;
    }
}
