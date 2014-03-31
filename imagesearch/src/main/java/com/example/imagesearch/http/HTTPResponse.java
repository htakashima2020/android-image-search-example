package com.example.imagesearch.http;

import org.apache.http.client.HttpClient;

import java.util.Dictionary;
import java.util.concurrent.ConcurrentHashMap;

public class HTTPResponse {

    private int statusCode;
    private Object data;
    private Exception exception;

    public HTTPResponse(int statusCode, Object data, Exception e) {
        this.statusCode = statusCode;
        this.data = data;
        this.exception = e;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public Exception getException() {
        return exception;
    }

    public Object getData() {
        return data;
    }

    public boolean didSucceed() {
        return this.statusCode >= 200 && this.statusCode < 300;
    }
}
