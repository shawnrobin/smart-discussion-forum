package com.smartforum.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Pushes real-time events from Laravel Reverb to the desktop UI.
 *
 * Reverb uses the Pusher protocol over WebSocket:
 *   ws://127.0.0.1:8080/app/{APP_KEY}
 *
 * After connecting, we subscribe to private channels using the Sanctum token
 * obtained from POST /broadcasting/auth.
 *
 * Events we care about:
 *   channel: private-topic.{topicId}   event: App\Events\ReplyCreated   → new reply
 *   channel: private-topic.{topicId}   event: App\Events\LikeUpdated    → like count
 *   channel: private-App.Models.User.{userId}  event: App\Events\UserNotificationCreated → notification
 */
public class RealtimeService {

    private static RealtimeService instance;
    private static final Logger LOG = Logger.getLogger(RealtimeService.class.getName());

    // Reverb config  (matches .env)
    private static final String WS_HOST   = "wss://smart-discussion-forum-x0vd.onrender.com/app/vqryuhowkvohjs8hcobc";
    private static final String AUTH_URL  = "https://smart-discussion-forum-x0vd.onrender.com/broadcasting/auth";

    private final OkHttpClient wsClient = new OkHttpClient();
    private WebSocket socket;
    private boolean connected = false;

    // channel → event-name → callback
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, Consumer<JsonObject>>> listeners
            = new ConcurrentHashMap<>();

    private RealtimeService() {}

    public static RealtimeService getInstance() {
        if (instance == null) instance = new RealtimeService();
        return instance;
    }

    // ── Connect ───────────────────────────────────────────────────
    public void connect() {
        if (connected) return;
        Request request = new Request.Builder().url(WS_HOST).build();
        socket = wsClient.newWebSocket(request, new WebSocketListener() {
            @Override public void onOpen(WebSocket ws, Response response) {
                connected = true;
                LOG.info("Reverb WebSocket connected");
            }
            @Override public void onMessage(WebSocket ws, String text) {
                handleMessage(text);
            }
            @Override public void onFailure(WebSocket ws, Throwable t, Response r) {
                connected = false;
                LOG.log(Level.WARNING, "Reverb WS failure", t);
            }
            @Override public void onClosed(WebSocket ws, int code, String reason) {
                connected = false;
            }
        });
    }

    public void disconnect() {
        if (socket != null) socket.close(1000, "App closing");
        connected = false;
    }

    public boolean isConnected() { return connected; }

    // ── Subscribe ─────────────────────────────────────────────────
    /**
     * Subscribe to a private Reverb channel and register an event listener.
     * @param channel  e.g. "private-topic.42"
     * @param event    e.g. "App\\Events\\ReplyCreated"  (or the broadcast name like ".reply.created")
     * @param callback receives the parsed event data JsonObject
     */
    public void subscribe(String channel, String event, Consumer<JsonObject> callback) {
        listeners.computeIfAbsent(channel, c -> new ConcurrentHashMap<>()).put(event, callback);

        if (!connected) return;

        // Obtain auth signature from Laravel
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
                        .post(body)
                        .build();

                try (Response resp = http.newCall(req).execute()) {
                    if (resp.isSuccessful() && resp.body() != null) {
                        String authBody = resp.body().string();
                        JsonObject authJson = JsonParser.parseString(authBody).getAsJsonObject();
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
            } catch (Exception ex) {
                LOG.log(Level.WARNING, "Subscribe failed for " + channel, ex);
            }
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

    // ── Message dispatch ──────────────────────────────────────────
    private void handleMessage(String text) {
        try {
            JsonObject msg = JsonParser.parseString(text).getAsJsonObject();
            String event   = msg.has("event") ? msg.get("event").getAsString() : "";
            String channel = msg.has("channel") ? msg.get("channel").getAsString() : "";

            // Pusher connection established — capture socket_id
            if ("pusher:connection_established".equals(event)) {
                JsonObject connData = JsonParser.parseString(msg.get("data").getAsString()).getAsJsonObject();
                currentSocketId = connData.has("socket_id") ? connData.get("socket_id").getAsString() : null;
                // Re-subscribe pending channels (reconnect case)
                resubscribeAll();
                return;
            }

            var channelListeners = listeners.get(channel);
            if (channelListeners == null) return;

            // Try exact match first, then strip dot prefix
            Consumer<JsonObject> cb = channelListeners.get(event);
            if (cb == null) cb = channelListeners.get(event.replaceFirst("^\\.", ""));
            if (cb == null) return;

            // data may be a JSON string or a JsonObject
            JsonObject data;
            if (msg.has("data")) {
                var raw = msg.get("data");
                if (raw.isJsonObject()) {
                    data = raw.getAsJsonObject();
                } else {
                    data = JsonParser.parseString(raw.getAsString()).getAsJsonObject();
                }
            } else {
                data = new JsonObject();
            }

            cb.accept(data);
        } catch (Exception ex) {
            LOG.log(Level.FINE, "handleMessage error", ex);
        }
    }

    private volatile String currentSocketId;

    private String getSocketId() {
        // Wait up to 3 s for connection established
        long deadline = System.currentTimeMillis() + 3000;
        while (currentSocketId == null && System.currentTimeMillis() < deadline) {
            try { Thread.sleep(100); } catch (InterruptedException ignored) {}
        }
        return currentSocketId;
    }

    private void resubscribeAll() {
        listeners.forEach((channel, events) -> {
            // Re-trigger subscribe handshake — use the first event as representative
            if (!events.isEmpty()) {
                String event = events.keys().nextElement();
                Consumer<JsonObject> cb = events.get(event);
                subscribe(channel, event, cb);
            }
        });
    }
}
