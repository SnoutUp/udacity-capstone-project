package com.udacity.garuolis.groceryreviews.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.udacity.garuolis.groceryreviews.MainActivity;
import com.udacity.garuolis.groceryreviews.R;
import com.udacity.garuolis.groceryreviews.data.ShopItem;

public class GroceryWidgetProvider extends AppWidgetProvider {
    public final static String ACTION_DATA_UPDATED      = "groceryreviews.ACTION_DATA_UPDATED";
    public final static String ACTION_LIST_ITEM_CLICKED = "groceryreviews.ACTION_LIST_ITEM_CLICKED";
    public final static String ACTION_EDIT_CLICKED      = "groceryreviews.ACTION_EDIT_CLICKED";

    public final static String EXTRA_USER_ID            = "user_id";
    public final static String EXTRA_PRODUCT_ID         = "product_id";
    public final static String EXTRA_ITEM_CHECK         = "item_check";
    ;

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int N = appWidgetIds.length;
        Log.v("mano", "Widget.onUpdate() " + N);

        for (int i=0; i<N; i++) {
            updateWidget(context, appWidgetManager, appWidgetIds[i]);
        }
    }

    public void updateWidget(Context context, AppWidgetManager appWidgetManager, int widgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_shopping_list);
        setRemoteAdapter(context, views, widgetId);

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(MainActivity.EXTRA_START_TAB, MainActivity.TAB_SHOPPING_LIST);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.layout_toolbar, pendingIntent);

        Intent openIntent = new Intent(context, GroceryWidgetProvider.class).setAction(ACTION_LIST_ITEM_CLICKED);
        PendingIntent pendingIntentTemplate = PendingIntent.getBroadcast(context, 0, openIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setPendingIntentTemplate(R.id.lv_list, pendingIntentTemplate);

        appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.lv_list);
        appWidgetManager.updateAppWidget(widgetId, views);
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        Log.v("mano", "widget enabled");
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Log.v("mano", "ON RECEIVE " + intent.getAction());
        String action = intent.getAction();
        if (action.equalsIgnoreCase(ACTION_LIST_ITEM_CLICKED)) {
            updateShoppingListState(intent.getStringExtra(EXTRA_USER_ID), intent.getStringExtra(EXTRA_PRODUCT_ID), intent.getBooleanExtra(EXTRA_ITEM_CHECK, false));
        } else if (action.equalsIgnoreCase(ACTION_DATA_UPDATED)) {

        }

        /*
        AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = widgetManager.getAppWidgetIds(new ComponentName(context, GroceryWidgetProvider.class));

        Intent updateIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE, null, context, GroceryWidgetProvider.class);
        updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);

        context.sendBroadcast(updateIntent);*/
    }

    private void updateShoppingListState(String userId, String productId, boolean marked) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child(ShopItem.NODE).child(userId).child(productId);
        ref.child("marked").setValue(marked);
    }

    private static void setRemoteAdapter(Context context, @NonNull final RemoteViews views, int widgetId) {
        Intent setIntent = new Intent(context, GroceryWidgetService.class);
        setIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        views.setRemoteAdapter(R.id.lv_list, setIntent);
    }

}
