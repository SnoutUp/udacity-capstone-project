package com.udacity.garuolis.groceryreviews;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.common.util.IOUtils;
import com.udacity.garuolis.groceryreviews.adapters.ApiSearchListAdapter;
import com.udacity.garuolis.groceryreviews.data.MyUtils;
import com.udacity.garuolis.groceryreviews.data.ProductSearchResult;
import com.udacity.garuolis.groceryreviews.databinding.ActivityProductSearchBinding;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ProductSearchActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, ApiSearchListAdapter.ItemClickListener {
    public final static String EXTRA_PRODUCT_NAME   = "product_name";
    private SearchView mSearchView;
    private ActivityProductSearchBinding mBinding;
    private ApiSearchListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_product_search);
        setSupportActionBar(mBinding.toolbar);
        getSupportActionBar().setTitle(R.string.search_lookup);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAdapter = new ApiSearchListAdapter(this, this);
        mBinding.rvList.setLayoutManager(new LinearLayoutManager(this));
        mBinding.rvList.setEmptyViewDetails(getString(R.string.search_result_list_empty), R.drawable.ic_cloud_search_outline_grey600_48dp);
        mBinding.rvList.setAdapter(mAdapter);
    }

    private void startLookingForProducts(String searchString) {
        Log.v("mano", "lookign up " + searchString );
        new LoadJsonDataTask().execute(MyUtils.FoodApiUrl(searchString));
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        //searchForItems(query);
        startLookingForProducts(query);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        //searchForItems(newText);
        return false;
    }

    @Override
    public void onClick(ProductSearchResult item) {
        Log.v("mano", "item clicked: " + item.title);

        Intent returnIntent = new Intent();
        returnIntent.putExtra(EXTRA_PRODUCT_NAME, item.brand + " " + item.title);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    class LoadJsonDataTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            String url = params[0];
            Log.v("mano", "url: " + url);
            String response = readJsonData(url);
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (result == null) {
                Snackbar.make(mBinding.rvList, R.string.info_api_request_failed, Snackbar.LENGTH_LONG).show();
            } else {

                List<ProductSearchResult> results = new ArrayList<>();
                try {
                    JSONObject jo = new JSONObject(result);
                    JSONArray ja = jo.getJSONArray("hints");
                    int count = ja.length();
                    for (int i = 0; i < count; i++) {
                        JSONObject food = ja.getJSONObject(i).getJSONObject("food");
                        String brand = food.getString("source");
                        if (food.has("brand")) {
                            brand = food.getString("brand");
                        }
                        String label = food.getString("label");
                        ProductSearchResult res = new ProductSearchResult(label, brand);
                        results.add(res);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                mAdapter.setItems(results);

                if (results.size() == 0) {
                    Snackbar.make(mBinding.rvList, R.string.info_no_product_search_results, Snackbar.LENGTH_LONG).show();
                }
            }
        }

    }

    private String readJsonData(String apiUrl) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            URL url = new URL(apiUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            InputStream stream = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(stream));

            StringBuffer buffer = new StringBuffer();
            String line = "";

            while ((line = reader.readLine()) != null) {
                buffer.append(line+"\n");
            }
            return buffer.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_product_list, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);MenuItem searchMenuItem = menu.findItem(R.id.search);

        mSearchView = (SearchView) searchMenuItem.getActionView();

        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        mSearchView.setSubmitButtonEnabled(true);
        mSearchView.setOnQueryTextListener(this);
        return super.onCreateOptionsMenu(menu);
    }

}
