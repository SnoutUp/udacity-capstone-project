package com.udacity.garuolis.groceryreviews.widget;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class GroceryWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new GroceryDataProvider(this, intent);
    }
}
