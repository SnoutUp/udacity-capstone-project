package com.udacity.garuolis.groceryreviews.data;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {
    public final static String NODE = "users";

    public String username;
    public String email;

    public User() {

    }

    public User(String username, String email) {
        this.username = username;
        this.email = email;
    }
}
