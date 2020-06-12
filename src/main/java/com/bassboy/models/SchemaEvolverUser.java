package com.bassboy.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Date;

@Entity
@Table(name = "user")
public class SchemaEvolverUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private long id;
    private String username;
    @JsonIgnore
    private String password;
    private String socialId;
    private String email;
    private String displayName;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdTimestamp;// String used instead of java.time.LocalDateTime to let db handle setting timestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date modifiedTimestamp;// String used instead of java.time.LocalDateTime to let db handle setting timestamp

    public SchemaEvolverUser() {
    }

    public SchemaEvolverUser(RequestUserObject user) {
        this.username = user.getUsername();
        this.password = null;
        this.socialId = user.getSocialId();
        this.email = user.getEmail();
        this.id = user.getId();
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setSocialId(String socialId) {
        this.socialId = socialId;
    }

    public String getUsername() {
        return username;
    }

    public String getSocialId() {
        return socialId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setEmail(String email) { this.email = email; }

    public String getEmail() { return email; }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Date getCreatedTimestamp() {
        return createdTimestamp;
    }

    public Date getModifiedTimestamp() {
        return modifiedTimestamp;
    }

    public void setCreatedTimestamp() {
        Date date = new Date();
        this.createdTimestamp = new Timestamp(date.getTime());
    }

    public void setModifiedTimestamp() {
        Date date = new Date();
        this.modifiedTimestamp = new Timestamp(date.getTime());
    }
}
