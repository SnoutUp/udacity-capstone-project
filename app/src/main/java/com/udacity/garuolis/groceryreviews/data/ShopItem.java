package com.udacity.garuolis.groceryreviews.data;

import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;

@IgnoreExtraProperties
public class ShopItem {
    public final static String NODE = "shopping_lists";

    @PropertyName("product_id")
    public String productId;

    @PropertyName("user_id")
    public String userId;

    @PropertyName("product_name")
    public String productName;

    public boolean marked;

    public ShopItem () {

    }

    public ShopItem (String userId, String productId, String productName) {
        this.userId         = userId;
        this.productId      = productId;
        this.productName    = productName;
        this.marked         = false;
    }


}
