package com.example.vlada.newsappstage2;

import android.content.AsyncTaskLoader;
import android.content.Context;
import java.util.List;


public class DataLoader extends AsyncTaskLoader<List<NewsData>> {

    // Query URL
    private String mUrl;

    // Constructs new loader
    public DataLoader(Context context, String url){
        super(context);
        mUrl = url;
    }

    @Override
    protected void onStartLoading(){
        forceLoad();
    }

    // Background thread.
    @Override
    public List<NewsData> loadInBackground(){
        if (mUrl == null){
            return null;
        }

        // Perform the network request, parse the response, and extract a list of news.
        List<NewsData> newsData = Utils.fetchNewsData(mUrl);
        return newsData;
    }

}