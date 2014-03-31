package com.example.imagesearch.http.reader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.example.imagesearch.http.image.ImageCache;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;

/**
 * Created by mark on 3/23/14.
 */
public class ImageHTTPStreamReader extends HTTPStreamReader {
    private final static String TAG = "ImageHTTPStreamReader";

    private String url;
    File file;
    FileOutputStream os;


    public ImageHTTPStreamReader(String url) {
        this.file = ImageCache.DefaultCache().fileFor(url);
        this.url = url;

        try {
            os = new FileOutputStream(file, true);
        } catch(Exception e) {
            Log.e(TAG, "Failed to open.. " + e.getMessage());
        }
    }

    @Override
    protected void readChunk(byte[] bytes, int numRead, int totalRead) {
        super.readChunk(bytes, 0, totalRead);

        try {
            os.write(bytes, 0, numRead);
        } catch(Exception e) {
            Log.e(TAG, "Failed to write.. " + e.getMessage());
        }
    }

    @Override
    protected Object outputFromBytes(byte[] bytes) {
        try {
            os.flush();
            os.close();
        } catch (IOException ioe) {
            Log.e(TAG, "Got error: " + ioe.getMessage());
        }
        ImageCache.DefaultCache().cache(url, file);

        return file;
//        Log.d(TAG, "Creating bitmap from bytes(" + bytes.length + ")");
//        Bitmap bitmap = null;
//        try {
//            os.flush();
//            os.close();
//            BitmapFactory.Options o2 = new BitmapFactory.Options();
//            o2.inSampleSize=3;
//            bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, o2);
//
//            Log.d(TAG, "Got number of bytes " + bytes.length + " for file " + file.getAbsolutePath());
//            Log.d(TAG, "Got file size: " + file.length());
//        } catch(Exception e) {
//            Log.e(TAG, "Could not decode file input stream..." + e.getMessage());
//        }
//
//        return bitmap;
    }
}