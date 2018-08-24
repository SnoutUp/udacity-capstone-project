package com.udacity.garuolis.groceryreviews.data;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class ProductReview {
    public final static String NODE = "product_reviews";
    public final static String NODE_USER = "user_product_reviews";

    public String id;

    public String review;
    public float rating;

    public Product product;

    @PropertyName("product_id")
    public String productId;

    @PropertyName("product_title")
    public String productTitle;

    @PropertyName("image_id")
    public String imageId;

    @PropertyName("user_id")
    private String userId;

    @PropertyName("user_name")
    public String userName;

    @Exclude
    public ReviewImage image;

    @Exclude
    public StorageReference imageRef;

    private HashMap<String, Object> timestampCreated;

    public ProductReview (){
    }

    public ProductReview(String userId, String id, String productId, float rating, String review) {
        this.id                 = id;
        this.userId             = userId;
        this.review             = review;
        this.rating             = rating;
        this.productId          = productId;

        HashMap<String, Object> timestampNow = new HashMap<>();
        timestampNow.put("timestamp", ServerValue.TIMESTAMP);
        this.timestampCreated = timestampNow;
    }

    public HashMap<String, Object> getTimestampCreated(){
        return timestampCreated;
    }

    @Exclude
    public long getTimestampCreatedLong(){
        return (long)timestampCreated.get("timestamp");
    }
}
