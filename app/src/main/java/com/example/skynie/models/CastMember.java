package com.example.skynie.models;

public class CastMember {

    public String id;
    public String movie_id;
    public String actor_name;
    public String character_name;
    public String photo_url;
    public String role;

    public CastMember() {}

    public CastMember(String id, String movie_id, String actor_name,
                      String character_name, String photo_url, String role) {
        this.id = id;
        this.movie_id = movie_id;
        this.actor_name = actor_name;
        this.character_name = character_name;
        this.photo_url = photo_url;
        this.role = role;
    }
}
