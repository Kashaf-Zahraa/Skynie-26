package com.example.skynie.models;

//MANY TO MANY TABLE LIKE IN DB
public class HallShowTime {
    public String id;                // Unique ID for this junction object "hst1"
    public String hallId;            // Reference to Hall "c1_h4"
    public String showtimeId;        // Reference to Showtime "st8"
    public String audioFormat;       // "Dolby Atmos", "IMAX 12-channel", "7.1 Surround", etc.

    public HallShowTime() {}

    // HallShowTime.java - FIXED constructor
    public HallShowTime(String id, String hallId, String showtimeId, String audioFormat) {
        this.id = id;
        this.hallId = hallId;
        this.showtimeId = showtimeId;
        this.audioFormat = audioFormat;
    }

    private String getAudioFormatForScreenType(String screenType) {
        switch (screenType) {
            case "IMAX":
                return "IMAX 12-channel";
            case "ScreenX":
                return "Dolby Atmos";
            case "4DMAX":
                return "4D Motion Audio";
            case "GOLD":
                return "7.1 Surround";
            default:
                return "Digital";
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHallId() {
        return hallId;
    }

    public void setHallId(String hallId) {
        this.hallId = hallId;
    }

    public String getShowtimeId() {
        return showtimeId;
    }

    public void setShowtimeId(String showtimeId) {
        this.showtimeId = showtimeId;
    }

    public String getAudioFormat() {
        return audioFormat;
    }

    public void setAudioFormat(String audioFormat) {
        this.audioFormat = audioFormat;
    }
}
