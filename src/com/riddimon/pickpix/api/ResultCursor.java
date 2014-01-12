package com.riddimon.pickpix.api;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ResultCursor {
	public static class Page {
		public String start;
		public String label;
	}

	@JsonProperty("resultCount")
	public int count;

	@JsonProperty("pages")
	public List<Page> pages;

	@JsonProperty("estimatedResultCount")
	public int resultCount;

	@JsonProperty("currentPageIndex")
	public int currentPage;

	@JsonProperty("moreResultsUrl")
	public String moreResultsUrl;

	@JsonProperty("searchResultTime")
	public String timeInSeconds;
}
