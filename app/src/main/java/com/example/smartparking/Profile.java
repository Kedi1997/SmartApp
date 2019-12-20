package com.example.smartparking;

import com.google.firebase.Timestamp;

import java.util.Date;

public class Profile {
    private String userId;
    private String location;
    private Timestamp startTime;
    private Timestamp endTime;

    public Profile(){
        // no-arg constructor needed for firestore
    }

    public Profile(String userId, String location, Timestamp startTime, Timestamp endTime){
        this.userId = userId;
        this.location = location;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Timestamp getStartTime() {
        Date date = startTime.toDate();
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }


}
