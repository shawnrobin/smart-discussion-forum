package com.smartforum.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Message {
    public int id;
    public String content;
    @SerializedName("topic_id")
    public int topicId;
    @SerializedName("user_id")
    public int userId;
    @SerializedName("parent_message_id")
    public Integer parentMessageId;
    @SerializedName("is_answer")
    public boolean isAnswer;
    public int likes;
    @SerializedName("liked_by_current_user")
    public boolean likedByCurrentUser;
    @SerializedName("created_at")
    public String createdAt;
    public int depth;
    public MessageUser user;
    public List<Message> replies;

    public static class MessageUser {
        public int id;
        public String name;
        public String initials;
    }
}
