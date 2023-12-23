package com.waterfall.wheresmytutor.models;

import java.util.ArrayList;

public class Student extends User {
    private ArrayList<String> myTutorsIds;

    public Student(String firstName, String lastName, String email, String userType, String userId, ArrayList<String> myTutorsIds) {
        super(firstName, lastName, email, userType, userId);
        this.myTutorsIds = myTutorsIds;
    }

    public Student() {
    }

    public ArrayList<String> getMyTutorsIds() {
        return myTutorsIds;
    }

    public void setMyTutorsIds(ArrayList<String> myTutorsIds) {
        this.myTutorsIds = myTutorsIds;
    }

}
