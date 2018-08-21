package com.udacity.garuolis.groceryreviews.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.udacity.garuolis.groceryreviews.R;
import com.udacity.garuolis.groceryreviews.data.ProductReview;
import com.udacity.garuolis.groceryreviews.data.ShopItem;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ShoppingListAdapter extends RecyclerView.Adapter <ShoppingListAdapter.ViewHolder> {
    List<ShopItem> items;
    ItemClickListener mListener;
    Context mContext;

    public ShoppingListAdapter(Context context, ShoppingListAdapter.ItemClickListener listener){
        items = new ArrayList<>();
        mListener   = listener;
        mContext    = context;
    }

    public void setItems(List<ShopItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_shopping_list, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ShopItem item = items.get(position);
        holder.mTitleView.setText(item.productName);
        holder.mCheckBox.setSelected(item.marked);

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
        public TextView mTitleView;
        public CheckBox mCheckBox;

        public ViewHolder(View v) {
            super (v);
            mTitleView  = v.findViewById(R.id.tv_title);
            mCheckBox   = v.findViewById(R.id.cb_checkbox);
        }
    }

    public interface ItemClickListener {
        public void onClick(ShopItem shopItem);
    }
}
