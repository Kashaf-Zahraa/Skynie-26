package com.example.skynie.models;

public class Seat {
    public String id;                    // Unique booking ID "b1"
    public String hallShowtimeId;        // Reference to HallShowTime "hst1"
    public String row;                   // "A", "B", "C"
    public String seatNumber;            // "1", "5", "7"
    public String userId="";                // User who booked (for authentication), default empty
    public int price;                    // Price at time of booking

    public Seat() {}

    public Seat(String id, String hallShowtimeId, String row, String seatNumber,
                   String userId, int price) {
        this.id = id;
        this.hallShowtimeId = hallShowtimeId;
        this.row = row;
        this.seatNumber = seatNumber;
        this.userId = userId;
        this.price = price;
    }
    public boolean isAvailable(){
        return userId == null || userId.isEmpty();
    }
    public void BookSeat(String userId){
        this.userId=userId;
    }
    public String getFullSeatNumber() {
        return row + seatNumber;  // Returns "A1", "B5", etc.
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRow() {
        return row;
    }

    public void setRow(String row) {
        this.row = row;
    }

    public String getHallShowtimeId() {
        return hallShowtimeId;
    }

    public void setHallShowtimeId(String hallShowtimeId) {
        this.hallShowtimeId = hallShowtimeId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}
