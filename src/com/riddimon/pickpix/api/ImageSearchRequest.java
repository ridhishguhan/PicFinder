package com.riddimon.pickpix.api;

import com.riddimon.pickpix.api.HttpUtils.HttpMethod;

public class ImageSearchRequest extends ServiceRequest {
	private static final ServiceApi API = new ServiceApi("https", "ajax.googleapis.com"
			, 80, "ajax/services/search/images");

	public String query;
	public Integer start;
	public Integer pageSize = 8;

	public ImageSearchRequest() {
		super(API, HttpMethod.GET);
	}

	@Override
	public void addParameters() {
		super.addParameters();
		addParameter("q", query);
		if (start != null) addParameter("start", start);
		if (pageSize != null) addParameter("rsz", pageSize);
	}

}
