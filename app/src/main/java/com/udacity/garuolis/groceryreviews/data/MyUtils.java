package com.udacity.garuolis.groceryreviews.data;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.udacity.garuolis.groceryreviews.R;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class MyUtils {
    public final static String TABLE_USERS = "users";
    private final static String FOOD_API_KEY = "12d75074c1135f012930008538670bfe";
    private final static String FOOD_API_USER = "8482abdb";
    private final static String FOOD_API_URL = "https://api.edamam.com/api/food-database/parser";

    private final static String STORAGE_IMAGE_DIR = "images";

    public static String FoodApiUrl(String searchString) {
        String encodedSearchString = "";
        try {
            encodedSearchString = URLEncoder.encode(searchString,  "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return FOOD_API_URL + "?ingr=" + encodedSearchString + "&app_id=" + FOOD_API_USER + "&app_key=" + FOOD_API_KEY;
    }

    public static String ImagePath(String imageId) {
        return STORAGE_IMAGE_DIR + "/" + ImageFileName(imageId);
    }

    public static boolean IsConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    private static String ImageFileName(String imageId) {
        return imageId + ".jpg";
    }

    public static void ShowInfoSnack(Context ctx, View view, String  string) {
        Snackbar snack = Snackbar.make(view, string, Snackbar.LENGTH_LONG);
        snack.getView().setBackgroundColor(ctx.getResources().getColor(R.color.snackInfo));
        snack.show();
    }

    public static void ShowErrorSnack(Context ctx, View view, String string) {
        Snackbar snack = Snackbar.make(view, string, Snackbar.LENGTH_LONG);
        snack.getView().setBackgroundColor(ctx.getResources().getColor(R.color.snackError));
        snack.show();
    }

    public static void ShowInfoSnack(Context ctx, View view, int stringId) {
        Snackbar snack = Snackbar.make(view, stringId, Snackbar.LENGTH_LONG);
        snack.getView().setBackgroundColor(ctx.getResources().getColor(R.color.snackInfo));
        snack.show();
    }

    public static void ShowErrorSnack(Context ctx, View view, int stringId) {
        Snackbar snack = Snackbar.make(view, stringId, Snackbar.LENGTH_LONG);
        snack.getView().setBackgroundColor(ctx.getResources().getColor(R.color.snackError));
        snack.show();
    }
}
