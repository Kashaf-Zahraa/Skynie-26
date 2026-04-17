package com.example.skynie.models;

public class Ticket {

    public String id;
    public String booking_id;
    public String seat_id;
    public String qr_code;
    public boolean is_used;
    public long used_at;

    public Ticket() {}

    public Ticket(String id, String booking_id, String seat_id,
                  String qr_code, boolean is_used, long used_at) {
        this.id = id;
        this.booking_id = booking_id;
        this.seat_id = seat_id;
        this.qr_code = qr_code;
        this.is_used = is_used;
        this.used_at = used_at;
    }
}
