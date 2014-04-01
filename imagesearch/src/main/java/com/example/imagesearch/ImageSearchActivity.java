package com.example.imagesearch;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ActionBar;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.view.MenuItem;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SearchView;
import android.provider.SearchRecentSuggestions;
import com.example.imagesearch.http.image.ImageCache;

public class ImageSearchActivity extends ActionBarActivity {

    private static String TAG = "ImageSearchActivity";
    private static String KEY_ACTIVE_SEARCH = "KEY|ACTIVE_SEARCH";
    private static String KEY_GRIDVIEW_POSITION = "KEY|GRIDVIEW_POSITION";
    private static String KEY_ADAPTER_URLS = "KEY|ADAPTER_URLS";
    private static int mShortDuration = 200;

    private View mContainer;
    private SearchView mActionSearchView;
    private GridView mGridView;
    private ImageAdapter mImageAdapter;
    private MenuItem mActionSearchItem;
    private Animator mCurrentAnimator;

    private String mActiveQuery;

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // configure image cache to use default cache directory
        ImageCache.CacheDirectory = getCacheDir();
        ImageCache.DefaultCache();

        // layout entire activity
        setContentView(R.layout.activity_image_search);

        // get container
        mContainer = findViewById(R.id.gridview_container);
        if (mContainer == null) {
            Log.e(TAG, "Container is null.. ");
        }

        // setup grid view
        setupGridView();

        // check to see if we have an active query otherwise just handle search intent
        if (savedInstanceState != null) {
            mActiveQuery = savedInstanceState.getString(KEY_ACTIVE_SEARCH);
            mImageAdapter.setActiveQuery(mActiveQuery);
            getActionBar().setSubtitle(mActiveQuery);

            mImageAdapter.setUrls(savedInstanceState.getStringArrayList(KEY_ADAPTER_URLS));
            mGridView.scrollTo(0, savedInstanceState.getInt(KEY_GRIDVIEW_POSITION));
            mImageAdapter.notifyDataSetInvalidated();

        } else {
            handleIntent(getIntent());
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);

        bundle.putStringArrayList(KEY_ADAPTER_URLS, mImageAdapter.getUrls());
        bundle.putString(KEY_ACTIVE_SEARCH, mActiveQuery);
        bundle.putInt(KEY_GRIDVIEW_POSITION, mGridView.getScrollY());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.image_search, menu);
        setupActionSearchBar(menu);
        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        cleanUp();
        setIntent(intent);
        handleIntent(intent);
    }

    private void cleanUp() {
        findViewById(R.id.expandedImageView).setVisibility(View.GONE);
        mImageAdapter.empty();
        mGridView.invalidateViews();
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            handleSearch(intent.getStringExtra(SearchManager.QUERY));
        }
    }

    private void setupGridView() {
        mGridView = (GridView)findViewById(R.id.gridview);
        mImageAdapter = new ImageAdapter(this);
        mGridView.setAdapter(mImageAdapter);

        mGridView.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                try {
                    mImageAdapter.nextPage();
                } catch(Exception e) {

                }
            }
        });
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String url = (String)adapterView.getItemAtPosition(i);
                ImageSearchActivity.this.zoomImageFromThumb(view, url);
            }
        });
    }

    /* zoomImageFromThumb(thumbView, url)

        see android developer "zoom image"
     */
    private void zoomImageFromThumb(final View thumbView, final String url) {
        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }

        // Load the high-resolution "zoomed-in" image.
        final ImageView expandedImageView = (ImageView) findViewById(
                R.id.expandedImageView);
        expandedImageView.setImageBitmap(ImageCache.DefaultCache().getHighQuality(url, mContainer.getHeight(), mContainer.getWidth()));

        // Calculate the starting and ending bounds for the zoomed-in image.
        // This step involves lots of math. Yay, math.
        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        thumbView.getGlobalVisibleRect(startBounds);
        mContainer
                .getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).
        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.
        thumbView.setAlpha(0f);
        expandedImageView.setVisibility(View.VISIBLE);

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        expandedImageView.setPivotX(0f);
        expandedImageView.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(expandedImageView, View.X,
                        startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.Y,
                        startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X,
                        startScale, 1f)).with(ObjectAnimator.ofFloat(expandedImageView,
                View.SCALE_Y, startScale, 1f));
        set.setDuration(mShortDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mCurrentAnimator = null;
            }
        });
        set.start();
        mCurrentAnimator = set;

        // Upon clicking the zoomed-in image, it should zoom back down
        // to the original bounds and show the thumbnail instead of
        // the expanded image.

        final float startScaleFinal = startScale;
        expandedImageView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (mCurrentAnimator != null) {
                    mCurrentAnimator.cancel();
                }

                // Animate the four positioning/sizing properties in parallel,
                // back to their original values.
                AnimatorSet set = new AnimatorSet();
                set.play(ObjectAnimator
                        .ofFloat(expandedImageView, View.X, startBounds.left))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.Y,startBounds.top))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.SCALE_X, startScaleFinal))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.SCALE_Y, startScaleFinal));
                set.setDuration(mShortDuration);
                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }
                });
                set.start();
                mCurrentAnimator = set;
            }
        });
    }

    /* configures the action bar search bar */
    private void setupActionSearchBar(Menu menu) {
        // setup action bar's search bar
        mActionSearchItem = menu.findItem(R.id.action_search);
        mActionSearchView = (SearchView) MenuItemCompat.getActionView(mActionSearchItem);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mActionSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        mActionSearchView.setIconifiedByDefault(false);
    }

    private void handleSearch(final String query) {
        mActiveQuery = query;

        Log.i(TAG, "User performed search: " + query + ".");
        ActionBar ab = getActionBar();
        ab.setSubtitle(query);
        if (mActionSearchItem != null) {
            mActionSearchItem.collapseActionView();
        }

        new Thread() {
            @Override
            public void run() {
                try {
                    // save the query
                    SearchRecentSuggestions suggestions = new SearchRecentSuggestions(ImageSearchActivity.this,
                            ImageQueryRecentSuggestionsProvider.AUTHORITY, ImageQueryRecentSuggestionsProvider.MODE);
                    suggestions.saveRecentQuery(query, null);
                } catch(Exception e) {

                } finally {

                }
            }
        }.start();

        try {
            mImageAdapter.updateWithQuery(query);
        } catch(Exception e) {
            Log.e(TAG, "could not update query.. " + e.getMessage());
        }
    }
}
