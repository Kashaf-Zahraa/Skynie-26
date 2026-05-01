// Hall.java
package com.example.skynie.models;

public class Hall {
    public String id;
    public String cinemaId;          // Reference to parent cinema "c1"
    public String hallNumber;        // "Hall 1", "Hall 7", etc.
    public String screenType;        // "IMAX", "GOLD", "ScreenX", "4DMAX" - one hall -> one screen type
    public int totalSeats;
    public boolean is_active;

    public Hall() {}

    public Hall(String id, String cinemaId, String hallNumber, String screenType, int totalSeats, boolean isActive) {
        this.id = id;
        this.cinemaId = cinemaId;
        this.hallNumber = hallNumber;
        this.screenType = screenType;
        this.totalSeats = totalSeats;
        this.is_active = isActive;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isActive() {
        return is_active;
    }

    public void setActive(boolean active) {
        is_active = active;
    }

    public String getCinemaId() {
        return cinemaId;
    }

    public void setCinemaId(String cinemaId) {
        this.cinemaId = cinemaId;
    }

    public String getHallNumber() {
        return hallNumber;
    }

    public void setHallNumber(String hallNumber) {
        this.hallNumber = hallNumber;
    }

    public String getScreenType() {
        return screenType;
    }

    public void setScreenType(String screenType) {
        this.screenType = screenType;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(int totalSeats) {
        this.totalSeats = totalSeats;
    }
}