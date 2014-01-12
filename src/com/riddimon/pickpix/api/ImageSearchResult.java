package com.riddimon.pickpix.api;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ImageSearchResult extends ServiceResult {
	@JsonProperty("responseData")
	ResponseData data;

	@JsonIgnore
	public List<ContentValues> getStorableResults(String queryString) {
		List<ContentValues> store = null;
		if (data.results != null) {
			store = new ArrayList<ContentValues>(data.results != null ? data.results.size() : 0);
			int pageNum = data.cursor.currentPage;
			int serNum = data.cursor.pages.get(pageNum).start;
			for (ImageResult res : data.results) {
				res.pageNum = pageNum;
				res.serialNum = serNum++;
				res.query = queryString;
				store.add(res.toContentValues());
			}
		}
		return store;
	}

	public static class ResponseData {
		@JsonProperty("results")
		public List<ImageResult> results;

		@JsonProperty("cursor")
		public ResultCursor cursor;
	}
}
