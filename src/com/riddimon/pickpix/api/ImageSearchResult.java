package com.riddimon.pickpix.api;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ImageSearchResult extends ServiceResult {
	@JsonProperty("results")
	List<ImageResult> results;

	@JsonProperty("cursor")
	ResultCursor cursor;

	@JsonIgnore
	public List<ContentValues> getStorableResults(String queryString) {
		List<ContentValues> store = null;
		if (results != null) {
			store = new ArrayList<ContentValues>(results != null ? results.size() : 0);
			int pageNum = cursor.currentPage;
			int serNum = cursor.pages.get(pageNum).start;
			for (ImageResult res : results) {
				res.pageNum = pageNum;
				res.serialNum = serNum++;
				res.query = queryString;
			}
		}
		return store;
	}
}
