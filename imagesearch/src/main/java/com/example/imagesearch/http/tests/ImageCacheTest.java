package com.example.imagesearch.http.tests;

import com.example.imagesearch.http.image.ImageCache;
import android.test.InstrumentationTestCase;

/**
 * Created by mark on 3/23/14.
 */
public class ImageCacheTest extends InstrumentationTestCase {

    public void testExistsReturnsFalseForURLNotAdded() throws Exception {
        ImageCache.CACHE_DIRECTORY = getInstrumentation().getTargetContext().getCacheDir();

        assertEquals(false, ImageCache.defaultCache().isCached("http://lajsdfas.com/lkasdjf.jpeg"));
    }
}
