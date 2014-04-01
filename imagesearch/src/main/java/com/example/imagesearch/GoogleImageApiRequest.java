package com.example.imagesearch;

import android.util.Log;

import com.example.imagesearch.http.HTTPClient;
import com.example.imagesearch.http.HTTPRequest;
import com.example.imagesearch.http.reader.GoogleImageHTTPStreamReader;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by mark on 3/28/14.
 */
public class GoogleImageApiRequest implements Callable<ArrayList<String>> {

    private static String TAG = "GoogleImageAPIRequest";
    private static String VERSION = "1.0";
    private static HTTPClient CLIENT = new HTTPClient("https://ajax.googleapis.com/ajax/services/search", 10, GoogleImageHTTPStreamReader.class);

    private Date createdAt;
    private int offset, resultCount;
    private String query;
    private ConcurrentHashMap<String, String>optionDictionary;

    public GoogleImageApiRequest(String query, int offset, int resultCount) {
        this.offset = offset;
        this.resultCount = resultCount;
        this.offset = offset;
        this.query = query;
        this.createdAt = new Date();
        buildDictionary();
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    @Override
    public ArrayList<String> call() {
        ArrayList<String> list = null;

        try {
            HTTPRequest request = CLIENT.GET("/images", optionDictionary);
            list = (ArrayList<String>)request.getResponse().getData();
        } catch(Exception e) {
            Log.e(TAG, "Failed to get URLs.. " + e.getMessage());
        }

        return list;
    }

    private void buildDictionary() {
        optionDictionary = new ConcurrentHashMap<String, String>();
        optionDictionary.put("rsz", new Integer(resultCount).toString());
        optionDictionary.put("start", new Integer(offset).toString());
        optionDictionary.put("v", VERSION);
        optionDictionary.put("q", query);
    }



}
