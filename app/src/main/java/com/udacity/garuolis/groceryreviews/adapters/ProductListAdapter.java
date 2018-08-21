package com.udacity.garuolis.groceryreviews.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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

public class ProductListAdapter extends RecyclerView.Adapter<ProductListAdapter.ViewHolder> {
    List<Product> items;
    ItemClickListener mListener;
    Context mContext;

    DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public ProductListAdapter(Context context, ItemClickListener listener){
        items = new ArrayList<>();
        mListener   = listener;
        mContext    = context;
    }

    public void setItems(List<Product> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        Product item = items.get(position);

        Date dateCreated = new Date(item.getTimestampCreatedLong());
        holder.mTitleView.setText(item.title + " " + sdf.format(dateCreated));

        if (item.imageRef != null) {
            Glide.with(mContext).using(new FirebaseImageLoader()).load(item.imageRef).into(holder.mImageView);
        }

        if (mListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onClick(item);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mTitleView;
        public ImageView mImageView;

        public ViewHolder(View v) {
            super (v);
            mTitleView = v.findViewById(R.id.tv_title);
            mImageView = v.findViewById(R.id.iv_image);
        }
    }

    public interface ItemClickListener {
        public void onClick(Product product);
    }
}
