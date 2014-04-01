package com.example.imagesearch.http;

import android.util.Log;

import com.example.imagesearch.http.reader.HTTPStreamReader;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * HTTPRequest
 */
public class HTTPRequest implements Callable<HTTPResponse> {
    private static final String TAG = "HTTPRequest";

    private HTTPStreamReader reader;
    private HTTPRequestListener listener;
    private URL url;
    private int timeoutInSeconds;
    private Future<HTTPResponse>future;
    private boolean cancel = false;

    public HTTPRequest(URL url, HTTPStreamReader reader, int timeoutInSeconds, HTTPRequestListener listener) {
        this.reader = reader;
        this.listener = listener;
        this.url = url;
        this.timeoutInSeconds = timeoutInSeconds;
    }

    public void setFuture(Future<HTTPResponse> future) {
        this.future = future;
    }

    // Blocking call to receive response
    public HTTPResponse getResponse() throws Exception {
       return future.get();
    }

    public HTTPResponse call() {
        HttpURLConnection connection;

        // open connection
        try {
            connection = (HttpURLConnection)url.openConnection();
            Log.d(TAG, "get " + url + "...");
        } catch(IOException e) {
            Log.e(TAG, "Error opening connection: " + e.getMessage());
            listener.didError(e);
            return new HTTPResponse(-1, null, e);
        }

        if (connection == null)
            return null;

        // set request method
        connection.setConnectTimeout(timeoutInSeconds * 1000);
        try {
            connection.setRequestMethod("GET");
        } catch(ProtocolException e) {
            Log.e(TAG, "Bad protocol: " + e.getMessage());
            listener.didError(e);
            return new HTTPResponse(-1, null, e);
        }

        // connect
        try {
            connection.connect();
        } catch(IOException e) {
            Log.e(TAG, "Could not connect: " + e.getMessage());
            listener.didError(e);
            return new HTTPResponse(-1, null, e);
        }

        // try to get content length
        int contentLength = connection.getContentLength();
        boolean canReceiveProgress = contentLength > 0;
        listener.canReceiveProgress(canReceiveProgress);
        Log.d(TAG, "Got content length: " + contentLength);

        // receive data
        InputStream input = null;
        try {
            input = new BufferedInputStream(connection.getInputStream(), 1024 * 8);
        } catch (IOException e) {
            Log.e(TAG, "Could not get input stream: " + e.getMessage());
            listener.didError(e);
            return new HTTPResponse(-1, null, e);
        }
        reader.setCanReceiveProgress(canReceiveProgress);
        reader.setContentLength(contentLength);
        reader.setListener(listener);
        Object content;
        try {
            content = reader.read(input);
        } catch(Exception e) {
            Log.e(TAG, "Got exception trying to read: " + e.getMessage());
            return new HTTPResponse(-1, null, e);
        }

        // get status & content
        int status = -1;
        try {
            status = connection.getResponseCode();
        } catch(IOException e) {
            Log.e(TAG, "Could not get status code: " + e.getMessage());
            listener.didError(e);
            return new HTTPResponse(-1, null, e);
        }

        Log.d(TAG, "Got status: " + status);
        HTTPResponse response = new HTTPResponse(status, content, null);

        // notify listener
        if (status == HttpURLConnection.HTTP_OK) { // success
            listener.didSucceed(response);
        } else { // failure
            listener.didFail(response);
        }

        return response;

    }

    public void cancel() {
        future.cancel(true);
        reader.setCanceled(true);
    }
}
