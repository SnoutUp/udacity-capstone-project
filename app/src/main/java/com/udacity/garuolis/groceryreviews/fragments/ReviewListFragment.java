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
import com.udacity.garuolis.groceryreviews.data.ProductReview;
import com.udacity.garuolis.groceryreviews.databinding.FragmentReviewListBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReviewListFragment extends Fragment implements ReviewListAdapter.ItemClickListener{
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private FirebaseDatabase mDatabase;
    private FirebaseStorage mStorage;
    private OnFragmentInteractionListener mListener;

    ValueEventListener mValueListener;

    List<ProductReview> mReviewList;
    ReviewListAdapter mAdapter;

    FragmentReviewListBinding mBinding;

    public ReviewListFragment() {
    }

    public static ReviewListFragment newInstance(String param1, String param2) {
        ReviewListFragment fragment = new ReviewListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupFragment();


        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    public void setupFragment() {
        mDatabase = FirebaseDatabase.getInstance();
        mStorage = FirebaseStorage.getInstance();

        mReviewList = new ArrayList<>();

        mValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mReviewList.clear();
                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    ProductReview review = snapshot.getValue(ProductReview.class);
                    review.imageRef = mStorage.getReference().child("images").child(snapshot.getKey() + ".jpg");
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
        startDbListeners();
        return mBinding.getRoot();
    }

    public void startDbListeners() {
        Query mProductQuery = mDatabase.getReference().child(ProductReview.NODE);
        mProductQuery.addValueEventListener(mValueListener);
    }

    public void stopDbListeners() {
        Query mProductQuery = mDatabase.getReference().child(ProductReview.NODE);
        mProductQuery.removeEventListener(mValueListener);
    }



    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        stopDbListeners();
    }

    @Override
    public void onClick(ProductReview product) {

    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
