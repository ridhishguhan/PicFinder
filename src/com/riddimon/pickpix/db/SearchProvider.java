package com.riddimon.pickpix.db;

import android.content.SearchRecentSuggestionsProvider;

public class SearchProvider extends SearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "com.riddimon.pickpix.db.SearchProvider";
    public final static int MODE = DATABASE_MODE_QUERIES;
    public SearchProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }
}
