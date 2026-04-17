package com.example.skynie.models;

public class User {

    public String id;
    public String full_name;
    public String email;
    public String phone_number;
    public String password_hash;
    public String profile_picture_url;
    public String otp_code;
    public long otp_expires_at;
    public boolean is_verified;
    public long created_at;

    public User() {}

    public User(String id, String full_name, String email, String phone_number,
                String password_hash, String profile_picture_url, String otp_code,
                long otp_expires_at, boolean is_verified, long created_at) {
        this.id = id;
        this.full_name = full_name;
        this.email = email;
        this.phone_number = phone_number;
        this.password_hash = password_hash;
        this.profile_picture_url = profile_picture_url;
        this.otp_code = otp_code;
        this.otp_expires_at = otp_expires_at;
        this.is_verified = is_verified;
        this.created_at = created_at;
    }
}
