package com.udacity.garuolis.groceryreviews.fragments;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.udacity.garuolis.groceryreviews.R;
import com.udacity.garuolis.groceryreviews.adapters.ShoppingListAdapter;
import com.udacity.garuolis.groceryreviews.data.ShopItem;
import com.udacity.garuolis.groceryreviews.databinding.FragmentItemListBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShoppingListFragment extends Fragment implements ShoppingListAdapter.ItemClickListener {
    private static final String ARG_USER_ID = "user_id";

    private FirebaseDatabase mDatabase;
    private OnFragmentInteractionListener mListener;
    private ValueEventListener mValueListener;
    private List<ShopItem> mShoppingList;
    private ShoppingListAdapter mAdapter;

    private String mUserId;

    private FragmentItemListBinding mBinding;

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
            mUserId = getArguments().getString(ARG_USER_ID);
        }
    }

    private void setupFragment() {
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_item_list, container, false);
        mBinding.rvList.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new ShoppingListAdapter(getContext(), this);
        mBinding.rvList.setAdapter(mAdapter);
        mBinding.rvList.setEmptyViewDetails(getString(R.string.shopping_list_empty), R.drawable.ic_basket);

        ItemTouchHelper swipeToDismissTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                Log.v("mano", "SWIPED " + direction);
                deleteShoppingListItem(mAdapter.getItem(viewHolder.getAdapterPosition()));
                mAdapter.notifyItemRemoved(viewHolder.getAdapterPosition());
            }

        });
        swipeToDismissTouchHelper.attachToRecyclerView(mBinding.rvList);
        startDbListeners();
        return mBinding.getRoot();
    }

    private void deleteShoppingListItem(ShopItem item) {
        mDatabase.getReference().child(ShopItem.NODE).child(mUserId).child(item.productId).removeValue();
        Snackbar.make(mBinding.rvList, getString(R.string.info_shopping_item_removed, item.productName), Snackbar.LENGTH_LONG).show();
    }

    private void startDbListeners() {
        Query mProductQuery = mDatabase.getReference().child(ShopItem.NODE).child(mUserId);
        mProductQuery.addValueEventListener(mValueListener);
    }

    private void stopDbListeners() {
        Query mProductQuery = mDatabase.getReference().child(ShopItem.NODE).child(mUserId);
        mProductQuery.removeEventListener(mValueListener);
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

    @Override
    public void onChecked(ShopItem shopItem, boolean state) {
        Log.v("mano", "checked " + shopItem.productName + " state: " + state);
        DatabaseReference ref = mDatabase.getReference().child(ShopItem.NODE).child(shopItem.userId).child(shopItem.productId);
        ref.child("marked").setValue(state);
    }

    private interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
