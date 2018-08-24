package com.udacity.garuolis.groceryreviews.fragments;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.FirebaseDatabase;
import com.udacity.garuolis.groceryreviews.R;
import com.udacity.garuolis.groceryreviews.adapters.CategoryListAdapter;
import com.udacity.garuolis.groceryreviews.databinding.FragmentItemListBinding;

public class CategoryListFragment extends Fragment  implements CategoryListAdapter.ItemClickListener{
    private FirebaseDatabase mDatabase;
    private CategoryListAdapter mAdapter;
    private CategorySelectListener mListener;
    private FragmentItemListBinding mBinding;

    public CategoryListFragment() {
    }

    public static CategoryListFragment newInstance() {
        return new CategoryListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_item_list, container, false);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        mBinding.rvList.setLayoutManager(layoutManager);
        mAdapter = new CategoryListAdapter(getContext(), this);
        mBinding.rvList.setAdapter(mAdapter);
        return mBinding.getRoot();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof CategorySelectListener) {
            Log.v("mano", "attach a listener");
            mListener = (CategorySelectListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(String categoryKey, String categoryTitle) {
        Log.v("mano", "clicked: " + categoryKey);
        if (mListener != null) {
            mListener.onCategorySelected(categoryKey, categoryTitle);
        }
    }

    public interface CategorySelectListener {
        void onCategorySelected(String categoryKey, String categoryTitle);
    }
}
