package com.example.chatapp.Model;

public class User {
    private String id;
    private String name;
    private String profile;
    private String status;
    private String email;

    public User(String id, String name, String profile, String status, String email) {
        this.id = id;
        this.name = name;
        this.profile = profile;
        this.status = status;
        this.email = email;
    }

    public User() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
