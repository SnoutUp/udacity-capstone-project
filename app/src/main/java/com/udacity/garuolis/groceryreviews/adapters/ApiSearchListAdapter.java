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

import com.udacity.garuolis.groceryreviews.R;
import com.udacity.garuolis.groceryreviews.data.ProductSearchResult;

import java.util.ArrayList;
import java.util.List;

public class ApiSearchListAdapter extends RecyclerView.Adapter<ApiSearchListAdapter.ViewHolder> {
    private List<ProductSearchResult> items;

    private ItemClickListener mListener;
    private Context mContext;
    public ApiSearchListAdapter(Context context, ItemClickListener listener){
        mListener   = listener;
        mContext    = context;

        items = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_search_result, parent, false);
        return new ViewHolder(v);
    }

    public void setItems(List<ProductSearchResult> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        ProductSearchResult item = items.get(position);

        holder.mTitleView.setText(item.title);
        holder.mBrandView.setText(item.brand);

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
        TextView mTitleView;
        TextView mBrandView;

        ViewHolder(View v) {
            super (v);
            mTitleView = v.findViewById(R.id.tv_title);
            mBrandView = v.findViewById(R.id.tv_brand);
        }
    }

    public interface ItemClickListener {
        void onClick(ProductSearchResult item);
    }
}
