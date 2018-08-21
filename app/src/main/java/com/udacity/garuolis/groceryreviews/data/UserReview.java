package com.udacity.garuolis.groceryreviews.data;

import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;

@IgnoreExtraProperties
public class UserReview {
    public final static String NODE = "user_reviews";

    @PropertyName("user_id")
    public String userId;

    @PropertyName("review_id")
    public String reviewId;
}
