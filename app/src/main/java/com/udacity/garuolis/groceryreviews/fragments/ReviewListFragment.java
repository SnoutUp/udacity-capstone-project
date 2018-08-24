package com.udacity.garuolis.groceryreviews.fragments;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.udacity.garuolis.groceryreviews.R;
import com.udacity.garuolis.groceryreviews.adapters.ReviewListAdapter;
import com.udacity.garuolis.groceryreviews.data.MyUtils;
import com.udacity.garuolis.groceryreviews.data.ProductReview;
import com.udacity.garuolis.groceryreviews.databinding.FragmentReviewListBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReviewListFragment extends Fragment implements ReviewListAdapter.ItemClickListener{
    private static final String ARG_USER_ID = "user_id";
    private FirebaseDatabase mDatabase;
    private FirebaseStorage mStorage;
    private ReviewItemClickListener mListener;

    private ValueEventListener mValueListener;

    private List<ProductReview> mReviewList;
    private ReviewListAdapter mAdapter;
    private FragmentReviewListBinding mBinding;

    private String mUserId;

    public ReviewListFragment() {
    }

    public static ReviewListFragment newInstance(String userId) {
        ReviewListFragment fragment = new ReviewListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USER_ID, userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupFragment();
        if (getArguments() != null) {
            mUserId = getArguments().getString(ARG_USER_ID);
        }
    }

    private void setupFragment() {
        mDatabase = FirebaseDatabase.getInstance();
        mStorage = FirebaseStorage.getInstance();

        mReviewList = new ArrayList<>();

        mValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mReviewList.clear();
                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    ProductReview review = snapshot.getValue(ProductReview.class);
                    review.imageRef = mStorage.getReference().child(MyUtils.ImagePath(review.id));
                    mReviewList.add(review);
                }

                Collections.reverse(mReviewList);
                mAdapter.setItems(mReviewList);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_review_list, container, false);
        mBinding.rvList.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new ReviewListAdapter(getContext(), this);
        mBinding.rvList.setAdapter(mAdapter);
        mBinding.rvList.setEmptyViewDetails(getString(R.string.review_list_is_empty), R.drawable.ic_food);

        startDbListeners();
        return mBinding.getRoot();
    }

    private void startDbListeners() {
        mDatabase.getReference().child(ProductReview.NODE_USER).child(mUserId).addValueEventListener(mValueListener);
    }

    private void stopDbListeners() {
        mDatabase.getReference().child(ProductReview.NODE_USER).child(mUserId).removeEventListener(mValueListener);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ReviewItemClickListener) {
            mListener = (ReviewItemClickListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        stopDbListeners();
    }

    @Override
    public void onClick(ProductReview productReview) {
        if (mListener != null) {
            mListener.onReviewClicked(productReview);
        }
    }

    public interface ReviewItemClickListener {
        void onReviewClicked(ProductReview productReview);
    }
}
