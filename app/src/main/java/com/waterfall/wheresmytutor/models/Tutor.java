package com.waterfall.wheresmytutor.models;

import java.util.ArrayList;

public class Tutor extends User{
    private String status;
    private double latitude, longitude;
    private ArrayList<String> monTimeSlots, tueTimeSlots, wedTimeSlots, thurTimeSlots, friTimeSlots;

    public Tutor(String firstName, String lastName, String email, String userType, String userId, String status, double latitude, double longitude, ArrayList<String> monTimeSlots, ArrayList<String> tueTimeSlots, ArrayList<String> wedTimeSlots, ArrayList<String> thurTimeSlots, ArrayList<String> friTimeSlots) {
        super(firstName, lastName, email, userType, userId);
        this.status = status;
        this.latitude = latitude;
        this.longitude = longitude;
        this.monTimeSlots = monTimeSlots;
        this.tueTimeSlots = tueTimeSlots;
        this.wedTimeSlots = wedTimeSlots;
        this.thurTimeSlots = thurTimeSlots;
        this.friTimeSlots = friTimeSlots;
    }

    public Tutor() {
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ArrayList<String> getMonTimeSlots() {
        return monTimeSlots;
    }

    public void setMonTimeSlots(ArrayList<String> monTimeSlots) {
        this.monTimeSlots = monTimeSlots;
    }

    public ArrayList<String> getTueTimeSlots() {
        return tueTimeSlots;
    }

    public void setTueTimeSlots(ArrayList<String> tueTimeSlots) {
        this.tueTimeSlots = tueTimeSlots;
    }

    public ArrayList<String> getWedTimeSlots() {
        return wedTimeSlots;
    }

    public void setWedTimeSlots(ArrayList<String> wedTimeSlots) {
        this.wedTimeSlots = wedTimeSlots;
    }

    public ArrayList<String> getThurTimeSlots() {
        return thurTimeSlots;
    }

    public void setThurTimeSlots(ArrayList<String> thurTimeSlots) {
        this.thurTimeSlots = thurTimeSlots;
    }

    public ArrayList<String> getFriTimeSlots() {
        return friTimeSlots;
    }

    public void setFriTimeSlots(ArrayList<String> friTimeSlots) {
        this.friTimeSlots = friTimeSlots;
    }



}
