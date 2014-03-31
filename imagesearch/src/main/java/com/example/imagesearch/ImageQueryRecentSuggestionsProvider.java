package com.example.imagesearch;

import android.content.SearchRecentSuggestionsProvider;

public class ImageQueryRecentSuggestionsProvider extends SearchRecentSuggestionsProvider {

    public final static String AUTHORITY = "com.example.imagesearch.RecentSuggestionProvider";
    public final static int MODE = DATABASE_MODE_QUERIES;

    public ImageQueryRecentSuggestionsProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }

}
