package com.udacity.garuolis.groceryreviews.data;

import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;

@IgnoreExtraProperties
public class ReviewImage {
    public final static String NODE = "review_images";

    public String name;

    @PropertyName("review_id")
    public String reviewId;

    @PropertyName("product_id")
    public String productId;

    public ReviewImage (){

    }

    public ReviewImage(String name, String reviewId, String productId) {
        this.name       = name;
        this.reviewId   = reviewId;
        this.productId  = productId;
    }

}
