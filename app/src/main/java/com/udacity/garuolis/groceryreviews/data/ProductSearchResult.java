package com.udacity.garuolis.groceryreviews.data;

public class ProductSearchResult {

    public String brand;
    public String title;

    public ProductSearchResult() {

    }

    public ProductSearchResult(String title, String brand) {
        this.title  = title;
        this.brand  = brand;
    }

}
