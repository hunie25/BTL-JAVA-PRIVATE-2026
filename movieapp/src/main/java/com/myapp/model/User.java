package com.myapp.model;

import javafx.stage.Stage;

public class User {
    private int id;
    private String username;
    private String password;
    private String email;
    private boolean verified;

    public User(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.verified = false;
    }

    public User(int id, String username, String password, String email, boolean verified) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.verified = verified;
    }

    // Hỗ trợ constructor cũ
    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.verified = true; // giả định verified cho dữ liệu cũ
    }

    public User(int id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.verified = true;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getEmail() { return email; }
    public boolean isVerified() { return verified; }
    
    public void setVerified(boolean verified) { this.verified = verified; }
}
