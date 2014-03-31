package com.example.imagesearch.http.image;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import com.example.imagesearch.ImageAdapter;
import com.example.imagesearch.R;

import java.io.File;

public interface ImageDownloadListener {
    public void didFinish(Bitmap bitmap);
    public void didReceiveProgress(float progress);
    public void canReceiveProgress(boolean canReceiveProgress);
    public void didError(Exception e);
}

