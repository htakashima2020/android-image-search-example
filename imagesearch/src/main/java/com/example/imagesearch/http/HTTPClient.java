package com.example.imagesearch.http;

import android.util.Log;

import com.example.imagesearch.http.reader.HTTPStreamReader;
import com.example.imagesearch.http.reader.StringHTTPStreamReader;

import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class HTTPClient {

    private static final String TAG = "HTTPClient";
    private static final ExecutorService POOL = Executors.newCachedThreadPool();
    private static final HTTPRequestListener DEFAULT_REQUEST_LISTENER = new HTTPRequestListener() {
        // empty listener
        @Override
        public void didReceiveData(byte[] data) {

        }
        @Override
        public void canReceiveProgress(boolean val) {

        }
        @Override
        public void didReceiveProgress(float progress) {

        }
        @Override
        public void didSucceed(HTTPResponse response) {

        }
        @Override
        public void didFail(HTTPResponse response) {

        }
        @Override
        public void didError(Exception e) {

        }
    };

    private int timeoutInSeconds = 10;
    private String baseUrl = "";
    private Class readerKlass;

    public HTTPClient() {
        this("");
    }
    public HTTPClient(String baseUrl) {
        this(baseUrl, 10);
    }
    public HTTPClient(String baseUrl, int timeout) {
        this(baseUrl, timeout, StringHTTPStreamReader.class);
    }

    public HTTPClient(String baseUrl, int timeout, Class readerKlass) {
        super();
        this.baseUrl = baseUrl;
        this.timeoutInSeconds = timeout;
        this.readerKlass = readerKlass;
    }

    public HTTPRequest get(String path) throws Exception {
        return get(path, null, DEFAULT_REQUEST_LISTENER);
    }

    public HTTPRequest get(String path, ConcurrentHashMap<String, String> options) throws Exception {
        return get(path, options, DEFAULT_REQUEST_LISTENER);
    }

    public HTTPRequest get(String path, ConcurrentHashMap<String, String> options, HTTPStreamReader reader) throws Exception {
        return get(path, options, reader, DEFAULT_REQUEST_LISTENER);
    }

    public HTTPRequest get(String path, ConcurrentHashMap<String, String> options, HTTPStreamReader reader, HTTPRequestListener listener)
            throws Exception {
        if (reader == null) {
            reader = (HTTPStreamReader)readerKlass.newInstance();
        }

        String pathParams = "";

        if (options != null) {
            // take dictionary of options and convert to "
            String stringOptions = "";
            for (Map.Entry<String, String> entry : options.entrySet()) {
                String key = URLEncoder.encode(entry.getKey(), "UTF-8");
                String value = URLEncoder.encode(entry.getValue(), "UTF-8").replaceAll("\\+", "%20");
                stringOptions += key + "=" + value + "&";
            }
            // remove trailing '&'
            pathParams = stringOptions.substring(0, stringOptions.length() - 1);
            Log.d(TAG, "Converted dictionary\n-----\n" + options + "\n----\nto: " + pathParams);
        }

        // build UrlConnection;
        String fullPath = baseUrl + path;
        if (pathParams.length() > 0) {
            fullPath += "?" + pathParams;
        }

        Log.d(TAG, "Using URL: " + fullPath);
        URL url = new URL(fullPath);

        HTTPRequest request = new HTTPRequest(url, reader, timeoutInSeconds, listener);
        Future<HTTPResponse> future = POOL.submit(request);
        request.setFuture(future);

        return request;
    }

    public HTTPRequest get(String path, ConcurrentHashMap<String, String> options, HTTPRequestListener listener)
            throws Exception {
        return get(path, options, null, listener);
    }
}


