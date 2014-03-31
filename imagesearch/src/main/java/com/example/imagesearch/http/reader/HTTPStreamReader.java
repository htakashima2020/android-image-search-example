package com.example.imagesearch.http.reader;

import android.util.Log;

import com.example.imagesearch.http.HTTPRequestListener;

import org.apache.http.util.ByteArrayBuffer;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;


public class HTTPStreamReader {
    private final String TAG = "HTTPStreamReader";

    protected HTTPRequestListener listener;
    protected boolean canReceiveProgress;
    protected int contentLength;
    protected boolean canceled = false;

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    public void setListener(HTTPRequestListener listener) {
        this.listener = listener;
    }

    public void setCanReceiveProgress(boolean canReceiveProgress) {
        this.canReceiveProgress = canReceiveProgress;
    }

    public void setContentLength(int contentLength) {
        this.contentLength = contentLength;
    }

    public Object read(InputStream input) throws Exception {
        int count = 0;
        int total = 0;
        byte[] buffer = new byte[1024 * 8];
        ByteArrayOutputStream data = new ByteArrayOutputStream(1024 * 8);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(input);

        while((count = bufferedInputStream.read(buffer)) != -1) {
            if (canceled) {
                return null;
            }

            total += count;
            data.write(buffer, 0, count);
            this.readChunk(buffer, count, total);
        }

        data.flush();
        data.close();

        return this.outputFromBytes(data.toByteArray());
    }

    protected void readChunk(byte[] bytes, int numRead, int totalRead) {
        listener.didReceiveData(bytes);

        if (canReceiveProgress) {
            // only report progress on items that have a content length
            float percentRead = (float)totalRead / (float)contentLength;
            listener.didReceiveProgress(percentRead);
        }
    }

    protected Object outputFromBytes(byte[] bytes) {
        return null;
    }

}
