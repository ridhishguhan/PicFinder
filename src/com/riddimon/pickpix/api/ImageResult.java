package com.riddimon.pickpix.api;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.BaseColumns;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.riddimon.pickpix.db.ImageSearchProvider;

public class ImageResult implements Parcelable{
	public static final String COL_SER_NUM = "number";
	public static final String COL_PAGE_NUM = "page";
	public static final String COL_IMAGE_ID = "imageId";
	public static final String COL_TITLE = "title";
	public static final String COL_QUERY = "query";
	public static final String COL_CONTENT = "content";
	public static final String COL_CONTENT_NO_FORMAT = "contentNoFormatting";
	public static final String COL_HEIGHT = "height";
	public static final String COL_WIDTH = "width";
	public static final String COL_PAGE_URL = "pageUrl";
	public static final String COL_THUMB_URL = "thumbUrl";
	public static final String COL_IMAGE_URL = "imageUrl";

	public static String TABLE_NAME = "images";
	public static String CREATE_TABLE_STATEMENT = "create table " + TABLE_NAME
			+ " ("
			+ BaseColumns._ID + " integer primary key, "
			+ COL_SER_NUM + " integer , "
			+ COL_IMAGE_ID + " varchar(256) not null, "
			+ COL_TITLE + " varchar(256), "
			+ COL_QUERY + " varchar(256), "
			+ COL_CONTENT + " varchar(256), "
			+ COL_CONTENT_NO_FORMAT + " varchar(256), "
			+ COL_PAGE_NUM + " integer, "
			+ COL_HEIGHT + " integer, "
			+ COL_WIDTH + " integer, "
			+ COL_PAGE_URL + " varchar(256), "
			+ COL_THUMB_URL + " varchar(256), "
			+ COL_IMAGE_URL + " varchar(256)"
			+ ")";

	public static final Uri URI = Uri.parse("content://" + ImageSearchProvider.AUTHORITY
			+ "/" + TABLE_NAME);

	@JsonIgnore
	public int serialNum;
	@JsonIgnore
	public int pageNum;
	@JsonIgnore
	public String query;

	@JsonProperty("imageId")
	public String imageId;
	
	@JsonProperty("content")
	public String content;

	@JsonProperty("contentNoFormatting")
	public String contentNoFormatting;

	@JsonProperty("height")
	public int height;
	@JsonProperty("width")
	public int width;

	@JsonProperty("originalContextUrl")
	public String pageUrl;

	@JsonProperty("tbUrl")
	public String thumbUrl;

	@JsonProperty("url")
	public String imageUrl;

	@JsonProperty("titleNoFormatting")
	public String title;

	@Override
	public int describeContents() {
		return 0;
	}

	public ImageResult(){}

	public ImageResult(Parcel in) {
		this.serialNum = in.readInt();
		String[] data = new String[7];
		in.readStringArray(data);
		imageId = data[0];
		content = data[1];
		contentNoFormatting = data[2];
		pageUrl = data[3];
		thumbUrl = data[4];
		imageUrl = data[5];
		title = data[6];
		query = data[7];

		height = in.readInt();
		width = in.readInt();
		pageNum = in.readInt();
	}

	@Override
	public void writeToParcel(Parcel out, int arg1) {
		out.writeInt(serialNum);
		out.writeStringArray(new String[] {
			imageId, content, contentNoFormatting, pageUrl, thumbUrl
					, imageUrl, title, query
		});
		out.writeInt(height);
		out.writeInt(width);
		out.writeInt(pageNum);
	}

	public ContentValues toContentValues() {
		ContentValues values = new ContentValues();
		values.put(COL_SER_NUM, serialNum);
		values.put(COL_IMAGE_ID, imageId);
		values.put(COL_CONTENT, content);
		values.put(COL_CONTENT_NO_FORMAT, contentNoFormatting);
		values.put(COL_PAGE_URL, pageUrl);
		values.put(COL_THUMB_URL, thumbUrl);
		values.put(COL_IMAGE_URL, imageUrl);
		values.put(COL_TITLE, title);
		values.put(COL_HEIGHT, height);
		values.put(COL_WIDTH, width);
		values.put(COL_QUERY, query);
		values.put(COL_PAGE_NUM, pageNum);
		return values;
	}

	public static ImageResult fromCursor(Cursor c) {
		ImageResult result = null;
		if (c != null && !c.isAfterLast()) {
			result = new ImageResult();
			result.serialNum = c.getInt(c.getColumnIndex(COL_SER_NUM));
			result.pageNum = c.getInt(c.getColumnIndex(COL_PAGE_NUM));
			result.height = c.getInt(c.getColumnIndex(COL_HEIGHT));
			result.width = c.getInt(c.getColumnIndex(COL_WIDTH));
			
			result.imageId = c.getString(c.getColumnIndex(COL_IMAGE_ID));
			result.imageUrl = c.getString(c.getColumnIndex(COL_IMAGE_URL));
			result.content = c.getString(c.getColumnIndex(COL_CONTENT));
			result.contentNoFormatting = c.getString(c.getColumnIndex(COL_CONTENT_NO_FORMAT));
			result.pageUrl = c.getString(c.getColumnIndex(COL_PAGE_URL));
			result.thumbUrl = c.getString(c.getColumnIndex(COL_THUMB_URL));
			result.title = c.getString(c.getColumnIndex(COL_TITLE));
			result.query = c.getString(c.getColumnIndex(COL_QUERY));
		}
		return result;
	}

	public static final Parcelable.Creator<ImageResult> CREATOR
			= new Creator<ImageResult>() {
				@Override
				public ImageResult[] newArray(int size) {
					return new ImageResult[size];
				}
				
				@Override
				public ImageResult createFromParcel(Parcel source) {
					return new ImageResult(source);
				}
			};
}
