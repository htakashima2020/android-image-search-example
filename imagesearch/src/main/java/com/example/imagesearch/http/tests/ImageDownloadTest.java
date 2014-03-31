package com.example.imagesearch.http.tests;

import android.graphics.Bitmap;
import android.test.InstrumentationTestCase;

import com.example.imagesearch.http.HTTPRequest;
import com.example.imagesearch.http.HTTPResponse;
import com.example.imagesearch.http.image.ImageDownload;
import com.example.imagesearch.http.image.ImageDownloadListener;

import java.io.File;
import java.io.FileInputStream;
import java.nio.channels.ByteChannel;
import java.nio.channels.FileChannel;

/**
 * Created by mark on 3/24/14.
 */
public class ImageDownloadTest extends InstrumentationTestCase {

    static String GOOGLE_LOGO_URL = "http://www.google.com/images/srpr/logo11w.png";

    private boolean imageFileOK(Bitmap bitmap) {
        try {
            return bitmap != null && bitmap.getByteCount() > 0;
        } catch(Exception e) {
            System.out.println("Got error trying to open file input stream.." + e.getMessage());
            return false;
        }
    }
    public void testCanGetImage() throws Exception {
        imageFileOK(ImageDownload.get(GOOGLE_LOGO_URL, false, null).getBitmap());
    }

    public void testCanGetImageFromCache() throws Exception {
        imageFileOK(ImageDownload.get(GOOGLE_LOGO_URL, true, null).getBitmap());
    }

}
