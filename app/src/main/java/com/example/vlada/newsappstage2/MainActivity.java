package com.example.vlada.newsappstage2;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<NewsData>> {

    private static final String NEWS_REQUEST_URL = "https://content.guardianapis.com/search?";

    // mEmptyStateNews is displayed when there is no data in the list
    private TextView mEmptyStateNews;

    // Adapter for news
    private NewsDataAdapter newsDataAdapter;

    // Constant value for the news loader ID
    private static final int NEWS_LOADER_ID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find the id listNews
        ListView listNews = findViewById(R.id.listNews);

        // Find the id empty_news
        mEmptyStateNews = findViewById(R.id.empty_news);
        listNews.setEmptyView(mEmptyStateNews);

        // Create new adapter that takes an empty list
        newsDataAdapter = new NewsDataAdapter(this, new ArrayList<NewsData>());
        listNews.setAdapter(newsDataAdapter);

        // Set an item click listener on the ListView, which sends an intent to a web browser
        // to open a website with more information about the selected news
        listNews.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                // Find the current news that was clicked on
                NewsData selectedNews = newsDataAdapter.getItem(position);
                // Convert the String URL into a URI object
                Uri selectedNewsUri = Uri.parse(selectedNews.getUrl());
                // Create a new intent to view the news URI
                Intent webIntent = new Intent(Intent.ACTION_VIEW, selectedNewsUri);
                // Send the intent to launch a new activity
                startActivity(webIntent);
            }
        });

        ConnectivityManager connectivityMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo networkInfo = connectivityMgr.getActiveNetworkInfo();

        // If there is a network connection, fetch data
        if (networkInfo != null && networkInfo.isConnected()) {
            // Get a reference to the LoaderManager, in order to interact with loaders.
            LoaderManager loaderManager = getLoaderManager();

            // Initialize the loader. Pass in the int ID constant defined above and pass in null for the bundle.
            loaderManager.initLoader(NEWS_LOADER_ID, null, this);
        } else {
            // Otherwise, display error
            // First, hide loading indicator so error message will be visible
            View loadingIndicator = findViewById(R.id.loader);
            loadingIndicator.setVisibility(View.GONE);

            // Update empty state with no internet connection error message
            mEmptyStateNews.setText(R.string.no_internet);
        }
    }

    @Override
    public Loader<List<NewsData>> onCreateLoader(int id, Bundle args) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        String noOfNews = sharedPreferences.getString(getString(R.string.no_of_news),
                getString(R.string.default_no_of_news));

        String orderByDate = sharedPreferences.getString(
                getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default));

        String sortBySection = sharedPreferences.getString(
                getString(R.string.settings_sort_by_key),
                getString(R.string.settings_sort_by_default));

        // Parse breaks apart the URI string that's passed into parameter
        Uri baseUri = Uri.parse(NEWS_REQUEST_URL);

        // buildUpon prepares the baseUri that we just parsed so we can add query parameters to it
        Uri.Builder uriBuilder = baseUri.buildUpon();

        // Apend query parameter and its value
        if (!sortBySection.equals(getString(R.string.all))) {
            uriBuilder.appendQueryParameter(getString(R.string.section), sortBySection);
        }

        uriBuilder.appendQueryParameter(getString(R.string.show_tags), getString(R.string.author));
        uriBuilder.appendQueryParameter(getString(R.string.order_by), orderByDate);
        uriBuilder.appendQueryParameter(getString(R.string.sort_by), noOfNews);
        uriBuilder.appendQueryParameter(getString(R.string.api_key), getString(R.string.guardian_key));

        // Create a new loader for the given URL
        return new DataLoader(this, uriBuilder.toString());

    }

    @Override
    public void onLoadFinished(Loader<List<NewsData>> loader, List<NewsData> data) {

        // Hide loading indicator because the data has been loaded
        View loadingProgress = findViewById(R.id.loader);
        loadingProgress.setVisibility(View.GONE);

        // Set empty state text to display
        mEmptyStateNews.setText(R.string.news_error);

        // Clear the adapter of previous data
        newsDataAdapter.clear();
        if (data != null && !data.isEmpty()) {
            newsDataAdapter.addAll(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<NewsData>> loader) {
        newsDataAdapter.clear();
    }

    @Override
    // Initialize the contents of the Activity's options menu
    public boolean onCreateOptionsMenu(Menu menu){
        // Inflate the Options Menu
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if (id == R.id.action_settings){
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

