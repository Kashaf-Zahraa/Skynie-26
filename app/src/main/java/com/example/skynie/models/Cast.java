package com.example.skynie.models;

import java.util.ArrayList;
import java.util.List;

public class Cast {
    private String movieId;
    private ArrayList<String> directors;
    private ArrayList<String> writers;
    private ArrayList<String> actors;
    public Cast(){}

    public Cast(ArrayList<String> actors, String movieId, ArrayList<String> directors, ArrayList<String> writers) {
        this.actors = actors;
        this.movieId = movieId;
        this.directors = directors;
        this.writers = writers;
    }

    public String getMovieId() {
        return movieId;
    }

    public void setMovieId(String movieId) {
        this.movieId = movieId;
    }

    public ArrayList<String> getDirectors() {
        return directors;
    }

    public void setDirectors(ArrayList<String> directors) {
        this.directors = directors;
    }

    public ArrayList<String> getActors() {
        return actors;
    }

    public void setActors(ArrayList<String> actors) {
        this.actors = actors;
    }

    public ArrayList<String> getWriters() {
        return writers;
    }

    public void setWriters(ArrayList<String> writers) {
        this.writers = writers;
    }
}
