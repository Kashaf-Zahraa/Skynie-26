package com.example.skynie.models;

public class User {

    public String id;
    public String full_name;
    public String email;
    public String password_hash;
    public String profile_picture_url;
    public User() {}

    public User(String id, String full_name, String email,String password_hash, String profile_picture_url) {
        this.id = id;
        this.full_name = full_name;
        this.email = email;
        this.password_hash = password_hash;
        this.profile_picture_url = profile_picture_url;
    }
}
