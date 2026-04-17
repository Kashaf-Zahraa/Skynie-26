package com.example.skynie.models;

public class Booking {

    public String id;
    public String user_id;
    public String showtime_id;
    public long booking_date;
    public float total_price;
    public String status;
    public String payment_method;
    public String payment_status;
    public String booking_reference;

    public Booking() {}

    public Booking(String id, String user_id, String showtime_id, long booking_date,
                   float total_price, String status, String payment_method,
                   String payment_status, String booking_reference) {
        this.id = id;
        this.user_id = user_id;
        this.showtime_id = showtime_id;
        this.booking_date = booking_date;
        this.total_price = total_price;
        this.status = status;
        this.payment_method = payment_method;
        this.payment_status = payment_status;
        this.booking_reference = booking_reference;
    }
}
