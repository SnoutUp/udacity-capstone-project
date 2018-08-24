package com.udacity.garuolis.groceryreviews;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.github.slugify.Slugify;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.udacity.garuolis.groceryreviews.data.MyUtils;
import com.udacity.garuolis.groceryreviews.data.Product;
import com.udacity.garuolis.groceryreviews.data.ProductReview;
import com.udacity.garuolis.groceryreviews.data.ReviewImage;
import com.udacity.garuolis.groceryreviews.databinding.ActivityReviewEditBinding;
import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickResult;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;


public class ReviewEditActivity extends BaseActivity implements IPickResult {
    public static final String KEY_PRODUCT_ID   = "product_id";
    private static final int REQ_PRODUCT_LOOKUP  = 1001;
    private Product mProduct;
    private ActivityReviewEditBinding mBinding;

    private String[] categoryTitles;
    private String[] categoryKeys;

    private Bitmap mSelectedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_review_edit);
        setSupportActionBar(mBinding.toolbar);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        categoryTitles = getResources().getStringArray(R.array.product_type_names);
        categoryKeys = getResources().getStringArray(R.array.product_type_keys);

        mBinding.content.rbRating.setRating(2.5f);

        mBinding.content.ivLookup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startLookupActivity();
            }
        });

        Intent intent = getIntent();
        if (intent.hasExtra(KEY_PRODUCT_ID)) {
            String productId = intent.getStringExtra(KEY_PRODUCT_ID);
            mDatabase.child(Product.NODE).child(productId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    mProduct = dataSnapshot.getValue(Product.class);
                    updateUiWithProduct(mProduct);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        updateCategorySpinner();
    }

    private void startLookupActivity() {
        Intent intent = new Intent(this, ProductSearchActivity.class);
        startActivityForResult(intent, REQ_PRODUCT_LOOKUP);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_PRODUCT_LOOKUP) {
            if (resultCode == RESULT_OK) {
                String productName = data.getStringExtra(ProductSearchActivity.EXTRA_PRODUCT_NAME);
                if (productName != null) {
                    mBinding.content.etProductName.setText(productName);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void updateCategorySpinner() {
        List<String>categoryStringList = new ArrayList<>();
        int indexToSelect = 0;

        categoryStringList.add(getString(R.string.spinner_title));

        for (int index = 0; index < categoryTitles.length; index ++) {
            categoryStringList.add(categoryTitles[index]);
            if (mProduct != null && mProduct.categoryKey.equalsIgnoreCase(categoryKeys[index])) {
                indexToSelect = index;
            }
        }

        ArrayAdapter<String> catAdapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_spinner_item, categoryStringList) {
            @Override
            public boolean isEnabled(int position) {
                if (position == 0) return false;
                return super.isEnabled(position);
            }
        };

        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mBinding.content.spinProductCategory.setAdapter(catAdapter);
        mBinding.content.spinProductCategory.setSelection(indexToSelect);
    }

    public void photoButtonClicked(View v) {
        PickImageDialog.build(new PickSetup()).show(this);
    }

    public void saveButtonClicked(View v) {
        createNewEntry();
    }

    private void showLoadingIndicator() {
        mBinding.content.bSubmit.setEnabled(false);
        mBinding.content.pbSpinner.setVisibility(View.VISIBLE);
    }

    private void hideLoadingIndicator() {
        mBinding.content.pbSpinner.setVisibility(View.GONE);
        mBinding.content.bSubmit.setEnabled(true);
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

    private void updateUiWithProduct(Product product) {
        mBinding.content.etProductName.setText(product.title);
        mBinding.content.etProductName.setEnabled(false);
    }

    private void createNewEntry() {
        Slugify slug    = new Slugify();


        String name     = mBinding.content.etProductName.getText().toString().trim();
        String review   = mBinding.content.etProductReview.getText().toString().trim();

        float rating    =  mBinding.content.rbRating.getRating();

        int spinnerIndex= mBinding.content.spinProductCategory.getSelectedItemPosition();
        int categoryIndex = (spinnerIndex > 0) ? spinnerIndex - 1 : -1;

        if (name.length() <= 2) {
            MyUtils.ShowErrorSnack(this, mBinding.body, R.string.error_missing_product_name);
            return;
        }

        if (spinnerIndex <= 0) {
            MyUtils.ShowErrorSnack(this, mBinding.body, R.string.error_missing_category);
            return;
        }

        if (review.length() <= 5) {
            MyUtils.ShowErrorSnack(this, mBinding.body, R.string.error_missing_review);
            return;
        }

        if (mSelectedImage != null && !MyUtils.IsConnected(this)) {
            MyUtils.ShowErrorSnack(this, mBinding.body, R.string.error_cant_upload);
            return;
        }

        showLoadingIndicator();

        if (mProduct == null) {
            String newProductKey = mDatabase.child(Product.NODE).push().getKey();
            mProduct = new Product(newProductKey, name, slug.slugify(name));

            if (categoryIndex != -1) {
                mProduct.categoryTitle  = categoryTitles[categoryIndex];
                mProduct.categoryKey    = categoryKeys[categoryIndex];
            }

            mDatabase.child(Product.NODE).child(newProductKey).setValue(mProduct);
        }

        String newReviewKey = mDatabase.child(ProductReview.NODE).push().getKey();
        ProductReview productReview = new ProductReview(getUserId(), newReviewKey, mProduct.id, rating, review);
        productReview.productTitle = name;

        mDatabase.child(ProductReview.NODE).child(newReviewKey).setValue(productReview);
        mDatabase.child(ProductReview.NODE_USER).child(getUserId()).child(newReviewKey).setValue(productReview);

        mDatabase.child(Product.NODE).child(mProduct.id).child("last_review").setValue(productReview);


        if (mSelectedImage != null) {
            uploadSelectedImage(newReviewKey);
        } else {
            MyUtils.ShowInfoSnack(this, mBinding.body, R.string.info_review_added );
            clearInputFields();
        }
    }

    private void clearInputFields() {
        hideLoadingIndicator();

        mBinding.content.etProductName.setText("");
        mBinding.content.etProductReview.setText("");
        mBinding.content.ivPhoto.setImageResource(R.drawable.placeholder);
        mBinding.content.spinProductCategory.setSelection(0);
        mBinding.content.rbRating.setRating(2.5f);


    }

    private void saveImageToDatabase(String productId, String reviewId, StorageMetadata meta) {
        String imageKey = mDatabase.child(ReviewImage.NODE).push().getKey();
        ReviewImage ri = new ReviewImage(meta.getName(), reviewId, productId);
        mDatabase.child(ReviewImage.NODE).child(imageKey).setValue(ri);

        // Update Product Review Entries With Image Id
        mDatabase.child(ProductReview.NODE).child(reviewId).child("image_id").setValue(imageKey);
        mDatabase.child(ProductReview.NODE_USER).child(getUserId()).child(reviewId).child("image_id").setValue(imageKey);

        MyUtils.ShowInfoSnack(this, mBinding.body, R.string.info_review_added);
        clearInputFields();
    }

    private void uploadSelectedImage(String reviewId) {
        StorageReference mountainImagesRef = mStorage.child(MyUtils.ImagePath(reviewId));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        mSelectedImage.compress(Bitmap.CompressFormat.JPEG, 75, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = mountainImagesRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                hideLoadingIndicator();
                exception.printStackTrace();
                MyUtils.ShowErrorSnack(ReviewEditActivity.this, mBinding.body, R.string.error_image_upload_failed);
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                saveImageToDatabase(mProduct.id, reviewId, taskSnapshot.getMetadata());
            }
        });
    }

    @Override
    public void onPickResult(PickResult r) {
        if (r.getError() == null) {
            mBinding.content.ivPhoto.setImageBitmap(r.getBitmap());
            mSelectedImage = r.getBitmap();
        } else {
            Toast.makeText(this, r.getError().getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
