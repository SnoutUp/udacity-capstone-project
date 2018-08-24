package com.udacity.garuolis.groceryreviews;

import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.udacity.garuolis.groceryreviews.data.Product;
import com.udacity.garuolis.groceryreviews.data.ProductReview;
import com.udacity.garuolis.groceryreviews.databinding.ActivityMainTabsBinding;
import com.udacity.garuolis.groceryreviews.fragments.CategoryListFragment;
import com.udacity.garuolis.groceryreviews.fragments.ReviewListFragment;
import com.udacity.garuolis.groceryreviews.fragments.ShoppingListFragment;

public class MainActivity extends BaseActivity  implements CategoryListFragment.CategorySelectListener, ReviewListFragment.ReviewItemClickListener {
    public final static String EXTRA_START_TAB      = "start_tab";


    private final static int TAB_PRODUCT_LIST    = 0;
    public final static int TAB_MY_REVIEWS      = 1;
    public final static int TAB_SHOPPING_LIST   = 2;

    private ValueEventListener mProductListener;

    //private DataBin

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ActivityMainTabsBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main_tabs);
        setSupportActionBar(mBinding.toolbar);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mBinding.vpContainer.setAdapter(mSectionsPagerAdapter);
        mBinding.vpContainer.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mBinding.tabs){
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                switch (position) {
                    case 2:
                        hideFab();
                        break;
                    default:
                        showFab();
                        break;
                }
            }
        });
        mBinding.tabs.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mBinding.vpContainer));

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            int startTab = bundle.getInt(EXTRA_START_TAB, TAB_PRODUCT_LIST);
            mBinding.vpContainer.setCurrentItem(startTab);
        }

        findViewById(R.id.fab_edit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showReviewEditActivity(null);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v("mano", getUserId());
    }

    @Override
    public void onCategorySelected(String categoryKey, String categoryTitle) {
        showProductListActivity(categoryKey, categoryTitle);
    }

    private void showProductListActivity(String categoryKey, String categoryTitle) {
        Intent intent = new Intent(this, ProductListActivity.class);
        intent.putExtra(ProductListActivity.EXTRA_CATEGORY_KEY, categoryKey);
        intent.putExtra(ProductListActivity.EXTRA_CATEGORY_TITLE, categoryTitle);
        startActivity(intent);
    }

    private void showProductReviewActvitiy(ProductReview productReview) {
        Intent intent = new Intent(this, ProductReviewActivity.class);
        intent.putExtra(ProductReviewActivity.EXTRA_REVIEW_ID, productReview.id);
        intent.putExtra(ProductReviewActivity.EXTRA_PRODUCT_ID, productReview.productId);
        startActivity(intent);
    }

    @Override
    public void onReviewClicked(ProductReview productReview) {
        showProductReviewActvitiy(productReview);
    }

    class SectionsPagerAdapter extends FragmentPagerAdapter {


        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 1:
                    return CategoryListFragment.newInstance();
                case 2:
                    return ShoppingListFragment.newInstance(getUserId());
                default:
                    return ReviewListFragment.newInstance(getUserId());
            }

        }

        @Override
        public int getCount() {
            return 3;
        }
    }

    private void showReviewEditActivity(String productId) {
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

    private void showFab() {
        mBinding.fabEdit.setVisibility(View.VISIBLE);
    }

    private void hideFab() {
        mBinding.fabEdit.setVisibility(View.GONE);
    }

    private void stopReadingData() {
        if (mProductListener != null) {
            Query mProductQuery = mDatabase.child(Product.NODE);
            mProductQuery.removeEventListener(mProductListener);
            mProductListener = null;
        }
    }
}
