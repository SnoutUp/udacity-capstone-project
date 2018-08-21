package com.udacity.garuolis.groceryreviews.fragments;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
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
import com.udacity.garuolis.groceryreviews.adapters.ShoppingListAdapter;
import com.udacity.garuolis.groceryreviews.data.ProductReview;
import com.udacity.garuolis.groceryreviews.data.ShopItem;
import com.udacity.garuolis.groceryreviews.databinding.FragmentItemListBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShoppingListFragment extends Fragment implements ShoppingListAdapter.ItemClickListener {
    private static final String ARG_USER_ID = "user_id";

    private FirebaseDatabase mDatabase;
    private OnFragmentInteractionListener mListener;
    ValueEventListener mValueListener;
    List<ShopItem> mShoppingList;
    ShoppingListAdapter mAdapter;

    String userId;

    FragmentItemListBinding mBinding;

    public ShoppingListFragment() {
    }

    public static ShoppingListFragment newInstance(String userId) {
        ShoppingListFragment fragment = new ShoppingListFragment();
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
            userId = getArguments().getString(ARG_USER_ID);
        }
    }

    public void setupFragment() {
        mDatabase = FirebaseDatabase.getInstance();
        mShoppingList = new ArrayList<>();

        mValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mShoppingList.clear();
                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    ShopItem shopItem = snapshot.getValue(ShopItem.class);
                    mShoppingList.add(shopItem);
                }

                Collections.reverse(mShoppingList);
                mAdapter.setItems(mShoppingList);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_item_list, container, false);
        mBinding.rvList.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new ShoppingListAdapter(getContext(), this);
        mBinding.rvList.setAdapter(mAdapter);
        startDbListeners();
        return mBinding.getRoot();
    }

    public void startDbListeners() {
        Query mProductQuery = mDatabase.getReference().child(ShopItem.NODE).child(userId);
        mProductQuery.addValueEventListener(mValueListener);
    }

    public void stopDbListeners() {
        Query mProductQuery = mDatabase.getReference().child(ShopItem.NODE).child(userId);
        mProductQuery.removeEventListener(mValueListener);
    }

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
    public void onClick(ShopItem product) {

    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
