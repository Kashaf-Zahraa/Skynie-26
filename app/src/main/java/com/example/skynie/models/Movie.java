package com.example.skynie.models;

public class Movie {

    public String id;
    public String title;
    public String description;
    public String poster_url;
    public String backdrop_url;
    public String trailer_url;
    public float rating;
    public int duration_minutes;
    public String language;
    public long release_date;
    public String pg_rating;
    public boolean is_now_showing;
    public boolean is_coming_soon;

    public Movie() {}

    public Movie(String id, String title, String description, String poster_url,
                 String backdrop_url, String trailer_url, float rating,
                 int duration_minutes, String language, long release_date,
                 String pg_rating, boolean is_now_showing, boolean is_coming_soon) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.poster_url = poster_url;
        this.backdrop_url = backdrop_url;
        this.trailer_url = trailer_url;
        this.rating = rating;
        this.duration_minutes = duration_minutes;
        this.language = language;
        this.release_date = release_date;
        this.pg_rating = pg_rating;
        this.is_now_showing = is_now_showing;
        this.is_coming_soon = is_coming_soon;
    }
}
