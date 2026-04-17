package com.example.skynie.models;

public class Hall {

    public String id;
    public String cinema_id;
    public String name;
    public String hall_type;
    public int total_rows;
    public int total_cols;

    public Hall() {}

    public Hall(String id, String cinema_id, String name,
                String hall_type, int total_rows, int total_cols) {
        this.id = id;
        this.cinema_id = cinema_id;
        this.name = name;
        this.hall_type = hall_type;
        this.total_rows = total_rows;
        this.total_cols = total_cols;
    }
}
