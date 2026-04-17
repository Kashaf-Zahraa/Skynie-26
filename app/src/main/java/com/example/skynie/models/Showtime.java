package com.example.skynie.models;

public class Showtime {

    public String id;
    public String movie_id;
    public String hall_id;
    public long show_date;
    public long show_time;
    public float base_price;
    public String format;
    public String status;

    public Showtime() {}

    public Showtime(String id, String movie_id, String hall_id, long show_date,
                    long show_time, float base_price, String format, String status) {
        this.id = id;
        this.movie_id = movie_id;
        this.hall_id = hall_id;
        this.show_date = show_date;
        this.show_time = show_time;
        this.base_price = base_price;
        this.format = format;
        this.status = status;
    }
}
