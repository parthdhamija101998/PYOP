package com.example.pyop.models;


import com.google.firebase.Timestamp;

public class CalendarEvent {
    String startDate;
    String endDate;
    String eventTime;
    String userID,eventTitle,eventDescription;
    Timestamp startDateIndex;

    public CalendarEvent() {
    }

    public Timestamp getStartDateIndex() {
        return startDateIndex;
    }

    public void setStartDateIndex(Timestamp startDateIndex) {
        this.startDateIndex = startDateIndex;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getEventTime() {
        return eventTime;
    }

    public void setEventTime(String eventTime) {
        this.eventTime = eventTime;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }
}
