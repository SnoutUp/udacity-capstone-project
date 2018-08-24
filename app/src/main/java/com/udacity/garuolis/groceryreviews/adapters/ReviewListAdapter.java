package com.udacity.garuolis.groceryreviews.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.udacity.garuolis.groceryreviews.R;
import com.udacity.garuolis.groceryreviews.data.ProductReview;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReviewListAdapter extends RecyclerView.Adapter <ReviewListAdapter.ViewHolder> {
    private List<ProductReview> items;
    private ItemClickListener mListener;
    private Context mContext;

    DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

    public ReviewListAdapter(Context context, ReviewListAdapter.ItemClickListener listener){
        items = new ArrayList<>();
        mListener   = listener;
        mContext    = context;
    }


    public void setItems(List<ProductReview> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_review, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ProductReview item = items.get(position);

        Date dateCreated = new Date(item.getTimestampCreatedLong());
        holder.mTitleView.setText(item.productTitle);
        //holder.mTitleView.setText(sdf.format(dateCreated));
        holder.mReviewView.setText(item.review);
        holder.mRatingBar.setRating(item.rating);
        holder.mRatingBar.setIsIndicator(true);

        if (item.imageRef != null) {
            Glide.with(mContext).using(new FirebaseImageLoader()).load(item.imageRef).placeholder(R.drawable.placeholder).into(holder.mImageView);
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

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView mTitleView;
        TextView mReviewView;

        RatingBar mRatingBar;
        ImageView mImageView;

        ViewHolder(View v) {
            super (v);
            mTitleView  = v.findViewById(R.id.tv_title);
            mReviewView = v.findViewById(R.id.tv_review);
            mRatingBar  = v.findViewById(R.id.rb_rating);
            mImageView  = v.findViewById(R.id.iv_image);
        }
    }

    public interface ItemClickListener {
        void onClick(ProductReview product);
    }
}
