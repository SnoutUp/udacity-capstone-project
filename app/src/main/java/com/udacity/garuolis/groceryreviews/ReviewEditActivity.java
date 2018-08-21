package com.udacity.garuolis.groceryreviews;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.slugify.Slugify;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseError;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.udacity.garuolis.groceryreviews.adapters.ProductListAdapter;
import com.udacity.garuolis.groceryreviews.data.Product;
import com.udacity.garuolis.groceryreviews.data.ProductCategory;
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
import java.util.Locale;


public class ReviewEditActivity extends AppCompatActivity implements IPickResult {
    public static final String KEY_PRODUCT_ID = "product_id";
    private Product mProduct;
    private DatabaseReference mDb;
    private StorageReference mSt;

    ActivityReviewEditBinding mBinding;

    List<ProductCategory> categoryList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_review_edit);
        setSupportActionBar(mBinding.toolbar);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (mDb == null) {
            mDb = FirebaseDatabase.getInstance().getReference();
        }

        if (mSt == null) {
            mSt = FirebaseStorage.getInstance().getReference();
        }


        mBinding.content.etProductReview.setText("Tasty product, would highly recommend with salad or pizza. Will buy again.");
        mBinding.content.rbRating.setRating(4.5f);

        Intent intent = getIntent();
        if (intent.hasExtra(KEY_PRODUCT_ID)) {
            String productId = intent.getStringExtra(KEY_PRODUCT_ID);
            mDb.child(Product.NODE).child(productId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    mProduct = dataSnapshot.getValue(Product.class);
                    Log.v("mano", "product loaded: " + mProduct.title + " category: " + mProduct.categoryId);
                    updateUiWithProduct(mProduct);
                    displayProductReviews(mProduct);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        loadCategories();
    }

    public void updateCategorySpinner(List<ProductCategory> categories) {
        List<String>categoryStringList = new ArrayList<>();
        int indexToSelect = 0;

        categoryStringList.add("Product Category");
        int index = 1;
        for (ProductCategory pc : categories) {
            categoryStringList.add(pc.title);

            if (mProduct != null && mProduct.categoryId.equalsIgnoreCase(pc.id)) {
                indexToSelect = index;
            }
            index ++;
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

        Log.v("mano", "selecting index: " + indexToSelect);
    }

    public void loadCategories() {
        categoryList = new ArrayList<>();

        mDb.child(ProductCategory.NODE).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                categoryList.clear();
                for (DataSnapshot snap : dataSnapshot.getChildren()) {
                    categoryList.add(snap.getValue(ProductCategory.class));
                }
                updateCategorySpinner(categoryList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void photoButtonClicked(View v) {
        PickImageDialog.build(new PickSetup()).show(this);
    }

    public void saveButtonClicked(View v) {
        createNewEntry();
    }

    public void displayProductReviews(Product product) {
        List<ProductReview> reviews = new ArrayList<>();
        TextView tv = (TextView) findViewById(R.id.tv_dump);

        mDb.child(ProductReview.NODE).orderByChild("product_id").equalTo(product.id).addValueEventListener(new ValueEventListener() {
        @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                reviews.clear();

                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    ProductReview review = snapshot.getValue(ProductReview.class);
                    reviews.add(review);
                }
                tv.setText("");
                String str = "";
                for (ProductReview pr : reviews) {
                    str += pr.review + "(" + pr.rating + ") | " + pr.getTimestampCreatedLong() + " \n";
                }

                tv.setText(str);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


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

    protected void updateUiWithProduct(Product product) {
        ((EditText) findViewById(R.id.et_product_name)).setText(product.title);
        ((EditText) findViewById(R.id.et_product_name)).setEnabled(false);
    }

    protected void createNewEntry() {
        Slugify slug    = new Slugify();

        String name     = ((EditText) findViewById(R.id.et_product_name)).getText().toString();
        String review   = ((EditText) findViewById(R.id.et_product_review)).getText().toString();
        float rating    = ((RatingBar) findViewById(R.id.rb_rating)).getRating();

        int spinnerIndex= mBinding.content.spinProductCategory.getSelectedItemPosition();

        ProductCategory category = null;

        if (categoryList != null && categoryList.size() > 0 && spinnerIndex > 0) {
            category = categoryList.get(spinnerIndex - 1);
        }

        if (mProduct == null) {
            String newProductKey = mDb.child(Product.NODE).push().getKey();
            mProduct = new Product(newProductKey, name, slug.slugify(name));

            if (category != null) {
                mProduct.categoryId = category.id;
            }

            mDb.child(Product.NODE).child(newProductKey).setValue(mProduct);
        }

        String newReviewKey = mDb.child(ProductReview.NODE).push().getKey();
        Log.v("mano", "create new review: " + newReviewKey + " for product: " + mProduct.id);

        mDb.child(ProductReview.NODE).child(newReviewKey).setValue(new ProductReview(mProduct.id, rating, review));

        uploadSelectedImage(newReviewKey);
    }

    public void saveImageToDatabase(String productId, String reviewId, StorageMetadata meta) {
        Log.v("mano", "Uploaded the image " + meta.toString());

        String imageKey = mDb.child(ReviewImage.NODE).push().getKey();
        ReviewImage ri = new ReviewImage(meta.getName(), reviewId, productId);
        mDb.child(ReviewImage.NODE).child(imageKey).setValue(ri);

        mDb.child(ProductReview.NODE).child(reviewId).child("image_id").setValue(imageKey);


        Snackbar.make(mBinding.content.tvDump, "Review added", Snackbar.LENGTH_SHORT).show();
        finish();
    }


    Bitmap selectedImage;
    protected void uploadSelectedImage(String reviewId) {
        if (selectedImage != null) {
            StorageReference mountainImagesRef = mSt.child("images/" + reviewId + ".jpg");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            selectedImage.compress(Bitmap.CompressFormat.JPEG, 75, baos);
            byte[] data = baos.toByteArray();

            UploadTask uploadTask = mountainImagesRef.putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                    Log.v("mano", "upload failed ", exception);
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                    saveImageToDatabase(mProduct.id, reviewId, taskSnapshot.getMetadata());

                }
            });
        }
    }




    @Override
    public void onPickResult(PickResult r) {
        if (r.getError() == null) {
            //If you want the Uri.
            //Mandatory to refresh image from Uri.
            //getImageView().setImageURI(null);

            //Setting the real returned image.
            //getImageView().setImageURI(r.getUri());

            //If you want the Bitmap.
            mBinding.content.ivPhoto.setImageBitmap(r.getBitmap());
            selectedImage = r.getBitmap();

            //Image path
            //r.getPath();
        } else {
            //Handle possible errors
            //TODO: do what you have to do with r.getError();
            Toast.makeText(this, r.getError().getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
