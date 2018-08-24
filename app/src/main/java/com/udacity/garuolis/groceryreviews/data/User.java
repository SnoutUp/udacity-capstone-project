package com.udacity.garuolis.groceryreviews.data;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
class User {
    public final static String NODE = "users";

    private String username;
    private String email;

    public User() {

    }

    public User(String username, String email) {
        this.username = username;
        this.email = email;
    }
}
