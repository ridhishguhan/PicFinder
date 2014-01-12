package com.riddimon.pickpix.db;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.riddimon.pickpix.api.ImageResult;

public class ImageSearchProvider extends ContentProvider {
	public static final String AUTHORITY = "com.riddimon.pickpix.provider";

	private static final String DB_NAME = "pickpix.db";
	private static final int VERSION = 3;

	private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	private static final int IMAGES = 1;
	private static final int IMAGE = 2;

	private SQLiteOpenHelper mHelper;

	static {
		sUriMatcher.addURI(AUTHORITY, ImageResult.TABLE_NAME, IMAGES);
	}

	private class DbHelper extends SQLiteOpenHelper {

		public DbHelper(Context context) {
			super(context, DB_NAME, null, VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(ImageResult.CREATE_TABLE_STATEMENT);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + ImageResult.TABLE_NAME);
			onCreate(db);
		}
	}

	private void notify(Uri uri) {
		getContext().getContentResolver().notifyChange(uri, null);
	}

	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		SQLiteDatabase db = mHelper.getWritableDatabase();
		String table = null;
		switch(sUriMatcher.match(arg0)) {
		case IMAGES:
			table = ImageResult.TABLE_NAME;
			break;
		}
		int del = 0;
		if (table != null) {
			del = db.delete(table, arg1, arg2);
			if (del > 0) notify(arg0);
		}
		return del;
	}

	@Override
	public String getType(Uri arg0) {
		return null;
	}

	
	@Override
	public Uri insert(Uri arg0, ContentValues arg1) {
		SQLiteDatabase db = mHelper.getWritableDatabase();
		String table = null;
		switch(sUriMatcher.match(arg0)) {
		case IMAGES:
			table = ImageResult.TABLE_NAME;
			break;
		}
		Uri uri = null;
		if (table != null) {
			long id = db.insert(table, null, arg1);
			if (id != -1) {
				uri = Uri.withAppendedPath(uri, String.valueOf(id));
				notify(arg0);
			}
		}
		return uri;
	}

	@Override
	public boolean onCreate() {
		mHelper = new DbHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri arg0, String[] arg1, String arg2, String[] arg3,
			String arg4) {
		SQLiteDatabase db = mHelper.getReadableDatabase();
		String table = null;
		String sortOrder = arg4;
		switch(sUriMatcher.match(arg0)) {
		case IMAGES:
			table = ImageResult.TABLE_NAME;
			if (sortOrder == null) {
				sortOrder = ImageResult.COL_SER_NUM + " ASC";
			}
			break;
		}
		Cursor c = null;
		if (table != null) {
			SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
			builder.setTables(table);
			c = builder.query(db, arg1, arg2, arg3, null, null, sortOrder);
		}
		if (c!= null) {
			c.setNotificationUri(getContext().getContentResolver(), arg0);
		}
		return c;
	}

	@Override
	public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) {
		SQLiteDatabase db = mHelper.getWritableDatabase();
		String table = null;
		switch(sUriMatcher.match(arg0)) {
		case IMAGES:
			table = ImageResult.TABLE_NAME;
			break;
		}
		int updated = 0;
		if (table != null) {
			updated = db.update(table, arg1, arg2, arg3);
		}
		notify(arg0);
		return updated;
	}

	@Override
	public int bulkInsert(Uri uri, ContentValues[] values) {
		SQLiteDatabase db = mHelper.getWritableDatabase();
		String table = null;
		switch(sUriMatcher.match(uri)) {
		case IMAGES:
			table = ImageResult.TABLE_NAME;
			break;
		}
		int ins = 0;
		if (table != null && values != null) {
			db.beginTransaction();
			for (ContentValues value : values) {
				long id = db.insert(table, null, value);
				++ins;
			}
			db.setTransactionSuccessful();
			db.endTransaction();
		}
		if (ins > 0) {
			notify(uri);
		}
		return ins;
	}
}
