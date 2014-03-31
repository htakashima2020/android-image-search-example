package com.example.imagesearch.http.reader;

import com.example.imagesearch.http.HTTPRequestListener;

public class StringHTTPStreamReader extends HTTPStreamReader {

    public void setListener(HTTPRequestListener listener) {
        this.listener = listener;
    }

    public void setCanReceiveProgress(boolean canReceiveProgress) {
        this.canReceiveProgress = canReceiveProgress;
    }

    public void setContentLength(int contentLength) {
        this.contentLength = contentLength;
    }

    @Override
    protected Object outputFromBytes(byte[] bytes) {
        return new String(bytes);
    }

}

