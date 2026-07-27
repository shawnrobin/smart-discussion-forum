package com.smartforum.model;

/**
 * A group member, as returned by GET /groups/{group}/members.
 * Used to populate the "exclude these people" picker on the reply form
 * (Requirement #3: excluding selected members from a communication).
 */
public class GroupMember {
    public int id;
    public String name;
    public String role;

    @Override
    public String toString() {
        return name;
    }
}
