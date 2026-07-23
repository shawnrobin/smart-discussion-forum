package com.smartforum.model;

public class Group {
    public int id;
    public String name;
    public String description;
    public int members;
    public int topics;

    @Override
    public String toString() {
        return name;
    }
}
