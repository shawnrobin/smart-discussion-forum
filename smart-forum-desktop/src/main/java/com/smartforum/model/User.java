package com.smartforum.model;

import com.google.gson.annotations.SerializedName;

public class User {
    public int id;
    public String name;
    public String email;
    public String username;
    public String role;
    public String status;
    public String bio;
    public String course;
    public String faculty;
    @SerializedName("year_of_study")
    public int yearOfStudy;

    public boolean isAdmin() {
        return "system_admin".equals(role);
    }

    public boolean isLecturer() {
        return "lecturer".equals(role) || "system_admin".equals(role);
    }

    public boolean isStudent() {
        return "student".equals(role);
    }

    public String getInitials() {
        if (name == null || name.isEmpty()) return "??";
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) return (parts[0].charAt(0) + "" + parts[1].charAt(0)).toUpperCase();
        return name.substring(0, Math.min(2, name.length())).toUpperCase();
    }
}
