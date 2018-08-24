package com.udacity.garuolis.groceryreviews.ui;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.udacity.garuolis.groceryreviews.R;

public class CustomRecyclerView extends RecyclerView {

    private String mEmptyText;
    private int mEmptyIcon = 0;
    private boolean emptyViewLayoutDone = false;

    private RelativeLayout mEmptyWrap;
    private TextView mEmptyTextView;
    private ImageView mEmptyImageView;

    public CustomRecyclerView(Context context) {
        super(context);
        createEmptyViews();
    }

    private void createEmptyViews() {
        mEmptyWrap = new RelativeLayout(getContext());
    }

    private void layoutEmptyViews() {
        mEmptyWrap.setLayoutParams(getLayoutParams());

        if (mEmptyText == null) {
            mEmptyText = getResources().getString(R.string.recycler_empty_default);
        }

        LinearLayout wrap = new LinearLayout(getContext());
        wrap.setOrientation(LinearLayout.VERTICAL);
        RelativeLayout.LayoutParams wrapLp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        wrapLp.setMargins(60,0,60,40);
        wrapLp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        wrap.setLayoutParams(wrapLp);

        mEmptyImageView = new ImageView(getContext());
        LinearLayout.LayoutParams imageLp = new LinearLayout.LayoutParams(128, 128);
        imageLp.gravity = Gravity.CENTER_HORIZONTAL;
        mEmptyImageView.setLayoutParams(imageLp);
        wrap.addView(mEmptyImageView);

        mEmptyTextView = new TextView(getContext());
        mEmptyTextView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        mEmptyTextView.setText(R.string.recycler_empty_default);
        mEmptyTextView.setPadding(20,20,20,20);

        mEmptyTextView.setGravity(Gravity.CENTER);
        mEmptyTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        wrap.addView(mEmptyTextView);

        mEmptyImageView.setImageResource(mEmptyIcon);
        mEmptyTextView.setText(mEmptyText);

        mEmptyWrap.addView(wrap);

        ViewGroup parent = (ViewGroup) getParent();
        if (parent != null) {
            parent.addView(mEmptyWrap);
        }

        emptyViewLayoutDone = true;
    }

    public void setEmptyViewDetails(String emptyText, int emptyIcon) {
        this.mEmptyText = emptyText;
        this.mEmptyIcon = emptyIcon;
    }

    @Override
    public void setAdapter(Adapter adapter) {
        super.setAdapter(adapter);
        if(adapter != null) {
            adapter.registerAdapterDataObserver(adapterObserver);
        }

        adapterObserver.onChanged();
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);

        if (!emptyViewLayoutDone) {
            layoutEmptyViews();
        }
    }

    private void showEmptyViews() {
        mEmptyWrap.setVisibility(View.VISIBLE);
    }

    private void hideEmptyViews() {
        mEmptyWrap.setVisibility(View.GONE);
    }

    public CustomRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        createEmptyViews();
    }

    public CustomRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        createEmptyViews();
    }

    private AdapterDataObserver adapterObserver = new AdapterDataObserver() {
        @Override
        public void onChanged() {
            Adapter<?> adapter = getAdapter();
            if(adapter != null) {
                if(adapter.getItemCount() == 0) {
                    showEmptyViews();
                } else {
                    hideEmptyViews();
                }
            }

        }
    };

}
