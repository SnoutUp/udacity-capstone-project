package com.udacity.garuolis.groceryreviews;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.github.slugify.Slugify;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.udacity.garuolis.groceryreviews.adapters.ProductListAdapter;
import com.udacity.garuolis.groceryreviews.data.MyUtils;
import com.udacity.garuolis.groceryreviews.data.Product;
import com.udacity.garuolis.groceryreviews.data.ProductReview;
import com.udacity.garuolis.groceryreviews.data.ShopItem;
import com.udacity.garuolis.groceryreviews.databinding.ActivityProductListBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProductListActivity extends BaseActivity  implements ProductListAdapter.ItemClickListener, SearchView.OnQueryTextListener{
    public final static String EXTRA_CATEGORY_KEY   = "category_key";
    public final static String EXTRA_CATEGORY_TITLE = "category_title";

    private ActivityProductListBinding mBinding;

    private List<Product> mProductList;
    private List<Product> mResultList;

    private ProductListAdapter mAdapter;
    private ValueEventListener mValueListener;

    private String mCategoryKey;
    private Slugify slugifier;
    private SearchView mSearchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_product_list);

        setSupportActionBar(mBinding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        mAdapter = new ProductListAdapter(this, this);

        mBinding.rvList.setLayoutManager(new LinearLayoutManager(this));
        mBinding.rvList.setAdapter(mAdapter);
        mBinding.rvList.setEmptyViewDetails(getString(R.string.category_product_list_is_empty), R.drawable.ic_basket);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String categoryTitle = bundle.getString(EXTRA_CATEGORY_TITLE);
            mCategoryKey = bundle.getString(EXTRA_CATEGORY_KEY);
            if (categoryTitle != null) {
                getSupportActionBar().setTitle(categoryTitle);
            }
        }

        slugifier = new Slugify();

        mProductList = new ArrayList<>();
        mResultList = new ArrayList<>();

        mValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mProductList.clear();
                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    Product product = snapshot.getValue(Product.class);
                    mProductList.add(product);
                }

                Collections.reverse(mProductList);
                mAdapter.setItems(mProductList);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };

        startListeningForProducts();
    }

    private void startListeningForProducts() {
        mDatabase.child(Product.NODE).orderByChild("category_type").equalTo(mCategoryKey).addValueEventListener(mValueListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_product_list, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);MenuItem searchMenuItem = menu.findItem(R.id.search);

        mSearchView = (SearchView) searchMenuItem.getActionView();

        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        mSearchView.setSubmitButtonEnabled(true);
        mSearchView.setOnQueryTextListener(this);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(Product product) {
        if (product.lastReview != null) {
            showProductReviewActvitiy(product.lastReview);
        }
    }

    private void showProductReviewActvitiy(ProductReview productReview) {
        Intent intent = new Intent(this, ProductReviewActivity.class);
        intent.putExtra(ProductReviewActivity.EXTRA_REVIEW_ID, productReview.id);
        intent.putExtra(ProductReviewActivity.EXTRA_PRODUCT_ID, productReview.productId);
        startActivity(intent);
    }

    @Override
    public void onBasketButtonClick(Product product) {
        toggleShoppingList(product);
    }

    private void toggleShoppingList(Product product) {

        DatabaseReference ref = mDatabase.child(ShopItem.NODE).child(getUserId()).child(product.id);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ShopItem item = dataSnapshot.getValue(ShopItem.class);

                if (item == null) {
                    ref.setValue(new ShopItem(getUserId(), product.id, product.title));
                    MyUtils.ShowInfoSnack(getBaseContext(), mBinding.rvList, getString(R.string.info_shopping_item_added, product.title));
                } else {
                    ref.removeValue();
                    MyUtils.ShowInfoSnack(getBaseContext(), mBinding.rvList, getString(R.string.info_shopping_item_removed, product.title));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        searchForItems(query);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        searchForItems(newText);
        return false;
    }


    private void searchForItems(String searchString) {
        String slugified = slugifier.slugify(searchString.toLowerCase().trim());

        mResultList.clear();
        for (Product p : mProductList) {
            String slugifiedTitle = slugifier.slugify(p.title);
            if (slugifiedTitle.contains(slugified) || slugified.length() == 0) {
                mResultList.add(p);
            }
        }

        mAdapter.setItems(mResultList);
    }
}
