package com.example.skynie.models;

public class Seat {

    public String id;
    public String hall_id;
    public String row_label;
    public int col_number;
    public String seat_type;

    public Seat() {}

    public Seat(String id, String hall_id, String row_label,
                int col_number, String seat_type) {
        this.id = id;
        this.hall_id = hall_id;
        this.row_label = row_label;
        this.col_number = col_number;
        this.seat_type = seat_type;
    }
}
