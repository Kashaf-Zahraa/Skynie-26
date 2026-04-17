package com.example.skynie.models;

public class BookingSeat {

    public String id;
    public String booking_id;
    public String seat_id;
    public String seat_type;
    public float price;

    public BookingSeat() {}

    public BookingSeat(String id, String booking_id, String seat_id,
                       String seat_type, float price) {
        this.id = id;
        this.booking_id = booking_id;
        this.seat_id = seat_id;
        this.seat_type = seat_type;
        this.price = price;
    }
}
