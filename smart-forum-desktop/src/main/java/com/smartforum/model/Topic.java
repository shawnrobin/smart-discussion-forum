package com.smartforum.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Topic {
    public int id;
    public String title;
    public String description;
    @SerializedName("group_id")
    public int groupId;
    @SerializedName("user_id")
    public int userId;
    @SerializedName("is_question")
    public boolean isQuestion;
    @SerializedName("is_answered")
    public boolean isAnswered;
    public int likes;
    @SerializedName("liked_by_current_user")
    public boolean likedByCurrentUser;
    @SerializedName("created_at")
    public String createdAt;
    public TopicUser user;
    public List<Message> messages;

    public String getStatus() {
        return isAnswered ? "answered" : (isQuestion ? "open" : "discussion");
    }

    public static class TopicUser {
        public int id;
        public String name;
        public String initials;
        public String role;
    }
}
