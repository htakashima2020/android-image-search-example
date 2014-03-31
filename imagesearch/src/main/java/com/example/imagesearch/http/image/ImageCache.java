package com.example.imagesearch.http.image;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Picture;
import android.graphics.drawable.PictureDrawable;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.util.Log;
import android.util.LruCache;

import com.larvalabs.svgandroid.SVG;
import com.larvalabs.svgandroid.SVGBuilder;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

/**
 * Created by mark on 3/23/14.
 */
public class ImageCache {

    private static String TAG = "ImageCache";
    private static int DEFAULT_SAMPLE_SIZE = 3;
    private static int CACHE_SIZE = 8 * 1024 * 1024; // 8MiB
    private static ExecutorService POOL = Executors.newCachedThreadPool();
    private static ImageCache DEFAULT = null;

    private static HashMap<Pattern, String> sPatternMap = null;
    public static File CacheDirectory;

    private File mSavedURLsDirectory;
    private String mName, mSavedURLsFilename, mSavedURLsPath;
    private ConcurrentHashMap<String, File> mURLtoFileMap;
    private LruCache<String, Bitmap> mURLtoBitmapCache;
    private LruCache<String, Bitmap> mURLtoHighQualityBitmapCache;

    /* DefaultCache()
        returns the default cache system
     */
    public static ImageCache DefaultCache() {
        if (DEFAULT == null) {
            DEFAULT = new ImageCache("default");
        }

        return DEFAULT;
    }

    public ImageCache(String name) {
        mName = name;
        mSavedURLsDirectory = CacheDirectory;
        mSavedURLsFilename = mName + "-cache.json";
        mSavedURLsPath = mSavedURLsDirectory + "/" + mSavedURLsFilename;
        mURLtoBitmapCache = new LruCache<String, Bitmap>(CACHE_SIZE);
        mURLtoHighQualityBitmapCache = new LruCache<String, Bitmap>(CACHE_SIZE / 4);
        setupExportTimerTask();
        importSavedURLs();
    }

    /* cache(String url, File file)

        Creates a bitmap from file and places it in the cache
     */
    public void cache(final String url, final File file) {
        // create local file mapping
        mURLtoFileMap.put(url, file);
        mURLtoBitmapCache.put(url, bitmapFromFile(file, DEFAULT_SAMPLE_SIZE));
    }

    /* isCached(url)

        Asks the cache if given url is cached
     */
    public boolean isCached(String url) {
        return mURLtoBitmapCache.get(url) != null || mURLtoFileMap.containsKey(url);
    }

    /* get(url)

     Retrieves bitmap for URL. It retrieves from its stores in the following order..
        1.) lru low quality cache
        2.) file mapping

     Returns null if not found
  */
    public Bitmap get(String url) {
        Bitmap bitmap = mURLtoBitmapCache.get(url); // cache

        if (bitmap != null) { // cache hit
        } else if (mURLtoFileMap.containsKey(url)) { // file cache
            Log.d(TAG, "Cache file-hit: " + url);
            bitmap = bitmapFromFile(mURLtoFileMap.get(url), DEFAULT_SAMPLE_SIZE);
            if (bitmap != null) {
                mURLtoBitmapCache.put(url, bitmap);
            }
        } else {
            Log.d(TAG, "Cache miss: " + url);
            bitmap = null;
        }

        return bitmap;
    }

    /* getHighQuality(url)

     Retrieves high quality bitmap for URL. It retrieves from its stores in the following order..
        1.) lru cache - gets bitmap from cache
        2.) file mapping - creates bitmap from file mapping

     Returns null if not found
    */
    public Bitmap getHighQuality(String url, int reqWidth, int reqHeight) {
        Bitmap bitmap = mURLtoHighQualityBitmapCache.get(url);
        if (bitmap == null) { // not cached
            final BitmapFactory.Options options = new BitmapFactory.Options();

            // decode bounds
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(fileFor(url).getAbsolutePath(), options);

            // calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

            // decode bitmap
            options.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeFile(fileFor(url).getAbsolutePath(), options);

            // store in cache
            if (bitmap != null) {
                mURLtoHighQualityBitmapCache.put(url, bitmap);
            }
        }

        return bitmap;
    }

    /* fileFor(string)

        Creates an MD5 hash representation of name and appends an extension
     */
    public synchronized File fileFor(String string)  {
        File file = mURLtoFileMap.get(string);

        if (file == null) {
            String extension = getExtension(string);
            Log.d(TAG, "Creating new file instance for string: " + string);

            // convert the url to a friendly filename by just converting it to a md5 hash
            MessageDigest md;
            byte [] bytesOfMessage;
            try {
                bytesOfMessage = string.getBytes("UTF-8");
                md = MessageDigest.getInstance("MD5");
            } catch(UnsupportedEncodingException uee) {
                Log.e(TAG, "Got unsupported encoding.. " + uee.getMessage());
                return null;
            } catch(NoSuchAlgorithmException nae) {
                Log.e(TAG, "No such algorithm.. " + nae.getMessage());
                return null;
            }

            byte[] digest = md.digest(bytesOfMessage);
            //convert the byte to hex format
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < digest.length; i++) {
                sb.append(Integer.toString((digest[i] & 0xff) + 0x100, 16).substring(1));
            }
            String filename = sb.toString() + extension;

            Log.d(TAG, "Created filename: " + filename);

            String path = CacheDirectory.getAbsolutePath() + "/" + filename;
            Log.d(TAG, "Using full path: " + path);
            file = new File(path);
            if (!file.exists()) {
                // touch if doesn't exist
                try {
                    file.createNewFile();
                } catch (IOException ioe) {
                    Log.e(TAG, "Got io exception trying to create new file : " + ioe.getMessage());
                }
            }

        }

        return file;
    }

    /* setupExportTimerTask

         schedules an export job to run every 5 seconds starting 5 seconds from now
    */
    private void setupExportTimerTask() {
        // export saved URLs every 5 seconds
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Log.i(TAG, "Exporting saved URLs");
                exportSavedURLs();
            }
        };
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(task, 5000, 5000);
    }

    /* exportSavedURLs

        persists the saved URLs to a JSON array
     */
    private void exportSavedURLs() {
        try {
            File file = new File(mSavedURLsPath);
            if (!file.exists() && !file.createNewFile()) {
                Log.e(TAG, "Failed to create new file");
            }

            JsonWriter writer = new JsonWriter(new BufferedWriter(new FileWriter(file, false)));
            writer.beginArray();
            for (Map.Entry<String, File>entry : mURLtoFileMap.entrySet()) {
                writer.beginObject();
                writer.name(entry.getKey()); // url
                writer.value(entry.getValue().getAbsolutePath()); // path
                writer.endObject();
            }
            writer.endArray();
            writer.flush();
            writer.close();
        } catch(Exception e) {
            Log.e(TAG, "Got error trying to export saved URLs: \n" + e.getMessage());
            Log.e(TAG, "Saved URLs Path: " + mSavedURLsPath);
        }
    }

    /* importSavedURLs

        reads saved URLs from a JSON array
     */
    private void importSavedURLs() {
        mURLtoFileMap = new ConcurrentHashMap<String, File>();

        // load in a JSON representation
        try {
            JsonReader reader = new JsonReader(new BufferedReader(new FileReader(new File(mSavedURLsPath))));
            reader.beginArray();
            while (reader.hasNext()) {
                reader.beginObject();
                while (reader.hasNext()) {
                    String url = reader.nextName();
                    File file = new File(reader.nextString());
                    if (file.exists()) {
                        mURLtoFileMap.put(url, file);
                    }
                }
                reader.endObject();
            }
            reader.endArray();

            Log.i(TAG, "Read in saved URLS:\n" + mURLtoFileMap);
            reader.close();
        } catch(Exception e) {
            Log.e(TAG, "Failed to read in URLs => File mapping:\n" + e.getMessage());
        }
    }

    /* importInBackground

    executes the import via thread pool
 */
    private void importInBackground() {
        POOL.submit(new Runnable() {
            @Override
            public void run() {
                importSavedURLs();
            }
        });
    }


    /* serializeSVG

        attempts to convert a svg file into a bitmap
     */
    private static Bitmap serializeSVG(File file) {
        Bitmap bitmap = null;
        try {
            SVG svg = new SVGBuilder().readFromInputStream(new BufferedInputStream(new FileInputStream(file))).build();
            if (svg != null) {
                bitmap = pictureDrawable2Bitmap(svg.getPicture());
            }
        } catch (Exception e) {
            Log.e(TAG, "Got exception trying to serialize from SVG format: " + e.getMessage());
        }

        return bitmap;
    }

    /* pictureDrawable2Bitmap

        converts a picture into a bitmap. used with serializeSVG
     */
    private static Bitmap pictureDrawable2Bitmap(Picture picture) {
        PictureDrawable pd = new PictureDrawable(picture);
        Bitmap bitmap = Bitmap.createBitmap(pd.getIntrinsicWidth(), pd.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawPicture(pd.getPicture());
        return bitmap;
    }

    private static Bitmap bitmapFromFile(File file, int sampleSize) {
        Bitmap bitmap = null;

        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = sampleSize;
            bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
            if (bitmap == null && getExtension(file.getName()).equals(".svg")) {
                bitmap = serializeSVG(file);
            }
        } catch(Exception e) {
            Log.e(TAG, "Failed to decode image file: " + e.getMessage());
        }

        return bitmap;
    }

    /* getExtension

        helper function to get the extension of a filename
     */
    private static String getExtension(String string) {
        if (sPatternMap == null) {
            sPatternMap = new HashMap<Pattern, String>();
            sPatternMap.put(Pattern.compile(".*\\.jpg"), ".jpg");
            sPatternMap.put(Pattern.compile(".*\\.png"), ".png");
            sPatternMap.put(Pattern.compile(".*\\.svg"), ".svg");
            sPatternMap.put(Pattern.compile(".*\\.gif"), ".gif");
        }

        String extension = ".unk";
        for (Pattern pattern : sPatternMap.keySet()) {
            if (pattern.matcher(string).matches()) {
                extension = sPatternMap.get(pattern);
                break;
            }
        }

        return extension;
    }


    private static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

}
