package com.udacity.garuolis.groceryreviews.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.udacity.garuolis.groceryreviews.R;
import com.udacity.garuolis.groceryreviews.data.ShopItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GroceryDataProvider implements RemoteViewsService.RemoteViewsFactory {

    private Context context;
    private Intent intent;
    private int widgetId;

    private List<ShopItem> mShopItems = new ArrayList<>();

    private FirebaseDatabase mDatabase;
    private FirebaseUser mUser;

    public GroceryDataProvider(Context context, Intent intent) {
        this.context    = context;
        this.intent     = intent;

        this.mDatabase  = FirebaseDatabase.getInstance();
        this.mUser      = FirebaseAuth.getInstance().getCurrentUser();

        this.widgetId   = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

        Log.v("mano", "GroceryDataProvider()" + this.mUser.getUid() + " widgetid: " + this.widgetId);
        loadData();
    }

    private void loadData() {

        DatabaseReference ref = this.mDatabase.getReference().child(ShopItem.NODE).child(this.mUser.getUid());
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mShopItems.clear();

                for (DataSnapshot snap : dataSnapshot.getChildren()) {
                    ShopItem item = snap.getValue(ShopItem.class);
                    mShopItems.add(item);
                }

                Collections.reverse(mShopItems);
                widgetDataChanged(context);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public void widgetDataChanged(Context context) {
        AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = widgetManager.getAppWidgetIds(new ComponentName(context, GroceryWidgetProvider.class));
        Intent updateIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE, null, context, GroceryWidgetProvider.class);
        updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        context.sendBroadcast(updateIntent);
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDataSetChanged() {
        Log.v("mano", "onDataSetChanged() " + mShopItems.size());
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        Log.v("mano", "returning size: " + mShopItems.size());
        return mShopItems.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        final RemoteViews remoteView = new RemoteViews(context.getPackageName(), R.layout.widget_list_item);
        ShopItem listItem = mShopItems.get(position);
        remoteView.setTextViewText(R.id.tv_title, listItem.productName);
        if (listItem.marked) {
            remoteView.setImageViewResource(R.id.iv_checkbox, R.drawable.cb_checked);
        } else {
            remoteView.setImageViewResource(R.id.iv_checkbox, R.drawable.cb_empty);
        }

        //remoteView.setTextViewText(R.id.content, listItem.getHeading());

        Intent fillIntent = new Intent();
        fillIntent.putExtra(GroceryWidgetProvider.EXTRA_PRODUCT_ID, listItem.productId);
        fillIntent.putExtra(GroceryWidgetProvider.EXTRA_USER_ID, listItem.userId);
        fillIntent.putExtra(GroceryWidgetProvider.EXTRA_ITEM_CHECK, !listItem.marked);

        remoteView.setOnClickFillInIntent(R.id.layout_container, fillIntent);
        return remoteView;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
