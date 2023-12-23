package com.waterfall.wheresmytutor.models;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class User {
    private String firstName, lastName, email, userType, userId;
    ArrayList<String> meetingIds;

    public User(String firstName, String lastName, String email, String userType, String userId) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.userType = userType;
        this.userId = userId;
    }

    public User(){

    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return getFirstName() + " " + getLastName();
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public ArrayList<String> getMeetingIds() {
        return meetingIds;
    }

    public void setMeetingIds(ArrayList<String> meetingIds) {
        this.meetingIds = meetingIds;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }

        final User userBeingTested = (User) obj;

        return this.userId.equals(userBeingTested.userId);
    }
}
