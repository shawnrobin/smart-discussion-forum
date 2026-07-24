package com.smartforum.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class BootstrapData {
    public boolean success;
    public User user;
    public Summary summary;
    public List<Notification> notifications;
    public List<Quiz> quizzes;
    public List<Recommendation> recommendations;

    public static class Summary {
        public int groups;
        public int topics;
        public int replies;
        public int unread_notifications;
        public int active_quizzes;
    }

    /** A recommended topic from the ML engine. */
    public static class Recommendation {
        public int id;           // topic id
        public String title;
        @SerializedName("group_id")
        public int groupId;
        @SerializedName("group_name")
        public String groupName;
        public String author;
        public int replies;
        public double score;
        @SerializedName("is_answered")
        public boolean isAnswered;

        // alias field returned by DesktopController (topic_id instead of id)
        @SerializedName("topic_id")
        public int topicId;

        public int getTopicId() { return topicId > 0 ? topicId : id; }
    }
}
