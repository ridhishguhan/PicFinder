package com.riddimon.pickpix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.support.v7.widget.SearchView.OnSuggestionListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.GridView;

import com.riddimon.pickpix.api.ImageResult;
import com.riddimon.pickpix.api.ImageSearchRequest;
import com.riddimon.pickpix.api.ResultCursor;
import com.riddimon.pickpix.api.ResultCursor.Page;
import com.riddimon.pickpix.db.SearchProvider;

public class SearchActivity extends ActionBarActivity implements LoaderCallbacks<Cursor> {
	private static final Logger logger = LoggerFactory.getLogger(SearchActivity.class.getSimpleName());
	private static final int LOADER = 1;
	private boolean mRegistered;
	private String mQuery;

	EndlessScrollListener mScrollListener = null;
	SampleGridViewAdapter mAdapter = null;
	GridView mGridview = null;
	ResultCursor mResultCursor;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		listenForResults(true);
		setContentView(R.layout.activity_search);
		mGridview = (GridView) findViewById(R.id.grid_view);
		mAdapter = new SampleGridViewAdapter(this, null);
		mGridview.setAdapter(mAdapter);
		LoaderManager lm = getSupportLoaderManager();
		Loader<Cursor> l = lm.getLoader(LOADER);
		if (l != null) {
			lm.restartLoader(LOADER, null, this);
		} else {
			lm.initLoader(LOADER, null, this);
		}
		if (savedInstanceState != null) {
			mQuery = savedInstanceState.getString("query");
			mResultCursor = savedInstanceState.getParcelable("results");
		}
		mScrollListener = new EndlessScrollListener();
		mGridview.setOnScrollListener(mScrollListener);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

	    if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
	        String query = intent.getStringExtra(SearchManager.QUERY);
	        SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
	                SearchProvider.AUTHORITY, SearchProvider.MODE);
	        suggestions.saveRecentQuery(query, null);
	        doSearch(true, query);
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

	private void doSearch(boolean fresh, String query) {
		// do search s
		mQuery = query;
		setProgressBarIndeterminateVisibility(true);
		ImageSearchRequest req = new ImageSearchRequest();
		req.query = query;
		req.pageSize = 8;
		req.start = 0;
		if (fresh || mResultCursor == null) {
			getContentResolver().delete(ImageResult.URI, null, null);
		} else {
			Page p = null;
			int wantedPage = 1;
			ContentResolver cr = getContentResolver();
			Cursor c = cr.query(ImageResult.URI, null, ImageResult.COL_QUERY + " = '" + query + "' AND "
					+ ImageResult.COL_PAGE_NUM + " >= " + wantedPage, null, ImageResult.COL_PAGE_NUM + " DESC");
			if (c != null && c.moveToFirst()) {
				ImageResult res = ImageResult.fromCursor(c);
				for (Page ps : mResultCursor.pages) {
					int label = Integer.parseInt(ps.label);
					if (label > res.pageNum) {
						wantedPage = label;
						p = ps;
						break;
					}
				}
			}
			if (c != null) c.close();
			if (p == null || wantedPage == 1) return;
			req.start = p.start;
		}
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
					return true;
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
			mResultCursor = intent.getParcelableExtra(SearchService.EX_CURSOR);
			mScrollListener.loading = false;
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

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		// TODO Auto-generated method stub
		String selection = null;
		if (mQuery != null) {
			selection = ImageResult.COL_QUERY + " = '"
					+ mQuery + "'";
		}
		return new CursorLoader(this, ImageResult.URI, null
				, selection, null, ImageResult.COL_SER_NUM + " ASC");
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
		mAdapter.changeCursor(arg1);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		mAdapter.changeCursor(null);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mQuery != null) outState.putString("query", mQuery);
		if (mResultCursor != null) outState.putParcelable("results", mResultCursor);
	}

    public class EndlessScrollListener implements OnScrollListener {

        private int visibleThreshold = 5;
        private int currentPage = 0;
        private int previousTotal = 0;
        private boolean loading = true;

        public EndlessScrollListener() {
        }
        public EndlessScrollListener(int visibleThreshold) {
            this.visibleThreshold = visibleThreshold;
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                int visibleItemCount, int totalItemCount) {
            if (loading) {
                if (totalItemCount > previousTotal) {
                    loading = false;
                    previousTotal = totalItemCount;
                    currentPage++;
                }
            }
            if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
                // I load the next page of gigs using a background task,
                // but you can call any function here.
                doSearch(false, mQuery);
                loading = true;
            }
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }
    }
}
