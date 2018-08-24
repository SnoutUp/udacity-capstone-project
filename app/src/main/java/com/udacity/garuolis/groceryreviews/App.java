package com.udacity.garuolis.groceryreviews;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
