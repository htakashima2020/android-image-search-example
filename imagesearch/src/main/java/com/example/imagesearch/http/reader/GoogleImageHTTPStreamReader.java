package com.example.imagesearch.http.reader;

import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.util.ArrayList;

/**
 * Created by mark on 3/25/14.
 */
public class GoogleImageHTTPStreamReader extends HTTPStreamReader {

    private final static String TAG = "GoogleImageHTTPStreamReader";

    // uses JsonReader and reads a stream of data in.
    // we only care about urls, so we basically just throw out the rest
    @Override
    public Object read(InputStream input) throws Exception {
        Log.d(TAG, "Beginning read...");

        ArrayList urls = new ArrayList<String>();

        String nextName;
        String peek;

        JsonReader reader = new JsonReader(new InputStreamReader(input));
        assert reader.peek() == JsonToken.BEGIN_OBJECT;
        Log.d(TAG, "Got peek - " + reader.peek());
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            Log.d(TAG, "0 - " + name);
            if (name.equals("responseData")) {
                reader.beginObject();
                while (reader.hasNext()) {
                    String nextname = reader.nextName();
                    if (nextname.equals("results")) {
                        reader.beginArray();
                        while (reader.hasNext()) {
                            reader.beginObject();
                            while (reader.hasNext()) {
                                String key = reader.nextName();
                                if (key.equals("url")) {
                                    urls.add(URLDecoder.decode(reader.nextString(), "UTF-8"));
                                } else {
                                    reader.skipValue();
                                }
                            }
                            reader.endObject();
                        }
                    } else {
                        reader.skipValue();
                    }
                }
            }
        }

        Log.i(TAG, "Got urls: \n" + urls);
        String[] contents = new String[urls.size()];
        urls.toArray(contents);

        return contents;
    }
}
