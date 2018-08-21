package com.udacity.garuolis.groceryreviews.data;

import com.github.slugify.Slugify;
import com.google.firebase.database.PropertyName;

public class ProductCategory {
    public final static String NODE = "product_categories";

    public String id;
    public String title;
    public String slug;

    @PropertyName("parent_id")
    public String parentId;

    @PropertyName("product_count")
    public int productCount = 0;

    public ProductCategory(){

    }

    public ProductCategory(String id, String title) {
        this.id         = id;
        this.title      = title;
        Slugify slugify = new Slugify();
        this.slug       = slugify.slugify(title);
    }

    public ProductCategory(String id, String title, String parentId) {
        this(id, title);
        this.parentId   = parentId;
    }

}
