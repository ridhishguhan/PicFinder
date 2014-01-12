package com.riddimon.pickpix.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import org.apache.http.util.ByteArrayBuffer;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;

public class Utils {

	public static final Boolean CLIENT_INVALID_REQUEST = true;

	public static String getRealPathFromURI(Context context, Uri contentUri) {
		String[] proj = { MediaStore.Images.Media.DATA };
		ContentResolver cr = context.getContentResolver();
		Cursor cursor = cr.query(contentUri, proj, null, null, null);
		//CursorLoader loader = new CursorLoader(context, contentUri, proj, null
		//		, null, null);
		//Cursor cursor = loader.loadInBackground();
		int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images
				.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}

	/**
	 * True if the specified timestamp is more than a day old
	 * @param ts
	 * @return
	 */
	public static boolean moreThanADayOld(long ts) {
		return !withinTimespan(ts, android.text.format.DateUtils.DAY_IN_MILLIS);
	}

	public static boolean moreThanADayOld(Date date) {
		return !withinTimespan(date, android.text.format.DateUtils.DAY_IN_MILLIS);
	}

	public static boolean withinTimespan(Date time, long span) {
		if (time == null) throw new NullPointerException("Date provided is NULL!");
		return withinTimespan(time.getTime(), span);
	}

	public static boolean withinTimespan(long time, long span) {
		return (System.currentTimeMillis() - time) <= span;
	}

	/**
	 * Read a stream as a String and return it
	 * @param stream
	 * @return
	 */
	public static String readStreamToString(InputStream stream) {
		String str = "";
		try {
			ByteArrayBuffer builder = new ByteArrayBuffer(1024);
			BufferedInputStream bis = new BufferedInputStream(stream);
			byte buffer[] = new byte[50 * 2024];
			if (bis != null) {
				int read = 0;
				while((read = bis.read(buffer)) != -1) {
					builder.append(buffer, 0, read);
				}
				str = new String(builder.toByteArray(), "UTF-8");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return str;
	}
}
