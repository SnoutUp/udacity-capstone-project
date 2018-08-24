package com.udacity.garuolis.groceryreviews;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class BaseActivity extends AppCompatActivity {
    private final static String TAG = BaseActivity.class.getName();

    private FirebaseAuth mAuth;
    DatabaseReference mDatabase;
    StorageReference mStorage;
    private FirebaseUser mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
    }

    String getUserId() {
        if (mUser != null) {
            return mUser.getUid();
        }
        return "udacity-user";
    }

    @Override
    protected void onStart() {
        super.onStart();
        setupUser();
    }

    private void setupUser() {
        mUser = mAuth.getCurrentUser();
        if (mUser == null) {
            authenticateUser();
        }
    }


    private void authenticateUser() {
        mAuth.signInAnonymously().addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    mUser = mAuth.getCurrentUser();
                    Log.d(TAG, "signInAnonymously:success " + mUser.getDisplayName() + " " + mUser.getUid());
                    //updateUI(user);
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInAnonymously:failure", task.getException());
                    Toast.makeText(BaseActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    //updateUI(null);
                }
            }
        });
    }
}
