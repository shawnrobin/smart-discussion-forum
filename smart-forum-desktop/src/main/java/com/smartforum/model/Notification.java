package com.smartforum.model;

import com.google.gson.annotations.SerializedName;

public class Notification {
    public int id;
    public String type;
    public String title;
    public String message;
    public boolean read;
    @SerializedName("created_at")
    public String createdAt;
    @SerializedName("related_entity_id")
    public Integer relatedEntityId;
    @SerializedName("related_entity_type")
    public String relatedEntityType;
}
