package com.riddimon.pickpix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.support.v7.widget.SearchView.OnSuggestionListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

import com.riddimon.pickpix.api.ImageSearchRequest;
import com.riddimon.pickpix.db.SearchProvider;

public class SearchActivity extends ActionBarActivity {
	private static final Logger logger = LoggerFactory.getLogger(SearchActivity.class.getSimpleName());
	private static final int LOADER = 1;
	private boolean mRegistered;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		listenForResults(true);
		setContentView(R.layout.activity_search);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

	    if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
	        String query = intent.getStringExtra(SearchManager.QUERY);
	        SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
	                SearchProvider.AUTHORITY, SearchProvider.MODE);
	        suggestions.saveRecentQuery(query, null);
	        doSearch(query);
	    }
	}

	
	private void listenForResults(boolean listen) {
		LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
		if (listen && !mRegistered) {
			IntentFilter filter = new IntentFilter();
			filter.addAction(SearchService.ACTION);
			lbm.registerReceiver(mEventReceiver, filter);
			mRegistered = true;
		} else if (!listen && mRegistered) {
			lbm.unregisterReceiver(mEventReceiver);
			mRegistered = false;
		}
	}

	private void doSearch(String query) {
		// do search s
		setProgressBarIndeterminateVisibility(true);
		ImageSearchRequest req = new ImageSearchRequest();
		req.query = query;
		req.pageSize = 8;
		req.start = 0;
		startService(new Intent(this, SearchService.class).putExtra(SearchService.OP, SearchService.OP_SEARCH)
				.putExtra(SearchService.EX_SEARCH_REQ, req));
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        // Handle your other action bar items...
	    boolean handled = super.onOptionsItemSelected(item);
	    if (!handled) {
	    	switch(item.getItemId()) {
	    	case R.id.action_search:
	    		onSearchRequested();
	    		handled = true;
	    	}
	    }
	    return handled;
	}

	@SuppressLint("NewApi")
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		logger.trace("Browse Activity : onCreateOptionsMenu");
		menu.clear();
		getMenuInflater().inflate(R.menu.search, menu);
		   // Get the SearchView and set the searchable configuration
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
		    SearchManager searchManager = (SearchManager) getSystemService(Context
		    		.SEARCH_SERVICE);
		    final MenuItem item = menu.findItem(R.id.action_search);
		    SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
		    // Assumes current activity is the searchable activity
		    searchView.setSearchableInfo(searchManager
		    		.getSearchableInfo(getComponentName()));
		    searchView.setIconifiedByDefault(true);
		    searchView.setOnSuggestionListener(new OnSuggestionListener() {
				@Override
				public boolean onSuggestionSelect(int arg0) {
		            MenuItem searchMenuItem = item;
		            if (searchMenuItem != null) {
		                searchMenuItem.collapseActionView();
		            }
					return false;
				}
				
				@Override
				public boolean onSuggestionClick(int arg0) {
		            MenuItem searchMenuItem = item;
		            if (searchMenuItem != null) {
		                searchMenuItem.collapseActionView();
		            }
					return false;
				}
			});
		    searchView.setOnQueryTextListener(new OnQueryTextListener() {
		        @Override
		        public boolean onQueryTextSubmit(String query) {
		            MenuItem searchMenuItem = item;
		            if (searchMenuItem != null) {
		                searchMenuItem.collapseActionView();
		            }
		            return false;
		        }
		        @Override
		        public boolean onQueryTextChange(String newText) {
		            // ...
		            return false;
		        }
		    });
		}
		super.onCreateOptionsMenu(menu);
		return true;
	}

	/**
	 * Class which receives events from the service and displays error messages
	 * or dismisses progress dialogs depending on the error
	 */
	private BroadcastReceiver mEventReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			setProgressBarIndeterminateVisibility(false);
		}
	};
	@Override
	protected void onStop() {
		super.onStop();
		if (isFinishing()) {
			listenForResults(false);
		}
	}


	@Override
	protected void onDestroy() {
		listenForResults(false);
		super.onDestroy();
	}
}
