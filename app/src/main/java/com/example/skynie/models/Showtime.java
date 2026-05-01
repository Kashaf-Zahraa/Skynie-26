// Showtime.java
package com.example.skynie.models;

import java.util.Date;
import java.util.List;

public class Showtime {
    public String id;
    public String cinemaId;          // Reference to cinema "c1"
    public String hallId;            // Reference to hall "c1_h1"
    public String movieId;           // Reference to movie "movie1"
    public String time;              // "10:30 AM", "1:45 PM"
    public String date;
    public int availableSeats;
    public double price;
    public List<String> bookedSeats; // ["A1", "A2", "B5"] - seat numbers that are booked

    public Showtime() {}

    public Showtime(String id, String cinemaId, String hallId, String movieId, String time,
                    String date, int availableSeats, double price, List<String> bookedSeats) {
        this.id = id;
        this.cinemaId = cinemaId;
        this.hallId = hallId;
        this.movieId = movieId;
        this.time = time;
        this.date = date;
        this.availableSeats = availableSeats;
        this.price = price;
        this.bookedSeats = bookedSeats;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCinemaId() {
        return cinemaId;
    }

    public void setCinemaId(String cinemaId) {
        this.cinemaId = cinemaId;
    }

    public String getHallId() {
        return hallId;
    }

    public void setHallId(String hallId) {
        this.hallId = hallId;
    }

    public String getMovieId() {
        return movieId;
    }

    public void setMovieId(String movieId) {
        this.movieId = movieId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public List<String> getBookedSeats() {
        return bookedSeats;
    }

    public void setBookedSeats(List<String> bookedSeats) {
        this.bookedSeats = bookedSeats;
    }
}