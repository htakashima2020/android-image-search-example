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

//    private boolean imageFileOK(Bitmap bitmap) {
//        try {
//            return bitmap != null && bitmap.getByteCount() > 0;
//        } catch(Exception e) {
//            System.out.println("Got error trying to open file input stream.." + e.getMessage());
//            return false;
//        }
//    }
//    public void testCanGetImage() throws Exception {
//        final boolean[] didGetBitmap = new boolean[1];
//
//        HTTPRequest request = ImageDownload.get("http://www.google.com/images/srpr/logo11w.png", false, new ImageDownloadListener() {
//            @Override
//            public void didError(Exception e) {
//
//            }
//
//            @Override
//            public void didFinish(Bitmap bitmapFile) {
//                assertTrue(imageFileOK(bitmapFile));
//            }
//
//            @Override
//            public void didReceiveProgress(float progress) {
//
//            }
//
//            @Override
//            public void canReceiveProgress(boolean canReceiveProgress) {
//
//            }
//        });
//        request.getResponse();
//
//        assertTrue(didGetBitmap[0]);
//    }
//
//    public void testCanGetImageFromCache() throws Exception {
//
//        final boolean[] didGetBitmap = new boolean[1];
//
//        HTTPRequest request = ImageDownload.get("http://www.google.com/images/srpr/logo11w.png", true, new ImageDownloadListener() {
//            @Override
//            public void didFinish(Bitmap bitmapFile) {
//                assertTrue(imageFileOK(bitmapFile));
//            }
//            @Override
//            public void didError(Exception e) {
//
//            }
//
//            @Override
//            public void didReceiveProgress(float progress) {
//
//            }
//
//            @Override
//            public void canReceiveProgress(boolean canReceiveProgress) {
//
//            }
//        });
//
//
//        assertTrue(didGetBitmap[0]);
//    }
//
//    public void testCanGetMultipleImages() throws Exception {
//        HTTPRequest request1, request2, request3;
//        HTTPResponse response1, response2, response3;
//
//        String[] urls = new String[]{"http://www.google.com/images/srpr/logo11w.png",
//                "http://4.bp.blogspot.com/-MzZCzWI_6Xc/UIUQp1qPfzI/AAAAAAAAHpA/OTwHCJSWFAY/s1600/cats_animals_kittens_cat_kitten_cute_desktop_1680x1050_hd-wallpaper-753974.jpeg",
//                "https://encrypted-tbn2.gstatic.com/images?q=tbn:ANd9GcQez1rqrg9WK-cJmmDT2sMk6T4nlJu_RikizwpfbHIULdlbA8Ok"
//        };
//
//        request1 = ImageDownload.get(urls[0], false, new ImageDownloadListener() {
//            @Override
//            public void didFinish(Bitmap bitmapFile) {
//                assertTrue(imageFileOK(bitmapFile));
//            }
//
//            @Override
//            public void didError(Exception e) {
//
//            }
//
//            @Override
//            public void didReceiveProgress(float progress) {
//
//            }
//
//            @Override
//            public void canReceiveProgress(boolean canReceiveProgress) {
//
//            }
//        });
//
//        request2 = ImageDownload.get(urls[1], false, new ImageDownloadListener() {
//            @Override
//            public void didError(Exception e) {
//
//            }
//
//            @Override
//            public void didFinish(Bitmap bitmap) {
//                System.out.println("Got bitmap.. " + bitmap);
//            }
//
//            @Override
//            public void didReceiveProgress(float progress) {
//
//            }
//
//            @Override
//            public void canReceiveProgress(boolean canReceiveProgress) {
//
//            }
//        });
//
//        request3 = ImageDownload.get(urls[2], false, new ImageDownloadListener() {
//            @Override
//            public void didFinish(Bitmap bitmap) {
//                System.out.println("Got bitmap.. " + bitmap);
//            }
//
//            @Override
//            public void didError(Exception e) {
//
//            }
//
//            @Override
//            public void didReceiveProgress(float progress) {
//
//            }
//
//            @Override
//            public void canReceiveProgress(boolean canReceiveProgress) {
//
//            }
//        });
//
//        // gather responses..
//        response1 = request1.getResponse();
//        response2 = request2.getResponse();
//        response3 = request3.getResponse();
//
//        // get all the bitmaps (tests conversion)
//        Bitmap bitmap1 = (Bitmap)response1.getData();
//        Bitmap bitmap2 = (Bitmap)response2.getData();
//        Bitmap bitmap3 = (Bitmap)response3.getData();
//        Bitmap[] bitmapArr = new Bitmap[]{ bitmap1, bitmap2, bitmap3};
//
//        // all bitmaps should have a different byte count
//        for(int x = 0; x < bitmapArr.length; x++) {
//            for(int y = 0; y < bitmapArr.length; y++) {
//                if (x != y) {
//                    assertNotSame(bitmapArr[x].getByteCount(), bitmapArr[y].getByteCount());
//                }
//            }
//        }
//
//
//    }
}
