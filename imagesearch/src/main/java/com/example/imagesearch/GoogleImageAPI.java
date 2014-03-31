package com.example.imagesearch;

import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by mark on 3/28/14.
 */
public class GoogleImageAPI {

    private static String TAG = "GoogleImageAPI";
    private static int NUM_RESULTS = 8;
    private static ExecutorService POOL = Executors.newCachedThreadPool();

    public static void fetchResults(String query, int offset, GoogleImageAPIListener listener) {
        GoogleImageApiRequest request = new GoogleImageApiRequest(query, offset, NUM_RESULTS);
        final Future<ArrayList<String>> future = POOL.submit(request);
        final GoogleImageAPIListener fListener = listener;
        POOL.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    // blocks on future get
                    fListener.gotNewURLs(future.get());
                } catch(Exception e) {
                    Log.e(TAG, "Got interrupted: " + e.getMessage());
                }
            }
        });

    }

}

