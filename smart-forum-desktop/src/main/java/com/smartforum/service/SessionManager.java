package com.smartforum.service;

import com.smartforum.model.BootstrapData;
import com.smartforum.model.User;

public class SessionManager {
    private static SessionManager instance;
    private String token;
    private User currentUser;
    private BootstrapData bootstrapData;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) instance = new SessionManager();
        return instance;
    }

    public void login(String token, User user) { this.token = token; this.currentUser = user; }

    public void setBootstrapData(BootstrapData data) {
        this.bootstrapData = data;
        if (data != null && data.user != null) this.currentUser = data.user;
    }

    public void logout() { token = null; currentUser = null; bootstrapData = null; }
    public boolean isLoggedIn() { return token != null; }
    public String getToken() { return token; }
    public User getCurrentUser() { return currentUser; }
    public BootstrapData getBootstrapData() { return bootstrapData; }
}
