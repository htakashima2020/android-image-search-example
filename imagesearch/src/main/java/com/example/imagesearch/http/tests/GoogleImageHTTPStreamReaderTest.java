package com.example.imagesearch.http.tests;

import android.test.InstrumentationTestCase;

import com.example.imagesearch.http.HTTPClient;
import com.example.imagesearch.http.HTTPRequest;
import com.example.imagesearch.http.reader.GoogleImageHTTPStreamReader;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by mark on 3/25/14.
 */
public class GoogleImageHTTPStreamReaderTest extends InstrumentationTestCase {

    private final static HTTPClient Client = new HTTPClient("https://ajax.googleapis.com/ajax/services/search");

    public void testCanRetrieveListOfUrls() throws Exception {
        ConcurrentHashMap<String, String> options = new ConcurrentHashMap<String, String>();
        options.put("v", "1.0");
        options.put("q", "cat");
        options.put("rsz", "8");

        GoogleImageHTTPStreamReader googleImageReader = new GoogleImageHTTPStreamReader();
        HTTPRequest request = Client.GET("/images", options, googleImageReader);
        ArrayList<String> urls = (ArrayList<String>)request.getResponse().getData();

        assertTrue(String.format("URLs length should not be empty got %s", urls.size()), urls.size() == 8);
    }
}
