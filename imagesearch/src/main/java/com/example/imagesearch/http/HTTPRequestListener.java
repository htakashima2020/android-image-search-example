package com.example.imagesearch.http;

/**
 * Created by mark on 3/23/14.
 */

public interface HTTPRequestListener {
    public abstract void didReceiveData(byte[] data);
    public abstract void canReceiveProgress(boolean val);
    public abstract void didReceiveProgress(float progress);
    public abstract void didSucceed(HTTPResponse response);
    public abstract void didFail(HTTPResponse response);
    public abstract void didError(Exception e);
}
