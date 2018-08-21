package com.udacity.garuolis.groceryreviews.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.udacity.garuolis.groceryreviews.R;
import com.udacity.garuolis.groceryreviews.data.Product;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CategoryListAdapter extends RecyclerView.Adapter<CategoryListAdapter.ViewHolder> {
    String[] mTitles;
    String[] mValues;

    ItemClickListener mListener;
    Context mContext;
    public CategoryListAdapter(Context context, ItemClickListener listener){
        mListener   = listener;
        mContext    = context;

        mTitles = context.getResources().getStringArray(R.array.product_type_names);
        mValues = context.getResources().getStringArray(R.array.product_type_keys);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        String item = mTitles[position];

        holder.mTitleView.setText(item);

        if (mListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onClick(mValues[position]);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mTitles.length;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mTitleView;
        public ImageView mImageView;

        public ViewHolder(View v) {
            super (v);
            mTitleView = v.findViewById(R.id.tv_title);
        }
    }

    public interface ItemClickListener {
        public void onClick(String categoryKey);
    }
}
