package com.riddimon.pickpix.api;

import android.os.Parcel;
import android.os.Parcelable;

import com.riddimon.pickpix.api.HttpUtils.HttpMethod;

public class ImageSearchRequest extends ServiceRequest implements Parcelable {
	private static final ServiceApi API = new ServiceApi("https", "ajax.googleapis.com"
			, 80, "ajax/services/search/images");

	public String query;
	public Integer start = 0;
	public Integer pageSize = 8;

	public ImageSearchRequest() {
		super(API, HttpMethod.GET);
	}

	public ImageSearchRequest(Parcel in) {
		super(API, HttpMethod.GET);
		this.start = in.readInt();
		this.pageSize = in.readInt();
		this.query = in.readString();
	}
	@Override
	public void addParameters() {
		super.addParameters();
		addParameter("q", query);
		if (start != null) addParameter("start", start);
		if (pageSize != null) addParameter("rsz", pageSize);
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(start);dest.writeInt(pageSize);dest.writeString(query);
	}

	public static final Parcelable.Creator<ImageSearchRequest> CREATOR = new Creator<ImageSearchRequest>() {
		
		@Override
		public ImageSearchRequest[] newArray(int size) {
			// TODO Auto-generated method stub
			return new ImageSearchRequest[size];
		}
		
		@Override
		public ImageSearchRequest createFromParcel(Parcel source) {
			// TODO Auto-generated method stub
			return new ImageSearchRequest(source);
		}
	};
}
