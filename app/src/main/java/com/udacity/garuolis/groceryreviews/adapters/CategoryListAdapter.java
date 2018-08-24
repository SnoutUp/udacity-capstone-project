package com.udacity.garuolis.groceryreviews.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.udacity.garuolis.groceryreviews.R;

public class CategoryListAdapter extends RecyclerView.Adapter<CategoryListAdapter.ViewHolder> {
    private String[] mTitles;
    private String[] mValues;

    private ItemClickListener mListener;
    private Context mContext;
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
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String item = mTitles[position];

        holder.mTitleView.setText(item);

        if (mListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos = holder.getAdapterPosition();
                    mListener.onClick(mValues[pos], mTitles[pos]);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mTitles.length;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView mTitleView;
        public ImageView mImageView;

        ViewHolder(View v) {
            super (v);
            mTitleView = v.findViewById(R.id.tv_title);
        }
    }

    public interface ItemClickListener {
        void onClick(String categoryKey, String categoryTitle);
    }
}
