package com.udacity.garuolis.groceryreviews;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.slugify.Slugify;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.udacity.garuolis.groceryreviews.adapters.ProductListAdapter;
import com.udacity.garuolis.groceryreviews.data.Product;
import com.udacity.garuolis.groceryreviews.data.ProductCategory;
import com.udacity.garuolis.groceryreviews.data.ProductReview;
import com.udacity.garuolis.groceryreviews.data.ReviewImage;
import com.udacity.garuolis.groceryreviews.databinding.ActivityMainTabsBinding;
import com.udacity.garuolis.groceryreviews.fragments.CategoryListFragment;
import com.udacity.garuolis.groceryreviews.fragments.ProductListFragment;
import com.udacity.garuolis.groceryreviews.fragments.ReviewListFragment;
import com.udacity.garuolis.groceryreviews.fragments.ShoppingListFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends BaseActivity  implements ProductListAdapter.ItemClickListener, CategoryListFragment.CategorySelectListener {
    public final static String EXTRA_START_TAB = "start_tab";

    public final static int TAB_PRODUCT_LIST    = 0;
    public final static int TAB_MY_REVIEWS      = 1;
    public final static int TAB_SHOPPING_LIST   = 2;

    private List<Product> mProductList;
    private List<ProductReview> mReviewList;

    private RecyclerView mRecycler;
    private ProductListAdapter mAdapter;

    private ValueEventListener mProductListener;
    private ValueEventListener mReviewListener;

    //private DataBin

    SectionsPagerAdapter mSectionsPagerAdapter;
    ActivityMainTabsBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main_tabs);
        setSupportActionBar(mBinding.toolbar);

        mProductList = new ArrayList<>();
        mReviewList = new ArrayList<>();

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mBinding.vpContainer.setAdapter(mSectionsPagerAdapter);
        mBinding.vpContainer.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mBinding.tabs));
        mBinding.tabs.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mBinding.vpContainer));

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            int startTab = bundle.getInt(EXTRA_START_TAB, TAB_PRODUCT_LIST);
            Log.v("mano", "starting with tab: " + startTab);
            mBinding.vpContainer.setCurrentItem(startTab);
        }

        /*
        mRecycler = findViewById(R.id.rv_list);
        mRecycler.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new ProductListAdapter(this, this);

        mReviewListAdapter = new ProductReviewAdapter(this, this);
        mRecycler.setAdapter(mReviewListAdapter);

        startReadingData();
               */

        /*
        findViewById(R.id.fab_edit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showReviewEditActivity(null);
            }
        });*/

    }

    @Override
    public void onCategorySelected(String categoryKey) {
        Log.v("mano", "should show page for category: " + categoryKey);
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 1:
                    return CategoryListFragment.newInstance();
                case 2:
                    return ShoppingListFragment.newInstance(mUser.getUid());
                default:
                    return ReviewListFragment.newInstance("a", "b");
            }

        }

        @Override
        public int getCount() {
            return 3;
        }
    }

    public void showReviewEditActivity(String productId) {
        Intent intent = new Intent(this, ReviewEditActivity.class);
        if (productId != null) {
            intent.putExtra(ReviewEditActivity.KEY_PRODUCT_ID, productId);
        }
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        stopReadingData();
    }

    public void stopReadingData() {
        if (mProductListener != null) {
            Query mProductQuery = mDatabase.child(Product.NODE);
            mProductQuery.removeEventListener(mProductListener);
            mProductListener = null;
        }
    }

    protected void startReadingData() {
        Slugify slug = new Slugify();

        Query mCategoryQuery = mDatabase.child(ProductCategory.NODE);

        Query mProductReviewQuery = mDatabase.child(ProductReview.NODE);
        Query mProductQuery = mDatabase.child(Product.NODE);

        mCategoryQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() == 0) {
                    String key = mDatabase.child(ProductCategory.NODE).push().getKey();
                    mDatabase.child(ProductCategory.NODE).child(key).setValue(new ProductCategory(key, "Milk Products"));
                    key = mDatabase.child(ProductCategory.NODE).push().getKey();
                    mDatabase.child(ProductCategory.NODE).child(key).setValue(new ProductCategory(key, "Meat Products"));
                    key = mDatabase.child(ProductCategory.NODE).push().getKey();
                    mDatabase.child(ProductCategory.NODE).child(key).setValue(new ProductCategory(key, "Bread Products"));
                    key = mDatabase.child(ProductCategory.NODE).push().getKey();
                    mDatabase.child(ProductCategory.NODE).child(key).setValue(new ProductCategory(key, "Vegetables"));
                    key = mDatabase.child(ProductCategory.NODE).push().getKey();
                    mDatabase.child(ProductCategory.NODE).child(key).setValue(new ProductCategory(key, "Fruit"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        mProductListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mProductList.clear();

                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    mProductList.add(snapshot.getValue(Product.class));
                }
                mAdapter.setItems(mProductList);
                loadReviewList();

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        mProductQuery.addValueEventListener(mProductListener);
    }

    public void loadReviewList() {
        Query mProductQuery = mDatabase.child(ProductReview.NODE);

        mProductListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mReviewList.clear();

                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    ProductReview review = snapshot.getValue(ProductReview.class);
                    review.imageRef = mStorage.child("images").child(snapshot.getKey() + ".jpg");
                    mReviewList.add(review);
                }

                Snackbar.make(findViewById(R.id.rv_list), "Loaded " + mReviewList.size() + " reviews", Snackbar.LENGTH_SHORT).show();

                Collections.reverse(mReviewList);
                //mReviewListAdapter.setItems(mReviewList);
                loadReviewImages();

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        mProductQuery.addValueEventListener(mProductListener);

    }

    public void loadReviewImages() {
        Query mReviewImageQuery = mDatabase.child(ReviewImage.NODE);

        mReviewImageQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    ReviewImage ri = snapshot.getValue(ReviewImage.class);
                    Log.v("mano", "review image: " + ri.name);

                    for (Product p : mProductList) {
                        if (p.id.equalsIgnoreCase(ri.productId)) {
                            p.image     = ri;
                            p.imageRef  = mStorage.child("images").child(ri.name);
                        }
                    }
                }

                mAdapter.setItems(mProductList);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onClick(Product product) {
        Log.v("mano", "product id: " + product.id);
        showReviewEditActivity(product.id);
    }
}
