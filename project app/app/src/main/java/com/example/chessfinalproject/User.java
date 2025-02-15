package com.example.chessfinalproject;

public class User {
    private String username;
    private String password;
    private UserCustomize userCustomize;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String username, String password, UserCustomize userCustomize) {
        this.username = username;
        this.password = password;
        this.userCustomize = userCustomize;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public UserCustomize getUserCustomize() {
        return userCustomize;
    }
}
