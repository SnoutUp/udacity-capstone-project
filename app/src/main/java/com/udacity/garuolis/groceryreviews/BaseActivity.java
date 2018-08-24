package com.udacity.garuolis.groceryreviews;

import android.content.SharedPreferences;
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
import com.udacity.garuolis.groceryreviews.data.Product;
import com.udacity.garuolis.groceryreviews.data.ProductReview;

public class BaseActivity extends AppCompatActivity {
    public final static String PREF_FIRST_LAUNCH    = "first_launch";
    public final static String PREFERENCES          = "preferences";

    protected FirebaseAuth mAuth;
    protected DatabaseReference mDatabase;
    protected StorageReference mStorage;
    protected FirebaseUser mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        setupUser();
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
    }

    private void setupUser() {
        mUser = mAuth.getCurrentUser();
        if (mUser == null) {
            authenticateUser();
        }
    }


    private void authenticateUser() {
        mAuth.signInAnonymously().addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                mUser = mAuth.getCurrentUser();
                SharedPreferences prefs = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
                boolean firstLaunch = prefs.getBoolean(PREF_FIRST_LAUNCH, true);
                if (firstLaunch) {
                    createInitialData();
                    SharedPreferences.Editor edit = prefs.edit();
                    edit.putBoolean(PREF_FIRST_LAUNCH, false);
                    edit.commit();
                }
            } else {
                Toast.makeText(BaseActivity.this, R.string.error_auth_failed, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // creating dummy content
    private void createInitialData() {
        createDummyData(new Product("cheese", "Test Cheese", "cheese"), new ProductReview(getUserId(), "cheese", "cheese", 4.5f, "Delicious cheese product."), "milk", "Milk Products");
        createDummyData(new Product("apples", "These Apples", "apples"), new ProductReview(getUserId(), "apples", "apples", 3.0f, "Weird, but tasty apples."), "fruits", "Fruits");
        createDummyData(new Product("cake", "Sweet Cake", "cake"), new ProductReview(getUserId(), "cake", "cake", 5.0f, "Who doesn't like cake?"), "sweets", "Sweets");
        createDummyData(new Product("milk", "Such Milk", "milk"), new ProductReview(getUserId(), "milk", "milk", 4.0f, "I like milk"), "milk", "Milk Products");
        createDummyData(new Product("bread", "Garlic Bread", "bread"), new ProductReview(getUserId(), "bread", "bread", 5.0f, "True garlicky goodness. The best form any bread can ever take."), "bread", "Bread");
        createDummyData(new Product("pickles", "Pickles", "pickles"), new ProductReview(getUserId(), "pickles", "pickles", 1.5f, "Too sour for my taste"), "vegetables", "Vegetables");
    }

    private void createDummyData(Product product, ProductReview productReview, String categoryKey, String categoryTitle) {
        product.categoryTitle  = categoryTitle;
        product.categoryKey    = categoryKey;
        mDatabase.child(Product.NODE).child(product.id).setValue(product);

        productReview.productTitle = product.title;
        productReview.imageId = productReview.id;

        mDatabase.child(ProductReview.NODE).child(productReview.id).setValue(productReview);
        mDatabase.child(ProductReview.NODE_USER).child(getUserId()).child(productReview.id).setValue(productReview);

        mDatabase.child(Product.NODE).child(product.id).child("last_review").setValue(productReview);
    }
}
