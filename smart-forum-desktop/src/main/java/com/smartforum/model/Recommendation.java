package com.smartforum.model;

import com.google.gson.annotations.SerializedName;

/**
 * ML-based topic recommendation — mirrors the "recommendations" array
 * returned by GET /desktop/bootstrap (Requirement #11: automatic topic
 * classification + recommendation based on past engagement).
 */
public class Recommendation {
    @SerializedName("topic_id")
    public int topicId;
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
}
