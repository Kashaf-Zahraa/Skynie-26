// Cinema.java
package com.example.skynie.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Cinema implements Serializable {
    public String id;
    public String name;
    public String address;
    public double latitude;
    public double longitude;
    public List<String> screenTypes;        // ["IMAX", "GOLD", "ScreenX", "4DMAX"]
    public List<String> hallIds;            // ["c1_h1", "c1_h2", "c1_h3"] - references to Hall objects
    public List<String> hallShowtimeIds;    // ["hst1", "hst2", "hst3"] - references to HallShowTime objects
    public boolean is_active;

    public Cinema() {}

    public Cinema(String id, String name, String address, double latitude, double longitude,
                  List<String> screenTypes, List<String> hallIds, List<String> hallShowtimeIds, boolean isActive) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.screenTypes = screenTypes;
        this.hallIds = hallIds;
        this.hallShowtimeIds = hallShowtimeIds;
        this.is_active = isActive;
    }
    public Cinema(String id, String name, String address, double latitude, double longitude, List<String> screenTypes, boolean isActive) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.screenTypes = screenTypes;
        this.is_active = isActive;
        this.hallIds = new ArrayList<>();
        this.hallShowtimeIds = new ArrayList<>();
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

    public List<String> getHallShowtimeIds() {
        return hallShowtimeIds;
    }

    public void setHallShowtimeIds(List<String> hallShowtimeIds) {
        this.hallShowtimeIds = hallShowtimeIds;
    }

    public List<String> getHallIds() {
        return hallIds;
    }

    public void setHallIds(List<String> hallIds) {
        this.hallIds = hallIds;
    }

    public List<String> getScreenTypes() {
        return screenTypes;
    }

    public void setScreenTypes(List<String> screenTypes) {
        this.screenTypes = screenTypes;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
}