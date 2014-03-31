package com.example.imagesearch;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by mark on 3/28/14.
 */
public class GoogleImageAPI {

    final static private String TAG = "GoogleImageAPI";
    final static private int NUM_RESULTS = 8;

    final static ExecutorService sExecutorService = Executors.newCachedThreadPool();

    private boolean cancel = false;

    public void cancel() {
        cancel = true;
    }

    public static void fetchResults(String query, int offset, GoogleImageAPIListener listener) {
        GoogleImageApiRequest request = new GoogleImageApiRequest(query, offset, NUM_RESULTS);
        final Future<ArrayList<String>> future = sExecutorService.submit(request);
        final GoogleImageAPIListener fListener = listener;
        new Thread() {
            @Override
            public void run() {
                try {
                    fListener.gotNewURLs(future.get());
                } catch(Exception e) {
                    Log.e(TAG, "Got interrupted: " + e.getMessage());
                }
            }
        }.start();
    }

    public void beginFetchingResultsForQuery(String query, GoogleImageAPIListener listener) {
        cancel = false;

        // we populate the requests upfront into a delayed queue to ensure
        // at least 1 second goes by before executing another request

        final ArrayList<GoogleImageApiRequest> queue = new ArrayList<GoogleImageApiRequest>();
        final GoogleImageAPIListener fListener = listener;

        for(int x = 0; x <= 64; x += 8) {
            // make 8 requests to get 64 image max
            GoogleImageApiRequest request = new GoogleImageApiRequest(query, x, 8);
            queue.add(request);
        }

        new Thread() {
            @Override
            public void run() {
                ArrayList<String> allURLs = new ArrayList<String>();

                for(GoogleImageApiRequest request : queue) {
                    if (cancel) {
                        break; // allow cancel
                    }

                    try {
                        ArrayList<String> urls = request.call();
                        for(String url : urls) {
                            allURLs.add(url);
                        }
                        Log.d(TAG, "Got urls: " + urls);
                        fListener.gotNewURLs(allURLs);
                        Thread.sleep(2000);
                    } catch(Exception e) {
                        Log.e(TAG, "Got error trying to retrieve all URLs: " + e.getMessage());
                    }
                }
            }
        }.start();
    }

}

