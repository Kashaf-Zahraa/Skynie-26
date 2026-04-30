package com.example.skynie.models;

public class Movie {

    public String id;
    public String title;
    public String description;
    public String poster_drawable;     // Changed from poster_url
    public String backdrop_drawable;   // Changed from backdrop_url
    public String trailer_url;
    public float rating;
    public int duration_minutes;
    public String language;
    public long release_date;
    public String pg_rating;
    public String is_now_showing;
    public String is_coming_soon;

    public Movie() {}

    public Movie(String id, String title, String description, String poster_drawable,
                 String backdrop_drawable, String trailer_url, float rating,
                 int duration_minutes, String language, long release_date,
                 String pg_rating, String is_now_showing, String is_coming_soon) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.poster_drawable = poster_drawable;
        this.backdrop_drawable = backdrop_drawable;
        this.trailer_url = trailer_url;
        this.rating = rating;
        this.duration_minutes = duration_minutes;
        this.language = language;
        this.release_date = release_date;
        this.pg_rating = pg_rating;
        this.is_now_showing = is_now_showing;
        this.is_coming_soon = is_coming_soon;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPoster_drawable() {
        return poster_drawable;
    }

    public void setPoster_drawable(String poster_drawable) {
        this.poster_drawable = poster_drawable;
    }

    public String getBackdrop_drawable() {
        return backdrop_drawable;
    }

    public void setBackdrop_drawable(String backdrop_drawable) {
        this.backdrop_drawable = backdrop_drawable;
    }

    public String getTrailer_url() {
        return trailer_url;
    }

    public void setTrailer_url(String trailer_url) {
        this.trailer_url = trailer_url;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public int getDuration_minutes() {
        return duration_minutes;
    }

    public void setDuration_minutes(int duration_minutes) {
        this.duration_minutes = duration_minutes;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public long getRelease_date() {
        return release_date;
    }

    public void setRelease_date(long release_date) {
        this.release_date = release_date;
    }

    public String getPg_rating() {
        return pg_rating;
    }

    public void setPg_rating(String pg_rating) {
        this.pg_rating = pg_rating;
    }

    public String getIs_now_showing() {
        return is_now_showing;
    }

    public void setIs_now_showing(String is_now_showing) {
        this.is_now_showing = is_now_showing;
    }

    public String getIs_coming_soon() {
        return is_coming_soon;
    }

    public void setIs_coming_soon(String is_coming_soon) {
        this.is_coming_soon = is_coming_soon;
    }


}