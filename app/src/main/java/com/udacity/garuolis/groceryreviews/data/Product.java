package com.udacity.garuolis.groceryreviews.data;

import com.github.slugify.Slugify;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;

@IgnoreExtraProperties
public class Product {
    public final static String NODE = "products";

    public String id;
    public String title;
    private String slug;

    @PropertyName("last_review")
    public ProductReview lastReview;

    @PropertyName("category_type")
    public String categoryKey;

    @PropertyName("category_title")
    public String categoryTitle;

    @PropertyName("image_id")
    public String imageId;

    private HashMap<String, Object> timestampCreated;

    @Exclude
    public StorageReference imageRef;

    public Product() {

    }

    public Product(String id, String title, String slug) {
        this.id     = id;
        this.title  = title;
        this.slug   = slug;
        HashMap<String, Object> timestampNow = new HashMap<>();
        timestampNow.put("timestamp", ServerValue.TIMESTAMP);
        this.timestampCreated = timestampNow;
    }

    public HashMap<String, Object> getTimestampCreated(){
        return timestampCreated;
    }

    @Exclude
    public long getTimestampCreatedLong(){
        if (timestampCreated != null) {
            return (long) timestampCreated.get("timestamp");
        }
        return 0;
    }
}
