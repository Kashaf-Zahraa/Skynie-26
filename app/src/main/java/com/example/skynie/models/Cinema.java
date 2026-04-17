package com.example.skynie.models;

public class Cinema {

    public String id;
    public String name;
    public String address;
    public String city;
    public String logo_url;
    public double latitude;
    public double longitude;

    public Cinema() {}

    public Cinema(String id, String name, String address, String city,
                  String logo_url, double latitude, double longitude) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.city = city;
        this.logo_url = logo_url;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
