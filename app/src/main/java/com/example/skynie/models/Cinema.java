package com.example.skynie.models;

public class Cinema {

    public String id;
    public String name;
    public String address;
    public double latitude;
    public double longitude;
    public String screen_types;   // comma-separated: "IMAX,GOLD,ScreenX"
    public boolean is_active;

    public Cinema() {}

    public Cinema(String id, String name, String address,
                  double latitude, double longitude,
                  String screen_types, boolean is_active) {
        this.id           = id;
        this.name         = name;
        this.address      = address;
        this.latitude     = latitude;
        this.longitude    = longitude;
        this.screen_types = screen_types;
        this.is_active    = is_active;
    }
}