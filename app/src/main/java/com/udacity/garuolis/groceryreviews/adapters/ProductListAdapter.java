package com.udacity.garuolis.groceryreviews.adapters;

import android.content.Context;
import android.media.Rating;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
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
    private List<Product> items;
    private ItemClickListener mListener;
    private Context mContext;

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
        ///  + " " + sdf.format(dateCreated)
        holder.mTitleView.setText(item.title);
        holder.mCategoryView.setText(item.categoryTitle);

        if (item.lastReview != null) {
            holder.mRatingBar.setVisibility(View.VISIBLE);
            holder.mRatingBar.setRating(item.lastReview.rating);
            if (item.lastReview.imageRef != null) {
                Glide.with(mContext).using(new FirebaseImageLoader()).load(item.lastReview.imageRef).into(holder.mImageView);
            }
        } else {
            holder.mRatingBar.setVisibility(View.GONE);
        }

        if (mListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onClick(item);
                }
            });

            holder.mImageBasket.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onBasketButtonClick(item);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView mTitleView;
        TextView mCategoryView;
        RatingBar mRatingBar;

        ImageView mImageView;
        ImageButton mImageBasket;

        ViewHolder(View v) {
            super (v);
            mTitleView = v.findViewById(R.id.tv_title);
            mCategoryView = v.findViewById(R.id.tv_category);
            mImageBasket = v.findViewById(R.id.ib_basket);
            mRatingBar = v.findViewById(R.id.rb_rating);
        }
    }

    public interface ItemClickListener {
        void onClick(Product product);
        void onBasketButtonClick(Product product);
    }
}
