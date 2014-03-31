package com.example.imagesearch.http.image;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Picture;
import android.graphics.drawable.PictureDrawable;
import android.util.Log;

import com.example.imagesearch.http.HTTPClient;
import com.example.imagesearch.http.HTTPRequest;
import com.example.imagesearch.http.HTTPRequestListener;
import com.example.imagesearch.http.HTTPResponse;
import com.example.imagesearch.http.reader.ImageHTTPStreamReader;
import com.larvalabs.svgandroid.SVG;
import com.larvalabs.svgandroid.SVGBuilder;
import com.larvalabs.svgandroid.SVGParseException;
import com.larvalabs.svgandroid.SVGParser;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by mark on 3/24/14.
 */
public class ImageDownload implements Runnable {

    private final static String TAG = "ImageDownload";

    public enum State {
        INITIALIZED,
        DOWNLOADING,
        SUCCEEDED,
        FAILED
    }

    private static HashMap<String, ImageDownload> sURLMap = new HashMap<String, ImageDownload>();
    private static ExecutorService sThreadPool = Executors.newCachedThreadPool();
    private static HTTPClient sHTTPClient = new HTTPClient();

    private State mState;
    private String mURL;
    private float mProgress;
    private File mFile;
    private Bitmap mBitmap;
    private ImageDownloadListener mDownloadListener;
    private boolean mCheckCache;
    private HTTPRequest mRequest;

    public static ImageDownload get(String url) {
        return ImageDownload.get(url, null);
    }

    public static ImageDownload get(String url, ImageDownloadListener listener) {
        return ImageDownload.get(url, true, listener);
    }

    public static ImageDownload get(String url, boolean checkCache, ImageDownloadListener listener) {
        ImageDownload download;

        if (!sURLMap.containsKey(url)) {
            // create new ImageDownload and queue it up for execution
            download = new ImageDownload(url, checkCache, listener);
            sURLMap.put(url, download);
            sThreadPool.submit(download);
        } else {
            download = sURLMap.get(url);
        }

        download.setDownloadListener(listener);

        return sURLMap.get(url);
    }

    public ImageDownload(String url, boolean checkCache, ImageDownloadListener listener) {
        mURL = url;
        mCheckCache = checkCache;
        mDownloadListener = listener;
        mState = State.INITIALIZED;
    }

    public State getState() {
        return mState;
    }

    public boolean isStarted() {
        return mState == State.INITIALIZED;
    }

    public boolean isDownloading() {
        return mState == State.DOWNLOADING;
    }

    public boolean isFinished() {
        return mState == State.SUCCEEDED;
    }

    public boolean isFailed() {
        return mState == State.FAILED;
    }

    public void cancel() {
        mRequest.cancel();
    }

    public float getProgress() {
        return mProgress;
    }

    public Bitmap getBitmap() {
        Bitmap bitmap = null;
        if (isFinished()) {
            try {
                mRequest.getResponse();
            } catch (Exception e) {
                Log.e(TAG, "Got exception waiting for response.. " + e.getMessage());
            }
            bitmap = ImageCache.DefaultCache().get(mURL);
        }

        return bitmap;
    }

    public synchronized ImageDownloadListener getDownloadListener() {
        return mDownloadListener;
    }

    public synchronized void setDownloadListener(ImageDownloadListener downloadListener) {
        mDownloadListener = downloadListener;
    }

    /* run()

       Retrieves the image

     */
    public void run() {
        // get the file for this url
        mFile = ImageCache.DefaultCache().fileFor(mURL);

        // check the cache
        if (ImageCache.DefaultCache().isCached(mURL)) {
            if (mDownloadListener != null) {
                mDownloadListener.didFinish(ImageCache.DefaultCache().get(mURL));
                mState = State.SUCCEEDED;
            }
        } else {
            ImageHTTPStreamReader imageReader = new ImageHTTPStreamReader(mURL);

            try {
                mRequest = sHTTPClient.GET(mURL, null, imageReader, new HTTPRequestListener() {
                    @Override
                    public void didReceiveData(byte[] data) {
                        mState = State.DOWNLOADING;
                    }

                    @Override
                    public void canReceiveProgress(boolean val) {
                    }

                    @Override
                    public void didReceiveProgress(float progress) {
                        mProgress = progress;
                        if (mDownloadListener != null) {
                            mDownloadListener.didReceiveProgress(progress);
                        }
                    }

                    @Override
                    public void didSucceed(final HTTPResponse response) {
                        mState = State.SUCCEEDED;
                        if (mDownloadListener != null) {
                            mDownloadListener.didFinish(ImageCache.DefaultCache().get(mURL));
                        }
                    }

                    @Override
                    public void didFail(HTTPResponse response) {
                        mState = State.FAILED;
                        if (mDownloadListener != null) {
                            mDownloadListener.didFinish(null);
                        }
                    }

                    @Override
                    public void didError(Exception e) {
                        mState = State.FAILED;
                        if (mDownloadListener != null) {
                            mDownloadListener.didError(e);
                        }
                    }
                });

            } catch(Exception e) {
                Log.e(TAG, "Got error trying to download image.. " + e.getMessage());
            }

        }
    }

//    public static HTTPRequest get(String url, boolean checkCache, final ImageDownloadListener listener) throws Exception {
//        Log.d(TAG, String.format("get(%s, %b, %s", url, checkCache, listener));
//
//        Bitmap bitmap;
//        if (checkCache && ImageCache.exists(url)) {
//            Log.i(TAG, "Returning cached item for: " + url);
//            listener.didReceiveProgress(1.0f);
//            listener.didFinish(BitmapFactory.decodeFile(ImageCache.fileFor(url).getAbsolutePath()));
//            return null;
//        }
//
//        if (checkCache) {
//            Log.i(TAG, "Cache miss for " + url);
//        }
//        ImageHTTPStreamReader imageReader = new ImageHTTPStreamReader(ImageCache.fileFor(url));
//        HTTPClient client = new HTTPClient();
//
//        HTTPRequest request = client.GET(url, null, imageReader, new HTTPRequestListener() {
//            @Override
//            public void didReceiveData(byte[] data) {
//
//            }
//
//            @Override
//            public void canReceiveProgress(boolean val) {
//            }
//
//            @Override
//            public void didReceiveProgress(float progress) {
//                listener.didReceiveProgress(progress);
//            }
//
//            @Override
//            public void didSucceed(final HTTPResponse response) {
//                 listener.didFinish((Bitmap)response.getData());
//            }
//
//            @Override
//            public void didFail(HTTPResponse response) {
//                listener.didFinish(null);
//            }
//
//            @Override
//            public void didError(Exception e) {
//                listener.didError(e);
//            }
//        });
//
//        return request;
//    }
//
//    public static HTTPRequest get(String url, final ImageDownloadListener listener) throws Exception {
//        return get(url, true, listener);
//    }

}
