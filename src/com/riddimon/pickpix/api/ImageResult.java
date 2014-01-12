package com.riddimon.pickpix.api;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.riddimon.pickpix.db.TraceProvider;

public class ImageResult implements Parcelable{
	public static final String COL_SER_NUM = "number";
	public static final String COL_IMAGE_ID = "imageId";
	public static final String COL_TITLE = "title";
	public static final String COL_CONTENT = "content";
	public static final String COL_CONTENT_NO_FORMAT = "contentNoFormatting";
	public static final String COL_HEIGHT = "height";
	public static final String COL_WIDTH = "width";
	public static final String COL_PAGE_URL = "pageUrl";
	public static final String COL_THUMB_URL = "thumbUrl";
	public static final String COL_IMAGE_URL = "pageUrl";

	public static String TABLE_NAME = "images";
	public static String CREATE_TABLE_STATEMENT = "create table " + TABLE_NAME
			+ " (" + COL_SER_NUM + " integer primary key, "
			+ COL_IMAGE_ID + " varchar(256) not null, "
			+ COL_TITLE + " varchar(256), "
			+ COL_CONTENT + " varchar(256), "
			+ COL_CONTENT_NO_FORMAT + " varchar(256), "
			+ COL_HEIGHT + " integer, "
			+ COL_WIDTH + " integer, "
			+ COL_PAGE_URL + " varchar(256), "
			+ COL_THUMB_URL + " varchar(256), "
			+ COL_IMAGE_URL + " varchar(256)"
			+ ")";

	public static final Uri URI = Uri.parse("content://" + TraceProvider.AUTHORITY
			+ "/" + TABLE_NAME);

	@JsonIgnore
	public int serialNum;

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

		height = in.readInt();
		width = in.readInt();
	}

	@Override
	public void writeToParcel(Parcel out, int arg1) {
		out.writeInt(serialNum);
		out.writeStringArray(new String[] {
			imageId, content, contentNoFormatting, pageUrl, thumbUrl
					, imageUrl, title
		});
		out.writeInt(height);
		out.writeInt(width);
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
