package com.bassboy.models;


public class RequestUserObject {

    private long id;
    private String username;
    private String socialId;
    private String email;

    @Override
    public String toString() {
        return "RequestUserObject{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", socialId='" + socialId + '\'' +
                ", email='" + email + '\'' +
                '}';
    }

    public RequestUserObject() {
    }

    public RequestUserObject(long id, String email) {
        this.id = id;
        this.email = email;
    }

    public RequestUserObject(String email) {
        this.email = email;
    }

    public RequestUserObject(long id) {
        this.id = id;
    }

    public RequestUserObject(long id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }

    public RequestUserObject(long id, String username, String displayName, String email) {
        this.id = id;
        this.username = username;
        this.socialId = displayName;
        this.email = email;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSocialId() {
        return socialId;
    }

    public void setSocialId(String socialId) {
        this.socialId = socialId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
