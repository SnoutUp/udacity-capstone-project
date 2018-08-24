package com.udacity.garuolis.groceryreviews.fragments;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.udacity.garuolis.groceryreviews.R;
import com.udacity.garuolis.groceryreviews.adapters.ProductListAdapter;
import com.udacity.garuolis.groceryreviews.data.Product;
import com.udacity.garuolis.groceryreviews.data.ShopItem;
import com.udacity.garuolis.groceryreviews.databinding.FragmentItemListBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProductListFragment extends Fragment implements ProductListAdapter.ItemClickListener {
    private FirebaseDatabase mDatabase;
    private OnFragmentInteractionListener mListener;
    private ValueEventListener mValueListener;
    private List<Product> mProductList;
    private ProductListAdapter mAdapter;

    private FragmentItemListBinding mBinding;

    public ProductListFragment() {
    }

    public static ProductListFragment newInstance() {
        ProductListFragment fragment = new ProductListFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupFragment();
    }

    private void setupFragment() {
        mDatabase = FirebaseDatabase.getInstance();
        mProductList = new ArrayList<>();

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
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_item_list, container, false);
        mBinding.rvList.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new ProductListAdapter(getContext(), this);
        mBinding.rvList.setAdapter(mAdapter);
        startDbListeners();
        return mBinding.getRoot();
    }

    private void startDbListeners() {
        Query mProductQuery = mDatabase.getReference().child(Product.NODE);
        mProductQuery.addValueEventListener(mValueListener);
    }

    private void stopDbListeners() {
        Query mProductQuery = mDatabase.getReference().child(Product.NODE);
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
    public void onClick(Product product) {
        Log.v("mano", "product: " + product.title);
        // Create Product in Shopping List

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference ref = mDatabase.getReference().child(ShopItem.NODE);
        ShopItem item = new ShopItem(user.getUid(), product.id, product.title);
        ref.child(user.getUid()).child(product.id).setValue(item);

    }

    @Override
    public void onBasketButtonClick(Product product) {

    }

    interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
