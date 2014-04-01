package com.example.imagesearch;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.example.imagesearch.http.HTTPClient;
import com.example.imagesearch.http.image.ImageCache;
import com.example.imagesearch.http.image.ImageDownload;
import com.example.imagesearch.http.image.ImageDownloadListener;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by mark on 3/24/14.
 */
public class ImageAdapter extends BaseAdapter {

    private static class CellViewHolder {
        public ImageCellDownloadListener downloadListener;
        public ImageView imageView;
        public ProgressBar progressBar;
    }

    private class ImageCellDownloadListener implements ImageDownloadListener {
        private ImageAdapter.CellViewHolder mCellViewHolder;

        public ImageCellDownloadListener(ImageAdapter.CellViewHolder cellViewHolder) {
            mCellViewHolder = cellViewHolder;
        }

        public void didError(Exception e) {
            mMainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    mCellViewHolder.imageView.setImageResource(R.drawable.warning_icon);
                }
            });
        }

        public void didFinish(final Bitmap bitmap) {
            mMainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    mCellViewHolder.progressBar.setProgress(0);
                    mCellViewHolder.progressBar.setVisibility(View.INVISIBLE);
                    mCellViewHolder.imageView.setVisibility(View.VISIBLE);
                    mCellViewHolder.imageView.setImageBitmap(bitmap);
                    mCellViewHolder.imageView.invalidate();

                }
            });
        }

        public void canReceiveProgress(boolean canReceiveProgress) {
            if (canReceiveProgress) {
                mMainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mCellViewHolder.imageView.setVisibility(View.INVISIBLE);
                        mCellViewHolder.progressBar.setVisibility(View.VISIBLE);
                        mCellViewHolder.progressBar.invalidate();
                    }
                });
            }
        }

        public void didReceiveProgress(float progress) {
            final float finalProgress = progress;
            mMainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    int progressLevel = (int) (finalProgress * (float) mCellViewHolder.progressBar.getMax());
                    mCellViewHolder.progressBar.setProgress(progressLevel);
                    mCellViewHolder.progressBar.invalidate();
                }
            });
        }
    }

    private final static String TAG = "ImageAdapter";
    private final static Handler mMainThreadHandler = new Handler(Looper.getMainLooper());
    private final static HTTPClient Client = new HTTPClient("https://ajax.googleapis.com/ajax/services/search");
    private final static GoogleImageAPI Api = new GoogleImageAPI();

    private HashMap<String, Bitmap>mURLBitmapMap;
    private HashMap<String, ImageDownload>mURLDownloadMap;
    private HashMap<CellViewHolder, ImageDownload>mActiveImageDownloadListenerMap;

    private String mActiveQuery;
    private Context mContext;
    private ArrayList<String> urls = new ArrayList<String>();
    private int mOffset = 0;

    public ImageAdapter(Context c) {
        mURLBitmapMap = new HashMap<String, Bitmap>();
        mURLDownloadMap = new HashMap<String, ImageDownload>();
        mActiveImageDownloadListenerMap = new HashMap<CellViewHolder, ImageDownload>();

        mContext = c;
    }

    public void setActiveQuery(String query) {
        mActiveQuery = query;
    }

    public void empty() {
        // cancel all image downloads
        for(ImageDownload download : mURLDownloadMap.values()) {
            download.cancel();
        }
        urls = new ArrayList<String>();
        mURLDownloadMap = new HashMap<String, ImageDownload>();
        mURLBitmapMap = new HashMap<String, Bitmap>();
        mActiveImageDownloadListenerMap = new HashMap<CellViewHolder, ImageDownload>();

        this.notifyDataSetChanged();
    }

    public int getCount() {
        return urls.size();
    }

    public Object getItem(int index) {
        return urls.get(index);
    }

    public long getItemId(int index) {
        return index;
    }

    public void updateWithQuery(String query) throws Exception {
//        Api.cancel();

        mActiveQuery = query;
        GoogleImageAPI.fetchResults(query, mOffset, new GoogleImageAPIListener() {
            @Override
            public void gotNewURLs(ArrayList<String> urls) {
                ImageAdapter.this.urls = urls;
                mMainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        ImageAdapter.this.notifyDataSetChanged();
                    }
                });
            }
        });
    }

    public void nextPage() throws Exception {

        GoogleImageAPI.fetchResults(mActiveQuery, this.urls.size(), new GoogleImageAPIListener() {
            @Override
            public void gotNewURLs(ArrayList<String> urls) {
                ImageAdapter.this.urls.addAll(urls);
                mMainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        ImageAdapter.this.notifyDataSetChanged();
                    }
                });
            }
        });



    }

    public ArrayList<String> getUrls() {
        return urls;
    }

    public void setUrls(ArrayList<String> urls) {
        this.urls = urls;
    }

    private ImageCellDownloadListener createListener(final CellViewHolder viewHolder) {
        return new ImageCellDownloadListener(viewHolder);
    }

    private ImageDownload download(String url, ImageDownloadListener listener) {
        ImageDownload imageDownload = ImageDownload.get(url, listener);
        return imageDownload;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        CellViewHolder container;

        if (convertView == null) {  // not recycled
            view = LayoutInflater.from(mContext).inflate(R.layout.image_cell, parent, false);
            container = new CellViewHolder();
            container.imageView = (ImageView)view.findViewById(R.id.cellImageView);
            container.progressBar = (ProgressBar)view.findViewById(R.id.cellProgressBar);
            container.downloadListener = createListener(container);
            view.setTag(container);
        } else {
            view = convertView;
            container = (CellViewHolder)convertView.getTag();
        }

        // we stop this container's last imagedownload to stop sending anymore updates
        // to this container. failure to do so will result in flickering
        ImageDownload oldImageDownload = mActiveImageDownloadListenerMap.get(container);
        if (oldImageDownload != null) {
            oldImageDownload.setDownloadListener(null);
        }

        String url = urls.get(position);
        container.progressBar.setProgress(0);
        if (ImageCache.DefaultCache().isCached(url)) { // cached image
            container.progressBar.setProgress(container.progressBar.getMax());
            Bitmap bitmap = ImageCache.DefaultCache().get(url);
            if (bitmap != null) {
                container.imageView.setVisibility(View.VISIBLE);
                container.progressBar.setVisibility(View.INVISIBLE);
                container.imageView.setImageBitmap(bitmap);
                mURLBitmapMap.put(url, bitmap);
            } else {
                Log.e(TAG, "Could not get bitmap from URL: " + url);
                container.imageView.setVisibility(View.VISIBLE);
                container.imageView.setImageResource(R.drawable.warning_icon);
                container.progressBar.setVisibility(View.INVISIBLE);
            }
            container.imageView.invalidate();
        } else if (!mURLDownloadMap.containsKey(url)) { // new image download
            container.imageView.setVisibility(View.INVISIBLE);
            container.progressBar.setVisibility(view.VISIBLE);
            ImageDownload download = download(url, container.downloadListener);
            mActiveImageDownloadListenerMap.put(container, download);
            mURLDownloadMap.put(url, download);

            download.setDownloadListener(createListener(container));
        } else { // existing image download
            ImageDownload imageDownload = mURLDownloadMap.get(url);
            imageDownload.setDownloadListener(container.downloadListener);
            mActiveImageDownloadListenerMap.put(container, imageDownload);
            imageDownload.setDownloadListener(createListener(container));

            switch (imageDownload.getState()) {
                case SUCCEEDED:
                    Bitmap bitmap = imageDownload.getBitmap();
                    if (bitmap != null) {
                        container.imageView.setVisibility(View.VISIBLE);
                        container.progressBar.setVisibility(View.INVISIBLE);
                        container.imageView.setImageBitmap(bitmap);
                        mURLBitmapMap.put(url, bitmap);
                    } else {
                        container.imageView.setVisibility(View.VISIBLE);
                        container.imageView.setImageResource(R.drawable.warning_icon);
                    }
                    container.imageView.invalidate();
                    break;
                case FAILED:
                    container.imageView.setVisibility(View.VISIBLE);
                    container.imageView.setImageResource(R.drawable.warning_icon);
                    break;
                case DOWNLOADING:
                    int progressLevel = (int) (imageDownload.getProgress() * (float) container.progressBar.getMax());
                    container.imageView.setVisibility(View.INVISIBLE);
                    container.imageView.setImageBitmap(null);
                    container.progressBar.setVisibility(view.VISIBLE);
                    container.progressBar.setProgress(progressLevel);
                    break;
                case INITIALIZED:
                    container.imageView.setImageBitmap(null);
                    container.imageView.setVisibility(View.INVISIBLE);
                    container.progressBar.setVisibility(view.VISIBLE);
                    break;
            }
        }

        view.invalidate();
        return view;
    }

}
