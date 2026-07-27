package com.smartforum.model;

import java.util.List;

public class BootstrapData {
    public boolean success;
    public User user;
    public Summary summary;
    public List<Notification> notifications;
    public List<Quiz> quizzes;
    public List<Recommendation> recommendations;
    public List<Participation> participation;

    public static class Summary {
        public int groups;
        public int topics;
        public int replies;
        public int unread_notifications;
        public int active_quizzes;
    }
}
