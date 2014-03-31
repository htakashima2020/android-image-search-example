package com.example.imagesearch.http.tests;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.example.imagesearch.http.image.ImageCache;
import android.test.InstrumentationTestCase;

import java.nio.ByteBuffer;

/**
 * Created by mark on 3/23/14.
 */
public class ImageCacheTest extends InstrumentationTestCase {

    public void testExistsReturnsFalseForURLNotAdded() throws Exception {
        ImageCache.CacheDirectory = getInstrumentation().getTargetContext().getCacheDir();

        assertEquals(false, ImageCache.DefaultCache().isCached("http://lajsdfas.com/lkasdjf.jpeg"));
    }

}
