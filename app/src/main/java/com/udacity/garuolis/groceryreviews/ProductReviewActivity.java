package com.udacity.garuolis.groceryreviews;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.udacity.garuolis.groceryreviews.data.MyUtils;
import com.udacity.garuolis.groceryreviews.data.Product;
import com.udacity.garuolis.groceryreviews.data.ProductReview;
import com.udacity.garuolis.groceryreviews.data.ShopItem;
import com.udacity.garuolis.groceryreviews.databinding.ActivityProductReviewBinding;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ProductReviewActivity extends BaseActivity {
    public final static String EXTRA_REVIEW_ID  = "review_id";
    public final static String EXTRA_PRODUCT_ID = "product_id";

    private ProductReview mProductReview;
    private Product mProduct;
    private Bitmap mReviewImage;

    private ValueEventListener mProductValueListener;
    private ValueEventListener mReviewValueListener;

    private String mReviewId;
    private String mProductId;

    private ActivityProductReviewBinding mBinding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_product_review);
        setSupportActionBar(mBinding.toolbar);

        getSupportActionBar().setTitle(R.string.title_activity_product_review);
        //getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareReview(mProduct, mProductReview);
            }
        });

        createListeners();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mReviewId = bundle.getString(EXTRA_REVIEW_ID);
            mProductId = bundle.getString(EXTRA_PRODUCT_ID);

            loadReviewData(mReviewId);
            loadProductData(mProductId);
        }


    }

    private void createListeners() {
        mProductValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mProduct = dataSnapshot.getValue(Product.class);
                displayProductData(mProduct);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        mReviewValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mProductReview = dataSnapshot.getValue(ProductReview.class);
                if (mProductReview != null && mProductReview.imageId != null) {
                    mProductReview.imageRef = mStorage.child(MyUtils.ImagePath(mProductReview.id));
                }
                displayReviewData(mProductReview);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        removeValueListeners();
    }

    private void loadReviewData(String reviewId) {
        mDatabase.child(ProductReview.NODE).child(reviewId).addListenerForSingleValueEvent(mReviewValueListener);
    }

    private void loadProductData(String productId) {
        mDatabase.child(Product.NODE).child(productId).addListenerForSingleValueEvent(mProductValueListener);
    }

    private void removeValueListeners() {
        mDatabase.child(ProductReview.NODE).child(mReviewId).removeEventListener(mReviewValueListener);
        mDatabase.child(Product.NODE).child(mReviewId).removeEventListener(mProductValueListener);

    }

    private void displayProductData(Product product) {
        mBinding.tvTitle.setText(product.title);
        mBinding.tvCategory.setText(product.categoryTitle);
    }

    private void displayReviewData(ProductReview productReview) {
        mBinding.content.rbRating.setIsIndicator(true);
        mBinding.content.rbRating.setRating(productReview.rating);
        mBinding.content.tvReview.setText(productReview.review);


        if (productReview.imageRef != null) {
            /// mBinding.ivHeaderImage
            Glide.with(this).using(new FirebaseImageLoader()).load(productReview.imageRef).asBitmap().centerCrop().into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                    mReviewImage = resource;
                    mBinding.ivHeaderImage.setImageBitmap(resource);
                }
            });
        } else {
            //mBinding.ivHeaderImage.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_to_list:
                toggleShoppingList(mProduct);
                break;
            case R.id.action_delete:
                showDeleteConfirmation(mProductReview);
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private void showDeleteConfirmation(ProductReview review) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.alert_deletion_title).setMessage(R.string.alert_deletion_text)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        deleteReview(review);
                    }
                }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void toggleShoppingList(Product product) {

        DatabaseReference ref = mDatabase.child(ShopItem.NODE).child(getUserId()).child(product.id);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ShopItem item = dataSnapshot.getValue(ShopItem.class);

                if (item == null) {
                    ref.setValue(new ShopItem(getUserId(), product.id, product.title));
                    MyUtils.ShowInfoSnack(getBaseContext(), mBinding.body, getString(R.string.info_shopping_item_added, product.title));
                } else {
                    ref.removeValue();
                    MyUtils.ShowInfoSnack(getBaseContext(), mBinding.body, getString(R.string.info_shopping_item_removed, product.title));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void deleteReview(ProductReview review) {
        mDatabase.child(Product.NODE).child(review.productId).removeValue();
        mDatabase.child(ProductReview.NODE).child(review.id).removeValue();
        mDatabase.child(ProductReview.NODE_USER).child(getUserId()).child(review.id).removeValue();
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_product_review, menu);
        return super.onCreateOptionsMenu(menu);
    }


    private void shareReview(Product product, ProductReview review) {
        if (review != null && product != null) {

            Intent share = new Intent(Intent.ACTION_SEND);

            if (mReviewImage != null) {
                share.setType("image/jpeg");
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                mReviewImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                File f = new File(Environment.getExternalStorageDirectory() + File.separator + "temp.jpg");
                try {
                    f.createNewFile();
                    FileOutputStream fo = new FileOutputStream(f);
                    fo.write(bytes.toByteArray());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                share.putExtra(Intent.EXTRA_STREAM, Uri.parse(Environment.getExternalStorageDirectory() + File.separator  + "temp.jpg"));
            } else {
                share.setType("text/plain");
            }

            share.putExtra(Intent.EXTRA_SUBJECT, product.title);
            share.putExtra(Intent.EXTRA_TEXT, review.review);
            startActivity(Intent.createChooser(share, "Share Image"));
        }
    }

}
