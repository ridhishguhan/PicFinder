package com.riddimon.pickpix.api;

import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ResultCursor implements Parcelable {
	public static class Page implements Parcelable {
		public int start;
		public String label;
		@Override
		public int describeContents() {
			// TODO Auto-generated method stub
			return 0;
		}
		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeString(label);
			dest.writeInt(start);
		}
		public Page(){}
		public Page(Parcel in) {
			label = in.readString();
			start = in.readInt();
		}
		public static final Parcelable.Creator<Page> CREATOR = new Creator<Page>() {
			
			@Override
			public Page[] newArray(int size) {
				// TODO Auto-generated method stub
				return new Page[size];
			}
			
			@Override
			public Page createFromParcel(Parcel source) {
				// TODO Auto-generated method stub
				return new Page(source);
			}
		}; 
	}

	@JsonProperty("resultCount")
	public String count;

	@JsonProperty("pages")
	public List<Page> pages;

	@JsonProperty("estimatedResultCount")
	public String resultCount;

	@JsonProperty("currentPageIndex")
	public int currentPage;

	@JsonProperty("moreResultsUrl")
	public String moreResultsUrl;

	@JsonProperty("searchResultTime")
	public String timeInSeconds;

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel arg0, int arg1) {
		arg0.writeStringArray(new String[] {count, resultCount, moreResultsUrl, timeInSeconds});
		arg0.writeInt(currentPage);
		arg0.writeInt(pages == null ? 0 : pages.size());
		if (pages != null) arg0.writeTypedList(pages);
	}

	public ResultCursor(){}

	public ResultCursor(Parcel in) {
		String[] data = new String[4];
		in.readStringArray(data);
		count = data[0];
		resultCount = data[1];
		moreResultsUrl = data[2];
		timeInSeconds = data[3];
		currentPage = in.readInt();
		int pc = in.readInt();
		if (pc > 0) in.readTypedList(pages, Page.CREATOR);
	}
	public static final Parcelable.Creator<Page> CREATOR = new Creator<Page>() {
		
		@Override
		public Page[] newArray(int size) {
			// TODO Auto-generated method stub
			return new Page[size];
		}
		
		@Override
		public Page createFromParcel(Parcel source) {
			// TODO Auto-generated method stub
			return new Page(source);
		}
	}; 
}
