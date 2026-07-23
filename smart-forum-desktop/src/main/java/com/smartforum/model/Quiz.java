package com.smartforum.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Quiz {
    public int id;
    public String title;
    public String description;
    @SerializedName("group_id")
    public int groupId;
    @SerializedName("start_time")
    public String startTime;
    @SerializedName("end_time")
    public String endTime;
    @SerializedName("duration_minutes")
    public int durationMinutes;
    public boolean published;
    public String status; // upcoming, active, closed
    public List<QuizQuestion> questions;

    public static class QuizQuestion {
        public int id;
        @SerializedName("question_text")
        public String questionText;
        @SerializedName("question_type")
        public String questionType; // multiple_choice, true_false, short_answer
        public List<String> options;
        public int points;
        @SerializedName("order_index")
        public int orderIndex;
    }
}
