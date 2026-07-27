package com.smartforum.model;

import com.google.gson.annotations.SerializedName;

/**
 * A student's participation score within one group — mirrors the
 * "participation" array returned by GET /desktop/bootstrap.
 * Backs Requirement #9 (marks awarded for forum participation).
 */
public class Participation {
    public int id;
    @SerializedName("user_id")
    public int userId;
    @SerializedName("group_id")
    public int groupId;
    @SerializedName("total_score")
    public String totalScore;
    @SerializedName("topics_created")
    public int topicsCreated;
    @SerializedName("topics_replied")
    public int topicsReplied;
    @SerializedName("answers_provided")
    public int answersProvided;
    public GroupRef group;

    public static class GroupRef {
        public int id;
        public String name;
    }
}
